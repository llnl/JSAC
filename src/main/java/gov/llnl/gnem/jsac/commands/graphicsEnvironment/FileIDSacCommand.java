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

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.FileIDInfo;
import gov.llnl.gnem.jsac.plots.FileIDInfo.FieldLayout;
import gov.llnl.gnem.jsac.plots.FileIDInfo.Format;
import gov.llnl.gnem.jsac.plots.FileIDInfo.Location;
import gov.llnl.gnem.jsac.plots.FileIDInfo.Type;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotColors;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FileIDSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> sizeParams = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("ADJUSTMENT", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("FORMAT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LOCATION", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("TYPE", ValuePossibilities.ONE_OR_MORE, String.class));
        descriptors.add(new AttributeDescriptor("FIELD_LAYOUT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("FL", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("SIZE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("COLOR", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("S", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("FONT", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("DELIMITER", ValuePossibilities.ONE_VALUE, String.class));
        sizeParams.add("SIZE");
        sizeParams.add("S");

    }

    public FileIDSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("FORMAT");
        if (objects != null && objects.size() == 1) {
            String tmp = ((String) objects.get(0)).toUpperCase();
            try {
                Format format = Format.valueOf(tmp);
                FileIDInfo.getInstance().setFormat(format);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid format specification: " + tmp);
            }

        }
        objects = TokenListParser.getObjects(parsedTokens, "ADJUSTMENT");
        if (objects != null && !objects.isEmpty()) {
            Double value = (Double) objects.get(0);
            FileIDInfo.getInstance().setSpacingAdjustment(value);
        }
        objects = TokenListParser.getObjects(parsedTokens, sizeParams);
        if (objects != null) {
            for (Object aSize : objects) {
                String tmp = (String) aSize;
                int size = PlotPrefs.getInstance().getSizeFromString(tmp);
                FileIDInfo.getInstance().setFontSize(size);
                break;
            }
        }
        objects = parsedTokens.remove("COLOR");
        if (objects != null && objects.size() == 1) {
            String tmp = (String) objects.get(0);
            if (tmp.toUpperCase().equals("AUTO")) {
                FileIDInfo.getInstance().setForceColor(false);
            } else {
                Color color = PlotColors.getColorByName(tmp);
                if (color == null) {
                    throw new IllegalStateException("Unrecognized color: " + tmp + "!");
                } else {
                    FileIDInfo.getInstance().setForceColor(true);
                    FileIDInfo.getInstance().setTextColor(color);
                }
            }
        }
        objects = TokenListParser.getObjects(parsedTokens, "FONT");
        if (objects != null && !objects.isEmpty()) {
            String fontName = (String) objects.get(0);
            FileIDInfo.getInstance().setFontName(fontName);
        }

        objects = TokenListParser.getObjects(parsedTokens, "DELIMITER");
        if (objects != null && !objects.isEmpty()) {
            String delimiter = (String) objects.get(0);
            if (delimiter.toUpperCase().equals("NONE")) {
                FileIDInfo.getInstance().setFieldDelimiter("");
            } else {
                FileIDInfo.getInstance().setFieldDelimiter(delimiter);
            }
        }

        objects = parsedTokens.remove("LOCATION");
        if (objects != null && objects.size() == 1) {
            String tmp = ((String) objects.get(0)).toUpperCase();
            try {
                Location location = Location.valueOf(tmp);
                FileIDInfo.getInstance().setLocation(location);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Location specification: " + tmp);
            }
        }
        objects = parsedTokens.remove("ON");
        if (objects != null) {
            FileIDInfo.getInstance().setOn(true);
        }
        objects = parsedTokens.remove("OFF");
        if (objects != null) {
            FileIDInfo.getInstance().setOn(false);
        }

        objects = parsedTokens.remove("FIELD_LAYOUT");
        if (objects == null) {
            objects = parsedTokens.remove("FL");
        }
        if (objects != null && objects.size() == 1) {
            String tmp = ((String) objects.get(0)).toUpperCase();
            if (tmp.equalsIgnoreCase("H") || tmp.equalsIgnoreCase("HORIZ")) {
                tmp = "HORIZONTAL";
            } else if (tmp.equalsIgnoreCase("V") || tmp.equalsIgnoreCase("VERT")) {
                tmp = "VERTICAL";
            }
            try {
                FieldLayout layout = FieldLayout.valueOf(tmp);
                FileIDInfo.getInstance().setFieldLayout(layout);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Field Layout specification: " + tmp);
            }
        }

        objects = parsedTokens.remove("TYPE");

        if (objects != null && objects.size() >= 1) {
            String type = (String) objects.get(0);
            if (type.equalsIgnoreCase("DEFAULT")) {
                FileIDInfo.getInstance().setType(Type.DEFAULT);
            } else if (type.equalsIgnoreCase("NAME")) {
                FileIDInfo.getInstance().setType(Type.NAME);
            } else if (type.equalsIgnoreCase("LIST")) {
                List<String> hdrList = new ArrayList<>();
                for (int j = 1; j < objects.size(); ++j) {
                    String field = ((String) objects.get(j));
                    hdrList.add(field);
                }
                if (!hdrList.isEmpty()) {
                    FileIDInfo.getInstance().setType(Type.LIST);
                    FileIDInfo.getInstance().replaceHdrList(hdrList);
                }
            }
        }

    }

    @Override
    public void execute() {

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"FILEID"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
