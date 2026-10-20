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
package gov.llnl.gnem.jsac.commands.graphicsAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.Scale;
import gov.llnl.gnem.jsac.plots.plotsp.PlotSPFrame;
import gov.llnl.gnem.jsac.plots.plotsp.PlotSPPanel;
import llnl.gnem.core.gui.plotting.PlotPrinter;
import llnl.gnem.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class PlotSPSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(PlotSPSacCommand.ComponentType.class);

    public static enum ComponentType {
        ASIS, RLIM, AMPH, RL, IM, AM, PH
    };

    private static Scale xscale = Scale.LOG;
    private static Scale yscale = Scale.LOG;
    private static ComponentType componentType = ComponentType.ASIS;

    private final Scanner sc = new Scanner(System.in);

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private final PlotPrintOptions plotPrintOptions;

    static {
        descriptors.add(new AttributeDescriptor("ASIS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("RLIM", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("AMPH", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("RL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("IM", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("AM", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("PH", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LINLIN", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LINLOG", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LOGLIN", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LOGLOG", ValuePossibilities.NO_VALUE, null));
        descriptors.addAll(PlotPrintOptions.getPrintDescriptors());
    }

    public PlotSPSacCommand() {
        plotPrintOptions = new PlotPrintOptions();
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> objects = parsedTokens.remove("LINLIN");
        if (objects != null) {
            xscale = Scale.LINEAR;
            yscale = Scale.LINEAR;
        }
        objects = parsedTokens.remove("LINLOG");
        if (objects != null) {
            xscale = Scale.LINEAR;
            yscale = Scale.LOG;
        }
        objects = parsedTokens.remove("LOGLIN");
        if (objects != null) {
            xscale = Scale.LOG;
            yscale = Scale.LINEAR;
        }
        objects = parsedTokens.remove("LOGLOG");
        if (objects != null) {
            xscale = Scale.LOG;
            yscale = Scale.LOG;
        }
        objects = parsedTokens.remove("ASIS");
        if (objects != null) {
            componentType = ComponentType.ASIS;
        }
        objects = parsedTokens.remove("RLIM");
        if (objects != null) {
            componentType = ComponentType.RLIM;
        }
        objects = parsedTokens.remove("AMPH");
        if (objects != null) {
            componentType = ComponentType.AMPH;
        }
        objects = parsedTokens.remove("RL");
        if (objects != null) {
            componentType = ComponentType.RL;
        }
        objects = parsedTokens.remove("IM");
        if (objects != null) {
            componentType = ComponentType.IM;
        }
        objects = parsedTokens.remove("AM");
        if (objects != null) {
            componentType = ComponentType.AM;
        }
        objects = parsedTokens.remove("PH");
        if (objects != null) {
            componentType = ComponentType.PH;
        }
        plotPrintOptions.initialize(parsedTokens);
    }

    private void displayPrompt() {
        System.out.print("PLOT (Q to quit plotting)>");
        System.out.flush();
    }

    @Override
    public void execute() {
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        if (traces.size() < 1) {
            log.warn("No data files to plot!");
            return;
        }
        if (plotPrintOptions.isJustListPrinters()) {
            PlotPrinter.getInstance().listPrinters();
            return;
        }
        displayPrompt();

        // Initialization for global ylimits if so requested.
        PairT<Double, Double> dataMinMax = PlotUtils.getGlobalLimits(traces);
        PlotPrefs.getInstance().getYlimits().setDataRange(dataMinMax);
        PlotPrefs.getInstance().getYlimits().resetIndex(); //If multiple limits have been specified this sets the pointer at 1st limit.

        int traceCount = SacDataModel.getInstance().getTraceCount();
        boolean usePlotNumber = traces.size() > 1;
        for (int j = 0; j < traces.size(); ++j) {
            SacTraceData sacData = traces.get(j);
            SpectralData dft = sacData.getSpectralData();
            if (dft == null) {
                continue;
            }
            SwingUtilities.invokeLater(() -> {
                PlotSPFrame.getInstance().setVisible(true);
            });
            ComponentType requested = getAppliedComponentType(componentType, dft.getPresentationFormat());
            final int m = j;
            SwingUtilities.invokeLater(() -> {
                PlotSPPanel.getInstance().plotDFT(sacData, m, xscale, yscale, requested);
                plotPrintOptions.maybePrintPlot(PlotSPPanel.getInstance(), m, usePlotNumber);
            });

            if (j >= traceCount - 1) {
                System.out.println("");
                return;
            }
            if (plotPrintOptions.isPauseAfterPlotting()) {
                String text = sc.nextLine().trim().toUpperCase();
                if (text.equals("Q") || text.equals("QUIT")) {
                    System.out.println("");
                    return;
                }
            }
            displayPrompt();

        }

    }

    private ComponentType getAppliedComponentType(ComponentType ctIn, PresentationFormat pf) {
        if (ctIn == ComponentType.ASIS) {
            return pf == PresentationFormat.AmplitudePhase ? ComponentType.AMPH : ComponentType.RLIM;
        } else {
            return ctIn;
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"PSP", "PLOTSP"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
