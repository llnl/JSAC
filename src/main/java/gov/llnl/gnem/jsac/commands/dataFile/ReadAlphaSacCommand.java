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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACDataEncoding;
import gov.llnl.gnem.jsac.util.FileFinder;
import gov.llnl.gnem.jsac.util.FileUtil;
import gov.llnl.gnem.jsac.util.PathManager;

/**
 *
 * @author dodge1
 */
public class ReadAlphaSacCommand implements SacCommand {

    private static String requestedDir;
    private final List<String> mytokens;
    private String cwd;
    private boolean readmore = false;

    public ReadAlphaSacCommand() {
        mytokens = new ArrayList<>();
        cwd = PathManager.getInstance().getCurrentDir().toString();
    }

    @Override
    public void initialize(String[] tokens) {
        mytokens.clear();
        cwd = PathManager.getInstance().getCurrentDir().toString();
        readmore = false;
        mytokens.addAll(Arrays.asList(tokens));
        mytokens.remove(0); // Don't need the command name anymore.
        readmore = FileUtil.maybeGetMoreOption(mytokens);
        try {
            requestedDir = FileUtil.maybeGetRequestedDir(mytokens);
            if (requestedDir == null) {
                requestedDir = cwd;
            }
        } catch (IllegalStateException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void execute() {
        if (mytokens.isEmpty()) {
            System.out.println("The READALPHA comand requires at least one argument!");
            return;
        }
        if (!readmore) {
            SacDataModel.getInstance().clear();
        }
        long start = System.currentTimeMillis();

        List<SacTraceData> results = new ArrayList<>();
        for (String regExString : mytokens) {
            List<Path> paths = new FileFinder(Paths.get(requestedDir), regExString).findMatches();
            results.addAll(getResults(paths));
        }

        long end = System.currentTimeMillis();
        double elapsed = (end - start) / 1000.0;
        SacDataModel.getInstance().addAll(results, elapsed);

    }

    private List<SacTraceData> getResults(List<Path> filesToRead) {
        List<SacTraceData> results = new ArrayList<>(filesToRead.parallelStream().map(this::createSacFile).filter(Objects::nonNull).collect(Collectors.toList()));
        return results;
    }

    private SacTraceData createSacFile(Path file) {
        return Misc.createSacFile(file, SACDataEncoding.ALPHANUMERIC);
    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = { "RA", "READALPHA" };
        return new ArrayList<>(Arrays.asList(names));
    }
}
