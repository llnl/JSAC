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
package gov.llnl.gnem.jsac.plots.fk;

import gov.llnl.gnem.jsac.plots.PlotPrefs;
import java.awt.BorderLayout;

import javax.swing.JFrame;

import llnl.gnem.core.gui.plotting.fkPlot.FKPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.util.PersistentPositionContainer;
import llnl.gnem.core.signalprocessing.arrayProcessing.FKResult;

/**
 *
 * @author dodge1
 */
public class FKPlotFrame extends PersistentPositionContainer {

    private final FKPlot viewer;

    private FKPlotFrame(String preferencePath, String title, int width, int height) {
        super(preferencePath, title, width, height);
        viewer = new FKPlot();
        getContentPane().add(viewer, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.ICONIFIED);
    }

    @Override
    protected void updateCaption() {
    }

    public static FKPlotFrame getInstance() {
        return PlotFrameHolder.INSTANCE;
    }

    public void plotFKResults(FKResult result) {
        viewer.plotFK(result, FKPlotProps.getInstance());
        viewer.getTitle().setFontSize(PlotPrefs.getInstance().getTitleFontSize());
        JSubplot subplot = viewer.getSubplot();
        subplot.getYaxis().setLabelFontSize(PlotPrefs.getInstance().getYlabelFontSize());
        subplot.getYaxis().setLabelOffset(PlotPrefs.getInstance().getyLabelOffsetMM());
        subplot.getYaxis().setTickFontSize(PlotPrefs.getInstance().getYlabelTickFontSize());
        viewer.getXaxis().setLabelFontSize(PlotPrefs.getInstance().getXlabelFontSize());
        viewer.getXaxis().setLabelOffset(PlotPrefs.getInstance().getxLabelOffsetMM());
        viewer.getXaxis().setTickFontSize(PlotPrefs.getInstance().getXlabelTickFontSize());
    }

    private static class PlotFrameHolder {

        private static final FKPlotFrame INSTANCE = new FKPlotFrame("gov/llnl/gnem/jsac/plot/fk", "FK", 600, 600);
    }
}
