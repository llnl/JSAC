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

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import gov.llnl.gnem.jsac.plots.actions.AutoScaleAction;
import gov.llnl.gnem.jsac.plots.actions.CloseAction;
import gov.llnl.gnem.jsac.plots.actions.ExportAction;
import gov.llnl.gnem.jsac.plots.actions.MagnifyAction;
import gov.llnl.gnem.jsac.plots.actions.OpenPickPrefsAction;
import gov.llnl.gnem.jsac.plots.actions.PrintAction;
import gov.llnl.gnem.jsac.plots.actions.ReduceAction;
import gov.llnl.gnem.jsac.plots.actions.UnzoomAllAction;


public class PlotToolbar extends JToolBar {

    public PlotToolbar(JFrame frame, SharedPlotPanel viewer, boolean isTimeDomain) {
        CloseAction ca = new CloseAction(this);
        ca.setTarget(frame);
        JButton button = new JButton(ca);
        addButton(button);
        addSeparator();

        ExportAction ea = new ExportAction(this);
        ea.setPlot(viewer);
        button = new JButton(ea);
        addButton(button);

        PrintAction pa = new PrintAction(this);
        pa.setPlot(viewer);
        button = new JButton(pa);
        addButton(button);
        addSeparator();
        if (isTimeDomain) {
            MagnifyAction ma = new MagnifyAction(this);
            ma.setPlot(viewer);
            button = new JButton(ma);
            addButton(button);

            ReduceAction ra = new ReduceAction(this);
            ra.setPlot(viewer);
            button = new JButton(ra);
            addButton(button);
        }
        AutoScaleAction aa = new AutoScaleAction(this);
        aa.setPlot(viewer);
        button = new JButton(aa);
        addButton(button);

        UnzoomAllAction ua = new UnzoomAllAction(this);
        ua.setPlot(viewer);
        button = new JButton(ua);
        addButton(button);

        ZoomTypeState state = new ZoomTypeState(this, viewer);
        button = new JButton(state);
        Object obj = state.getValue("LARGE_ICON_KEY");
        if (obj != null && obj instanceof ImageIcon) {
            ImageIcon io = (ImageIcon) obj;
            button.setIcon(io);
        }
        addButton(button);
        addSeparator();

        if (isTimeDomain) {
            OpenPickPrefsAction oppa = new OpenPickPrefsAction(this);
            oppa.setTarget(frame);
            button = new JButton(oppa);
            addButton(button);
        }
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(500, 10));
        add(spacer);
    }

    private void addButton(JButton button) {
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        button.setPreferredSize(new Dimension(38, 36));
        button.setMaximumSize(new Dimension(38, 36));
        add(button);
    }
}
