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
package gov.llnl.gnem.jsac.util;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import gov.llnl.gnem.jsac.io.SACDataEncoding;

/**
 *
 * @author dodge1
 */
public class FileUtil {

    public static String maybeGetRequestedDir(List<String> mytokens) {
        String requestedDir = null;
        if (mytokens.size() > 2 && (mytokens.get(0).toUpperCase().equals("DIR"))) {
            mytokens.remove(0);
            String tmp = mytokens.remove(0);
            if (tmp.toUpperCase().toUpperCase().equals("CURRENT")) {
                requestedDir = PathManager.getInstance().getCurrentDir().toString();
            } else {
                Path apath = PathManager.getInstance().resolvePath(tmp);
                if (apath.toFile().exists()) {
                    if (apath.toFile().isDirectory()) {
                        requestedDir = apath.toString();
                    } else {
                        throw new IllegalStateException("Requested directory: " + apath.toString() + " is not a directory!");
                    }
                } else {
                    throw new IllegalStateException("Requested directory: " + apath.toString() + " does not exist!");
                }
            }
        }
        return requestedDir;
    }

    public static boolean maybeGetMoreOption(List<String> mytokens) {
        boolean readmore = false;
        if (mytokens.size() > 1 && (mytokens.get(0).toUpperCase().equals("M") || mytokens.get(0).toUpperCase().equals("MORE"))) {
            readmore = true;
            mytokens.remove(0); // Don't need more option anymore.
        }
        return readmore;
    }

    public static SACDataEncoding maybeGetDataEncodingList(List<String> mytokens) {
        SACDataEncoding result = SACDataEncoding.BINARY;
        if (mytokens.size() > 1 && (mytokens.get(0).toUpperCase().equals("A") || mytokens.get(0).toUpperCase().equals("ALPHA"))) {
            result = SACDataEncoding.ALPHANUMERIC;
            mytokens.remove(0);
        }
        return result;
    }

    public static void processOnePath(PathMatcher pm, Path path, List<String> filesToRead) {
        if (pm.matches(path.getFileName())) {
            System.out.println(path);
            filesToRead.add(path.toAbsolutePath().toFile().getAbsolutePath());
        }
    }

}
