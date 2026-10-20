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

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;

/**
 *
 * @author dodge1
 */
public class DeleteChannelSacCommand implements SacCommand {

    private final List<String> mytokens;
    private boolean deleteAll = false;
    private final List<String> filesToDelete;
    private final List<Integer> fileNumbersToDelete;

    public DeleteChannelSacCommand() {
        mytokens = new ArrayList<>();
        filesToDelete = new ArrayList<>();
        fileNumbersToDelete = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        mytokens.clear();
        deleteAll = false;
        filesToDelete.clear();
        fileNumbersToDelete.clear();
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
        parseRemainingTokens();
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().deleteChannels(deleteAll, fileNumbersToDelete, filesToDelete);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"DC", "DELETECHANNEL"};
        return new ArrayList<>(Arrays.asList(names));
    }

    private void parseRemainingTokens() {
        for(int j = 0; j < mytokens.size(); ++j){
            String token = mytokens.get(j);
            if(token.toUpperCase().equals("ALL")){
                this.deleteAll = true;
                return;
            }
            else{
                if (NumberUtils.isParsable(token)) { // Single number...
                    int tmp = Integer.parseInt(token);
                    fileNumbersToDelete.add(tmp-1); // command is 1-based, but data model is 0-based.
                }
                else{
                    String[] subTokens = token.split("-"); // Check for range.
                    if(subTokens.length == 2 && NumberUtils.isParsable(subTokens[0])&& NumberUtils.isParsable(subTokens[1])){
                        int rangeStart = Integer.parseInt(subTokens[0])-1;
                        int rangeEnd = Integer.parseInt(subTokens[1]);
                        for(int k = rangeStart; k < rangeEnd; ++k){
                            fileNumbersToDelete.add(k);
                        }
                    }else{ // Assume this is a file name.
                        filesToDelete.add(token);
                    }
                }
            }
        }
       
    }
}
