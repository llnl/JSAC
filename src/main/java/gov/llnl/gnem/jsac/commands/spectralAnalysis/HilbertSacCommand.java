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

package gov.llnl.gnem.jsac.commands.spectralAnalysis;

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
 * @author dodge1
 */
public class HilbertSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("SPECTRAL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("FIR", ValuePossibilities.TWO_VALUES, Double.class));
    }

    static HilbertTransformerType type = HilbertTransformerType.SPECTRAL;

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        if (parsedTokens.containsKey("SPECTRAL") || parsedTokens.containsKey("SP")) {
            type = HilbertTransformerType.SPECTRAL;
        } else if (parsedTokens.containsKey("FIR")) {
            type = HilbertTransformerType.FIR;
        } else {
            type = HilbertTransformerType.SPECTRAL;
        }

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().applyHilbert(type);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "HILBERT" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
