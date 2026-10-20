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
package gov.llnl.gnem.jsac.commands.graphicsEnvironment;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.llnl.gnem.jsac.commands.SacCommand;

public class ListFontsSacCommand implements SacCommand {

    private final List<String> fontList;

    public ListFontsSacCommand() {
        fontList = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {

        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Set<String> tmp = new HashSet<>(Arrays.asList(fonts));
        fontList.addAll(tmp);
        Collections.sort(fontList);
    }

    @Override
    public void execute() {
        int columns = 100;

        int totalTokens = fontList.size();

        int maxLen = 0;
        for (String string : fontList) {
            int len = string.length();
            if (len > maxLen) {
                maxLen = len;
            }
        }
        int tokensPerLine = columns / (maxLen + 1);
        String fmt = "%" + maxLen + "s ";

        int j = 0;
        while (j < fontList.size()) {
            for (int k = 0; k < tokensPerLine; ++k) {
                System.out.print(String.format(fmt, fontList.get(j++)));
                if (j >= totalTokens) {
                    break;
                }
            }
            System.out.print("\n");

        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "FONTS", "LISTFONTS", "LF" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
