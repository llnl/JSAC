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
package gov.llnl.gnem.jsac.transfer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gov.llnl.gnem.jsac.util.PathManager;
import gov.llnl.gnem.response.FreqLimits;
import gov.llnl.gnem.response.ResponseType;
import gov.llnl.gnem.response.ResponseTypeChecker;
import llnl.gnem.core.util.FileUtil.DriveMapper;

/**
 *
 * @author dodge1
 */
public class TransferSubOptionParser {

    public static FreqLimits maybeGetFreqLimits(Map<String, List<Object>> parsedTokens) {
        List<Object> parameters = parsedTokens.get("FREQLIMITS");
        if (parameters == null) {
            parameters = parsedTokens.get("FREQ");
        }
        if (parameters != null && parameters.size() == 4) {
            double v1 = (Double) parameters.get(0);
            double v2 = (Double) parameters.get(1);
            double v3 = (Double) parameters.get(2);
            double v4 = (Double) parameters.get(3);
            return new FreqLimits(v1, v2, v3, v4);
        }
        return null;
    }

    public static ResponseOptions getOptions(List<Object> values) {
        String filename = null;
        Path searchDir = maybeGetSearchDir(values);
        if (values.size() < 2) { //Search current directory forPZ files
            return new ResponseOptions(null,filename, searchDir);
        }
        ResponseType type = null;
        filename = maybeGetSubtype(values);
        if (filename != null) {
             type = ResponseTypeChecker.determineResponseType(filename);
        }
        return new ResponseOptions(type, filename, searchDir);
    }

    public static ResponseOptions getToOptions(List<Object> values) {
        Path searchDir = maybeGetSearchDir(values);
        String filename = maybeGetSubtype(values);
        if (filename == null) {
            throw new IllegalStateException("File name must be specified!");
        }
        return new ResponseOptions(null,filename, searchDir);
    }

    public static String maybeGetSubtype(List<Object> values) {
        Path searchDir = PathManager.getInstance().getCurrentDir();
        List<Integer> indices = new ArrayList<>();
        for (int j = 1; j < values.size(); ++j) {
            String tmp = (String) values.get(j);
            if (tmp.equalsIgnoreCase("DIR")) {
                indices.add(j);
                if (j < values.size() - 1) { // value following subtype...
                    tmp = (String) values.get(j + 1);
                    indices.add(j + 1);
                    Path apath = PathManager.getInstance().resolvePath(tmp);
                    searchDir = validateDirectory(apath, searchDir, tmp);
                }
            }
            if (tmp.equalsIgnoreCase("S") || tmp.equalsIgnoreCase("SUBTYPE")) {
                indices.add(j);
                if (j < values.size() - 1) { // value following subtype...
                    tmp = (String) values.get(j + 1);
                    indices.add(j + 1);
                    Path apath = PathManager.getInstance().resolvePath(searchDir, tmp);
                    return validateFile(apath, tmp);
                } else {
                    throw new IllegalStateException("SUBTYPE must be followed by a file name!");
                }
            }
        }
        Collections.sort(indices, Collections.reverseOrder());
        for (int val : indices) {
            values.remove(val);
        }
        return null;
    }

    private static String validateFile(Path apath, String suppliedFileName) throws IllegalStateException {
        File file = apath.toFile();
        File foo = new File(DriveMapper.getInstance().maybeMapPath(file.getAbsolutePath()));
        if (foo.exists()) {
            if (foo.isFile()) {
                return apath.toString();
            } else {
                throw new IllegalStateException("File " + file.toString() + " is not a file! ");
            }
        } else {
            throw new IllegalStateException("Error: " + suppliedFileName + " does not exist!");
        }
    }

    private static Path validateDirectory(Path apath, Path searchDir, String tmp) throws IllegalStateException {
        File file = apath.toFile();
        File foo = new File(DriveMapper.getInstance().maybeMapPath(file.getAbsolutePath()));
        if (foo.exists()) {
            if (foo.isDirectory()) {
                searchDir = apath;
            } else {
                throw new IllegalStateException("File " + file.toString() + " is not a directory! ");
            }
        } else {
            throw new IllegalStateException("Error: Directory " + tmp + " does not exist!");
        }
        return searchDir;
    }

    private static Path maybeGetSearchDir(List<Object> values) {
        Path result = PathManager.getInstance().getCurrentDir();
        List<Integer> indices = new ArrayList<>();
        for (int j = 1; j < values.size(); ++j) {
            String tmp = (String) values.get(j);
            if (tmp.equalsIgnoreCase("DIR")) {
                indices.add(j);
                if (j < values.size() - 1) { // value following subtype...
                    indices.add(j + 1);
                    tmp = (String) values.get(j + 1);
                    result = PathManager.getInstance().resolvePath(tmp);
                    File file = result.toFile();
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            return result;
                        } else {
                            throw new IllegalStateException(file.toString() + " is not a directory! ");
                        }
                    } else {
                        throw new IllegalStateException("Error: " + file.getAbsolutePath() + " does not exist!");
                    }
                } else {
                    throw new IllegalStateException("DIR option was specified but no directory name was supplied!");
                }
            }
        }
        Collections.sort(indices, Collections.reverseOrder());
        for (int val : indices) {
            values.remove(val);
        }
        return result;
    }

}
