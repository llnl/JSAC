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
package gov.llnl.gnem.jsac.plots;

import java.awt.Color;

import llnl.gnem.core.gui.plotting.plotPrefs.PlotPreferenceModel;

public class PlotBorderData {
    private boolean drawPlotBorder;
    private boolean drawSubplotBorders;
    private Color borderColor;
    private double subplotSpacing;
    private double borderWidthMM;

    private PlotBorderData() {
        drawPlotBorder = true;
        drawSubplotBorders = true;
        borderColor = Color.WHITE;
        subplotSpacing = 0;
        borderWidthMM = 20;
    }

    public boolean isDrawSubplotBorders() {
        return drawSubplotBorders;
    }

    public static PlotBorderData getInstance() {
        return PlotBorderDataHolder.INSTANCE;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        PlotPreferenceModel.getInstance().getPrefs().getBorderPrefs().setBackgroundColor(borderColor);
    }

    public boolean isDrawPlotBorder() {
        return drawPlotBorder;
    }

    public void setDrawPlotBorder(boolean drawPlotBorder) {
        this.drawPlotBorder = drawPlotBorder;
    }

    public double getBorderWidthMM() {
        return borderWidthMM;
    }

    public double getSubplotSpacing() {
        return subplotSpacing;
    }

    public void setDrawSubplotBorders(boolean drawSubplotBorders) {
        this.drawSubplotBorders = drawSubplotBorders;
    }

    public void setSubplotSpacing(double subplotSpacing) {
        this.subplotSpacing = subplotSpacing;
        PlotPrefs.getInstance().setSubplotSpacing(subplotSpacing);
    }

    public void setBorderWidthMM(double borderWidthMM) {
        this.borderWidthMM = borderWidthMM;
    }

    private static class PlotBorderDataHolder {

        private static final PlotBorderData INSTANCE = new PlotBorderData();
    }
}
