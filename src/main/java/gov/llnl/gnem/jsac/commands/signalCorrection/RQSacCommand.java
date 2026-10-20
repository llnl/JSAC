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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;

/**
 *
 * @author dodge1
 */
public class RQSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(RQSacCommand.class);

    private static double q = 1.0;
    private static double r = 0.0;
    private static double c = 1.0;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("Q", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("R", ValuePossibilities.ONE_VALUE, Double.class));
        descriptors.add(new AttributeDescriptor("C", ValuePossibilities.ONE_VALUE, Double.class));
    }

    public RQSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens, true);
        List<Object> tmp = parsedTokens.remove("Q");
        if (tmp != null && tmp.size() == 1) {
            q = (double) tmp.get(0);
        }
        tmp = parsedTokens.remove("R");
        if (tmp != null && tmp.size() == 1) {
            r = (double) tmp.get(0);
        }
        tmp = parsedTokens.remove("C");
        if (tmp != null && tmp.size() == 1) {
            c = (double) tmp.get(0);
        }
    }

    @Override
    public void execute() {
        for (SacTraceData std : SacDataModel.getInstance().getData()) {
            if (!std.isSpectral()) {
                String msg = String.format("1305: Illegal operation on time series file");
                log.warn(msg);
            } else {
                SpectralData sd = std.getSpectralData();
                sd.rq(q, r, c);
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "RQ" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
