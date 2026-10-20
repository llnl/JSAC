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
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author dodge1
 */
public class PauseSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(PauseSacCommand.class);

    private boolean doExecute = true;
    private String message = "Pausing";
    private long period = 1;
    private boolean pauseForPeriod = false;
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("PERIOD", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("MESSAGE", ValuePossibilities.ONE_VALUE, String.class));
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> periodArgs = parsedTokens.get("PERIOD");
        if (periodArgs == null) {

        } else if (periodArgs.isEmpty()) {
            log.warn("The PERIOD option requires a sub-argument!");
            doExecute = false;
            return;
        } else {
            String tmp = ((String) periodArgs.get(0)).trim().toUpperCase();
            switch (tmp) {
                case "ON":
                    pauseForPeriod = true;
                    break;
                case "OFF":
                    pauseForPeriod = false;
                    break;
                default:
                    try {
                        int tmpVal = Integer.parseInt(tmp);
                        if (tmpVal > 0) {
                            period = tmpVal;
                            pauseForPeriod = true;
                        } else {
                            log.warn("Expected a positive Integer value for period but got: " + tmp + "!");
                            doExecute = false;
                        }
                    } catch (NumberFormatException e) {
                        doExecute = false;
                        log.warn("Expected an Integer value for period but got: " + tmp + "!");
                    }
                    break;
            }
        }
        List<Object> arguments = parsedTokens.get("MESSAGE");
        if (arguments != null && arguments.size() == 1) {
            message = (String) arguments.get(0);
        }
    }

    @Override
    public void execute() {
        if (!doExecute) {
            return;
        }
        System.out.println(message);
        if (pauseForPeriod) {
            try {
                TimeUnit.SECONDS.sleep(period);

            } catch (InterruptedException e) {
                // Handle the case where the thread is interrupted while sleeping
                Thread.currentThread().interrupt(); // Re-interrupt the thread
            }

        } else {
            try (Scanner sc = new Scanner(System.in)) {
                sc.nextLine().trim();
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"PAUSE"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
