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
package gov.llnl.gnem.jsac.plots.fk;

import java.awt.Color;
import llnl.gnem.core.gui.plotting.fkPlot.FKPlotProperties;
import llnl.gnem.core.gui.plotting.fkPlot.PlotOptions;


public class FKPlotProps implements FKPlotProperties
{
    private Color lineColor;
    private int lineThickness;
    private Color textColor;
    private Color textBackgroundColor;
    private PlotOptions plotOptions;
    private String plotTitle;
    
    private FKPlotProps() {
        lineColor = Color.blue;
        lineThickness = 1;
        textColor = Color.BLACK;
        textBackgroundColor = Color.WHITE;
        plotOptions = PlotOptions.CONTOURS_AND_HEAT_MAP;
        plotTitle =null;
    }
    
    public static FKPlotProps getInstance() {
        return FKPlotPropsHolder.INSTANCE;
    }

    @Override
    public Color getLineColor() {
        return lineColor;
     }

    @Override
    public int getLineThickness() {
        return lineThickness;
    }

    @Override
    public Color getTextColor() {
        return textColor;
    }

    @Override
    public Color getTextBackgroundColor() {
        return textBackgroundColor;
    }

    @Override
    public PlotOptions getPlotOptions() {
        return plotOptions;
    }

    @Override
    public boolean isShowHeatMap() {
        return plotOptions.isShowHeatMap();
     }

    @Override
    public boolean isShowContours() {
        return plotOptions.isShowContours();
     }

    @Override
    public String getPlotTitle() {
        return plotTitle;
    }

    @Override
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    @Override
    public void setLineThickness(int lineThickness) {
        this.lineThickness = lineThickness;
    }

    @Override
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    @Override
    public void setPlotTitle(String plotTitle) {
        this.plotTitle = plotTitle;
    }

    @Override
    public void setTextBackgroundColor(Color textBackgroundColor) {
        this.textBackgroundColor = textBackgroundColor;
    }

    @Override
    public void setPlotOptions(PlotOptions plotOptions) {
        this.plotOptions = plotOptions;
    }
    
    private static class FKPlotPropsHolder {

        private static final FKPlotProps INSTANCE = new FKPlotProps();
    }
}
