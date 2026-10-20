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

package gov.llnl.gnem.jsac.commands.signalMeasurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.util.TimeT;

/**
 *
 * @author dodge1
 */
public class MarktimesSacCommand implements SacCommand {

    enum DistanceType {
        HEADER, VALUE
    };

    enum OriginType {
        HEADER, VALUE, GMT
    };

    private static List<Double> velocities = new ArrayList<>();
    private static String toMarker = "T0";
    private static DistanceType distType = DistanceType.HEADER;
    private static double specifiedDistance = 0.0;
    private static OriginType originType = OriginType.HEADER;
    private static double specifiedGMT = 0.0;
    private static double specifiedOriginOffsetTime = 0;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> refs;

    static {
        String[] foo = { "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9" };
        refs = new ArrayList<>(Arrays.asList(foo));
        Double[] boo = { 2.0, 3.0, 4.0, 5.0, 6.0 };
        velocities = new ArrayList<>(Arrays.asList(boo));
    }

    static {
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("DISTANCE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("ORIGIN", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("VELOCITIES", ValuePossibilities.ONE_OR_MORE, Double.class));
        descriptors.add(new AttributeDescriptor("V", ValuePossibilities.ONE_OR_MORE, Double.class));
    }

    public MarktimesSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        processDistanceOption(parsedTokens);
        processOriginOption(parsedTokens);

        processVelocitiesOption(parsedTokens);
        List<Object> values = parsedTokens.remove("TO");
        if (values != null && values.size() == 1) {
            String tmp = (String) values.get(0);
            if (refs.contains(tmp.toUpperCase())) {
                toMarker = tmp.toUpperCase();
            } else {
                throw new IllegalStateException("Invalid marker: " + tmp.toUpperCase());
            }
        }

    }

    private void processVelocitiesOption(Map<String, List<Object>> parsedTokens) {
        List<Object> values = parsedTokens.remove("VELOCITIES");
        if (values == null) {
            values = parsedTokens.remove("V");
        }
        if (values != null && !values.isEmpty()) {
            velocities.clear();
            for (Object obj : values) {
                velocities.add((Double) obj);
            }
        }
    }

    private void processOriginOption(Map<String, List<Object>> parsedTokens) throws NumberFormatException, IllegalStateException {
        List<Object> values = parsedTokens.remove("ORIGIN");
        if (values != null && !values.isEmpty()) {
            String token = (String) values.get(0);
            if (token.equalsIgnoreCase("HEADER")) {
                originType = OriginType.HEADER;
            } else if (token.equalsIgnoreCase("GMT") || token.equalsIgnoreCase("UTC")) {
                if (values.size() < 7) {
                    throw new IllegalStateException("ORIGIN GMT option was chosen, but not enough tokens were supplied to calculate GMT time!");
                } else {
                    int year = Integer.parseInt(((String) values.get(1)));
                    int jdate = Integer.parseInt(((String) values.get(2)));
                    int hour = Integer.parseInt(((String) values.get(3)));
                    int minute = Integer.parseInt(((String) values.get(4)));
                    int second = Integer.parseInt(((String) values.get(5)));
                    int msec = Integer.parseInt(((String) values.get(6)));
                    TimeT tmp = new TimeT(year, jdate, hour, minute, second, msec);
                    specifiedGMT = tmp.getEpochTime();
                    originType = OriginType.GMT;
                }
            } else if (NumberUtils.isParsable(token)) {
                specifiedOriginOffsetTime = Double.parseDouble(token);
                originType = OriginType.VALUE;
            } else {
                throw new IllegalArgumentException("Unrecognized ORIGIN specifier: " + token);
            }
        }
    }

    private void processDistanceOption(Map<String, List<Object>> parsedTokens) throws IllegalArgumentException {
        List<Object> values = parsedTokens.remove("DISTANCE");
        if (values != null && values.size() == 1) {
            String token = (String) values.get(0);
            if (token.equalsIgnoreCase("HEADER")) {
                distType = DistanceType.HEADER;
            } else if (NumberUtils.isParsable(token)) {
                double v = Double.parseDouble(token);
                if (v > 0) {
                    specifiedDistance = v;
                    distType = DistanceType.VALUE;
                } else {
                    throw new IllegalArgumentException("Specified distance must be > 0!");
                }
            } else {
                throw new IllegalArgumentException("Unrecognized DISTANCE specifier: " + token);
            }
        }
    }

    @Override
    public void execute() {

        List<SacTraceData> data = SacDataModel.getInstance().getData();
        for (SacTraceData std : data) {
            SACHeader hdr = std.getSACHeader();
            double dist = getDistanceValue(hdr);

            Double time = getTimeOffset(hdr);

            int idx = refs.indexOf(toMarker);
            for (double vel : velocities) {
                double tt = dist / vel;
                if (time != null) {
                    double phaseTime = time + tt;
                    hdr.setT(idx, phaseTime);
                    String label = String.format("%5.1f", vel);
                    hdr.setKt(idx++, label);
                }
                if (idx >= 10) {
                    System.out.println("No more Tn header variables available! Truncating input to first 10 provided.");
                    return;
                }
            }

        }
    }

    private Double getTimeOffset(SACHeader hdr) {
        switch (originType) {
        case HEADER:
            return hdr.getO();
        case VALUE:
            return specifiedOriginOffsetTime;
        case GMT:
            return specifiedGMT - hdr.getReferenceTime();
        }
        return null;
    }

    private double getDistanceValue(SACHeader hdr) throws IllegalStateException {
        double dist = specifiedDistance;
        if (distType == DistanceType.HEADER) {
            if (hdr.hasEventInfo() && hdr.hasStationInfo()) {
                dist = hdr.getDistanceKm();
            } else {
                throw new IllegalStateException("Cannot compute distance from header!");
            }
        }
        return dist;
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "MARKTIMES", "MARKT" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
