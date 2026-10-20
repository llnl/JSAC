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

package gov.llnl.gnem.jsac.commands.executiveFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.SacCommandParser;

/**
 *
 * @author dodge1
 */
public class HistorySacCommand implements SacCommand {

    @Override
    public void initialize(String[] tokens) {
    }

    @Override
    public void execute() {
        int j = 1;
        for (String cmd : SacCommandParser.getInstance().getHistory()) {
            System.out.println(String.format("%4d\t%s", j++, cmd));
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"HIST", "HISTORY"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
