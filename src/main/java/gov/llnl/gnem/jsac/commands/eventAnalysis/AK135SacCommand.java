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

package gov.llnl.gnem.jsac.commands.eventAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.traveltime.SinglePhaseTraveltimeCalculator;
import llnl.gnem.core.traveltime.Ak135.TraveltimeCalculatorProducer;

/**
 *
 * @author dodge1
 */
public class AK135SacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(AK135SacCommand.class);

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> refs;

    static {
        String[] foo = { "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9" };
        refs = new ArrayList<>(Arrays.asList(foo));
    }

    static {
        descriptors.add(new AttributeDescriptor("PHASENAME", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("NAME", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("LIST", ValuePossibilities.NO_VALUE, null));
    }

    private final List<String> phaseNames;
    private static String targetField = "T0";
    private final Map<String, SinglePhaseTraveltimeCalculator> phaseCalculatorMap;
    private static final Collection<String> allowableNames = new ArrayList<>();
    static {
        try {
            allowableNames.addAll(TraveltimeCalculatorProducer.getInstance().getAllowablePhases());
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Failed retrieving list of allowable phase names!");
        }
    }

    public AK135SacCommand() {
        phaseNames = new ArrayList<>();
        phaseCalculatorMap = new LinkedHashMap<>();
    }

    @Override
    public void initialize(String[] tokens) {
        phaseNames.clear();
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty()) {
            return;
        }
        if (parsedTokens.get("LIST") != null) { // just list the phases
            for (String phase : allowableNames) {
                System.out.println(phase);
            }
            return;
        }
        for (String header : refs) {
            List<Object> objs = parsedTokens.remove(header);
            if (objs != null) {
                targetField = header;
                break;
            }
        }
        phaseNames.addAll(getPhaseNames(parsedTokens));

        for (String phase : phaseNames) {
            try {
                phaseCalculatorMap.put(phase, TraveltimeCalculatorProducer.getInstance().getSinglePhaseTraveltimeCalculator(phase));
            } catch (IOException | ClassNotFoundException ex) {
                log.error("Could not implement calculator for phase: {}", phase);
            }
        }

    }

    @Override
    public void execute() {
        if (!phaseCalculatorMap.isEmpty() && targetField != null) {
            List<SacTraceData> data = SacDataModel.getInstance().getData();
            for (SacTraceData std : data) {
                SACHeader hdr = std.getSACHeader();
                if (hdr.hasEventInfo() && hdr.hasStationInfo()) {
                    Double otime = hdr.getO();
                    if (otime != null) {
                        double dist = hdr.getDistanceDeg();
                        Double depth = hdr.getEvdp();
                        if (depth == null) {
                            depth = 15.0; // mid-crustal
                        }
                        int index = Integer.parseInt(targetField.substring(1));
                        for (String phase : phaseCalculatorMap.keySet()) {
                            if (index > 9) {
                                break;
                            }
                            SinglePhaseTraveltimeCalculator ptc = phaseCalculatorMap.get(phase);
                            double tt = ptc.getTT1D(dist, depth);
                            if (tt > 0) {
                                double phaseTime = otime + tt;
                                hdr.setT(index, phaseTime);
                                hdr.setKt(index, phase);
                                ++index;
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "AK", "AK135" };
        return new ArrayList<>(Arrays.asList(names));
    }

    private Collection<String> getPhaseNames(Map<String, List<Object>> parsedTokens) {
        Collection<String> result = new ArrayList<>();
        List<Object> values = parsedTokens.get("PHASENAME");
        if (values == null) {
            values = parsedTokens.get("NAME");
        }
        if (values == null) {
            throw new IllegalStateException("No PhaseName keyword supplied!");
        }
        if (values.isEmpty()) {
            throw new IllegalStateException("No Phase Name was supplied!");
        }
        for (Object obj : values) {
            String tmp = (String) obj;
            if (allowableNames.contains(tmp)) {
                result.add(tmp);
            } else {
                System.out.println("The name (" + tmp + ") is not an allowable phase name! Skipping...");
            }
        }
        return result;
    }

}
