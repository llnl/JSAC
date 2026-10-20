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

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import llnl.gnem.core.gui.plotting.ZoomType;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.util.Utility;
import llnl.gnem.core.util.ButtonAction;

/**
 *
 * @author dodge1
 */
public class ZoomTypeState extends ButtonAction {

    private static final long serialVersionUID = -530506762945701646L;

    private final Preferences prefs;
    private String description;
    private final JMultiAxisPlot viewer;
    private ZoomType zoomType;

    public ZoomTypeState(JToolBar owner, JMultiAxisPlot viewer) {
        super("Set Zoom-Type", Utility.getIcon(owner, "miscIcons/boxzoom32.gif"));
        this.viewer = viewer;
        prefs = Preferences.userNodeForPackage(this.getClass());

        String type = prefs.get("ZOOM_TYPE", "Zoom_Box");
        ImageIcon icon;
        if (type.equals(ZoomType.ZOOM_BOX.toString())) {
            setZoomType(ZoomType.ZOOM_BOX);
            description = "Click to change to Zoom-All";
            icon = Utility.getIcon(owner, "miscIcons/boxzoom32.gif");
        } else {
            setZoomType(ZoomType.ZOOM_ALL);
            description = "Click to change to Box-Zoom";
            icon = Utility.getIcon(owner, "miscIcons/zoomall32.gif");
        }
        putValue(SHORT_DESCRIPTION, description);

        putValue("SMALL_ICON", icon);
        putValue("LARGE_ICON_KEY", icon);

        updateZoomTypes();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            String type = prefs.get("ZOOM_TYPE", "Zoom_Box");
            ImageIcon icon;
            if (type.equals(ZoomType.ZOOM_BOX.toString())) {
                setZoomType(ZoomType.ZOOM_ALL);
                description = "Click to change to Box-Zoom";
                icon = Utility.getIcon(this, "miscIcons/zoomall32.gif");
            } else {
                setZoomType(ZoomType.ZOOM_BOX);
                description = "Click to change to Zoom-All";
                icon = Utility.getIcon(this, "miscIcons/boxzoom32.gif");
            }
            putValue(SHORT_DESCRIPTION, description);
            putValue("SMALL_ICON", icon);
            button.setIcon(icon);
            button.setToolTipText(description);
            prefs.put("ZOOM_TYPE", zoomType.toString());
            updateZoomTypes();
        }
    }

    private void setZoomType(ZoomType zoomType) {
        this.zoomType = zoomType;
    }

    public void updateZoomTypes() {
        viewer.setZoomType(zoomType);
    }
}
