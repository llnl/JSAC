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
package gov.llnl.gnem.jsac.plots.plot2;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;
import gov.llnl.gnem.jsac.plots.CLabel;
import gov.llnl.gnem.jsac.plots.FileIDInfo;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.SharedPlotPanel;
import gov.llnl.gnem.jsac.plots.TimeMode;
import static gov.llnl.gnem.jsac.plots.TimeMode.ABSOLUTE;
import static gov.llnl.gnem.jsac.plots.TimeMode.RELATIVE;
import gov.llnl.gnem.jsac.plots.Xlimits;
import gov.llnl.gnem.jsac.util.PartialDataWindow;
import java.util.ArrayList;
import java.util.logging.Level;
import llnl.gnem.core.gui.plotting.MouseMode;
import llnl.gnem.core.gui.plotting.MouseOverPlotObject;
import llnl.gnem.core.gui.plotting.PickCreationInfo;
import llnl.gnem.core.gui.plotting.PlotObjectClicked;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JPlotKeyMessage;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.PickMovedState;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.VPickLine;
import llnl.gnem.core.gui.plotting.plotobject.DataText;
import llnl.gnem.core.gui.plotting.plotobject.Line;
import llnl.gnem.core.gui.plotting.plotobject.PlotObject;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.PairT;
import llnl.gnem.core.util.SeriesMath;

public class Plot2Panel extends SharedPlotPanel implements Observer {

    private static Plot2Panel INSTANCE = null;
    private final Map<Line, SacTraceData> lineTraceMap;
    private final Map<Line, JSubplot> lineSubplotMap;

    public static Plot2Panel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Plot2Panel();
        }
        return INSTANCE;
    }

    private Plot2Panel() {
        super(JMultiAxisPlot.XAxisType.Standard);
        lineTraceMap = new HashMap<>();
        lineSubplotMap = new HashMap<>();
        addPlotObjectObserver(this);
    }

    public void plotData(List<SacTraceData> data, TimeMode timeMode, String ReferenceMarkerName) {
        clear();
        lineTraceMap.clear();
        lineSubplotMap.clear();
        JSubplot sp = addSubplot();
        sp.setGenerateLineSelectionRegion(false);

        double vOffset = 3;

        float minYval = Float.MAX_VALUE;
        float maxYval = -minYval;
        double minXval = Double.MAX_VALUE;
        double maxXval = -minXval;
        SacTraceData earliest = PlotUtils.getEarliestTrace(data); // This is the trace with the earliest begin time (epoch time)
        Double referenceTime = earliest.getReferenceTime();
        if (referenceTime == null) {
            referenceTime = 0.0;
        }
        List<String> idStrings = new ArrayList<>();
        List<Color> colorList = new ArrayList<>();
        for (int j = 0; j < data.size(); ++j) {
            SacTraceData std = data.get(j);
            Double value = std.getReferenceTime();
            if (value == null) {
                String msg = String.format("File %s has no reference time set. Setting reference time to 0.0 to continue...", std.toString());
                ApplicationLogger.getInstance().log(Level.WARNING, msg);
                std.getSACHeader().setReferenceTime(0.0);
            }

            SacTraceData td = data.get(j);
            SacPlotData spd = td.getPlotData();
            if (spd.hasNoData()) {
                continue;
            }
            spd.maybeApplyFloorToYvalues();
//            spd.maybeApplyXLimits(td);
            if (spd.hasNoData()) {
                continue;
            }

            float[] data1;
            Color color;
            if (spd.isFrequencyDomainData()) {
                PresentationFormat fmt = spd.getPresentationFormat();
                if (fmt == PresentationFormat.AmplitudePhase) {
                    data1 = spd.getPositiveFreqAmplitudeArray();
                } else {
                    data1 = spd.getPositiveFreqRealArray();
                }
                float[] frequencies = spd.getPositiveFrequencies();
                color = plotSingleLine(j, sp, frequencies, data1, td);
                if (minXval > frequencies[0]) {
                    minXval = frequencies[0];
                }
                double v = frequencies[data1.length - 1];
                if (maxXval < v) {
                    maxXval = v;
                }
            } else {
                float[] xValues = getXValues(spd, timeMode, earliest, td, ReferenceMarkerName);

                data1 = spd.getYValues();
                color = plotSingleLine(j, sp, xValues, data1, td);
                float t0 = xValues[0];
                if (minXval > t0) {
                    minXval = t0;
                }
                float v = xValues[xValues.length - 1];
                if (v > maxXval) {
                    maxXval = v;
                }
            }
            sp.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
            Color textColor = FileIDInfo.getInstance().getTextColor(color);

            Double reftimeShift = getDisplayReftimeShift(timeMode, earliest, td, ReferenceMarkerName);

            Collection<String> tmpList = PlotUtils.getPlotStrings(vOffset, td, earliest, reftimeShift);
            for (int m = 0; m < tmpList.size(); ++m) {
                colorList.add(textColor);
            }
            idStrings.addAll(tmpList);
            idStrings.add("   ");// Want a space between each set of file ID strings
            colorList.add(textColor);
            PlotUtils.setYlabelAttributes(sp);
            PlotUtils.setSubplotAttributes(sp);
            float min = SeriesMath.getMin(data1);
            if (min < minYval) {
                minYval = min;
            }
            float max = SeriesMath.getMax(data1);
            if (max > maxYval) {
                maxYval = max;
            }
        }

        PlotUtils.addStringsToPlot(idStrings, colorList, vOffset, sp);
        // First limits operates if ylim is off. The second when ylim is on.
        PlotUtils.setYAxisLimits(sp, minYval, maxYval, PlotPrefs.getInstance().getYscale());
        PlotUtils.maybeSetYlimits(sp);
        Xlimits xlimits = PlotPrefs.getInstance().getXlimits();
        if (xlimits.isLimitsOn()) {
            minXval = xlimits.getPdw().getStartOffset();
            maxXval = xlimits.getPdw().getEndOffset();
        }
        PlotUtils.setXlimits(this, sp, minXval, maxXval, PlotPrefs.getInstance().getXscale());
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        PlotUtils.setXaxisColor(this);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        for (CLabel label : PlotPrefs.getInstance().getCLabels()) {
            DataText dt = new DataText(label.getxValue(), label.getyValue(),
                    label.getLabelText(), label.getFontName(),
                    label.getFontSize(), label.getTextColor(),
                    label.getHorizAlignment(), label.getVertAlignment());
            sp.AddPlotObject(dt);
        }
        PlotUtils.maybeDisplayPLabels(sp);

        repaint();
    }

    private Double getDisplayReftimeShift(TimeMode timeMode, SacTraceData earliest, SacTraceData td, String ReferenceMarkerName) {
        Double reftimeShift = 0.0;
        switch (timeMode) {
            case RELATIVE: {
                SacTraceData reference = earliest;
                reftimeShift = td.getSACHeader().getReferenceTime() - reference.getReferenceTime();
                break;
            }
            case ABSOLUTE: {
                reftimeShift = -(Double) td.getSACHeader().getValue(ReferenceMarkerName);
                break;
            }
        }
        return reftimeShift;
    }

    private float[] getXValues(SacPlotData spd, TimeMode timeMode, SacTraceData earliest, SacTraceData td, String ReferenceMarkerName) {
        float[] xValues = null;
        switch (timeMode) {
            case ABSOLUTE: {
                SacTraceData reference = earliest;
                Double traceShift = td.getSACHeader().getReferenceTime() - reference.getReferenceTime();
                xValues = spd.getShiftedXValues(traceShift);
                break;
            }
            case RELATIVE: {
                Double traceShift = -(Double) td.getSACHeader().getValue(ReferenceMarkerName);
                xValues = spd.getShiftedXValues(traceShift);
                break;
            }
        }
        return xValues;
    }

    private Color plotSingleLine(int j, JSubplot sp, float[] xData, float[] yData, SacTraceData td) {
        Line line = null;
        if (j == 0) {
            line = (Line) sp.Plot(xData, yData);
        } else {
            line = new Line(xData, yData);
            sp.AddPlotObject(line);
        }
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());
        PlotUtils.setYAxisScale(sp);
        lineTraceMap.put(line, td);
        lineSubplotMap.put(line, sp);
        Color color = PlotColors.getInstance().getLineColor();
        line.setColor(color);
        return color;
    }

    @Override
    public void update(Observable o, Object obj) {
        if (obj instanceof MouseOverPlotObject) {
            MouseOverPlotObject mopo = (MouseOverPlotObject) obj;
            PlotObject po = mopo.getPlotObject();
            if (po instanceof VPickLine) {
                VPickLine vpl = (VPickLine) po;
            }
        } else if (obj instanceof PlotObjectClicked && this.getMouseMode() != MouseMode.CREATE_PICK) {
            PlotObjectClicked poc = (PlotObjectClicked) obj;
            if (poc.po instanceof Line) {
            } else if (poc.po instanceof VPickLine) {
                VPickLine vpl = (VPickLine) poc.po;
            }

        } else if (obj instanceof MouseMode) {
            // System.out.println( "owner.setMouseModeMessage((MouseMode) obj);");
        } else if (obj instanceof JPlotKeyMessage) {
            JPlotKeyMessage msg = (JPlotKeyMessage) obj;
            KeyEvent e = msg.getKeyEvent();
            if (e.getKeyChar() == '+') {
                zoomInAroundMouse(msg);
                return;
            }
            //       ControlKeyMapper controlKeyMapper = msg.getControlKeyMapper();
            int keyCode = e.getKeyCode();

            PlotObject po = msg.getPlotObject();
            if (keyCode == 127 && po instanceof Line) {
            }

        } else if (obj instanceof PickMovedState) {
            PickMovedState pms = (PickMovedState) obj;
            double deltaT = pms.getDeltaT();
            VPickLine vpl = pms.getPickLine();
        } else if (obj instanceof PickCreationInfo) {
            PickCreationInfo pci = (PickCreationInfo) obj;
            PlotObject po = pci.getSelectedObject();
            if (po instanceof Line) {
                Line line = (Line) po;
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
        Collection<Line> lines = lineTraceMap.keySet();
        PlotUtils.rescaleLines(lines, factor);
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
    public void exportPlot() {
        exportSVG();
    }

    public void printPlot() {
        print();
    }

    @Override
    public void scaleAllTraces(boolean resetYlimits) {
        double xmin = getXaxis().getMin();
        double xmax = getXaxis().getMax();
        PlotUtils.scaleAllTraces(xmin, xmax, lineSubplotMap);
        repaint();
    }

    public PairT<Double, Double> getTimeMinMax(Collection<SacTraceData> traces) {
        double minVal = Double.MAX_VALUE;
        double maxVal = -minVal;
        for (SacTraceData std : traces) {
            Double b = std.getSACHeader().getB();
            Double e = std.getSACHeader().getE();
            if (b != null && b < minVal) {
                minVal = b;
            }
            if (e != null && e > maxVal) {
                maxVal = e;
            }
        }

        Xlimits limits = PlotPrefs.getInstance().getXlimits();
        if (limits.isLimitsOn()) {
            PartialDataWindow pdw = limits.getPdw();
            String startRef = pdw.getStartReference();
            String endRef = pdw.getEndReference();
            if (startRef == null && endRef == null) {
                minVal = pdw.getStartOffset();
                maxVal = pdw.getEndOffset();
            } else if (startRef != null && endRef != null) {
                Double minStart = null;
                Double maxEnd = null;
                boolean applyMinValue = true;
                boolean applyMaxValue = true;
                for (SacTraceData std : traces) {
                    Object obj = std.getSACHeader().getValue(startRef);
                    if (obj != null) {
                        Double tmp = (Double) obj + pdw.getStartOffset();
                        if (minStart == null || tmp < minStart) {
                            minStart = tmp;
                        }
                    } else {
                        applyMinValue = false;
                    }
                    obj = std.getSACHeader().getValue(endRef);
                    if (obj != null) {
                        Double tmp = (Double) obj + pdw.getEndOffset();
                        if (maxEnd == null || tmp > maxEnd) {
                            maxEnd = tmp;
                        }
                    } else {
                        applyMaxValue = false;
                    }
                }
                if (applyMinValue && minStart != null) {
                    minVal = minStart;
                }
                if (applyMaxValue && maxEnd != null) {
                    maxVal = maxEnd;
                }
            }
        }

        return new PairT<>(minVal, maxVal);
    }
}
