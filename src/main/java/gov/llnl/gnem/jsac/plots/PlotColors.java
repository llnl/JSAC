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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import llnl.gnem.core.gui.plotting.plotPrefs.PlotPreferenceListener;
import llnl.gnem.core.gui.plotting.plotPrefs.PlotPreferenceModel;
import llnl.gnem.core.gui.plotting.plotPrefs.PlotPresentationPrefs;

public class PlotColors implements PlotPreferenceListener {

    private Color backGroundColor;
    private Color skeletonColor;
    private Color lineColor;
    private boolean incrementColors;
    private int listIndex = 0;
    private final List<Color> colorList;
    private final PlotPresentationPrefs prefs;
    private static final Color[] defaultList = {Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.PINK};
    private static final Map<String, Color> nameToColorMap = new HashMap<>();

    static {
        nameToColorMap.put("WHITE", Color.WHITE);
        nameToColorMap.put("RED", Color.RED);
        nameToColorMap.put("ORANGE", Color.ORANGE);
        nameToColorMap.put("PINK", Color.PINK);
        nameToColorMap.put("GREEN", Color.GREEN);
        nameToColorMap.put("YELLOW", Color.YELLOW);
        nameToColorMap.put("BLUE", Color.BLUE);
        nameToColorMap.put("MAGENTA", Color.MAGENTA);
        nameToColorMap.put("CYAN", Color.CYAN);
        nameToColorMap.put("BLACK", Color.BLACK);
        nameToColorMap.put("DARK_GRAY", Color.DARK_GRAY);
        nameToColorMap.put("GRAY", Color.GRAY);
        nameToColorMap.put("LIGHT_GRAY", Color.LIGHT_GRAY);
        nameToColorMap.put("PINK", Color.PINK);
    }

    public static Color getColorByName(String name) {
        Color result = nameToColorMap.get(name.toUpperCase());
        return result != null ? result : Color.BLACK;
    }

    private PlotColors() {
        backGroundColor = Color.WHITE;
        skeletonColor = Color.BLACK;
        lineColor = Color.BLACK;
        incrementColors = false;
        colorList = new ArrayList<>();
        resetColorList();
        prefs = PlotPreferenceModel.getInstance().getPrefs();
        PlotPreferenceModel.getInstance().addListener(this);
        updateFromPlotPreferenceModel();
    }

    private void resetColorList() {
        colorList.clear();
        colorList.addAll(Arrays.asList(defaultList));
        listIndex = 0;
    }

    public void setListToStandard() {
        resetColorList();
    }

    public void setColorList(List<String> colors) {
        colorList.clear();
        for (String color : colors) {
            Color c = nameToColorMap.get(color.toUpperCase());
            if (c != null) {
                colorList.add(c);
            }
        }
        listIndex = 0;
        if (colorList.isEmpty()) {// Something went wrong just use the standard list.
            resetColorList();
        }
    }

    public Color getBackGroundColor() {
        return backGroundColor;
    }

    public void setBackGroundColor(String colorString) {
        Color color = nameToColorMap.get(colorString.toUpperCase());
        if (color != null) {
            backGroundColor = color;
            prefs.getPlotRegionPrefs().setBackgroundColor(backGroundColor);
        }
    }

    public Color getSkeletonColor() {
        return skeletonColor;
    }

    public void setSkeletonColor(String colorString) {
        Color color = nameToColorMap.get(colorString.toUpperCase());
        if (color != null) {
            skeletonColor = color;
        }
    }

    public Color getLineColor() {
        if (incrementColors && !colorList.isEmpty()) {
            Color result = colorList.get(listIndex);
            ++listIndex;
            if (listIndex == colorList.size()) {
                listIndex = 0;
            }
            return result;
        } else {
            return lineColor;
        }
    }

    public void setLineColor(String colorString) {
        Color color = nameToColorMap.get(colorString.toUpperCase());
        if (color != null) {
            lineColor = color;
            prefs.setTraceColor(lineColor);
        }
    }

    public static PlotColors getInstance() {
        return PlotColorsHolder.INSTANCE;
    }

    public void setIncrementColors(boolean value) {
        incrementColors = value;
    }

    @Override
    public void preferencesChanged() {
        updateFromPlotPreferenceModel();
    }

    private void updateFromPlotPreferenceModel() {
        backGroundColor = prefs.getPlotRegionPrefs().getBackgroundColor();
        lineColor = prefs.getTraceColor();
    }

    private static class PlotColorsHolder {

        private static final PlotColors INSTANCE = new PlotColors();
    }
}
