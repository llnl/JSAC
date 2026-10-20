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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.llnl.gnem.jsac.commands.graphicsEnvironment;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dodge1
 */
public class TitleSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SIZE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, null));
    }

    private boolean showLabel = false;

    public TitleSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> sizes = parsedTokens.remove("SIZE");
        if (sizes != null) {
            for (Object aSize : sizes) {
                String titleSize = (String) aSize;
                PlotPrefs.getInstance().setTitleFontSize(titleSize.toUpperCase());
                break;
            }
        }

        List<Object> objects = parsedTokens.remove("ON");
        if (objects != null) {
            showLabel = true;
            PlotPrefs.getInstance().setShowTitle(showLabel);
        }
        objects = parsedTokens.remove("OFF");
        if (objects != null) {
            showLabel = false;
            PlotPrefs.getInstance().setShowTitle(showLabel);
        }

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            String label = (String) objects.get(0);
            PlotPrefs.getInstance().setShowTitle(true);
            PlotPrefs.getInstance().setTitle(label);
        }
    }

    @Override
    public void execute() {

    }

    @Override
    public  Collection<String> getCommandNames() {
        String[] names = {"TITLE"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
