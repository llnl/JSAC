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

/**
 *
 * @author dodge1
 */
public class CopyhdrSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("FROM", ValuePossibilities.ONE_VALUE, String.class));
    }

    private String fromValue = null;
    private final List<String> headerValues;

    public CopyhdrSacCommand() {

        headerValues = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        headerValues.clear();
        fromValue = null;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        if (parsedTokens.isEmpty()) {
            String msg = String.format("COPYHDR requires a FROM value and at least one header name!");
            throw new IllegalStateException(msg);
        }
        List<Object> junk = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (junk != null && !junk.isEmpty()) {
            StringBuilder sb = new StringBuilder("Unexpected tokens: ");
            for (Object obj : junk) {
                sb.append(obj).append(" ");
            }
            sb.append("!");
            throw new IllegalStateException(sb.toString());
        }

        List<Object> files = parsedTokens.remove("FROM");
        if (files == null || files.size() != 1) {
            String msg = String.format("COPYHDR requires a single FROM value!");
            throw new IllegalStateException(msg);
        }
        fromValue = (String) files.get(0);

        for (String fieldName : parsedTokens.keySet()) {
            headerValues.add(fieldName);
        }
    }

    @Override
    public void execute() {
        if (fromValue != null && !headerValues.isEmpty()) {
            SacDataModel.getInstance().copyHeaderValues(fromValue, headerValues);
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "COPYHDR" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
