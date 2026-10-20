/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.commands.arrayProcessing;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.arrayProcessing.ArrayData;
import gov.llnl.gnem.jsac.arrayProcessing.ArrayDefinition;
import gov.llnl.gnem.jsac.arrayProcessing.SacArrayModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.arrayMap.ArrayMapPlotFrame;
import gov.llnl.gnem.jsac.plots.arrayMap.ArrayMapPlotPanel;
import javax.swing.SwingUtilities;

public class ArrayMapSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("TITLE", ValuePossibilities.ONE_VALUE, String.class));
    }

    enum MapType {
        ARRAY, COARRAY
    }
    private MapType selectedMapType;
    private String plotTitle;

    public ArrayMapSacCommand() {
    }

    @Override
    public void execute() {
        ArrayData adata = new ArrayData(SacDataModel.getInstance().getData(), "CurrentArrayData");
        SacArrayModel.getInstance().addArray(adata);
        if (plotTitle == null) {
            ArrayDefinition ad = adata.getArrayDefinition();
            if (ad != null) {
                plotTitle = ad.getArrayName();
            } else {
                plotTitle = adata.getID();
            }
        }
        double[][] values = selectedMapType == MapType.COARRAY ? adata.getCoarray() : adata.getArray();
        boolean isArrayPlot = selectedMapType == MapType.ARRAY;
        String[] stations = adata.getStations();
        double[] centroid = getCentroidForPlotting(adata);

        SwingUtilities.invokeLater(() -> {
            ArrayMapPlotFrame.getInstance().setIsArrayPlot(isArrayPlot);
            ArrayMapPlotFrame.getInstance().setVisible(true);
            ArrayMapPlotPanel.getInstance().plotCoordinates(values, isArrayPlot, stations, centroid,plotTitle);
        });
    }

    public double[] getCentroidForPlotting(ArrayData adata) {
        double[] latitudes = adata.getLatitudes();
        double[] longitudes = adata.getLongitudes();
        double[] centroid = new double[2];
        for (int ich = 0; ich < latitudes.length; ich++) {
            centroid[1] += longitudes[ich];
            centroid[0] += latitudes[ich];
        }
        for (int i = 0; i < 2; i++) {
            centroid[i] /= latitudes.length;
        }
        return centroid;
    }

    @Override
    public void initialize(String[] tokens) {
        plotTitle = null;
        selectedMapType = MapType.ARRAY;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        List<Object> tmp = parsedTokens.remove("TITLE");
        if (tmp != null && tmp.size() == 1) {
            plotTitle = (String) tmp.get(0);
        }

        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp != null && tmp.size() == 1) {
            String selection = (String) tmp.get(0);
            if (selection.equalsIgnoreCase("ARRAY")) {
                // do nothing
            } else if (selection.equalsIgnoreCase("COARRAY")) {
                selectedMapType = MapType.COARRAY;
            } else {
                throw new IllegalStateException("Invalid Map Type: " + selection);
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"ARRAYMAP"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
