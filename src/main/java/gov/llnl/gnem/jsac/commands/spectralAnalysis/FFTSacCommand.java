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

package gov.llnl.gnem.jsac.commands.spectralAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;

/**
 *
 * @author dodge1
 */
public class FFTSacCommand implements SacCommand {

    private boolean removeMean = false;
    private PresentationFormat format = PresentationFormat.AmplitudePhase;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("WOMEAN", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("WMEAN", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("RLIM", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("AMPH", ValuePossibilities.NO_VALUE, null));
    }

    public FFTSacCommand() {
    }

    @Override
    public void initialize(String[] tokens) {
        removeMean = false;
        format = PresentationFormat.AmplitudePhase;

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        Set<String> keys = new HashSet<>();
        for (String value : parsedTokens.keySet()) {
            keys.add(value.toUpperCase());
        }

        if (keys.contains("WOMEAN")) {
            removeMean = true;
        }
        if (keys.contains("WMEAN")) {
            removeMean = false;
        }
        if (keys.contains("RLIM")) {
            format = PresentationFormat.RealImaginary;
        }
        if (keys.contains("AMPH")) {
            format = PresentationFormat.AmplitudePhase;
        }
    }

    @Override
    public void execute() {
        SacDataModel.getInstance().computeFFT(removeMean, format);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "FFT", "DFT" };
        return new ArrayList<>(Arrays.asList(names));
    }

}
