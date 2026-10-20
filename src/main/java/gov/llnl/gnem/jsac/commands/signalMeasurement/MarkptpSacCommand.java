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
package gov.llnl.gnem.jsac.commands.signalMeasurement;

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

public class MarkptpSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(MarkptpSacCommand.class);

    private static String toMarker = "T0";
    private static double length = 5.0;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static final List<String> refs;

    static {
        String[] foo = { "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8" };
        refs = new ArrayList<>(Arrays.asList(foo));
    }

    static {
        descriptors.add(new AttributeDescriptor("TO", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("LENGTH", ValuePossibilities.ONE_OR_MORE, Double.class));
    }

    public MarkptpSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);

        List<Object> values = parsedTokens.remove("TO");
        if (values != null && values.size() == 1) {
            String tmp = (String) values.get(0);
            if (refs.contains(tmp.toUpperCase())) {
                toMarker = tmp.toUpperCase();
            } else {
                throw new IllegalStateException("Invalid marker: " + tmp.toUpperCase() + " Only T0 - T8 are allowed.");
            }
        }

        values = parsedTokens.remove("LENGTH");
        if (values != null && values.size() == 1) {
            length = (double) values.get(0);
        }

    }

    @Override
    public void execute() {

        List<SacTraceData> data = SacDataModel.getInstance().getData();
        int idx = refs.indexOf(toMarker);
        for (SacTraceData std : data) {
            if (std.isSpectral()) {
                String msg = String.format("ERROR 1307: Illegal operation on spectral file");
                log.warn(msg);
                continue;
            }

            std.markptp(MTWSacCommand.getPartialDataWindow(), length, idx);

        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "MARKPTP", "MARKP" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
