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

package gov.llnl.gnem.jsac.commands.graphicsEnvironment;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotBorderData;
import gov.llnl.gnem.jsac.plots.PlotColors;

/**
 *
 * @author dodge1
 */
public class BorderSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("WIDTH", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("BACKGROUND", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("SUBPLOT_BORDERS", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("SUBPLOT_SPACING", ValuePossibilities.ONE_VALUE, Double.class));
    }

    public BorderSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("WIDTH");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            PlotBorderData.getInstance().setBorderWidthMM((Double) tmp);
        }
        objects = parsedTokens.remove("SUBPLOT_SPACING");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            PlotBorderData.getInstance().setSubplotSpacing((Double) tmp);
        }
        objects = parsedTokens.remove("BACKGROUND");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            Color color = PlotColors.getColorByName((String) tmp);
            if (color != null) {
                PlotBorderData.getInstance().setBorderColor(color);
            }
        }

        objects = parsedTokens.remove("SUBPLOT_BORDERS");
        if (objects != null) {
            if (objects.size() == 1) {
                String tmp = (String) objects.get(0);
                PlotBorderData.getInstance().setDrawSubplotBorders(tmp.equalsIgnoreCase("ON"));
            }
        }

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            for (Object obj : objects) {
                String tmp = (String) obj;
                PlotBorderData.getInstance().setDrawPlotBorder(tmp.equalsIgnoreCase("ON"));
            }
        }

    }

    @Override
    public void execute() {

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"BORDER"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
