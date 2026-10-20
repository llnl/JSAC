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

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.FilterDesignData;
import llnl.gnem.core.gui.util.PersistentPositionContainer;

public class PlotFilterDesignFrame extends PersistentPositionContainer {

    private final SpectrumPanel dsp;
    private final DigitalImpulsePanel dip;

    private PlotFilterDesignFrame(String preferencePath, String title, int width, int height) {
        super(preferencePath, title, width, height);
        JTabbedPane tabbedPane = new JTabbedPane();

        dsp = new SpectrumPanel();
        dip = new DigitalImpulsePanel();
        tabbedPane.addTab("Spectral Data", null, dsp,
                "Displays the specta of the design");
        tabbedPane.addTab("Digital Impulse", null, dip,
                "Displays the impulse response of the filter");
         this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.ICONIFIED);
    }

    @Override
    protected void updateCaption() {
    }

    public static PlotFilterDesignFrame getInstance() {
        return PlotFrameHolder.INSTANCE;
    }

    public void plotDesignData(FilterDesignData fdd) {
        double deltaF = fdd.getDeltaF();
         float[] digitalGroupDelay = fdd.getDigitalGroupDelay();
        double deltaT = fdd.getDeltaT();
        for (int j = 0; j < digitalGroupDelay.length; ++j) {
            digitalGroupDelay[j] *= deltaT;
        }
       
        
        dsp.plot(deltaF, fdd.getAnalogSpectrumRealPart(), 
                fdd.getAnalogSpectrumImagPart(),
                fdd.getDigitalSpectrumRealPart(),
                fdd.getDigitalSpectrumImagPart(),
                fdd.getAnalogGroupDelay(),
                digitalGroupDelay);

        dip.plotImpulseResponse(fdd);

    }

    private static class PlotFrameHolder {

        private static final PlotFilterDesignFrame INSTANCE = new PlotFilterDesignFrame("gov/llnl/gnem/jsac/plotdesign", "plotdesign", 600, 600);
    }
}
