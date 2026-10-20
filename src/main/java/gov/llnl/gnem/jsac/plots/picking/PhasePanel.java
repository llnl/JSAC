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
package gov.llnl.gnem.jsac.plots.picking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.plots.picking.KeyPhaseMapper.PhaseUsage;
import llnl.gnem.core.util.SpringUtilities;
import llnl.gnem.core.traveltime.Ak135.TraveltimeCalculatorProducer;

public class PhasePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(PhasePanel.class);

    private final JPopupMenu menu;
    private final PhaseSelectionAdapter phaseSelectListener;
    private PhaseInfo selectedPhaseInfo;

    private final Map<PhaseUsage, PhaseInfo> usageInfoMap;

    public PhasePanel() {
        super(new BorderLayout());
        JLabel instructionLabel = new JLabel("<html> <FONT COLOR=BLUE>\n"
                + "Keys 1-9 and 0 may be bound to any AK135 phase by right-clicking and choosing "
                + "from the popup menu. Key (i) is mapped to T(i-1) in SAC header.</html>");
        add(instructionLabel, BorderLayout.NORTH);
        phaseSelectListener = new PhaseSelectionAdapter();

        usageInfoMap = new HashMap<>();
        JPanel controlPanel = buildControlPanel();
        add(controlPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder("Key-Phase Maps"));

        menu = new JPopupMenu();
        buildPhasePopupMenu();

    }

    private void buildPhasePopupMenu() {
        try {
            JMenu bigPMenu = new JMenu("P*");
            JMenu bigSMenu = new JMenu("S*");
            JMenu otherMenu = new JMenu("Other phases");
            menu.add(bigPMenu);
            menu.add(bigSMenu);
            menu.add(otherMenu);
            Collection<String> phases = TraveltimeCalculatorProducer.getInstance().getAllowablePhases();
            phases.forEach(phase -> {
                if (phase.startsWith("P")) {
                    JMenuItem item = new JMenuItem(phase);
                    item.addActionListener(this);
                    bigPMenu.add(item);
                } else if (phase.startsWith("S")) {
                    JMenuItem item = new JMenuItem(phase);
                    item.addActionListener(this);
                    bigSMenu.add(item);
                } else {
                    JMenuItem item = new JMenuItem(phase);
                    item.addActionListener(this);
                    otherMenu.add(item);
                }
            });
        } catch (IOException | ClassNotFoundException ex) {
            log.error("Failed retrieving phases from traveltime model.");
        }
    }

    private JPanel buildControlPanel() {
        JPanel result = new JPanel(); // Using Flow Layout by default...
        int radix = 10;
        KeyPhaseMapper mapper = KeyPhaseMapper.getInstance();
        for (int j = 1; j < 10; ++j) {
            char vChar = Character.forDigit(j, radix);
            PhaseUsage phaseUsage = mapper.getMappedPhase(vChar);
            if (phaseUsage == null) {
                phaseUsage = new PhaseUsage("P", true);
            }
            PhaseInfo info = new PhaseInfo(vChar, phaseUsage);

            info.setBackground(Color.white);
            info.setOpaque(true);

            info.addMouseListener(phaseSelectListener);
            result.add(info);
            usageInfoMap.put(phaseUsage, info);
        }
        char vChar = Character.forDigit(0, radix);
        PhaseUsage phaseUsage = mapper.getMappedPhase(vChar);
        if (phaseUsage == null) {
            phaseUsage = new PhaseUsage("P", false);
        }
        PhaseInfo info = new PhaseInfo(vChar, phaseUsage);
        info.setBackground(Color.white);
        info.setOpaque(true);
        info.addMouseListener(phaseSelectListener);
        result.add(info);
        usageInfoMap.put(phaseUsage, info);
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String phase = e.getActionCommand();
        if (selectedPhaseInfo != null) {
            selectedPhaseInfo.setPhase(phase);
        }

    }

    public PhaseInfo getSelectedPhaseInfo() {
        return selectedPhaseInfo;
    }

    public static class PhaseInfo extends JPanel {

        private static final long serialVersionUID = 1L;
        private final char key;
        private final PhaseUsage phaseUsage;

        private final JLabel phaseLabel;
        private final char base = '1';

        private PhaseInfo(char key, PhaseUsage phaseUsage) {
            super(new SpringLayout());
            this.key = key;
            this.phaseUsage = phaseUsage;
            phaseLabel = new JLabel("phase (" + phaseUsage.getPhase() + ")");
            add(phaseLabel);
            JLabel label = new JLabel("" + key + " ==> T" + getTIndex());
            add(label);

            SpringUtilities.makeCompactGrid(
                    this,
                        2,
                        1,
                        1,
                        1, //initX, initY
                        1,
                        1);
            setBorder(BorderFactory.createRaisedBevelBorder());
        }

        public final int getTIndex() {
            int tmp = key - base;
            return tmp < 0 ? tmp + 10 : tmp;
        }

        private void setPhase(String phase) {
            phaseUsage.setPhase(phase);
            phaseLabel.setText("phase (" + phaseUsage.getPhase() + ")");
            revalidate();
            KeyPhaseMapper.getInstance().saveState();
        }

    }

    public void setSelectedPhaseUsage(PhaseUsage usage) {
        PhaseInfo pi = usageInfoMap.get(usage);
        if (pi != null) {
            pi.setBorder(BorderFactory.createLoweredBevelBorder());
            pi.revalidate();
            selectedPhaseInfo = pi;
        }
    }

    void unsetSelectedPhaseUsage() {
        if (selectedPhaseInfo != null) {
            selectedPhaseInfo.setBorder(BorderFactory.createRaisedBevelBorder());
            selectedPhaseInfo.revalidate();
            selectedPhaseInfo = null;
        }
    }

    private class PhaseSelectionAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            Component comp = e.getComponent();
            if (comp instanceof PhaseInfo) {
                PhaseInfo pi = (PhaseInfo) comp;

                if (e.getButton() == 3) {
                    selectedPhaseInfo = pi;
                    menu.show(e.getComponent(), e.getX(), e.getY());

                }

            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Component comp = e.getComponent();
            if (comp instanceof PhaseInfo) {
                PhaseInfo pi = (PhaseInfo) comp;

                pi.setBorder(BorderFactory.createLoweredBevelBorder());
                pi.revalidate();

            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component comp = e.getComponent();
            if (comp instanceof PhaseInfo) {
                PhaseInfo pi = (PhaseInfo) comp;

                pi.setBorder(BorderFactory.createRaisedBevelBorder());
                pi.revalidate();

            }
        }
    }
}
