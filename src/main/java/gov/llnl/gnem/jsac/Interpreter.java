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
package gov.llnl.gnem.jsac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Supplier;

import org.jline.builtins.Completers.TreeCompleter;
import org.jline.builtins.Completers.TreeCompleter.Node;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.SacCommandExecutor;
import gov.llnl.gnem.jsac.commands.SacCommandParser;

/**
 *
 * @author dodge1
 */
public class Interpreter implements Runnable {

    private static final String SAC_PROMPT = "SAC>";

    private static final Logger log = LoggerFactory.getLogger(Interpreter.class);

    private Supplier<String> displayPromptFunc;

    private LineReader reader;
    private final Scanner sc = new Scanner(System.in);

    public Interpreter() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();

            List<String> commands = SacCommandParser.getInstance().getCommands();
            List<Node> completionNodes = new ArrayList<>();
            Completer completer;
            if (commands != null && !commands.isEmpty()) {
                List<String> helpCommands = new ArrayList<>();
                for (String value : commands) {
                    if (!"HELP".equalsIgnoreCase(value)) {
                        completionNodes.add(TreeCompleter.node(value));
                        helpCommands.add(value);
                    }
                }
                if (!helpCommands.isEmpty()) {
                    completionNodes.add(TreeCompleter.node("HELP", TreeCompleter.node(helpCommands.toArray())));
                }
            }

            if (completionNodes.isEmpty()) {
                completer = new NullCompleter();
            } else {
                completer = new TreeCompleter(completionNodes);
            }

            reader = LineReaderBuilder.builder().completer(completer).terminal(terminal).build();
            reader.setOpt(LineReader.Option.HISTORY_IGNORE_DUPS);
            reader.setOpt(LineReader.Option.CASE_INSENSITIVE);
            reader.unsetOpt(LineReader.Option.HISTORY_BEEP);

            displayPromptFunc = this::displayJlinePrompt;
            log.trace("Using JLine 3 terminal.");
        } catch (IOException e) {
            log.debug("Unable to create JLine terminal, defaulting to system prompt.", e);
            displayPromptFunc = this::displaySystemDefaultPrompt;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String text = displayPromptFunc.get().trim();
                log.trace("Got text: {}", text);
                String[] commands = text.split(";");
                for (String cmd : commands) {
                    log.debug(cmd);
                    if (isCommand(cmd)) {
                        try {
                            SacCommand command = SacCommandParser.getInstance().parseCommandLine(cmd);
                            log.trace("Created command.");
                            if (command != null) {
                                try {
                                    SacCommandExecutor.getInstance().executeCommand(command);
                                    log.trace("Executed command.");
                                } catch (Exception ex) {
                                    log.warn("Failed executing command: {}", ex.getMessage());
                                }
                            } else {
                                log.warn("Unrecognized command:{}", cmd);
                            }
                        } catch (Exception ex) {
                            log.warn("Error parsing or preparing command: {}", ex.getMessage());
                        }
                    }
                }
            } catch (NoSuchElementException ex) {
                //Ctrl+C will evoke a runtime error on sc.next() that we want to catch here.
                log.trace(ex.getLocalizedMessage(), ex);
            } catch (UserInterruptException e) {
                reader.getTerminal().writer().println("Interrupt (Ctrl+C)");
                reader.getTerminal().writer().flush();
                break;
            } catch (EndOfFileException e) {
                reader.getTerminal().writer().println("End of file (Ctrl+D)");
                reader.getTerminal().writer().flush();
                break;
            }
        }

    }

    private static boolean isCommand(String text) {
        return !text.isEmpty() && !text.startsWith("*") && !text.startsWith("--");
    }

    private String displaySystemDefaultPrompt() {
        System.out.print(SAC_PROMPT);
        System.out.flush();
        return sc.nextLine().trim();
    }

    private String displayJlinePrompt() {
        return reader.readLine(SAC_PROMPT);
    }

    public LineReader getReader() {
        return reader;
    }
}
