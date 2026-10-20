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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.SacCommandParser;

/**
 *
 * @author dodge1
 */
public class HelpSacCommand implements SacCommand {

    private List<String> mytokens = new ArrayList<>();

    @Override
    public void initialize(String[] tokens) {
        mytokens = new ArrayList<>();
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
    }

    @Override
    public void execute() {
        if (mytokens.isEmpty()) {
            System.out.print(defaultUsage());
        } else {
            String commandName = mytokens.get(0).toUpperCase();
            SacCommand cmd = SacCommandParser.getInstance().getHelpString(commandName);
            if (cmd != null) {
                System.out.println(cmd.getHelpString());
            } else {
                System.out.println("Command " + commandName + " not found!");
            }
        }
    }

    private String defaultUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("SYNTAX: HELP COMMAND_NAME where COMMAND_NAME is the name of a legal SAC command.");
        sb.append("\n");
        Map<String, Set<String>> pkgMap = SacCommandParser.getInstance().getCommandMap();
        for (String pkg : pkgMap.keySet()) {
            sb.append(pkg + ":");
            sb.append("\n");
            Set<String> cmds = pkgMap.get(pkg);
            for (String str : cmds) {
                sb.append("\t" + str);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String getHelpString() {
        return defaultUsage();
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "HELP", "H" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
