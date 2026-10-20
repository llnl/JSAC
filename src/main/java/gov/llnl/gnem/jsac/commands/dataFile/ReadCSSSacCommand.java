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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.css.CssProcessor;
import gov.llnl.gnem.jsac.util.FileFinder;
import gov.llnl.gnem.jsac.util.PathManager;
import llnl.gnem.core.waveform.io.css.PathType;

/**
 *
 * @author dodge1
 */
public class ReadCSSSacCommand implements SacCommand {

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("WFDISCFILE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("WFILE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("MORE", ValuePossibilities.NO_VALUE, null));
    }
    private String wfdiscFile = null;
    private boolean more = false;

    public ReadCSSSacCommand() {

    }

    @Override
    public void initialize(String[] tokens) {
        more = false;
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }
        List<Object> wfFiles = parsedTokens.remove("WFDISCFILE");
        if (wfFiles == null) {
            wfFiles = parsedTokens.remove("WFILE");
        }
        if (wfFiles != null && wfFiles.size() == 1) {
            for (Object obj : wfFiles) {
                wfdiscFile = (String) obj;
            }
        }

        List<Object> lo = parsedTokens.remove("MORE");
        if (lo != null) {
            more = true;
        }

    }

    @Override
    public void execute() {
        if (wfdiscFile == null) {
            System.out.println("You must specify at least a WFDISC filename!");
            return;
        }
        if (!more) {
            SacDataModel.getInstance().clear();
        }

        List<Path> paths = new FileFinder(PathManager.getInstance().getCurrentDir(), wfdiscFile).findMatches();   //.findMatchingFiles(PathManager.getInstance().getCurrentDir().toString(),filename);

        for (Path path : paths) {
            try {
                long start = System.currentTimeMillis();
                List<SacTraceData> data = CssProcessor.readCSS(path.toString(), PathType.FilePathPlusRel);
                long end = System.currentTimeMillis();
                double elapsed = (end - start) / 1000.0;
                SacDataModel.getInstance().addAll(data, elapsed);
            } catch (Exception ex) {
                Logger.getLogger(ReadCSSSacCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"RCSS", "READCSS"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
