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
package gov.llnl.gnem.jsac.plots.plotDFIRDesign;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import llnl.gnem.core.gui.util.PersistentPositionContainer;

public class PlotDFIRDesignFrame extends PersistentPositionContainer {

    private final DFIRDesignPanel ddp;

    private PlotDFIRDesignFrame(String preferencePath, String title, int width, int height) {
        super(preferencePath, title, width, height);
 
        ddp = new DFIRDesignPanel();
        this.getContentPane().add(ddp, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.ICONIFIED);
    }

    @Override
    protected void updateCaption() {
    }

    public static PlotDFIRDesignFrame getInstance() {
        return PlotFrameHolder.INSTANCE;
    }

    public void plotDesignData(float[] response, double df, String titleText) {
        ddp.plotDesignData(response, df, titleText);
    }

    private static class PlotFrameHolder {

        private static final PlotDFIRDesignFrame INSTANCE = new PlotDFIRDesignFrame("gov/llnl/gnem/jsac/plotdesign", "plotdesign", 600, 600);
    }
}
