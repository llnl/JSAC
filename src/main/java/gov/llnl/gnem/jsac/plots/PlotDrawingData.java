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

import java.util.ArrayList;
import java.util.List;

import llnl.gnem.core.gui.plotting.PenStyle;
import llnl.gnem.core.gui.plotting.plotPrefs.PlotPreferenceListener;
import llnl.gnem.core.gui.plotting.plotPrefs.PlotPreferenceModel;
import llnl.gnem.core.gui.plotting.plotPrefs.PlotPresentationPrefs;

/**
 *
 * @author dodge1
 */
public class PlotDrawingData  implements PlotPreferenceListener{

    private boolean applyFloor;
    private double floorValue;
    private boolean drawLines = true;
    private PenStyle penStyle = PenStyle.SOLID;

    private final PenStyle[] allPenStyles = PenStyle.getAllStyles();
    private int currentPenStyleIndex = 0;
    private boolean incrementPenStyles = false;
    private final List<Integer> incrementList;
    private final PlotPresentationPrefs prefs;

    private PlotDrawingData() {

        incrementList = new ArrayList<>();
        for (int j = 1; j < allPenStyles.length; ++j) {
            incrementList.add(allPenStyles[j].getIndex());
        }
        prefs = PlotPreferenceModel.getInstance().getPrefs();
        PlotPreferenceModel.getInstance().addListener(this);
        updateFromPlotPreferenceModel();
        
    }

    public static PlotDrawingData getInstance() {
        return PlotCharacteristicsHolder.INSTANCE;
    }

    public void setApplyFloor(boolean applyFloor) {
        this.applyFloor = applyFloor;
    }

    public void setFloorValue(double value) {
        this.floorValue = Math.abs(value);
    }

    public void setLineIncrementList(String standard) {
        currentPenStyleIndex = 0;
        incrementList.clear();
        for (int j = 1; j < allPenStyles.length; ++j) {
            incrementList.add(allPenStyles[j].getIndex());
        }

    }

    public void setLineIncrementList(List<Integer> incList) {
        if (incList.isEmpty()) {
            return;
        }
        currentPenStyleIndex = 0;
        incrementList.clear();
        for (Integer idx : incList) {
            if (idx > 0 && idx <= PenStyle.getMaxIndex()) {
                incrementList.add(idx);
            }
        }
    }

    public void setPenStyle(int v) {
        if (v >= 0 && v <= PenStyle.getMaxIndex()) {
            currentPenStyleIndex = 0;
            incrementPenStyles = false;
            penStyle = PenStyle.getPenStyleByIndex(v);
        }
    }

    @Override
    public void preferencesChanged() {
        updateFromPlotPreferenceModel();
    }

    private void updateFromPlotPreferenceModel() {
        penStyle= prefs.getTracePenStyle();
    }

    private static class PlotCharacteristicsHolder {

        private static final PlotDrawingData INSTANCE = new PlotDrawingData();
    }

    public boolean isApplyFloor() {
        return applyFloor;
    }

    public double getFloorValue() {
        return floorValue;
    }

    public boolean isDrawLines() {
        return drawLines;
    }

    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
    }

    public PenStyle getPenStyle() {
        if (this.incrementPenStyles) {
            int idx = incrementList.get(currentPenStyleIndex);
            PenStyle p = PenStyle.getPenStyleByIndex(idx);
            currentPenStyleIndex++;
            if (currentPenStyleIndex == incrementList.size()) {
                currentPenStyleIndex = 0;
            }
            return drawLines ? p : PenStyle.NONE;
        }
        return drawLines ? penStyle : PenStyle.NONE;
    }

    public void setPenStyle(PenStyle penStyle) {
        this.penStyle = penStyle;
        currentPenStyleIndex = 0;
        incrementPenStyles = false;
    }

    public void setIncrementPenStyles(boolean incrementPenStyles) {
        this.incrementPenStyles = incrementPenStyles;
    }

}
