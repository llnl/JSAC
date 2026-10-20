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
package gov.llnl.gnem.jsac.commands.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;

public class HighpassSacCommand extends IIRSacImplementation implements SacCommand {

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        initialParsing(parsedTokens);

        List<Object> parameters = maybeGetCorners(parsedTokens);
        if (parameters != null && parameters.size() == 1) {
            double c = (double) parameters.get(0);
            if (c <= 0.0) {
                System.out.println("Corner frequency must be greater than 0.0, reset to " + NOMINAL_CUTOFF);
                c = NOMINAL_CUTOFF;
            }
            cutoff1 = c;
        }

    }

    @Override
    public void execute() {
        SacDataModel.getInstance().applyFilter(design, order, PassbandType.HIGHPASS, cutoff1, cutoff2, epsilon, transitionBW, twoPass);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "HP", "HIGHPASS" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
