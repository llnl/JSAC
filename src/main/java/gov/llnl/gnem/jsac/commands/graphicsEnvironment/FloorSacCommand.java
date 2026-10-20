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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;

/**
 *
 * @author dodge1
 */
public class FloorSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(FloorSacCommand.class);

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, null));
    }

    private boolean applyFloor = false;

    public FloorSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("ON");
        if (objects != null) {
            applyFloor = true;
            PlotDrawingData.getInstance().setApplyFloor(applyFloor);
        }
        objects = parsedTokens.remove("OFF");
        if (objects != null) {
            applyFloor = false;
            PlotDrawingData.getInstance().setApplyFloor(applyFloor);
        }

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            String valueString = (String) objects.get(0);
            if (NumberUtils.isParsable(valueString)) {
                Double value = Double.valueOf(valueString);
                if (value > 0) {
                    PlotDrawingData.getInstance().setFloorValue(value);
                    PlotDrawingData.getInstance().setApplyFloor(true);
                } else {
                    log.warn("Invalid floor value: " + value);
                }
            } else {
                log.warn("Cannot interpret floor string: " + valueString);
            }
        }
    }

    @Override
    public void execute() {

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "FLOOR" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
