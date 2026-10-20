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
package gov.llnl.gnem.jsac.commands.dataFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
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
import gov.llnl.gnem.jsac.io.SACAlphaReader;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.io.enums.Iztype;

/**
 *
 * @author dodge1
 */
public class FuncgenSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("IMPULSE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("STEP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("BOXCAR", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("TRIANGLE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SINE", ValuePossibilities.TWO_VALUES, Float.class));
        descriptors.add(new AttributeDescriptor("LINE", ValuePossibilities.TWO_VALUES, Float.class));
        descriptors.add(new AttributeDescriptor("QUADRATIC", ValuePossibilities.THREE_VALUES, Float.class));
        descriptors.add(new AttributeDescriptor("CUBIC", ValuePossibilities.FOUR_VALUES, Float.class));
        descriptors.add(new AttributeDescriptor("SEIS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SEISMOGRAM", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("NPTS", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("DELTA", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("BEGIN", ValuePossibilities.ONE_VALUE, Float.class));
        descriptors.add(new AttributeDescriptor("B", ValuePossibilities.ONE_VALUE, Float.class));

    }

    private FunctionType getType(Map<String, List<Object>> groups) {
        for (FunctionType type : FunctionType.values()) {
            String name = type.name();
            Object foo = groups.get(name);
            if (foo != null) {
                return type;
            }
        }
        return null;
    }

    enum FunctionType {
        IMPULSE, STEP, BOXCAR, TRIANGLE, SINE, LINE, QUADRATIC, CUBIC, SEISMOGRAM, SEIS
    }

    private static FunctionType functionType = FunctionType.IMPULSE;
    private static int npts = 100;
    private static double delta = 1.0;
    private static double begin = 0.0;
    private static double frequency = 0.05;
    private static double phase = 0.0;
    private static double slope = 1.0;
    private static double intercept = 1.0;
    private static double aCoeff = 1;
    private static double bCoeff = 1;
    private static double cCoeff = 1;
    private static double dCoeff = 1;

    public FuncgenSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }
        FunctionType type = getType(parsedTokens);
        if (type != null) {
            functionType = type;
        }

        List<Object> funcParams = parsedTokens.get(functionType.name());
        maybeSetTypeParams(funcParams);

        maybeSetNpts(parsedTokens.get("NPTS"));
        maybeSetDelta(parsedTokens.get("DELTA"));
        maybeSetBegin(parsedTokens.get("BEGIN"));
        maybeSetBegin(parsedTokens.get("B")); // In case user chooses 'B' the header variable name
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().clear();
        switch (functionType) {
            case IMPULSE:
            case STEP:
            case BOXCAR:
            case TRIANGLE:
            case SINE:
            case LINE:
            case QUADRATIC:
            case CUBIC: {
                SacTraceData std = createTraceData();
                SacDataModel.getInstance().setSingleSeismogram(std);
                break;
            }
            case SEIS:
            case SEISMOGRAM: {
                SacTraceData std = createSacFile();
                SacDataModel.getInstance().setSingleSeismogram(std);
                break;
            }

        }
    }

    private SacTraceData createTraceData() {
        float[] data = createFunction();
        SACHeader hdr = new SACHeader();
        hdr.setDelta(delta);
        hdr.setB(begin);
        hdr.setNpts(npts);
        hdr.setE(begin + (npts - 1) * delta);
        hdr.setIftype(FileType.ITIME);
        hdr.setLeven(1);
        hdr.setIztype(Iztype.IDAY);
        hdr.setKstnm("sta");
        hdr.setKcmpnm("Q");
        String kevnm = String.format("FUNCGEN: %s", functionType);
        hdr.setKevnm(kevnm);
        SACFile sf = new SACFile(hdr, data);
        return new SacTraceData(Paths.get(functionType.toString()), sf);
    }

    private float[] createFunction() {
        float[] data = new float[npts];
        switch (functionType) {
            case IMPULSE: {
                int pos = npts / 2;
                data[pos] = 1.0f;
                break;
            }
            case STEP: {
                int pos = npts / 2;
                for (int j = pos; j < npts; ++j) {
                    data[j] = 1.0f;
                }
                break;
            }
            case BOXCAR: {

                int p1 = npts / 3;
                int p2 = 2 * npts / 3;

                for (int j = p1; j < p2; ++j) {
                    data[j] = 1;
                }
                break;
            }
            case TRIANGLE: {
                int s = npts / 6;
                int c = npts / 2;

                data[c] = 1.0f;
                for (int is = 1; is < s; is++) {
                    data[c + is] = 1.0f - ((float) is / (float) s);
                    data[c - is] = data[c + is];
                }
                break;
            }
            case SINE: {
                double phaseInRadians = Math.toRadians(phase);
                for (int j = 0; j < npts; ++j) {
                    double t = j * delta;
                    data[j] = (float) Math.sin(phaseInRadians + 2 * Math.PI * frequency * t);
                }
                break;
            }
            case LINE:
                for (int j = 0; j < npts; ++j) {
                    data[j] = (float) (intercept + j * slope);
                }
                break;
            case QUADRATIC: {
                for (int j = 0; j < npts; ++j) {
                    double t = j * delta;
                    data[j] = (float) (aCoeff * t * t + bCoeff * t + cCoeff);
                }
            }
            case CUBIC: {
                for (int j = 0; j < npts; ++j) {
                    double t = j * delta;
                    data[j] = (float) (aCoeff * t * t * t + bCoeff * t * t + cCoeff * t + dCoeff);
                }
            }
            case SEIS:
                break;
            case SEISMOGRAM:
                break;
            default:
                break;
        }
        return data;
    }

    private void maybeSetNpts(List<Object> values) {
        if (values != null && values.size() == 1) {
            Integer v = (Integer) values.get(0);
            npts = v;
        }
    }

    private void maybeSetDelta(List<Object> values) {
        if (values != null && values.size() == 1) {
            Double v = (Double) values.get(0);
            delta = v;
        }
    }

    private void maybeSetBegin(List<Object> values) {
        if (values != null && values.size() == 1) {
            Float tmp = (Float) values.get(0);
            if (tmp != null) {
                begin = (Double) tmp.doubleValue();
            }
        }
    }

    private void maybeSetTypeParams(List<Object> funcParams) {

        switch (functionType) {
            case SINE: {
                if (funcParams != null && funcParams.size() == 2) {
                    Float v1 = (Float) funcParams.get(0);
                    Float v2 = (Float) funcParams.get(1);
                    frequency = v1 != null ? v1 : frequency;
                    phase = v2 != null ? v2 : phase;
                }
                break;
            }
            case LINE: {
                if (funcParams != null && funcParams.size() == 2) {
                    Float v1 = (Float) funcParams.get(0);
                    Float v2 = (Float) funcParams.get(1);
                    slope = v1 != null ? v1 : slope;
                    intercept = v2 != null ? v2 : intercept;
                }
                break;
            }

            case QUADRATIC: {
                if (funcParams != null && funcParams.size() == 3) {
                    Float v1 = (Float) funcParams.get(0);
                    Float v2 = (Float) funcParams.get(1);
                    Float v3 = (Float) funcParams.get(2);
                    aCoeff = v1 != null ? v1 : aCoeff;
                    bCoeff = v2 != null ? v2 : bCoeff;
                    cCoeff = v3 != null ? v3 : cCoeff;
                }
                break;
            }
            case CUBIC: {
                if (funcParams != null && funcParams.size() == 4) {
                    Float v1 = (Float) funcParams.get(0);
                    Float v2 = (Float) funcParams.get(1);
                    Float v3 = (Float) funcParams.get(2);
                    Float v4 = (Float) funcParams.get(3);
                    aCoeff = v1 != null ? v1 : aCoeff;
                    bCoeff = v2 != null ? v2 : bCoeff;
                    cCoeff = v3 != null ? v3 : cCoeff;
                    dCoeff = v4 != null ? v4 : dCoeff;
                }
                break;
            }
            case BOXCAR:
                break;
            case IMPULSE:
                break;
            case SEIS:
                break;
            case SEISMOGRAM:
                break;
            case STEP:
                break;
            case TRIANGLE:
                break;
            default:
                break;
        }
    }

    private SacTraceData createSacFile() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                return new SacTraceData(Paths.get(FunctionType.SEISMOGRAM.toString()), sac);
            }
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"FG", "FUNCGEN"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
