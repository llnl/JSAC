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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevI;
import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevII;
import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import llnl.gnem.core.signalprocessing.filter.FilterDesign;

public class IIRSacImplementation {

    public static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("BUTTER", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BU", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BESSEL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("C1", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("C2", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("NPOLES", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("N", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("PASSES", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("P", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("TRANBW", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("T", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("ATTEN", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("A", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("CORNERS", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("CORNER", ValuePossibilities.ONE_OR_MORE, Double.class));
        descriptors.add(new AttributeDescriptor("CO", ValuePossibilities.ONE_OR_MORE, Double.class));
        descriptors.add(new AttributeDescriptor("C", ValuePossibilities.ONE_OR_MORE, Double.class));

    }

    protected static final int NOMINAL_ORDER = 2;
    protected static final double NOMINAL_CUTOFF = 0.1;
    protected static final double NOMINAL_HIGH_CUTOFF = 0.4;

    protected static FilterDesign design;
    protected static int order = NOMINAL_ORDER;
    protected static PassbandType passband;
    protected static double cutoff1 = NOMINAL_CUTOFF; // not used for LP
    protected static double cutoff2 = NOMINAL_HIGH_CUTOFF; // not used for HP
    protected static double attenuation = 30.0;
    protected static double transitionBW = 0.3;
    protected static boolean twoPass = false;
    protected static double epsilon;

    public IIRSacImplementation() {
        design = null;
    }

    public static void resetDefaults() {
        design = null;
        order = NOMINAL_ORDER;
        cutoff1 = NOMINAL_CUTOFF;
        cutoff2 = NOMINAL_HIGH_CUTOFF;
        attenuation = 30.0;
        transitionBW = 0.3;
        twoPass = false;
    }

    protected Map<String, List<Object>> initialParsing(Map<String, List<Object>> parsedTokens) {

        boolean designSpecified = false;

        Set<String> keys = new HashSet<>();
        for (String value : parsedTokens.keySet()) {
            keys.add(value.toUpperCase());
        }

        if (keys.contains("BUTTER") || keys.contains("BU")) {
            design = FilterDesign.Butterworth;
            designSpecified = true;
        }

        if (keys.contains("BESSEL") || keys.contains("BE")) {
            if (designSpecified) {
                System.out.println("Cannot specify more than one design type, ignoring request for Bessel filter");
            } else {
                System.out.println("Bessel filters not implemented, using Butterworth");
                design = FilterDesign.Butterworth;
                designSpecified = true;
            }
        }

        if (keys.contains("C1")) {
            if (designSpecified) {
                System.out.println("Cannot specify more than one design type, ignoring request for Chebyshev type 1 filter");
            } else {
                design = FilterDesign.Chebyshev1;
                designSpecified = true;
            }
        }

        if (keys.contains("C2")) {
            if (designSpecified) {
                System.out.println("Cannot specify more than one design type, ignoring request for Chebyshev type 2 filter");
            } else {
                design = FilterDesign.Chebyshev2;
                designSpecified = true;
            }
        }

        if (design == null) {
            design = FilterDesign.Butterworth;
            designSpecified = true;
        }

        List<Object> parameters = maybeGetNPoles(parsedTokens);
        if (parameters != null && !parameters.isEmpty()) {
            order = (int) parameters.get(0);
            if (order < 1) {
                System.out.println("Order must be greater than 0, reset to " + NOMINAL_ORDER);
                order = NOMINAL_ORDER;
            }
        }

        if (design == FilterDesign.Chebyshev1 || design == FilterDesign.Chebyshev2) {

            parameters = maybeGetAttenuation(parsedTokens);
            if (parameters != null && !parameters.isEmpty()) {
                attenuation = (double) parameters.get(0);
            }

            parameters = maybeGetTransitionBandwidth(parsedTokens);
            if (parameters != null && !parameters.isEmpty()) {
                transitionBW = (double) parameters.get(0);
            }

            AnalogPrototypeInspector inspector = null;

            if (design == FilterDesign.Chebyshev1) {

                double[] cp = ChebyshevParameters.calculateEpsilon(attenuation, transitionBW, order);
                epsilon = cp[0];

                AnalogChebyshevI ACI = new AnalogChebyshevI(order, epsilon);
                inspector = new AnalogPrototypeInspector(ACI);

            } else if (design == FilterDesign.Chebyshev2) {

                epsilon = 1.0 / Math.sqrt(attenuation * attenuation - 1.0);

                AnalogChebyshevII ACII = new AnalogChebyshevII(order, epsilon);
                inspector = new AnalogPrototypeInspector(ACII.lptolp(1.0 + transitionBW));
            }

            if (inspector != null) {
                System.out.println("Response at analog prototype passband edge:  " + inspector.getMagnitude(1.0));
                System.out.println("Response at analog prototype stopband edge:  " + inspector.getMagnitude(1.0 + transitionBW));
            }
        }

        parameters = maybeGetNPasses(parsedTokens);
        if (parameters != null && !parameters.isEmpty()) {
            int passes = (int) parameters.get(0);
            switch (passes) {
            case 1:
                twoPass = false;
                break;
            case 2:
                twoPass = true;
                break;
            default:
                System.out.println("Number of passes must be 1 or 2");
                break;
            }
        }
        return parsedTokens;
    }

    private List<Object> maybeGetNPasses(Map<String, List<Object>> parsedTokens) {
        List<Object> parameters = parsedTokens.get("PASSES");
        if (parameters == null) {
            parameters = parsedTokens.get("P");
        }
        return parameters;
    }

    private List<Object> maybeGetTransitionBandwidth(Map<String, List<Object>> parsedTokens) {
        List<Object> parameters = parsedTokens.get("TRANBW");
        if (parameters == null) {
            parameters = parsedTokens.get("T");
        }
        return parameters;
    }

    private List<Object> maybeGetNPoles(Map<String, List<Object>> parsedTokens) {
        List<Object> parameters = parsedTokens.get("NPOLES");
        if (parameters == null) {
            parameters = parsedTokens.get("N");
        }
        return parameters;
    }

    private List<Object> maybeGetAttenuation(Map<String, List<Object>> parsedTokens) {
        List<Object> parameters = parsedTokens.get("ATTEN");
        if (parameters == null) {
            parameters = parsedTokens.get("A");
        }
        return parameters;
    }

    protected List<Object> maybeGetCorners(Map<String, List<Object>> parsedTokens) {
        List<Object> parameters = parsedTokens.get("CORNERS");
        if (parameters == null) {
            parameters = parsedTokens.get("CORNER");
        }
        if (parameters == null) {
            parameters = parsedTokens.get("CO");
        }
        if (parameters == null) {
            parameters = parsedTokens.get("C");
        }
        return parameters;
    }
}
