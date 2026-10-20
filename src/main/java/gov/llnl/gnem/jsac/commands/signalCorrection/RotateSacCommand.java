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

package gov.llnl.gnem.jsac.commands.signalCorrection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.util.TraceRotator;
import gov.llnl.gnem.jsac.util.TraceRotator.RotationStyle;
import gov.llnl.gnem.jsac.util.TraceRotator.RotationType;

/**
 *
 * @author dodge1
 */
public class RotateSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(RotateSacCommand.class);

    private static RotationStyle rotationStyle = RotationStyle.NORMAL;
    private static RotationType rotationType = RotationType.TO_GCP;
    private double rotationAngle = 0.0;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("THROUGH", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("BY", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("NORMAL", ValuePossibilities.NO_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("REVERSED", ValuePossibilities.NO_VALUE, String.class));
    }

    public RotateSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        List<Object> tmp = parsedTokens.remove("TO");
        if (tmp != null && tmp.size() == 1) {
            String value = (String) tmp.get(0);
            if (value.equalsIgnoreCase("GCP")) {
                rotationType = RotationType.TO_GCP;
            } else if (NumberUtils.isParsable(value)) {
                rotationAngle = Double.parseDouble(value);
                rotationType = RotationType.TO_VALUE;
            }
        }

        tmp = parsedTokens.remove("THROUGH");
        if (tmp == null) {
            tmp = parsedTokens.remove("BY");
        }
        if (tmp != null && tmp.size() == 1) {
            rotationAngle = (Double) tmp.get(0);
            rotationType = RotationType.THROUGH_VALUE;
        }

        tmp = parsedTokens.remove("NORMAL");
        if (tmp != null) {
            rotationStyle = RotationStyle.NORMAL;
        }

        tmp = parsedTokens.remove("REVERSED");
        if (tmp != null) {
            rotationStyle = RotationStyle.REVERSED;
        }
        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp != null && !tmp.isEmpty()) {
            StringBuilder sb = new StringBuilder("Unexpected tokens ( ");
            for (Object obj : tmp) {
                sb.append(obj).append(" ");
            }
            sb.append(")");
            throw new IllegalStateException(sb.toString());
        }
    }

    @Override
    public void execute() {
        List<SacTraceData> files = SacDataModel.getInstance().getData();
        if (files.isEmpty()) {
            log.warn("1301: No data files read in.");
        } else {
            TraceRotator.rotateTraces(files, rotationStyle, rotationType, rotationAngle);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "ROTATE" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
