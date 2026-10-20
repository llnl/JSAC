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
package gov.llnl.gnem.jsac.commands;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author dodge1
 */
public class SacCommandParser {

    private final Map<String, SacCommand> cmdStringMap;
    private final List<String> history;

    private SacCommandParser() {
        cmdStringMap = new HashMap<>();
        history = new ArrayList<>();
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public void initialize() {
        ServiceLoader<SacCommand> commands = ServiceLoader.load(SacCommand.class);

        boolean isHeadless = GraphicsEnvironment.isHeadless();
        if (isHeadless) {
            System.out.println("Headless environment detected, omitting graphical commands.");
        }
        for (SacCommand command : commands) {
            if (command == null || command instanceof NullSacCommand) {
                continue;
            }

            //Check if this is a headless environment and, if so, 
            //skip over any commands under the graphics sub packages
            Collection<String> strings = command.getCommandNames();
            for (String s : strings) {
                if (!isHeadless || (!s.isBlank() && !command.getClass().getName().contains("gov.llnl.gnem.jsac.commands.graphics"))) {
                    cmdStringMap.put(s, command);
                }
            }
        }
    }

    public SacCommand getHelpString(String cmd) {
        return cmdStringMap.get(cmd);
    }

    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        for (SacCommand command : cmdStringMap.values()) {
            if (command == null || command instanceof NullSacCommand) {
                continue;
            }
            commands.addAll(command.getCommandNames());
        }
        return commands;
    }

    public Map<String, Set<String>> getCommandMap() {
        Map<String, Set<String>> pkgMethodMap = new TreeMap<>();
        for (String cmd : cmdStringMap.keySet()) {
            SacCommand sc = cmdStringMap.get(cmd);
            if (sc != null) {
                String tmp = sc.toString();
                String[] parts = tmp.split("@");
                tmp = parts[0];
                parts = tmp.split("\\.");
                int np = parts.length;
                String pkgName = parts[np - 2];
                try {
                    Collection<String> strings = sc.getCommandNames();
                    if (!strings.isEmpty()) {
                        List<String> tmp2 = new ArrayList<>(strings);
                        StringBuilder sb = new StringBuilder(tmp2.get(0));
                        for (int j = 1; j < tmp2.size(); ++j) {
                            sb.append(" (").append(tmp2.get(j)).append(")");
                        }
                        String cmdName = sb.toString();
                        Set<String> commands = pkgMethodMap.get(pkgName);
                        if (commands == null) {
                            commands = new TreeSet<>();
                            pkgMethodMap.put(pkgName, commands);
                        }
                        commands.add(cmdName);
                    }
                } catch (IllegalArgumentException | SecurityException ex) {
                }

            }
        }
        return pkgMethodMap;
    }

    public static SacCommandParser getInstance() {
        return SacCommandParserHolder.INSTANCE;
    }

    private String processRepeatRequest(String text) {
        int n = history.size();
        String tmp = text.trim();
        if (tmp.contains("!!")) {
            return history.get(n - 1);
        }
        tmp = tmp.substring(1);
        if (NumberUtils.isParsable(tmp)) {
            int val = Integer.parseInt(tmp);
            if (val > 0) { // asking to repeat a command by its number...
                --val; //history is one-based, arraylist is 0-based
                if (val < n) {
                    return history.get(val);
                }
            } else if (val < 0) {
                int m = n + val;
                if (m >= 0) {
                    return history.get(m);
                }
            }
        } else { // maybe part of previous command string...
            for (int j = n - 1; j >= 0; --j) {// process commands in reverse order.
                String str = history.get(j);
                if (str.indexOf(tmp) == 0) {
                    return str;
                }
            }
        }
        return text;
    }

    private static class SacCommandParserHolder {

        private static final SacCommandParser INSTANCE = new SacCommandParser();
    }

    public SacCommand parseCommandLine(String text) {
        //Process possible repeats of previous commands...
        if (text.trim().indexOf("!") == 0) {
            text = processRepeatRequest(text);
        }
        history.add(text); // add to history list

        String[] tokens = TokenListParser.tokenizeString(text);
        if (tokens.length < 1) {
            return new NullSacCommand();
        } else {
            String cmdName = tokens[0].toUpperCase();
            SacCommand cmd = cmdStringMap.get(cmdName);
            if (cmd != null) {
                cmd.initialize(tokens);
                return cmd;
            }
        }
        return null;
    }

}
