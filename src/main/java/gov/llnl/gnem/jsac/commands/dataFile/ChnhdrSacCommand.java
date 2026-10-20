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
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import llnl.gnem.core.util.PairT;

/**
 *
 * @author dodge1
 */
public class ChnhdrSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FILE", ValuePossibilities.ONE_OR_MORE, Integer.class));
        descriptors.add(new AttributeDescriptor("ALLT", ValuePossibilities.ONE_VALUE, Double.class));
    }

    private final List<Integer> fileNumbers;
    private final List<PairT<String, Object>> newHeaderValues;
    private Double allTValue = null;
    private String newAgency;

    public ChnhdrSacCommand() {

        fileNumbers = new ArrayList<>();
        newHeaderValues = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        fileNumbers.clear();
        newHeaderValues.clear();
        allTValue = null;
        newAgency = null;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> files = parsedTokens.remove("FILE");
        if (files != null) {
            for (Object fileNumber : files) {
                fileNumbers.add((Integer) fileNumber);
            }
        }

        List<Object> allTValues = parsedTokens.remove("ALLT");
        if (allTValues != null && allTValues.size() == 1) {
            allTValue = (Double) allTValues.get(0);
        }
        for (String fieldName : parsedTokens.keySet()) {
            List<Object> values = parsedTokens.get(fieldName);
            if (values != null && values.size() == 1) {
                newHeaderValues.add(new PairT<>(fieldName, values.get(0)));
            }
            else if(values != null && values.size() == 2){
                Object tmp = values.get(0);
                Object tmp2 = values.get(1);
                if(tmp instanceof String && tmp2 instanceof String){
                    String tmp3 = (String)tmp;
                    if(tmp3.toUpperCase().equals("AGENCY")){
                        newAgency = (String)tmp2;
                    }
                }
            }
        }
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().changeHeaderValues(fileNumbers, newHeaderValues, allTValue,newAgency);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"CH", "CHNHDR"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
