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
package gov.llnl.gnem.jsac.plots.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import gov.llnl.gnem.jsac.plots.SharedPlotPanel;
import llnl.gnem.core.gui.util.Utility;

/**
 *
 * @author dodge1
 */
public class ReduceAction extends AbstractAction {


    private SharedPlotPanel plot;

 

    public ReduceAction(Object owner) {
        super("Reduce", Utility.getIcon(owner, "miscIcons/pagedown32.gif"));
        putValue(SHORT_DESCRIPTION, "Decrease Magnification");
        putValue(MNEMONIC_KEY, KeyEvent.VK_R);
    }

    public void setPlot(SharedPlotPanel plot) {
        this.plot = plot;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (plot != null) {
            plot.reduceTraces();
        }
    }

}
