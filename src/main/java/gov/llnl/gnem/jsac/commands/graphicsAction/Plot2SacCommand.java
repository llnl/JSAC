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

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.TimeMode;
import gov.llnl.gnem.jsac.plots.plot2.Plot2Frame;
import gov.llnl.gnem.jsac.plots.plot2.Plot2Panel;
import java.util.logging.Level;
import llnl.gnem.core.gui.plotting.PlotPrinter;
import llnl.gnem.core.util.ApplicationLogger;
import llnl.gnem.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class Plot2SacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(Plot2SacCommand.class);

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    private final PlotPrintOptions plotPrintOptions;
    private static String markerName = "B";

    static {
        descriptors.add(new AttributeDescriptor("ABSOLUTE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("RELATIVE", ValuePossibilities.ZERO_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("ABS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("REL", ValuePossibilities.ZERO_OR_MORE, String.class));
        descriptors.addAll(PlotPrintOptions.getPrintDescriptors());
    }

    public static TimeMode timeMode = TimeMode.ABSOLUTE;

    public Plot2SacCommand() {
        plotPrintOptions = new PlotPrintOptions();
    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        List<Object> result = parsedTokens.remove("ABSOLUTE");
        if (result == null) {
            result = parsedTokens.remove("ABS");
        }
        if (result != null) { //Parameter was specified
            timeMode = TimeMode.ABSOLUTE;
        }
        result = parsedTokens.remove("RELATIVE");
        if (result == null) {
            result = parsedTokens.remove("REL");
        }
        if (result != null) { //Parameter was specified
            timeMode = TimeMode.RELATIVE;
            if (!result.isEmpty()) {
                String tmp = (String) result.get(0);
                if (SACHeader.isValidTimeOffsetFieldName(tmp)) {
                    markerName = (String) result.get(0);
                } else {
                    String msg = String.format("Error: The value (%s) is not a valid time field or is not allowable for use in the P1 command.\n"
                            + "Please choose a value from the set(%s)", tmp, SACHeader.getTimeOffsetFieldNames());
                    ApplicationLogger.getInstance().log(Level.SEVERE, msg);
                    throw new IllegalArgumentException("Invalid time field option!");
                }
            }
        }
        plotPrintOptions.initialize(parsedTokens);
    }

    private void displayPrompt() {
        System.out.print("PLOT2 (Q to quit plotting)>");
        System.out.flush();
    }

    @Override
    public void execute() {
        List<SacTraceData> data = SacDataModel.getInstance().getPlottableData();
        if (data.size() < 1) {
            log.warn("No data files to plot!");
            return;
        }
        if (plotPrintOptions.isJustListPrinters()) {
            PlotPrinter.getInstance().listPrinters();
            return;
        }
        PlotUtils.verifyAllRequiredMarkersSet(data, markerName);

        displayPrompt();

        // Initialization for global ylimits if so requested.
        PairT<Double, Double> dataMinMax = PlotUtils.getGlobalLimits(data);
        PlotPrefs.getInstance().getYlimits().setDataRange(dataMinMax);
        PlotPrefs.getInstance().getYlimits().resetIndex(); //If multiple limits have been specified this sets the pointer at 1st limit.

        SwingUtilities.invokeLater(() -> {
            Plot2Frame.getInstance().setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            Plot2Panel.getInstance().plotData(data, timeMode, markerName);
            plotPrintOptions.maybePrintPlot(Plot2Panel.getInstance(), 1, false);
        });
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"P2", "PLOT2"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
