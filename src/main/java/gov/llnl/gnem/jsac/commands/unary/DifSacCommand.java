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

package gov.llnl.gnem.jsac.commands.unary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;

/**
 *
 * @author dodge1 revised by D. Harris to make consistent with IRIS version and
 *         to add highly accurate equiripple (Parks-McClellan) differentiators
 */
public class DifSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("TWO", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("THREE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FIVE", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("EQUIRIPPLE", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("EQ", ValuePossibilities.TWO_VALUES, Double.class));
    }

    static DifferentiatorType difType = DifferentiatorType.TWO_POINT_OPERATOR;

    public DifSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        if (parsedTokens.containsKey("TWO")) {
            difType = DifferentiatorType.TWO_POINT_OPERATOR;
        } else if (parsedTokens.containsKey("THREE")) {
            difType = DifferentiatorType.THREE_POINT_OPERATOR;
        } else if (parsedTokens.containsKey("FIVE")) {
            difType = DifferentiatorType.FIVE_POINT_OPERATOR;
        } else if (parsedTokens.containsKey("EQUIRIPPLE") || parsedTokens.containsKey("EQ")) {
            difType = DifferentiatorType.EQUIRIPPLE;
        } else {
            difType = DifferentiatorType.TWO_POINT_OPERATOR;
        }

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().differentiate(difType);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "DIF" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
