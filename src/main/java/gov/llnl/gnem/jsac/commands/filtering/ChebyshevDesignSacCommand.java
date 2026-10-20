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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevII;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;

public class ChebyshevDesignSacCommand implements SacCommand {

    private static final int MAX_ORDER = 12;

    public static List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("HELP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("H", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("TRANBW", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("T", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("ATTEN", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("A", ValuePossibilities.ONE_VALUE, Double.class));
    }

    private boolean help = false;
    boolean haveTBW = false;
    boolean haveAtten = false;
    private double TBW;
    private double A;

    @Override
    public void execute() {

        if (help) {
            System.out.println(getHelpString());
        } else if (haveTBW && haveAtten) {

            double epsilon = 1.0 / Math.sqrt(A * A - 1.0);

            System.out.println("      Specifications for Chebyshev Filters (I and II)     \n");
            System.out.println("  Stopband attenuation:  " + A);
            System.out.println("  Transition bandwidth:  " + TBW);
            System.out.println();
            System.out.println(" Order      Response at Passband Edge      Passband Ripple\n");

            DecimalFormat F = new DecimalFormat("0.00000000");

            for (int order = 2; order <= MAX_ORDER; order++) {
                AnalogChebyshevII ACII = new AnalogChebyshevII(order, epsilon);
                AnalogPrototypeInspector inspector = new AnalogPrototypeInspector(ACII.lptolp(1.0 + TBW));
                double pbr = inspector.getMagnitude(1.0);
                StringBuilder S = new StringBuilder("  ");
                if (order < 10) {
                    S.append(" ");
                }
                S.append(order);
                S.append("               ").append(F.format(pbr));
                S.append("                 ").append(F.format(1.0 - pbr));
                System.out.println(S.toString());
            }
            System.out.println();

        } else {
            System.out.println("Must specify both attenuation and transition bandwidth");
        }

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> parameters = parsedTokens.get("HELP");
        if (parameters == null) {
            parameters = parsedTokens.get("H");
        }
        if (parameters != null) {
            help = true;
        } else {

            parameters = parsedTokens.get("TRANBW");
            if (parameters == null) {
                parameters = parsedTokens.get("T");
            }
            if ((parameters != null) && (parameters.size() == 1)) {
                TBW = (double) parameters.get(0);
                haveTBW = true;
            }

            parameters = parsedTokens.get("ATTEN");
            if (parameters == null) {
                parameters = parsedTokens.get("A");
            }
            if ((parameters != null) && (parameters.size() == 1)) {
                A = (double) parameters.get(0);
                haveAtten = true;
            }

        }

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "CHEBYSHEVDESIGN", "CHD" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
