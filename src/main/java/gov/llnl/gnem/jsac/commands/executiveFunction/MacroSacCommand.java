/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2023 Lawrence Livermore National Laboratory
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

package gov.llnl.gnem.jsac.commands.executiveFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.SacCommandExecutor;
import gov.llnl.gnem.jsac.commands.SacCommandParser;
import gov.llnl.gnem.jsac.commands.TokenListParser;

/**
 *
 * @author dodge1
 */
public class MacroSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(MacroSacCommand.class);

    private String macroFileName = null;
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> arguments = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        macroFileName = (String) arguments.get(0);
    }

    @Override
    public void execute() {
        try (Scanner scanner = new Scanner(new File(macroFileName))) {
            while (scanner.hasNextLine()) {
                String text = scanner.nextLine().trim();
                SacCommand command = SacCommandParser.getInstance().parseCommandLine(text);
                if (command != null) {
                    try {
                        SacCommandExecutor.getInstance().executeCommand(command);
                    } catch (Exception ex) {
                        log.warn("Failed command: " + text);
                    }
                }

            }
        } catch (FileNotFoundException ex) {
            log.warn("File not found: " + macroFileName);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "M", "MACRO" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
