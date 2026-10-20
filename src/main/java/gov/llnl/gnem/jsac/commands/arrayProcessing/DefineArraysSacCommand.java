/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.commands.arrayProcessing;

import gov.llnl.gnem.jsac.arrayProcessing.ArrayDefinitions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.util.PathManager;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class DefineArraysSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private boolean clearArrays = false;

    static {
        descriptors.add(new AttributeDescriptor("FILENAME", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CLEAR", ValuePossibilities.NO_VALUE, String.class));
    }
    private Path filename;

    public DefineArraysSacCommand() {
    }

    @Override
    public void execute() {
        if (clearArrays) {
            ArrayDefinitions.getInstance().clear();
        } else {
            try {
                ArrayDefinitions.getInstance().loadDefinitionsFromFile(filename);
            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    public void initialize(String[] tokens) {
        filename = null;
        clearArrays = false;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> parameters = parsedTokens.get("CLEAR");
        if (parameters != null) {
            clearArrays = true;
            return;
        }
        parameters = parsedTokens.get("FILENAME");
        if (parameters == null || parameters.size() != 1) {
            throw new IllegalStateException("you must supply the name of the file containing array definition data!");
        }
        String tmp = (String) parameters.get(0);
        filename = PathManager.getInstance().resolveAndValidateFile(tmp);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"DEFINEARRAYS", "DA"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
