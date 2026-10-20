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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.print.PrintService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.plot.PlotFrame;
import llnl.gnem.core.gui.plotting.JPlotContainer;
import llnl.gnem.core.gui.plotting.PlotPrinter;

/**
 *
 * @author dodge1
 */
public class PlotPrintOptions {

    private static final Logger log = LoggerFactory.getLogger(PlotPrintOptions.class);
    private static final int MIN_DPI = 50;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> allowableFormats = new ArrayList<>(Arrays.asList("PNG", "GIF", "SVG"));
    private static final String DEFAULT_FORMAT = "PNG";
    private static final int DEFAULT_DPI = 300;
    private boolean print = false;
    private String selectedFormat;
    private String fileName;
    private int selectedDPI;
    private boolean listPrintersOnly;
    private String printerName;

    public PlotPrintOptions() {
        print = false;
        selectedFormat = null;
        fileName = null;
        selectedDPI = DEFAULT_DPI;
        listPrintersOnly = false;
        printerName = null;
    }

    static {
        descriptors.add(new AttributeDescriptor("PRINT", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FORMAT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("FILENAME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("DPI", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("LIST_PRINTERS", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("LIST", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("PRINTERNAME", ValuePossibilities.ONE_VALUE, String.class));
    }

    public static List<AttributeDescriptor> getPrintDescriptors() {
        return new ArrayList<>(descriptors);
    }

    public void initialize(Map<String, List<Object>> parsedTokens) {
        print = false;
        selectedFormat = null;
        fileName = null;
        selectedDPI = DEFAULT_DPI;
        listPrintersOnly = false;
        printerName = null;

        List<Object> result = TokenListParser.getObjects(parsedTokens, "PRINT");
        print = result != null;

        result = TokenListParser.getObjects(parsedTokens, "FORMAT");
        if (result != null && !result.isEmpty()) {
            String tmp = (String) result.get(0);
            if (!allowableFormats.contains(tmp.toUpperCase())) {
                throw new IllegalStateException("Unsupported format: " + tmp + "!");
            } else {
                selectedFormat = tmp.toUpperCase();
            }
        }
        result = TokenListParser.getObjects(parsedTokens, "FILENAME");
        if (result != null && !result.isEmpty()) {
            String tmp = ((String) result.get(0)).trim();
            if (!tmp.isEmpty()) {
                if (selectedFormat == null) {
                    selectedFormat = DEFAULT_FORMAT;
                }
                if (tmp.toUpperCase().endsWith("." + selectedFormat.toUpperCase())) {
                    int index = tmp.lastIndexOf(".");
                    fileName = tmp.substring(0, index);
                } else {
                    fileName = tmp;
                }

            }
        }

        result = TokenListParser.getObjects(parsedTokens, "DPI");
        if (result != null && !result.isEmpty()) {
            Integer tmp = (Integer) result.get(0);
            if (tmp < MIN_DPI) {
                throw new IllegalStateException("Unsupported resolution: " + tmp + "!");
            } else {
                selectedDPI = tmp;
            }
        }

        result = TokenListParser.getObjects(parsedTokens, "LIST_PRINTERS", "LIST");
        listPrintersOnly = result != null;

        result = TokenListParser.getObjects(parsedTokens, "PRINTERNAME");
        if (result != null && !result.isEmpty()) {
            String tmp = ((String) result.get(0)).trim();
            if (!tmp.isEmpty()) {
                printerName = tmp;
            }
        }

    }

    public boolean isJustListPrinters() {
        return print && listPrintersOnly;
    }

    public boolean isPauseAfterPlotting() {
        return !print;
    }

    public void maybePrintPlot(JPlotContainer plot, int plotNumber, boolean usePlotNumber) {
        if (print) {
            try {
                if (fileName == null) {
                    if (printerName != null) {
                        PrintService service = PlotPrinter.getInstance().getServiceByName(printerName);
                        if (service != null) {
                            PlotPrinter.getInstance().printCurrentPlot(plot, true, service);
                        } else {
                            throw new IllegalStateException("No such printer: " + printerName + "!");
                        }
                    } else {
                        PlotPrinter.getInstance().printCurrentPlot(plot, true);
                    }
                } else {
                    int width = PlotFrame.getInstance().getWidth();
                    int height = PlotFrame.getInstance().getHeight();
                    String printName = constructFileName(plotNumber, usePlotNumber);
                    PlotPrinter.getInstance().exportPlot(plot, width, height, selectedDPI, selectedFormat, printName);
                }
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }

    }

    private String constructFileName(final int plotNumber, boolean usePlotNumber) {
        StringBuilder printName = new StringBuilder().append(fileName);
        if (usePlotNumber) {
            printName.append(".").append(plotNumber +1 ).append(".").append(selectedFormat.toLowerCase());
        } else {
            printName.append(".").append(selectedFormat.toLowerCase());
        }
        return printName.toString();
    }

    public String getPrintHelpString(String cmdName, boolean requiresFFT) {
        String fftString = requiresFFT ? "fft\n		" : "";
        String fmt = "     PRINT :			Prints the resulting plot to a printer or to a file.\n"
                + "	 \n"
                + "		FORMAT			When printing to a file, FORMAT controls the output format.\n"
                + "						Allowable formats are PNG,GIF,SVG. Default format is PNG\n"
                + "						\n"
                + "		FILENAME		The name of the output file. If multiple SAC files are in memory\n"
                + "						then the file number is appended to the file name, \n"
                + "						e.g., filename.1.png, filename.2.png, etc.\n"
                + "						\n"
                + "		PRINTERNAME		If no filename is specified then output is sent to a printer. If no\n"
                + "						printer name is specified, then output is sent to the default printer.\n"
                + "						Printer names must be specified as shown by the LIST_PRINTERS option.\n"
                + "						Names with spaces must be enclosed in quotes.\n"
                + "		{LIST}_PRINTERS	If the LIST sub-option is specified then JSAC will simply list the\n"
                + "						available printers.\n"
                + "						\n"
                + "		DPI				For raster formats (PNG,GIF) This specifies the resolution in dots-per-inch.\n"
                + "						The default is 300 dpi.\n"
                + "						\n"
                + "EXAMPLES\n"
                + "	To list printers only:\n"
                + "		%s print list\n"
                + "		\n"
                + "	To send plots to the default printer:\n"
                + "		fg seis\n"
                + "		%s%s print\n"
                + "	\n"
                + "	To send plots to a named printer:\n"
                + "		fg seis\n"
                + "		%s%s print printername \"Adobe PDF\"\n"
                + "	\n"
                + "	To export the plot to an SVG-format file named example.svg\n"
                + "		fg seis\n"
                + "		%s%s print filename example format svg";

        return String.format(fmt, cmdName, fftString, cmdName, fftString, cmdName, fftString, cmdName);
    }
}
