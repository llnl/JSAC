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

import java.awt.BorderLayout;

import javax.swing.JFrame;

import gov.llnl.gnem.jsac.plots.PlotPanelTab;
import llnl.gnem.core.gui.util.PersistentPositionContainer;

/**
 *
 * @author dodge1
 */
public class PlotSPFrame extends PersistentPositionContainer{

    private final PlotPanelTab plotPanelTab;
     private PlotSPFrame( String preferencePath, String title, int width, int height )
    {
        super(preferencePath, title, width,height );

        plotPanelTab = new PlotPanelTab(this,PlotSPPanel.getInstance(),false);
        this.getContentPane().add(plotPanelTab, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.ICONIFIED);
    }



    @Override
    protected void updateCaption()
    {
    }    
    public static PlotSPFrame getInstance() {
        return PlotFrameHolder.INSTANCE;
    }

    public void exportPlot() {
        plotPanelTab.exportPlot();
    }

    public void printPlot() {
        plotPanelTab.printPlot();
    }

    public void magnifyTraces() {
        plotPanelTab.magnifyTraces();
    }

    public void reduceTraces() {
        plotPanelTab.reduceTraces();
    }

    public void autoScaleTraces() {
        plotPanelTab.autoScaleTraces();
    }

    public void unzoomAll() {
        plotPanelTab.unzoomAll();
    }
    
    private static class PlotFrameHolder {

        private static final PlotSPFrame INSTANCE = new PlotSPFrame( "gov/llnl/gnem/jsac/plotsp", "plotsp", 600, 600);
    }
}
