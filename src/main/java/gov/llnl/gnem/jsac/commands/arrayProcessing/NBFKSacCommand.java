/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.commands.arrayProcessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.arrayProcessing.ArrayData;
import gov.llnl.gnem.jsac.arrayProcessing.FKImplementation;
import gov.llnl.gnem.jsac.arrayProcessing.SacArrayModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.commands.arrayProcessing.enums.GridType;
import llnl.gnem.core.signalprocessing.arrayProcessing.FKResult;

import gov.llnl.gnem.jsac.commands.arrayProcessing.enums.FKType;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.fk.FKPlotFrame;
import gov.llnl.gnem.jsac.plots.fk.FKPlotProps;
import java.awt.Color;
import java.io.IOException;
import javax.swing.SwingUtilities;
import llnl.gnem.core.gui.plotting.fkPlot.PlotOptions;
import llnl.gnem.core.traveltime.Ak135.TraveltimeCalculatorProducer;
import llnl.gnem.core.traveltime.SinglePhaseTraveltimeCalculator;

public class NBFKSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("MLM", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("PDS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("NORMALIZE", ValuePossibilities.ZERO_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("N", ValuePossibilities.ZERO_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("EPS", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("SLOWNESS", ValuePossibilities.ONE_VALUE, Float.class));
        descriptors.add(new AttributeDescriptor("S", ValuePossibilities.ONE_VALUE, Float.class));
        descriptors.add(new AttributeDescriptor("FREQUENCY", ValuePossibilities.ONE_VALUE, Float.class));
        descriptors.add(new AttributeDescriptor("F", ValuePossibilities.ONE_VALUE, Float.class));
        descriptors.add(new AttributeDescriptor("GRID", ValuePossibilities.ONE_OR_MORE, Integer.class));
        descriptors.add(new AttributeDescriptor("G", ValuePossibilities.ONE_OR_MORE, Integer.class));
        descriptors.add(new AttributeDescriptor("LEVELS", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("L", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("DB", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("TITLE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("T", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("WINDOW", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("WIN", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("WRITE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("W", ValuePossibilities.ONE_VALUE, String.class));

        descriptors.add(new AttributeDescriptor("CONTOUR_LINE_COLOR", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CLC", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CONTOUR_TEXT_COLOR", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CTC", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CONTOUR_TEXT_BACKGROUND", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CTB", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CONTOUR_LINE_THICKNESS", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("CLT", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("PLOT_OPTIONS", ValuePossibilities.ONE_VALUE, String.class));

    }

    private boolean valid;

    private FKType fktype;
    private boolean normalize;
    private double epsilon;
    private float maxSlowness;      // in sec/km
    private float freq;
    private GridType gridType;
    private int[] gridSize;
    private int nlevels;
    private boolean logarithmicScaling;
    private boolean windowWaveforms;
    private double[] window;
    private String filename;

    public NBFKSacCommand() {
    }

    @Override
    public void execute() {

        if (valid) {

            ArrayData adata = new ArrayData(SacDataModel.getInstance().getData(), "CurrentArrayData");
            SacArrayModel.getInstance().addArray(adata);
            double[][] localArrayCoordinates = adata.getArray();
            int nch = localArrayCoordinates.length;
            float[] xnorth = new float[nch];
            float[] xeast = new float[nch];
            for (int ich = 0; ich < nch; ich++) {
                xnorth[ich] = (float) localArrayCoordinates[ich][1];
                xeast[ich] = (float) localArrayCoordinates[ich][0];
            }

            float delta = (float) adata.getSamplingInterval();

            int[] windowLimits = new int[2];
            for (int i = 0; i < 2; i++) {
                if (windowWaveforms) {
                    windowLimits[i] = (int) Math.round(window[i] / delta);
                }
            }

            float[][] x;
            if (windowWaveforms) {
                x = adata.getWindowedWaveforms(windowLimits);
            } else {
                x = adata.getWaveforms();
            }

            if (normalize) {

                for (int ich = 0; ich < nch; ich++) {
                    double tmp = 0.0;
                    float[] xx = x[ich];
                    for (int i = 0; i < xx.length; i++) {
                        tmp += xx[i] * xx[i];
                    }
                    float scale = (float) (1.0 / Math.sqrt(tmp));
                    for (int i = 0; i < xx.length; i++) {
                        xx[i] *= scale;
                    }
                }

            }

            int N = x[0].length;
            int M = (int) (1.5 * Math.sqrt(N));

            double dt = adata.getSamplingInterval();

            FKImplementation implementation = new FKImplementation(x, dt, xeast, xnorth, epsilon, M, fktype);

            int nk = gridSize[0] / 2;

            FKResult result = implementation.evaluateNarrowband(freq, maxSlowness, nk);
            SwingUtilities.invokeLater(() -> {
                FKPlotFrame.getInstance().setVisible(true);
                FKPlotFrame.getInstance().plotFKResults(result);
            });
        }
    }

    @Override
    public void initialize(String[] tokens) {

        // defaults
        valid = true;

        fktype = FKType.PDS;
        normalize = true;
        epsilon = 0.001;
        maxSlowness = 0.5f;
        freq = 2.0f;
        gridType = GridType.CARTESIAN;
        gridSize = new int[2];
        gridSize[0] = 201;
        nlevels = 11;
        logarithmicScaling = false;
        windowWaveforms = false;
        window = null;
        filename = "NBFK";
        FKPlotProps.getInstance().setPlotTitle(null);
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        if (parsedTokens.containsKey("PDS")) {
            fktype = FKType.PDS;
        }

        if (parsedTokens.containsKey("MLM")) {
            fktype = FKType.MLM;
        }

        if (parsedTokens.containsKey("NORMALIZE") || parsedTokens.containsKey("N")) {

            List<Object> parameters = parsedTokens.get("NORMALIZE");
            if (parameters == null) {
                parameters = parsedTokens.get("N");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                normalize = true;
            } else if (parameters.get(0).equals("OFF")) {
                normalize = false;
            } else {
                valid = false;
            }

        }

        if (parsedTokens.containsKey("EPS")) {

            List<Object> parameters = parsedTokens.get("EPS");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                epsilon = (double) parameters.get(0);
            }

        }

        if (parsedTokens.containsKey("SLOWNESS") || parsedTokens.containsKey("S")) {

            List<Object> parameters = parsedTokens.get("SLOWNESS");
            if (parameters == null) {
                parameters = parsedTokens.get("S");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                maxSlowness = (float) parameters.get(0);
            }

        }

        if (parsedTokens.containsKey("FREQUENCY") || parsedTokens.containsKey("F")) {

            List<Object> parameters = parsedTokens.get("FREQUENCY");
            if (parameters == null) {
                parameters = parsedTokens.get("F");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                freq = (float) parameters.get(0);
            }

        }

        if (parsedTokens.containsKey("GRID") || parsedTokens.containsKey("G")) {

            List<Object> parameters = parsedTokens.get("GRID");
            if (parameters == null) {
                parameters = parsedTokens.get("G");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {

                gridSize[0] = (int) parameters.get(0);
                gridType = GridType.CARTESIAN;
                if (parameters.size() >= 2) {
                    gridSize[1] = (int) parameters.get(1);
                    gridType = GridType.POLAR;
                }

            }

        }

        if (parsedTokens.containsKey("LEVELS") || parsedTokens.containsKey("L")) {

            List<Object> parameters = parsedTokens.get("LEVELS");
            if (parameters == null) {
                parameters = parsedTokens.get("L");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                nlevels = (int) parameters.get(0);
            }

        }

        if (parsedTokens.containsKey("DB")) {
            logarithmicScaling = true;
        }

        if (parsedTokens.containsKey("TITLE") || parsedTokens.containsKey("T")) {

            List<Object> parameters = parsedTokens.get("TITLE");
            if (parameters == null) {
                parameters = parsedTokens.get("T");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                String value = (String) parameters.get(0);
                FKPlotProps.getInstance().setPlotTitle(value);
            }

        }

        if (parsedTokens.containsKey("WINDOW") || parsedTokens.containsKey("WIN")) {

            List<Object> parameters = parsedTokens.get("WINDOW");
            if (parameters == null) {
                parameters = parsedTokens.get("WIN");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() == 2) {
                windowWaveforms = true;
                window = new double[2];
                window[0] = (double) parameters.get(0);
                window[1] = (double) parameters.get(1);
            } else {
                valid = false;
            }

        }

        if (parsedTokens.containsKey("WRITE") || parsedTokens.containsKey("W")) {

            List<Object> parameters = parsedTokens.get("WRITE");
            if (parameters == null) {
                parameters = parsedTokens.get("W");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                filename = (String) parameters.get(0);
            }

        }

        if (parsedTokens.containsKey("CONTOUR_LINE_COLOR") || parsedTokens.containsKey("CLC")) {
            List<Object> parameters = parsedTokens.get("CONTOUR_LINE_COLOR");
            if (parameters == null) {
                parameters = parsedTokens.get("CLC");
            }
            if (parameters != null) {
                if (parameters.size() == 1) {
                    Object obj = parameters.get(0);
                    try {
                        Color color = PlotColors.getColorByName((String) obj);
                        FKPlotProps.getInstance().setLineColor(color);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Invalid line color: " + obj);
                    }
                }
            }
        }

        if (parsedTokens.containsKey("CONTOUR_TEXT_COLOR") || parsedTokens.containsKey("CTC")) {
            List<Object> parameters = parsedTokens.get("CONTOUR_TEXT_COLOR");
            if (parameters == null) {
                parameters = parsedTokens.get("CTC");
            }
            if (parameters != null) {
                if (parameters.size() == 1) {
                    Object obj = parameters.get(0);
                    try {
                        Color color = PlotColors.getColorByName((String) obj);
                        FKPlotProps.getInstance().setTextColor(color);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Invalid text color: " + obj);
                    }
                }
            }
        }

        if (parsedTokens.containsKey("CONTOUR_TEXT_BACKGROUND") || parsedTokens.containsKey("CTB")) {
            List<Object> parameters = parsedTokens.get("CONTOUR_TEXT_BACKGROUND");
            if (parameters == null) {
                parameters = parsedTokens.get("CTB");
            }
            if (parameters != null) {
                if (parameters.size() == 1) {
                    Object obj = parameters.get(0);
                    try {
                        Color color = PlotColors.getColorByName((String) obj);
                        FKPlotProps.getInstance().setTextBackgroundColor(color);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Invalid text background color: " + obj);
                    }
                }
            }
        }

        if (parsedTokens.containsKey("PLOT_OPTIONS")) {
            List<Object> parameters = parsedTokens.get("PLOT_OPTIONS");
            if (parameters != null) {
                if (parameters.size() == 1) {
                    Object obj = parameters.get(0);
                    try {
                        PlotOptions po = PlotOptions.valueOf(((String) obj).toUpperCase());
                        FKPlotProps.getInstance().setPlotOptions(po);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Invalid PlotOption: " + obj);
                    }
                }
            }
        }

        if (parsedTokens.containsKey("CONTOUR_LINE_THICKNESS") || parsedTokens.containsKey("CLT")) {
            List<Object> parameters = parsedTokens.get("CONTOUR_LINE_THICKNESS");
            if (parameters == null) {
                parameters = parsedTokens.get("CLT");
            }
            if (parameters != null) {
                if (parameters.size() == 1) {
                    Object obj = parameters.get(0);
                    int thickness = (Integer) obj;
                    if (thickness <= 0) {
                        thickness = 1;
                    }
                    if (thickness > 10) {
                        thickness = 10;
                    }
                    FKPlotProps.getInstance().setLineThickness(thickness);
                }
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"NBFK"};
        return new ArrayList<>(Arrays.asList(names));
    }

    private double[] createDefaultWindow() {
        windowWaveforms = true;
        List<SacTraceData> data = SacDataModel.getInstance().getData();
        for (SacTraceData std : data) {
            double[] tmp = getWindowForP(std);
            if (tmp != null) {
                return tmp;
            }
        }
        return new double[]{140.0, 160.0};
    }

    private double[] getWindowForP(SacTraceData std) {
        SACHeader hdr = std.getSACHeader();
        if (hdr.hasEventInfo() && hdr.hasStationInfo()) {
            Double otime = hdr.getO();
            if (otime != null) {
                double dist = hdr.getDistanceDeg();
                Double depth = hdr.getEvdp();
                if (depth == null) {
                    depth = 15.0; // mid-crustal
                }
                try {
                    SinglePhaseTraveltimeCalculator ptc = TraveltimeCalculatorProducer.getInstance().getSinglePhaseTraveltimeCalculator("P");
                    double tt = ptc.getTT1D(dist, depth);
                    if (tt > 0) {
                        double phaseTime = otime + tt;
                        return new double[]{phaseTime, phaseTime + 10.0};
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    System.getLogger(BBFKSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }
        return null;
    }

}
