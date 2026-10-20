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
package gov.llnl.gnem.jsac.plots.plotFilterDesign;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.FilterDesignData;
import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.plotobject.Line;

/**
 *
 * @author dodge1
 */
public class DigitalImpulsePanel extends JMultiAxisPlot {

    void plotImpulseResponse(FilterDesignData fdd) {
        clear();
        double deltaT = fdd.getDeltaT();
        float[] data = fdd.getDigitalImpulseResponse();
        JSubplot subplot = addSubplot();
        Line line = (Line) subplot.Plot(0, deltaT, data);
        line.setPenStyle(PlotDrawingData.getInstance().getPenStyle());

        PlotUtils.setYAxisScale(subplot);
        line.setColor(PlotColors.getInstance().getLineColor());
        subplot.getYaxis().setLabelText("Amplitude");
        subplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        PlotUtils.maybeSetYlimits(subplot);
        PlotUtils.setYlabelAttributes(subplot);
        PlotUtils.setSubplotAttributes(subplot);
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        PlotUtils.setXaxisColor(this);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        getXaxis().setLabelText("Time (s)");

        repaint();
    }

}
