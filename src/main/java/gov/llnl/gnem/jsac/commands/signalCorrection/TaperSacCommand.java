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

package gov.llnl.gnem.jsac.commands.signalCorrection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;
import llnl.gnem.core.util.TaperType;

/**
 *
 * @author dodge1
 */
public class TaperSacCommand implements SacCommand {

    private static TaperType taperType = TaperType.Hanning;
    private static double taperPercent = 5.0;
    private List<String> mytokens;

    @Override
    public void initialize(String[] tokens) {
        mytokens = new ArrayList<>();
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
    }

    @Override
    public void execute() {
        if (!mytokens.isEmpty()) {
            maybeSetType();
            maybeSetWidth();
        }
        SacDataModel.getInstance().applyTaper(taperType, taperPercent);
    }

    private void maybeSetType() {
        int ntokens = mytokens.size();
        for (int j = 0; j < ntokens; ++j) {
            String token = mytokens.get(j);
            if (token.equals("TYPE")) {
                if (j > ntokens - 2) {
                    System.out.println("The TYPE option requires specification of one Window Type.");
                } else {
                    String tmp = mytokens.get(j + 1);
                    switch (tmp) {
                    case "HANN":
                    case "HANNING":
                        taperType = TaperType.Hanning;
                        return;
                    case "HAMMING":
                        taperType = TaperType.Hamming;
                        return;
                    case "COSINE":
                        taperType = TaperType.Cosine;
                        return;
                    default:
                        System.out.println("Unrecognized taper type: " + tmp);
                    }
                }
            }
        }
    }

    private void maybeSetWidth() {
        int ntokens = mytokens.size();
        for (int j = 0; j < ntokens; ++j) {
            String token = mytokens.get(j);
            if (token.equals("WIDTH")) {
                if (j > ntokens - 2) {
                    System.out.println("The WIDTH option requires specification of a value between 0 and 0.5.");
                } else {
                    taperPercent = Double.parseDouble(mytokens.get(j + 1)) * 100;
                }
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "TAPER" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
