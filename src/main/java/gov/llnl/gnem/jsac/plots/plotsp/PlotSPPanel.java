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
package gov.llnl.gnem.jsac.plots.plotsp;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import gov.llnl.gnem.jsac.commands.graphicsAction.PlotSPSacCommand.ComponentType;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.plots.FileIDInfo;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.Scale;
import gov.llnl.gnem.jsac.plots.SharedPlotPanel;
import gov.llnl.gnem.jsac.plots.Xlimits;
import java.awt.Color;
import llnl.gnem.core.gui.plotting.AxisScale;
import llnl.gnem.core.gui.plotting.MouseOverPlotObject;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JPlotKeyMessage;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.plotobject.Line;
import llnl.gnem.core.gui.plotting.plotobject.PlotObject;
import llnl.gnem.core.gui.plotting.plotobject.SymbolStyle;

/**
 * @author dodge1
 */
public class PlotSPPanel extends SharedPlotPanel implements Observer {

    private static PlotSPPanel INSTANCE = null;
    private final Map<Line, SacTraceData> lineTraceMap;
    private final Map<Line, JSubplot> lineSubplotMap;

    public static PlotSPPanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlotSPPanel();
        }
        return INSTANCE;
    }

    private PlotSPPanel() {
        super(XAxisType.Standard);
        lineTraceMap = new HashMap<>();
        lineSubplotMap = new HashMap<>();
        addPlotObjectObserver(this);
    }

    public void plotDFT(SacTraceData sacData, int fileNumber, Scale xScale, Scale yScale, ComponentType requested) {
        clear();
        lineTraceMap.clear();
        lineSubplotMap.clear();
        setXaxisType(XAxisType.Standard);

        if (sacData == null) {
            return;
        }
        SpectralData dft = sacData.getSpectralData();
        if (dft == null) {
            return;
        }
        if (isSinglePlot(requested)) {
            plotDFTSingle(sacData, fileNumber, xScale, yScale, requested);
        } else {
            plotDFTPair(sacData, fileNumber, xScale, yScale, requested);
        }

        repaint();

    }

    private void plotDFTPair(SacTraceData sacData, int fileNumber, Scale xScale, Scale yScale, ComponentType requested) {

        setXaxisType(XAxisType.Standard);

        SpectralData dft = sacData.getSpectralData();

        float[] data1 = null;
        float[] data2 = null;
        String ylabel1 = null;
        String ylabel2 = null;
        Scale yScale1 = yScale;
        Scale yScale2 = yScale;
        switch (requested) {
            case RLIM:
                data1 = dft.getPositiveFreqRealArray();
                ylabel1 = "Real component";
                yScale1 = Scale.LINEAR;
                data2 = dft.getPositiveFreqImagArray();
                ylabel2 = "Imag component";
                yScale2 = Scale.LINEAR;
                break;
            case AMPH:
                data1 = dft.getPositiveFreqAmplitudeArray();
                ylabel1 = "Amplitude";
                data2 = dft.getPositiveFreqPhaseArray();
                ylabel2 = "Phase (radians)";
                yScale2 = Scale.LINEAR;
                break;
        }

        if (data1 == null || data2 == null) {
            return;
        }
        double deltaF = dft.getDelfreq();

        if (data1.length <= 1 || data2.length <= 1) {
            return;
        }
        int lineWidth = 1;
        JSubplot subplot1 = addPlot(deltaF, lineWidth, data1, sacData, fileNumber, yScale1, ylabel1);

        JSubplot subplot2 = addPlot(deltaF, lineWidth, data2, sacData, fileNumber, yScale2, ylabel2);

        setXscale(xScale);
        double minFreq = deltaF;
        double maxFreq = data1.length * deltaF;
        Xlimits xlimits = PlotPrefs.getInstance().getXlimits();
        if (xlimits.isLimitsOn()) {
            minFreq = xlimits.getPdw().getStartOffset();
            maxFreq = xlimits.getPdw().getEndOffset();
        }

        PlotUtils.setXlimits(this, subplot1, minFreq, maxFreq, xScale);
        PlotUtils.setXlimits(this, subplot2, minFreq, maxFreq, xScale);

        PlotUtils.setXlabelAttributes(this);
        getXaxis().setLabelText("Frequency (Hz)");
        getXaxis().setLabelFontSize(14);

        PlotUtils.setXaxisColor(this);

        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);

    }

    private JSubplot addPlot(double deltaF, int lineWidth, float[] data, SacTraceData sacData, int fileNumber, Scale yScale, String ylabel1) {
        JSubplot subplot = addSubplot();
        Line line = (Line) subplot.Plot(deltaF, deltaF, lineWidth, data);
        lineTraceMap.put(line, sacData);
        lineSubplotMap.put(line, subplot);
        PlotUtils.setYAxisLimits(subplot, data, yScale);
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
        line.setSymbolStyle(SymbolStyle.CIRCLE);
        line.setPlotLineSymbols(true);
        line.setColor(PlotColors.getInstance().getLineColor());
        subplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        Double markerCorrection = 0.0;
        Color textColor = FileIDInfo.getInstance().getTextColor(line.getColor());
        PlotUtils.addTextToPlot(sacData, subplot, fileNumber, markerCorrection, textColor);

        PlotUtils.setYlabelAttributes(subplot);
        subplot.getYaxis().setLabelText(ylabel1);
        subplot.getYaxis().setLabelFontSize(16);
        PlotUtils.setSubplotAttributes(subplot);
        setYAxisScale(subplot, yScale);
        PlotUtils.maybeSetYlimits(subplot);
        return subplot;
    }

    private void plotDFTSingle(SacTraceData sacData, int fileNumber, Scale xScale, Scale yScale, ComponentType requested) {
        JSubplot subplot = addSubplot();
        setXaxisType(XAxisType.Standard);

        SacPlotData spd = sacData.getPlotData();
        float[] data = null;
        String ylabel = null;
        if (null != requested) {
            switch (requested) {
                case AM:
                    data = spd.getPositiveFreqAmplitudeArray();
                    ylabel = "Amplitude";
                    break;
                case PH:
                    data = spd.getPositiveFreqPhaseArray();
                    ylabel = "Phase (radians)";
                    break;
                case RL:
                    data = spd.getPositiveFreqRealArray();
                    ylabel = "Real component";
                    break;
                case IM:
                    data = spd.getPositiveFreqImagArray();
                    ylabel = "Imag component";
                    break;
                default:
                    break;
            }
        }
        if (data == null) {
            return;
        }

        if (data.length <= 1) {
            return;
        }
        int lineWidth = 1;
        float[] frequencies = spd.getPositiveFrequencies();
        Line line = (Line) subplot.Plot(frequencies, data);
        line.setWidth(lineWidth);
        lineTraceMap.put(line, sacData);
        lineSubplotMap.put(line, subplot);
        PlotUtils.setYAxisLimits(subplot, data, yScale);
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
        line.setSymbolStyle(SymbolStyle.CIRCLE);
        line.setPlotLineSymbols(true);
        line.setColor(PlotColors.getInstance().getLineColor());
        subplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());

        Double markerCorrection = 0.0;
        Color textColor = FileIDInfo.getInstance().getTextColor(line.getColor());
        PlotUtils.addTextToPlot(sacData, subplot, fileNumber, markerCorrection, textColor);

        setYAxisScale(subplot, yScale);
        PlotUtils.maybeSetYlimits(subplot);
        setXscale(xScale);
        double minFreq = frequencies[0];
        double maxFreq = frequencies[data.length - 1];
        Xlimits xlimits = PlotPrefs.getInstance().getXlimits();
        if (xlimits.isLimitsOn()) {
            minFreq = xlimits.getPdw().getStartOffset();
            maxFreq = xlimits.getPdw().getEndOffset();
        }
        PlotUtils.setXlimits(this, subplot, minFreq, maxFreq, xScale);
        PlotUtils.setYlabelAttributes(subplot);
        subplot.getYaxis().setLabelText(ylabel);
        subplot.getYaxis().setLabelFontSize(16);
        PlotUtils.setXlabelAttributes(this);
        getXaxis().setLabelText("Frequency (Hz)");
        getXaxis().setLabelFontSize(14);

        PlotUtils.setXaxisColor(this);
        PlotUtils.setSubplotAttributes(subplot);

        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        repaint();

    }

    @Override
    public void update(Observable o, Object obj) {
        if (obj instanceof MouseOverPlotObject) {
            MouseOverPlotObject mopo = (MouseOverPlotObject) obj;
            PlotObject po = mopo.getPlotObject();
        } else if (obj instanceof JPlotKeyMessage) {
            JPlotKeyMessage msg = (JPlotKeyMessage) obj;
            KeyEvent e = msg.getKeyEvent();
            if (e.getKeyChar() == '+') {
                zoomInAroundMouse(msg);
            }
        }
    }

    @Override
    public void zoomInAroundMouse(JPlotKeyMessage msg) {
        PlotUtils.zoomInAroundMouse(this, msg);
    }

    @Override
    public void magnifyTraces() {
        scaleTraces(2.0);

    }

    private void scaleTraces(double factor) {
        for (JSubplot subplot : this.lineSubplotMap.values()) {
            JMultiAxisPlot.scaleTraces(subplot, factor);
        }
        this.repaint();
    }

    @Override
    public void reduceTraces() {
        scaleTraces(0.5);
    }

    @Override
    public void autoScaleTraces() {
        scaleAllTraces(false);
    }

    @Override
    public void scaleAllTraces(boolean resetYlimits) {
        double xmin = getXaxis().getMin();
        double xmax = getXaxis().getMax();
        PlotUtils.scaleAllTraces(xmin, xmax, lineSubplotMap);
        repaint();
    }

    private void setXscale(Scale scale) {
        if (scale == Scale.LOG) {
            getCoordinateTransform().setXScale(AxisScale.LOG);
        } else {
            getCoordinateTransform().setXScale(AxisScale.LINEAR);
        }
    }

    private void setYAxisScale(JSubplot subplot, Scale scale) {
        if (scale == Scale.LOG) {
            subplot.getCoordinateTransform().setYScale(AxisScale.LOG);
        } else {
            subplot.getCoordinateTransform().setYScale(AxisScale.LINEAR);
        }
    }

    private boolean isSinglePlot(ComponentType requested) {
        switch (requested) {
            case RLIM:
            case AMPH:
                return false;
            default:
                return true;
        }
    }

}
