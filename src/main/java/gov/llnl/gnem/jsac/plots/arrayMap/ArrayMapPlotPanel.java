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
package gov.llnl.gnem.jsac.plots.arrayMap;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.SharedPlotPanel;
import java.awt.Color;
import llnl.gnem.core.gui.plotting.HorizAlignment;
import llnl.gnem.core.gui.plotting.VertAlignment;
import llnl.gnem.core.gui.plotting.ZoomLimits;
import llnl.gnem.core.gui.plotting.colormap.DefinedColormap;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JPlotKeyMessage;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.XAxis;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.YAxis;
import llnl.gnem.core.gui.plotting.plotobject.Circle;
import llnl.gnem.core.gui.plotting.plotobject.DataText;
import llnl.gnem.core.gui.plotting.plotobject.Line;
import llnl.gnem.core.gui.plotting.plotobject.Plus;
import llnl.gnem.core.gui.plotting.transforms.Coordinate;

public class ArrayMapPlotPanel extends SharedPlotPanel implements Observer {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ArrayMapPlotPanel.class);

    private static ArrayMapPlotPanel INSTANCE = null;

    public static ArrayMapPlotPanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ArrayMapPlotPanel();
        }
        return INSTANCE;
    }

    private final JSubplot subplot;
    private DefinedColormap colormap;

    private ArrayMapPlotPanel() {
        super(JMultiAxisPlot.XAxisType.Standard);
        subplot = addSubplot();
        addPlotObjectObserver(this);

    }

    @Override
    public void clear() {

        subplot.Clear();
        repaint();
    }

    public void plotCoordinates(double[][] coordinates, boolean isArrayPlot, String[] stations, double[] centroid, String titleText) {
        clear();
        setXaxisType(XAxisType.Standard);

        double minX = Double.MAX_VALUE;
        double maxX = -minX;
        double minY = minX;
        double maxY = maxX;
        for (int j = 0; j < coordinates.length; ++j) {
            double east = coordinates[j][0];
            if (east < minX) {
                minX = east;
            }
            if (east > maxX) {
                maxX = east;
            }

            double north = coordinates[j][1];
            if (north < minY) {
                minY = north;
            }
            if (north > maxY) {
                maxY = north;
            }
            double elev = coordinates[j][2];
            Circle circle = new Circle(east, north, 6);
            Color color = Color.blue;
            if (isArrayPlot) {
                color = colormap != null ? colormap.getColor(elev) : Color.blue;
            }
            circle.setFillColor(color);
            circle.setSelectable(false);
            subplot.AddPlotObject(circle);
            if (isArrayPlot) {
                plotStationText(stations, j, east, north);
            }
        }
        double centroidEast = 0;
        double centroidNorth = 0;
        if (isArrayPlot) {
            plotCentroidSymbol(coordinates, centroidEast, centroidNorth, centroid);
        }
        setAxisLimits(minX, maxX, minY, maxY);
        PlotUtils.setYAxisScale(subplot);

        PlotUtils.maybeSetYlimits(subplot);

        PlotUtils.setYlabelAttributes(subplot);
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        subplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        subplot.getYaxis().setLabelText("North offset (km)");
        getXaxis().setLabelText("East offset (km)");
        PlotUtils.setXaxisColor(this);
        PlotUtils.setSubplotAttributes(subplot);
        String myTitle = isArrayPlot ? titleText: titleText+ " (coarray)";
        getTitle().setText(myTitle);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        repaint();
    }

    public void plotStationText(String[] stations, int j, double east, double north) {
        String stationCode = stations[j];
        DataText text = new DataText(east, north, stationCode);
        text.setxOffsetPixels(20);
        text.setFontSize(15);
        text.setVerticalAlignment(VertAlignment.CENTER);
        subplot.AddPlotObject(text);
    }

    public void setAxisLimits(double minX, double maxX, double minY, double maxY) {
        minX = Math.floor(minX) - 1;
        maxX = Math.ceil(maxX) + 1;
        minY = Math.floor(minY) - 1;
        maxY = Math.ceil(maxY) + 1;
        subplot.getYaxis().setMax(maxY);
        subplot.getYaxis().setMin(minY);
        subplot.getXaxis().setMin(minX);
        subplot.getXaxis().setMax(maxX);
    }

    public void plotCentroidSymbol(double[][] coordinates, double centroidEast, double centroidNorth, double[] centroid) {
        for (int j = 0; j < coordinates.length; ++j) {
            centroidEast += coordinates[j][0];
            centroidNorth += coordinates[j][1];
        }
        centroidEast/=coordinates.length;
        centroidNorth/=coordinates.length;
        Plus centroidCross = new Plus(centroidEast, centroidNorth, 10.0);
        subplot.AddPlotObject(centroidCross);
        String centroidLabel = String.format("Lat = %6.3f, lon = %6.3f", centroid[0], centroid[1]);
        DataText text = new DataText(centroidEast, centroidNorth, centroidLabel);
        text.setHorizontalAlignment(HorizAlignment.CENTER);
        text.setyOffsetPixels(20);
        subplot.AddPlotObject(text);
    }

    @Override
    public void update(Observable o, Object obj) {
    }

    @Override
    public void zoomInAroundMouse(JPlotKeyMessage msg) {
        Coordinate coord = msg.getCurrentCoord();
        JSubplot plot = msg.getSubplot();
        if (plot != null) {
            YAxis yaxis = plot.getYaxis();
            if (yaxis != null) {
                double ymin = yaxis.getMin();
                double ymax = yaxis.getMax();
                double yrange4 = (ymax - ymin) / 4;
                XAxis xaxis = plot.getXaxis();
                if (xaxis != null) {
                    double xmin = xaxis.getMin();
                    double xmax = xaxis.getMax();
                    double xrange4 = (xmax - xmin) / 4;
                    ZoomLimits zl = new ZoomLimits(coord.getWorldC1() - xrange4, coord.getWorldC1() + xrange4, coord.getWorldC2() - yrange4, coord.getWorldC2() + yrange4);

                    this.zoomToNewLimits(zl);
                    this.repaint();
                }
            }
        }
    }

    @Override
    public void magnifyTraces() {
        JMultiAxisPlot.scaleTraces(subplot, 2.0);
        this.repaint();
    }

    @Override
    public void reduceTraces() {
        JMultiAxisPlot.scaleTraces(subplot, 0.5);
        this.repaint();
    }

    @Override
    public void autoScaleTraces() {
        scaleAllTraces(false);
    }

    @Override
    public void scaleAllTraces(boolean resetYlimits) {
        double xmin = getXaxis().getMin();
        double xmax = getXaxis().getMax();

        Line[] lines = subplot.getLines();

        for (Line line : lines) {
            float[] data = line.getYdata();
            if (data != null && data.length > 1) {
                int idx1 = 0;
                int idx2 = data.length - 1;

                double delta = line.getIncrement();
                double lineStartTime = line.getXBegin();
                if (xmin > lineStartTime) {
                    idx1 = (int) (Math.round(xmin - lineStartTime) / delta);
                }
                if (xmax < line.getXEnd()) {
                    idx2 = Math.min((int) (Math.round(xmax - lineStartTime) / delta), data.length - 1);
                }
                if (idx2 > idx1 + 1) {
                    double ymin = Double.MAX_VALUE;
                    double ymax = -ymin;
                    for (int j = idx1; j <= idx2; ++j) {
                        if (data[j] < ymin) {
                            ymin = data[j];
                        }
                        if (data[j] > ymax) {
                            ymax = data[j];
                        }
                    }
                    subplot.setYlimits(ymin, ymax);

                }
            }
        }
        repaint();
    }

    void setColorMap(DefinedColormap colormap) {
        this.colormap = colormap;
    }

}
