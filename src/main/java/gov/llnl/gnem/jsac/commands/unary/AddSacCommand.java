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

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;

/**
 *
 * @author dodge1
 */
public class AddSacCommand implements SacCommand {

    private List<String> mytokens;
    private List<Float> constants;

    @Override
    public void initialize(String[] tokens) {
        mytokens = new ArrayList<>();
        constants = new ArrayList<>();
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
        parseConstants();
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().addConstants(constants);
    }

    private void parseConstants() {
        for (String token : mytokens) {
            constants.add(Float.parseFloat(token));
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "ADD" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
