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
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.plot.PlotFrame;
import gov.llnl.gnem.jsac.plots.plot.PlotPanel;
import llnl.gnem.core.gui.plotting.PlotPrinter;
import llnl.gnem.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class PlotSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(PlotSacCommand.class);

    private final Scanner sc = new Scanner(System.in);
    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private final PlotPrintOptions plotPrintOptions;

    public PlotSacCommand() {
        plotPrintOptions = new PlotPrintOptions();
    }

    static {
        descriptors.addAll(PlotPrintOptions.getPrintDescriptors());
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        plotPrintOptions.initialize(parsedTokens);
    }

    private void displayPrompt() {
        System.out.print("PLOT (Q to quit plotting)>");
        System.out.flush();
    }

    @Override
    public void execute() {
        List<SacTraceData> data = SacDataModel.getInstance().getPlottableData();
        if (plotPrintOptions.isJustListPrinters()) {
            PlotPrinter.getInstance().listPrinters();
            return;
        }
        if (data.size() < 1) {
            log.warn("No data files to plot!");
            return;
        }

        PairT<Double, Double> dataMinMax = PlotUtils.getGlobalLimits(data);
        PlotPrefs.getInstance().getYlimits().setDataRange(dataMinMax);
        PlotPrefs.getInstance().getYlimits().resetIndex(); //If multiple limits have been specified this sets the pointer at 1st limit.
        int traceCount = SacDataModel.getInstance().getTraceCount();
        boolean usePlotNumber = traceCount > 1;
        for (int j = 0; j < data.size(); ++j) {
            SacTraceData sacData = data.get(j);
            if (!sacData.isPlottable()) {
                continue;
            }
            SwingUtilities.invokeLater(() -> {
                PlotFrame.getInstance().setVisible(true);
            });
            final int fileNumber = j;
            SwingUtilities.invokeLater(() -> {
                PlotPanel.getInstance().plotSeismogram(sacData, fileNumber);
                plotPrintOptions.maybePrintPlot(PlotPanel.getInstance(), fileNumber, usePlotNumber);
            });

            if (j >= traceCount - 1) {
                return;
            }
            if (plotPrintOptions.isPauseAfterPlotting()) {
                String text = sc.nextLine().trim().toUpperCase();
                if (text.equals("Q") || text.equals("QUIT")) {
                    System.out.println("");
                    return;
                }
                displayPrompt();
            }

        }

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"P", "PLOT"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
