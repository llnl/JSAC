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
package gov.llnl.gnem.jsac.commands.binary;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACDataEncoding;
import gov.llnl.gnem.jsac.util.PathManager;
import gov.llnl.gnem.jsac.util.TraceMerger;
import gov.llnl.gnem.jsac.util.TraceMerger.GapStrategy;
import llnl.gnem.core.util.StreamKey;

/**
 *
 * @author dodge1
 */
public class MergeSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();
    private static GapStrategy gapStrategy = GapStrategy.ZERO;
    private final List<Path> pathsToMerge;

    static {
        descriptors.add(new AttributeDescriptor("GAP", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("MAX_SAMPLE_INCONSISTENCY", ValuePossibilities.ONE_VALUE, Double.class));
    }

    public MergeSacCommand() {
        pathsToMerge = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        gapStrategy = GapStrategy.ZERO;
        pathsToMerge.clear();
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        List<Object> objects = parsedTokens.remove("GAP");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            gapStrategy = GapStrategy.valueOf((String) tmp);
        }
        objects = parsedTokens.remove("MAX_SAMPLE_INCONSISTENCY");
        if (objects != null && objects.size() == 1) {
            Object tmp = objects.get(0);
            TraceMerger.setMAX_SAMPLE_INCONSISTENCY((Double) tmp);
        }

        objects = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
        if (objects != null && !objects.isEmpty()) {
            for (Object obj : objects) {
                String valueString = (String) obj;
                Path path = PathManager.getInstance().resolveAndValidateFile(valueString);
                pathsToMerge.add(path);
            }
        }

    }

    @Override
    public void execute() {
        Map<StreamKey, List<SacTraceData>> keyCollectionMap = new HashMap<>();
        for (Path path : pathsToMerge) {
            SacTraceData std = Misc.createSacFile(path, SACDataEncoding.BINARY);
            if (std != null) {
                StreamKey sk = std.getStreamKey();
                List<SacTraceData> cstd = keyCollectionMap.get(sk);
                if (cstd == null) {
                    cstd = new ArrayList<>();
                    keyCollectionMap.put(sk, cstd);
                }
                cstd.add(std);
            }
        }
        for (SacTraceData std : SacDataModel.getInstance().getData()) {
            StreamKey sk = std.getStreamKey();
            List<SacTraceData> cstd = keyCollectionMap.get(sk);
            if (cstd == null) {
                cstd = new ArrayList<>();
                keyCollectionMap.put(sk, cstd);
            }
            cstd.add(std);

        }

        SacDataModel.getInstance().clear();
        for (StreamKey key : keyCollectionMap.keySet()) {
            SacTraceData std = TraceMerger.merge(keyCollectionMap.get(key), gapStrategy);
            if (std != null) {
                SacDataModel.getInstance().add(std);
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"MERGE"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
