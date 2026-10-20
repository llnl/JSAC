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
package gov.llnl.gnem.jsac.commands.dataFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import java.util.List;
import java.util.Map;


public class SynchronizeSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private boolean round;
    private boolean begin;

    static {
        descriptors.add(new AttributeDescriptor("BEGIN", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("ROUND", ValuePossibilities.ONE_VALUE, String.class));
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().synchronize(begin, round);
    }

    @Override
    public void initialize(String[] tokens) {
        round = false;
        begin = false;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        List<Object> tmp = parsedTokens.remove("BEGIN");
        if (tmp != null && tmp.size() == 1) {
            String value = (String) tmp.get(0);
            begin = value.equalsIgnoreCase("ON");
        }
        tmp = parsedTokens.remove("ROUND");
        if (tmp != null && tmp.size() == 1) {
            String value = (String) tmp.get(0);
            round = value.equalsIgnoreCase("ON");
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"SYNCHRONIZE", "SYNC"};
        return new ArrayList<>(Arrays.asList(names));
    }
}
