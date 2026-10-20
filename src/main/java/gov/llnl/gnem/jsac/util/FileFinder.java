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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dodge1
 */
public class FileFinder {

    private final Path startingDir;
    private final String request;
    private final List<PathMatcher> matchers;
    private final List<Path> matches = new ArrayList<>();
    private final int numMatchers;

    public FileFinder(Path startingDir, String request) {
        this.startingDir = startingDir;
        this.request = request;
        matchers = getMatchers(request);
        numMatchers = matchers.size();
    }

    public List<Path> findMatches() {
        matches.clear();
        if (request == null) {
            try {
                Files.list(startingDir).forEach(this::addPath);
            } catch (IOException ex) {
                Logger.getLogger(FileFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                Path maybeAbsolutePath = Paths.get(request);
                if (maybeAbsolutePath.toFile().isDirectory() && maybeAbsolutePath.isAbsolute()) {
                    Files.list(maybeAbsolutePath).forEach(p -> matches.add(p));
                } else if (maybeAbsolutePath.isAbsolute()) {
                    process(maybeAbsolutePath.getParent(), numMatchers - 1);
                } else {
                    process(startingDir, 0);
                }
            } catch (InvalidPathException | IOException ex) {
                process(startingDir, 0);
            }
        }
        return matches;
    }

    private static List<PathMatcher> getMatchers(String request) {
        List<PathMatcher> tmp = new ArrayList<>();
        if (request != null && !request.isEmpty()) {
            StringTokenizer st = new StringTokenizer(request, "\\,/");

            while (st.hasMoreTokens()) {
                PathMatcher p = FileSystems.getDefault().getPathMatcher("glob:" + st.nextToken());
                tmp.add(p);
            }
        }
        return tmp;
    }

    private void addPath(Path path) {
        matches.add(path);
    }

    private void process(Path path, int level) {
        PathMatcher pm = matchers.get(level);
        try {
            if (level == numMatchers - 1) {
                Files.list(path).forEach(p -> maybeAddPath(p, pm));
            } else if (level < numMatchers - 1) {
                Files.list(path).forEach(p -> maybeProcessDirectory(p, pm, level + 1));
            }
        } catch (IOException ex) {
            Logger.getLogger(FileFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void maybeAddPath(Path path, PathMatcher pm) {
        if (pm.matches(path.getFileName())) {
            matches.add(path);
        }
    }

    private void maybeProcessDirectory(Path p, PathMatcher pm, int level) {
        if (p.toFile().isDirectory() && pm.matches(p.getFileName())) {
            process(p, level);
        }
    }

}
