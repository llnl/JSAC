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

package gov.llnl.gnem.jsac.commands.graphicsEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.Xlimits;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class XlimSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("ON", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OFF", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("SIGNAL", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("B", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("E", ValuePossibilities.ONE_OR_MORE, Float.class));

        descriptors.add(new AttributeDescriptor("O", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("A", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("F", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T0", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T1", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T2", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T3", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T4", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T5", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T6", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T7", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T8", ValuePossibilities.ONE_OR_MORE, Float.class));
        descriptors.add(new AttributeDescriptor("T9", ValuePossibilities.ONE_OR_MORE, Float.class));

    }

    public XlimSacCommand() {

    }

    @Override
    public void execute() {
        //Nothing to do. This command just sets its own state to be queried by other commands.
    }

    @Override
    public void initialize(String[] tokens) {
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        Xlimits limits = PlotPrefs.getInstance().getXlimits();
        PartialDataWindow pdw = limits.getPdw();
        List<Object> tmp = parsedTokens.remove("ON");
        if (tmp != null) {
            limits.setLimitsOn(true);
            pdw.setEnabled(true);
        }
        tmp = parsedTokens.remove("OFF");
        if (tmp != null) {
            limits.setLimitsOn(false);
            pdw.setEnabled(false);
        }
        tmp = parsedTokens.remove("SIGNAL");
        if (tmp != null) { // As per SAC manual...
            limits.setLimitsOn(true);
            pdw.setEnabled(true);
            pdw.setStartReference("A");
            pdw.setEndReference("A");
            pdw.setStartOffset(-1F);
            pdw.setEndOffset(1.0F);
        }

        // Now check whether the user just supplied 2 numbers (times relative to 0)
        tmp = parsedTokens.remove(TokenListParser.LEFT_OVER_TOKENS);
        if (tmp != null && tmp.size() == 2) {
            limits.setLimitsOn(true);
            pdw.setNoReferences();
            String s1 = (String) tmp.get(0);
            String s2 = (String) tmp.get(1);
            Float t1 = NumberUtils.isParsable(s1) ? Float.valueOf(s1) : null;
            Float t2 = NumberUtils.isParsable(s2) ? Float.valueOf(s2) : null;
            if (t1 != null && t2 != null) {
                pdw.setStartOffset(t1);
                pdw.setEndOffset(t2);
                pdw.setEnabled(true);
            }
        }

        // Any remaining tokens are for nominated header references
        int tokenNumber = 0;
        for (String ref : parsedTokens.keySet()) {
            tmp = parsedTokens.get(ref);
            if (tmp != null && tmp.size() == 2) {
                limits.setLimitsOn(true);
                Float t1 = (Float) tmp.get(0);
                Float t2 = (Float) tmp.get(1);
                pdw.setStartReference(ref);
                pdw.setEndReference(ref);
                pdw.setStartOffset(t1);
                pdw.setEndOffset(t2);
                pdw.setEnabled(true);

            } else if (tmp != null && tmp.size() == 1) {
                limits.setLimitsOn(true);
                Float s = (Float) tmp.get(0);

                if (tokenNumber == 0) {
                    pdw.setStartReference(ref);
                    pdw.setStartOffset(s);
                } else {
                    pdw.setEndReference(ref);
                    pdw.setEndOffset(s);
                }
                pdw.setEnabled(true);

            }
            ++tokenNumber;
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"XLIM"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
