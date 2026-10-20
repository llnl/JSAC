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
package gov.llnl.gnem.jsac.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACAlphaReader;
import gov.llnl.gnem.jsac.io.SACDataEncoding;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACFileReader;
import gov.llnl.gnem.jsac.util.PathManager;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.util.FileUtil.DriveMapper;

/**
 *
 * @author dodge1
 */
public class Misc {

    private static final Logger log = LoggerFactory.getLogger(Misc.class);
    private static final String FORMAT_7 = "yyyyDDD";
    private static final String FORMAT_10 = "yyyy/MM/dd";
    private static final String FORMAT_17 = "yyyy/DDD:HH:mm:ss";
    private static final String FORMAT_19 = "yyyy/MM/dd:HH:mm:ss";
    private static final String FORMAT_23 = "yyyy/MM/dd-HH:mm:ss.SSS";

    public static double parseTimeString(String string) {
        try {
            switch (string.length()) {
                case 7: { //yyyyddd
                    TimeT tmp = new TimeT(string, FORMAT_7);
                    return tmp.getEpochTime();
                }
                case 10: {// yyyy/MM/dd
                    TimeT tmp = new TimeT(string, FORMAT_10);
                    return tmp.getEpochTime();
                }
                case 17: {// yyyy/DDD:HH:mm:ss
                    TimeT tmp = new TimeT(string, FORMAT_17);
                    return tmp.getEpochTime();
                }
                case 19: {// yyyy/MM/dd:HH:mm:ss
                    TimeT tmp = new TimeT(string, FORMAT_19);
                    return tmp.getEpochTime();
                }
                case 23: {
                    TimeT tmp = new TimeT(string, FORMAT_23);
                    return tmp.getEpochTime();
                }
                default:
                    break;
            }
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Unrecognized time string: " + string + "!");
        }
        throw new IllegalArgumentException("Unrecognized time string: " + string + "!");
    }

    public static String listTimeFormats() {
        StringBuilder sb = new StringBuilder("Available Time Formats:\n");
        sb.append("\t" + FORMAT_7 + "\n");
        sb.append("\t" + FORMAT_10 + "\n");
        sb.append("\t" + FORMAT_17 + "\n");
        sb.append("\t" + FORMAT_19 + "\n");
        sb.append("\t" + FORMAT_23 + "\n");
        return sb.toString();
    }

    public static void buildAndProcessList(List<String> filenames, SacTraceData.BinOpType opType, boolean replaceHeaders) {
        List<SacTraceData> addList = buildBinOpList(filenames);
        int maxAddIdex = addList.size() - 1;
        List<SacTraceData> data = SacDataModel.getInstance().getData();

        for (int j = 0; j < data.size(); ++j) {
            SacTraceData target = data.get(j);
            int k = j <= maxAddIdex ? j : maxAddIdex;
            SacTraceData source = addList.get(k);
            target.applyBinOp(source, replaceHeaders, opType);
        }
    }

    private static List<SacTraceData> buildBinOpList(List<String> filenames) {
        List<SacTraceData> addList = new ArrayList<>();
        for (String fname : filenames) {
            String mappedFileName = DriveMapper.getInstance().maybeMapPath(fname);
            Path path = Paths.get(mappedFileName);
            if (!path.isAbsolute()) {
                path = PathManager.getInstance().resolvePath(mappedFileName);
            }
            File file = path.toFile();
            if (!file.exists()) {
                System.out.println("File: " + fname + " not found! skipping...");
            } else {
                try {
                    SACFile sac = SACFileReader.readFile(path);
                    SacTraceData std = new SacTraceData(path, sac);
                    addList.add(std);
                } catch (IOException ex) {
                    System.out.println("Failed reading file: " + fname + "! skipping...");
                }
            }
        }
        return addList;
    }

    public static SacTraceData createSacFile(Path file, SACDataEncoding dataEncoding) {
        try {
            switch (dataEncoding) {
                case BINARY: {
                    SACFile sac = SACFileReader.readFile(file);
                    return new SacTraceData(file, sac);
                }
                case ALPHANUMERIC: {
                    SACFile sac = SACAlphaReader.readFile(file);
                    return new SacTraceData(file, sac);
                }
                default:
                    throw new IllegalStateException("Unknown data encoding: " + dataEncoding);
            }

        } catch (IOException ex) {
            log.warn("Failed reading: {}", file.toString());
            return null;
        }
    }
}
