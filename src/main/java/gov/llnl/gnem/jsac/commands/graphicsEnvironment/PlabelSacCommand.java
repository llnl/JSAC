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
import gov.llnl.gnem.jsac.plots.PLabel;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotColors;

public class PlabelSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> positionParams = new ArrayList<>();
    private static final List<String> sizeParams = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("CLEAR", ValuePossibilities.ZERO_OR_MORE, Integer.class));
        descriptors.add(new AttributeDescriptor("SIZE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("S", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("POSITION", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("P", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("N", ValuePossibilities.ONE_VALUE, Integer.class));
        descriptors.add(new AttributeDescriptor("FONT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("COLOR", ValuePossibilities.ONE_VALUE, String.class));

        positionParams.add("POSITION");
        positionParams.add("P");
        sizeParams.add("SIZE");
        sizeParams.add("S");

    }

    private boolean showLabel = true;
    private static int size = PlotPrefs.getInstance().getDefaultFontSize();
    private static String fontName = "Arial";
    private static Color textColor = Color.black;

    public PlabelSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        Double x = null;
        Double y = null;
        String labelText = null;
        Integer n = null;

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = TokenListParser.getObjects(parsedTokens, "N");
        if (objects != null && objects.size() == 1) {
            n = (Integer) objects.get(0);

        }
        objects = TokenListParser.getObjects(parsedTokens, "CLEAR");
        if (objects != null) {
            if (n != null) {
                PlotPrefs.getInstance().clearPLabel(n - 1);
                n = null;
            } else if (objects.size() == 1) {
                Integer tmp = (Integer) objects.get(0);
                PlotPrefs.getInstance().clearPLabel(tmp - 1);
            } else {
                PlotPrefs.getInstance().clearPLabels();
            }
            return;
        }
        objects = TokenListParser.getObjects(parsedTokens, sizeParams);
        if (objects != null) {
            for (Object aSize : objects) {
                String tmp = (String) aSize;
                size = PlotPrefs.getInstance().getSizeFromString(tmp);
                break;
            }
        }

        objects = TokenListParser.getObjects(parsedTokens, "FONT");
        if (objects != null && !objects.isEmpty()) {
            fontName = (String) objects.get(0);
        }
        objects = TokenListParser.getObjects(parsedTokens, "COLOR");
        if (objects != null && !objects.isEmpty()) {
            String tmp = (String) objects.get(0);
            textColor = PlotColors.getColorByName(tmp);
        }

        objects = TokenListParser.getObjects(parsedTokens, positionParams);
        if (objects != null && objects.size() == 2) {
            x = (Double) objects.remove(0);
            y = (Double) objects.remove(0);
        }

        objects = TokenListParser.getObjects(parsedTokens, "ON");
        if (objects != null) {
            showLabel = true;

        }
        objects = TokenListParser.getObjects(parsedTokens, "OFF");
        if (objects != null) {
            showLabel = false;
        }

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            labelText = (String) objects.get(0);
            showLabel = true;
        }
        PlotPrefs.getInstance().setShowPLabels(showLabel);
        if ((x == null && y != null) || (y == null && x != null)) {
            throw new IllegalStateException("You must supply either both X and Y values or neither to the PLABEL command!");
        }
        if (labelText == null || labelText.isEmpty()) {
            return;
        }

        if (x == null && y == null && n == null) { // Plot below the last PLabel if exists
            PLabel lastLabel = PlotPrefs.getInstance().getLastPLabel();
            if (lastLabel != null) {
                x = lastLabel.getxValue();
                y = -1.0;
                PLabel result = new PLabel(labelText, fontName, textColor, x, y, size);
                PlotPrefs.getInstance().addPLabel(result);
            } else { // plot in default position
                PLabel result = new PLabel(labelText, fontName, textColor, size);
                PlotPrefs.getInstance().addPLabel(result);
            }
        } else if (n != null) { // Modify the nth PLabel
            if (x != null && y != null) { // modify all attributes
                PLabel result = new PLabel(labelText, fontName, textColor, x, y, size);
                PlotPrefs.getInstance().replacePLabel(n - 1, result);
            } else {// just modify non-position attributes
                PLabel label = PlotPrefs.getInstance().getPLabel(n - 1);
                PLabel result = new PLabel(labelText, fontName, textColor, label.getxValue(), label.getyValue(), size);
                PlotPrefs.getInstance().replacePLabel(n - 1, result);
            }
        } else if (x != null && y != null) {
            PLabel result = new PLabel(labelText, fontName, textColor, x, y, size);
            PlotPrefs.getInstance().addPLabel(result);
        }

    }

    @Override
    public void execute() {

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "PLABEL" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
