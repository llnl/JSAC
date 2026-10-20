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
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.util.SacHeaderComparator;

/**
 *
 * @author dodge1
 */
public class SortSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    private final SacHeaderComparator comparator = new SacHeaderComparator();

    public SortSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty() && !comparator.hasSortFields()) {
            return; // Cannot sort without at least one field declared.
        }
        if (!parsedTokens.isEmpty()) {
            comparator.clear(); //New fields specified so remove previous sort fields.
            for (String fieldName : parsedTokens.keySet()) {
                boolean ascending = true;
                List<Object> values = parsedTokens.get(fieldName);
                if (values != null && !values.isEmpty()) {
                    Object p = values.get(0);
                    if (p != null && p.equals("DESCEND")) {
                        ascending = false;
                    }
                }
                comparator.addField(fieldName, ascending);
            }
        }
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().sort(comparator);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"SORT"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
