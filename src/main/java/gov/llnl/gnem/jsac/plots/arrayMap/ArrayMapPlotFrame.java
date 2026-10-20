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

import java.awt.BorderLayout;

import javax.swing.JFrame;

import gov.llnl.gnem.jsac.plots.PlotPanelTab;
import java.awt.Color;
import llnl.gnem.core.gui.plotting.colormap.ColorBar;
import llnl.gnem.core.gui.plotting.colormap.DefinedColormap;
import llnl.gnem.core.gui.util.PersistentPositionContainer;

public class ArrayMapPlotFrame extends PersistentPositionContainer {
    
    private final ArrayMapPlotPanel plotPanelTab;
    private final DefinedColormap colormap;
    private final ColorBar colorbar;
    
    private ArrayMapPlotFrame(String preferencePath, String title, int width, int height) {
        super(preferencePath, title, width, height);
        plotPanelTab = ArrayMapPlotPanel.getInstance();
        getContentPane().add(plotPanelTab, BorderLayout.CENTER);
        
        colormap = new DefinedColormap();
        colormap.setDefaultColor(Color.black);
        colormap.installJetMap();
        colormap.setMinMax(-0.2, 0.2);
        colorbar = new ColorBar(colormap);
        colorbar.setLabel("Vertical offset (km)");
        
        plotPanelTab.setColorMap(colormap);
        getContentPane().add(colorbar, BorderLayout.EAST);
        setDefaultCloseOperation(JFrame.ICONIFIED);
    }
    
    public void setIsArrayPlot(boolean value) {
        colorbar.setVisible(value);
    }
    
    @Override
    protected void updateCaption() {
    }
    
    public static ArrayMapPlotFrame getInstance() {
        return PlotFrameHolder.INSTANCE;
    }
    
    private static class PlotFrameHolder {
        
        private static final ArrayMapPlotFrame INSTANCE = new ArrayMapPlotFrame("gov/llnl/gnem/jsac/plot/array", "Array Map", 600, 600);
    }
}
