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
package gov.llnl.gnem.jsac.plots.plotresp;

import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;

import javax.swing.JFrame;

import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.response.TransferData;
import java.awt.GridLayout;
import java.util.logging.Level;
import llnl.gnem.core.gui.plotting.AxisScale;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.plotobject.Line;
import llnl.gnem.core.gui.util.PersistentPositionContainer;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.SeriesMath;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import org.apache.commons.math3.complex.Complex;

public class PlotRespFrame extends PersistentPositionContainer {

    private final JMultiAxisPlot ampPlot;
    private final JMultiAxisPlot phasePlot;

    private PlotRespFrame(String preferencePath, String title, int width, int height) {
        super(preferencePath, title, width, height);
        this.getContentPane().removeAll();
        setLayout(new GridLayout(2, 1));
        ampPlot = new JMultiAxisPlot();
        phasePlot = new JMultiAxisPlot();
        add(ampPlot);
        add(phasePlot);
        this.setDefaultCloseOperation(JFrame.ICONIFIED);
    }

    public void plotDataBlock(StreamKey key, TimeT effective, TransferData transferData, boolean unwrapPhase) {
        ampPlot.clear();
        ampPlot.getPlotBorder().setDrawBox(true);
        phasePlot.clear();
        phasePlot.getPlotBorder().setDrawBox(true);

        double[] frequencies = transferData.getFrequencies();
        Complex[] cdata = transferData.getData();
        float[] amplitude = new float[cdata.length - 1];
        float[] phase = new float[cdata.length - 1];
        float[] freq = new float[cdata.length - 1];
        boolean overflow = false;
        for (int j = 1; j < frequencies.length; ++j) {
            freq[j - 1] = (float) frequencies[j];
            float tmp = (float) cdata[j].abs();
            if (Float.isInfinite(tmp)) {
                overflow = true;
            }
            amplitude[j - 1] = tmp;
            phase[j - 1] = (float) Math.toDegrees(cdata[j].getArgument());
        }
        if (overflow) {
            ApplicationLogger.getInstance().log(Level.WARNING, "One or more amplitude samples overflowed to Infinity!");
        }
        if (unwrapPhase) {
            phase = unwrap(phase);
        }
        JSubplot ampSp = ampPlot.addSubplot();
        ampSp.getPlotRegion().setDrawBox(true);
        Line line = (Line) ampSp.Plot(freq, amplitude);
        line.setColor(PlotColors.getInstance().getLineColor());
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());

        ampSp.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        PlotUtils.setYlabelAttributes(ampSp);
        PlotUtils.setSubplotAttributes(ampSp);
        ampSp.getCoordinateTransform().setXScale(AxisScale.LOG);
        ampSp.getCoordinateTransform().setYScale(AxisScale.LOG);

        double yAxisMin = Math.pow(10.0, Math.floor(Math.log10(SeriesMath.getMin(amplitude))));
        double yAxisMax = Math.pow(10.0, Math.ceil(Math.log10(SeriesMath.getMax(amplitude))));
        ampSp.getYaxis().setMin(yAxisMin);
        ampSp.getYaxis().setMax(yAxisMax);

        double xAxisMin = Math.pow(10.0, Math.floor(Math.log10(freq[0])));
        double xAxisMax = Math.pow(10.0, Math.ceil(Math.log10(freq[freq.length - 1])));
        ampPlot.setAllXlimits(xAxisMin, xAxisMax);
        String unitString = transferData.getWorkingUnits().getUnitObj().toString();
        ampSp.getYaxis().setLabelText("Counts /( " + unitString + " )");
        ampPlot.getXaxis().setLabelText("Frequency (Hz)");
        ampPlot.getTitle().setText(constructTitleText("Amplitude", key, effective));

        JSubplot phaseSp = phasePlot.addSubplot();
        line = (Line) phaseSp.Plot(freq, phase);
        line.setColor(PlotColors.getInstance().getLineColor());
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
        phaseSp.getCoordinateTransform().setXScale(AxisScale.LOG);
        phaseSp.getCoordinateTransform().setYScale(AxisScale.LINEAR);

        phaseSp.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        PlotUtils.maybeSetYlimits(phaseSp);
        PlotUtils.setYlabelAttributes(phaseSp);
        PlotUtils.setSubplotAttributes(phaseSp);

        phasePlot.setAllXlimits(xAxisMin, xAxisMax);
        phasePlot.getXaxis().setLabelText("Frequency (Hz)");
        phaseSp.getYaxis().setLabelText("Phase (degrees)");
        phasePlot.getTitle().setText(constructTitleText("Phase", key, effective));

        repaint();
    }

    private String constructTitleText(String plotType, StreamKey key, TimeT effective) {
        if (key != null) {
            return String.format("%s (%s) Effective: %s", plotType, key, effective.toString());
        } else {
            return String.format("%s Effective: %s", plotType, effective.toString());
        }
    }

    public static float[] unwrap(float[] wrappedPhase) {
        if (wrappedPhase == null || wrappedPhase.length == 0) {
            return new float[0];
        }

        float[] unwrapped = new float[wrappedPhase.length];
        unwrapped[0] = wrappedPhase[0];

        // Track the cumulative offset in degrees (equivalent to 2*pi multiples)
        float correction = 0.0f;

        for (int i = 1; i < wrappedPhase.length; i++) {
            float diff = wrappedPhase[i] - wrappedPhase[i - 1];

            // If the jump is greater than +180 degrees, subtract 360 degrees
            if (diff > 180.0) {
                correction -= 360.0;
            } // If the jump is less than -180 degrees, add 360 degrees
            else if (diff < -180.0) {
                correction += 360.0;
            }

            unwrapped[i] = wrappedPhase[i] + correction;
        }

        return unwrapped;
    }

    @Override
    protected void updateCaption() {
    }

    public static PlotRespFrame getInstance() {
        return PlotFrameHolder.INSTANCE;
    }

    private static class PlotFrameHolder {

        private static final PlotRespFrame INSTANCE = new PlotRespFrame("gov/llnl/gnem/jsac/plotresp", "plotresp", 600, 600);
    }
}
