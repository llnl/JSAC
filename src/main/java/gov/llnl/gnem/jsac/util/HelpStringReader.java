/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.jsac.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.CoreTextContentNodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;

import gov.llnl.gnem.jsac.commands.SacCommand;

public class HelpStringReader {
    private static final Map<Class<?>, String> helpStrings = new HashMap<>();
    private static String HELP_STRING = "No help available";

    public static String getHelpString(Class<? extends SacCommand> clazz) {
        String help = helpStrings.get(clazz);
        if (help != null) {
            return help;
        } else if (clazz != null) {
            String helpName = "/docs/" + clazz.getSimpleName() + ".md";
            try (InputStream helpDoc = clazz.getResourceAsStream(helpName)) {
                if (helpDoc != null) {
                    List<String> lines = IOUtils.readLines(helpDoc, StandardCharsets.UTF_8);
                    if (lines != null && !lines.isEmpty()) {
                        Parser parser = Parser.builder().build();
                        Node document = parser.parse(lines.stream().collect(Collectors.joining("\n")));
                        TextContentRenderer renderer = TextContentRenderer.builder().nodeRendererFactory(new TextContentNodeRendererFactory() {
                            @Override
                            public NodeRenderer create(TextContentNodeRendererContext context) {
                                return new HelpTextNodeRenderer(context);
                            }
                        }).build();
                        help = renderer.render(document);
                        helpStrings.put(clazz, help);
                        return help;
                    }
                }
            } catch (IOException e) {
            }
        }
        return HELP_STRING;
    }
}
