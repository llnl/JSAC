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

package gov.llnl.gnem.jsac.commands.fileSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.util.PathManager;

/**
 *
 * @author dodge1
 */
public class CdSacCommand implements SacCommand {

    private String newDir = null;

    @Override
    public void initialize(String[] tokens) {
        if (tokens.length == 2) {
            newDir = tokens[1];
        }
    }

    @Override
    public void execute() {
        if (newDir != null) {
            try {
                PathManager.getInstance().changeDirectory(newDir);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"CD", "CHDIR"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
