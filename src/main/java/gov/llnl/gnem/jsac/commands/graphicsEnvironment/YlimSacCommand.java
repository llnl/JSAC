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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.Ylimits;

/**
 *
 * @author dodge1
 */
public class YlimSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, null));
    }

    public YlimSacCommand() {

    }

    @Override
    public void execute() {
        //Nothing to do. This command just sets its own state to be queried by other commands.
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        Ylimits limits = PlotPrefs.getInstance().getYlimits();

        List<Object> tmp = parsedTokens.remove("ON");
        if (tmp != null) {
            limits.setLimitsOn(true);

        }
        tmp = parsedTokens.remove("OFF");
        if (tmp != null) {
            limits.setLimitsOn(false);
        }

        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp == null) {
            return;
        }
        Queue<Object> queue = new LinkedList<>(tmp);
        if (!queue.isEmpty()) {
            limits.reset();
        }
        while (!queue.isEmpty()) {
            Object obj = queue.remove();
            String v = (String) obj;
            if (v.equalsIgnoreCase("ALL")) {
                limits.addAllLimit();
                limits.setLimitsOn(true);
            } else if (v.equalsIgnoreCase("PM") && !queue.isEmpty()) {
                Object obj2 = queue.remove();
                if (NumberUtils.isParsable((String) obj2)) {
                    float value = Math.abs(Float.parseFloat((String) obj2));
                    limits.addLimit(-value, value);
                    limits.setLimitsOn(true);
                }
            } else if (!queue.isEmpty()) {
                Object obj2 = queue.remove();
                if (NumberUtils.isParsable((String) obj) && NumberUtils.isParsable((String) obj2)) {
                    float v1 = Float.parseFloat((String) obj);
                    float v2 = Float.parseFloat((String) obj2);
                    limits.addLimit(v1, v2);
                    limits.setLimitsOn(true);
                }
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"YLIM"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
