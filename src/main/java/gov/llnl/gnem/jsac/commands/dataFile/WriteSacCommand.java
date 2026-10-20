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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACAlphaWriter;
import gov.llnl.gnem.jsac.io.SACDataEncoding;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACFileWriter;
import gov.llnl.gnem.jsac.io.SACHeader;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.util.PathManager;
import llnl.gnem.core.util.FileUtil.DriveMapper;

public class WriteSacCommand implements SacCommand {

    private static final Logger log = LoggerFactory.getLogger(WriteSacCommand.class);

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {
        descriptors.add(new AttributeDescriptor("DIR", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("KSTCMP", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("ALPHA", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("OVER", ValuePossibilities.NO_VALUE, null));
        descriptors.add(new AttributeDescriptor("APPEND", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("PREPEND", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("DELETE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("CHANGE", ValuePossibilities.TWO_VALUES, String.class));
    }

    private final List<Path> directoryList;
    private final List<String> fileList;
    private SACDataEncoding dataEncoding = SACDataEncoding.BINARY;

    public WriteSacCommand() {
        directoryList = new ArrayList<>();
        fileList = new ArrayList<>();
    }

    @Override
    public void initialize(String[] tokens) {
        directoryList.clear();
        fileList.clear();
        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        Set<String> keys = new HashSet<>();
        for (String value : parsedTokens.keySet()) {
            keys.add(value.toUpperCase());
        }

        // initial population of directory list and file list
        List<SacTraceData> traceData = SacDataModel.getInstance().getData();

        for (SacTraceData trace : traceData) {
            Path dir = PathManager.getInstance().getCurrentDir();
            directoryList.add(dir);
            fileList.add(trace.getFilename().getFileName().toString());
        }

        List<Object> tmp = parsedTokens.remove("ALPHA");
        if (tmp != null) {
            dataEncoding = SACDataEncoding.ALPHANUMERIC;
        }

        if (keys.contains("OVER")) {
        } else {

            if (keys.contains("DIR")) {

                List<Object> parameters = parsedTokens.get("DIR");
                Path dir = null;
                String s = (String) parameters.get(0);
                if (s == null) {
                    s = "NONE";
                }

                switch (s.toUpperCase()) {
                    case "OFF":
                    case "CURRENT":
                        dir = PathManager.getInstance().getCurrentDir();
                        break;
                    default:
                        dir = PathManager.getInstance().resolvePath(DriveMapper.getInstance().maybeMapPath(s));
                        break;
                }

                for (int i = 0; i < directoryList.size(); i++) {
                    directoryList.set(i, dir);
                }
            }

            if (keys.contains("KSTCMP")) {

                for (int i = 0; i < traceData.size(); i++) {
                    SACHeader header = traceData.get(i).getSACHeader();
                    String filename = header.getKstnm().trim() + "." + header.getKcmpnm().trim();
                    fileList.set(i, filename);
                }

                // check for duplicates
                for (int i = 0; i < fileList.size(); i++) {
                    String S = fileList.get(i);
                    int count = 1;
                    for (int j = i + 1; j < fileList.size(); j++) {
                        String s = fileList.get(j);
                        if (S.equals(s)) {
                            fileList.set(j, s + "." + count);
                        }
                        count++;
                    }
                }

                // add .sac to end of file name
                for (int i = 0; i < fileList.size(); i++) {
                    fileList.set(i, fileList.get(i) + ".sac");
                }

            } else {
                List<Object> leftovers = parsedTokens.get(TokenListParser.LEFT_OVER_TOKENS);
                if (leftovers != null) {
                    if (fileList.size() != leftovers.size()) {
                        log.info("Number of arguments ({}) must match number of files in memory ({})", leftovers.size(), fileList.size());
                        fileList.clear();
                        directoryList.clear();
                        throw new IllegalStateException("Error in argument list!");
                    } else {
                        for (int i = 0; i < fileList.size(); i++) {
                            fileList.set(i, (String) leftovers.get(i));
                        }
                    }
                }
            }
            if (keys.contains("APPEND")) {
                List<Object> parameters = parsedTokens.get("APPEND");
                String s = (String) parameters.get(0);
                for (int i = 0; i < fileList.size(); i++) {
                    fileList.set(i, fileList.get(i) + s);
                }
            } else if (keys.contains("PREPEND")) {
                List<Object> parameters = parsedTokens.get("PREPEND");
                String s = (String) parameters.get(0);
                for (int i = 0; i < fileList.size(); i++) {
                    fileList.set(i, s + fileList.get(i));
                }
            } else if (keys.contains("DELETE")) {
                List<Object> parameters = parsedTokens.get("DELETE");
                String s = (String) parameters.get(0);
                for (int i = 0; i < fileList.size(); i++) {
                    fileList.set(i, fileList.get(i).replaceFirst(Pattern.quote(s), ""));
                }
            } else if (keys.contains("CHANGE")) {
                List<Object> parameters = parsedTokens.get("CHANGE");
                if (parameters.size() != 2) {
                    log.info("Incorrect number of parameters: {}", parameters.size());
                } else {
                    String s1 = (String) parameters.get(0);
                    String s2 = (String) parameters.get(1);
                    for (int i = 0; i < fileList.size(); i++) {
                        fileList.set(i, fileList.get(i).replaceFirst(Pattern.quote(s1), s2));
                    }
                }
            }

        }

    }

    @Override
    public void execute() {
        List<SacTraceData> traceData = SacDataModel.getInstance().getData();
        if (fileList == null || fileList.isEmpty()) {
            log.error("No list of filenames to write provided, nothing to do.");
            return;
        }
        if (fileList.size() != traceData.size()) {
            log.error("A different number of file names ({}) was provided that files in memory ({}), aborting.", fileList.size(), traceData.size());
            return;
        }
        for (int i = 0; i < traceData.size(); i++) {
            String filePath;
            String s1 = fileList.get(i);
            String s2 = DriveMapper.getInstance().maybeMapPath(s1);
            Path path = Paths.get(s2);
            if (path.isAbsolute()) {
                if (path.getParent().toFile().exists()) {
                    filePath = path.toString();
                } else {
                    log.info("Directory: {} does not exist! skipping...", path.getParent().toString());
                    continue;
                }
            } else {
                Path apath = directoryList.get(i);
                File file = apath.toFile();
                if (file.exists()) {
                    filePath = directoryList.get(i).resolve(path).normalize().toString();
                } else {
                    log.info("Directory: {} does not exist! skipping...", file.toString());
                    continue;
                }
            }

            log.info("Writing {}", filePath);
            filePath = DriveMapper.getInstance().maybeMapPath(filePath);
            try {
                SacTraceData td = traceData.get(i);
                if (dataEncoding == SACDataEncoding.BINARY) {
                    SACFileWriter writer = SACFileWriter.fromStringPath(filePath);

                    SACHeader header = td.getSACHeader();
                    header.setNpts(0);
                    writer.setHeader(header);
                    if (td.getData() != null) {
                        writer.write(td.getData());
                    } else if (td.getSpectralData() != null) {
                        writer.write(td.getSpectralData().getRealArray(), td.getSpectralData().getImagArray(), FileType.IRLIM);
                    } else {
                        log.error("Attempting to write SAC file {} but both data and spectral data arrays are empty.", filePath);
                    }

                    writer.close();
                } else {
                    SACFile sac = td.getSacFile();
                    SACAlphaWriter.writeFile(sac, Paths.get(filePath));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"W", "WRITE"};
        return new ArrayList<>(Arrays.asList(names));
    }

}
