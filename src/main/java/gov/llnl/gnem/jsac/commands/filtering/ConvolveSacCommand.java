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
package gov.llnl.gnem.jsac.commands.filtering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACFileReader;
import gov.llnl.gnem.jsac.io.SACHeader;

public class ConvolveSacCommand implements SacCommand {

    private static final float EPS = 1.0e-06f;
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("PULSE", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("TRI", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("TRIANGLE", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("GAUSS", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("CENTERED", ValuePossibilities.ONE_VALUE, String.class));
    }

    private static int pulseIndex = 1;
    private static String filePath = null;
    private static double v = -1.0;
    private static boolean centered = false;
    private static float[] template = null;
    private static List<SacTraceData> traceData = null;
    private static double delta;

    public ConvolveSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            throw new IllegalArgumentException("No arguments passed to convolve function. Convolve has mandatory arguments (see HELP CONVOLVE).");
        }

        traceData = SacDataModel.getInstance().getData();

        // check consistency of sampling intervals
        boolean first = true;

        delta = -1.0;

        for (SacTraceData std : traceData) {
            if (first) {
                delta = std.getDelta();
                first = false;
            } else if ((std.getDelta() - delta) > EPS * delta) {
                throw new IllegalStateException("A data file has an inconsistent sampling interval, delta: " + std.getDelta());
            }
        }

        if (parsedTokens.containsKey("PULSE")) {
            List<Object> parameters = parsedTokens.get("PULSE");
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("PULSE was specified but missing required arguments.");
            }
            pulseIndex = (int) parameters.get(0);
            template = traceData.get(pulseIndex - 1).getData();
        } else if (parsedTokens.containsKey("TRI") || parsedTokens.containsKey("TRIANGLE")) {
            List<Object> parameters = parsedTokens.get("TRI");
            if (parameters == null) {
                parameters = parsedTokens.get("TRIANGLE");
            }
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("[TRI]ANGLE was specified but missing required arguments.");
            }
            v = (double) parameters.get(0);

            int n = (int) Math.round(v / delta);
            template = new float[2 * n + 1];
            template[n] = 1.0f;
            float sum = 1.0f;
            for (int i = 1; i <= n; i++) {
                template[n + i] = 1.0f - (float) i / (float) n;
                template[n - i] = template[n + i];
                sum += 2 * template[n + i];
            }
            for (int i = 0; i < template.length; i++) {
                template[i] /= sum;
            }

        } else if (parsedTokens.containsKey("GAUSS")) {
            List<Object> parameters = parsedTokens.get("GAUSS");
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("GAUSS was specified but missing required arguments.");
            }
            v = (double) parameters.get(0);

            int n = 6 * (int) Math.round(v / delta);
            template = new float[2 * n + 1];
            template[n] = 1.0f;
            float sum = 1.0f;
            for (int i = 1; i <= n; i++) {
                float x = (float) (i * delta);
                template[n + i] = (float) Math.exp(-0.5 * x * x / (v * v));
                template[n - i] = template[n + i];
                sum += 2 * template[n + i];
            }
            for (int i = 0; i < template.length; i++) {
                template[i] /= sum;
            }
        }

        if (parsedTokens.containsKey("CENTERED")) {
            List<Object> parameters = parsedTokens.get("CENTERED");
            if (parameters == null || parameters.size() <= 0) {
                throw new IllegalArgumentException("CENTERED was specified but missing required arguments.");
            }
            String offon = (String) parameters.get(0);
            if (offon.toUpperCase().equals("ON")) {
                centered = true;
            }
        }

        if (parsedTokens.containsKey(TokenListParser.LEFT_OVER_TOKENS)) {
            List<Object> parameters = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
            filePath = (String) parameters.get(0);

            try {
                SACFileReader reader = SACFileReader.fromStringPath(filePath);
                SACHeader header = reader.getHeader();

                if (Math.abs(header.getDelta() - delta) > EPS * delta) {
                    throw new IllegalStateException("Template sampling interval does not match data sampling interval: " + header.getDelta());
                }

                template = new float[header.getNpts()];
                reader.read(template);
                reader.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("File access exception attempting to open template file " + filePath + ". Error msg: " + e.getMessage());
            }
        }

        //If template is still null by now we either got an unknown type of
        // template or couldn't read it so we should throw an error.
        if (template == null) {
            throw new IllegalArgumentException("Unable to parse a template function for arguments [" + tokens + "]");
        }
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().convolve(template, centered);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CONVOLVE" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
