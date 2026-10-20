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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import llnl.gnem.core.util.FileUtil.DriveMapper;

/**
 *
 * @author dodge1
 */
public class PathManager {

    private Path currentDir;

    private PathManager() {
        currentDir = Paths.get("").toAbsolutePath();
    }

    public static PathManager getInstance() {
        return PathManagerHolder.INSTANCE;
    }

    public Path makeAbsolute(Path relativePath) {
        return currentDir.resolve(relativePath).normalize();
    }

    public Path resolvePath(String pathString) {
        String mapped = DriveMapper.getInstance().maybeMapPath(pathString);
        Path apath = Paths.get(mapped);
        if (apath.isAbsolute()) {
            return apath;
        } else {
            return makeAbsolute(apath);
        }
    }

    public Path resolvePath(Path base, String pathString) {
        String tmp = DriveMapper.getInstance().maybeMapPath(pathString);
        Path apath = Paths.get(tmp);
        if (apath.isAbsolute()) {
            return apath;
        } else {
            return base.resolve(pathString).normalize();
        }
    }

    public Path resolveAndValidateFile(String filename) {

        String tmp = DriveMapper.getInstance().maybeMapPath(filename);
        Path apath = Paths.get(tmp);
        if (apath.isAbsolute()) {
            return validateFile(apath, filename);
        } else {
            apath = this.resolvePath(tmp);
            return validateFile(apath, filename);
        }
    }

    private static Path validateFile(Path apath, String suppliedFileName) throws IllegalStateException {
        File file = apath.toFile();
        if (file.exists()) {
            if (file.isFile()) {
                return apath;
            } else {
                throw new IllegalStateException("File " + suppliedFileName + " is not a file! ");
            }
        } else {
            throw new IllegalStateException("Error: " + suppliedFileName + " does not exist!");
        }
    }

    private static class PathManagerHolder {

        private static final PathManager INSTANCE = new PathManager();
    }

    public Path getCurrentDir() {
        return currentDir;
    }

    public void changeDirectory(String unmapped) {

        Path newPath = Paths.get(DriveMapper.getInstance().maybeMapPath(unmapped));
        if (newPath.isAbsolute()) {
            File file = newPath.toFile();
            if (!file.exists()) {
                throw new IllegalStateException("Path (" + newPath + ") does not exist!");
            } else if (file.isFile()) {
                throw new IllegalStateException("Path (" + newPath + ") is not a directory!");
            } else {
                currentDir = newPath.normalize();
            }
        } else {
            Path apath = currentDir.resolve(newPath);
            File file = apath.toFile();
            if (!file.exists()) {
                throw new IllegalStateException("Path (" + apath + ") does not exist!");
            } else if (file.isFile()) {
                throw new IllegalStateException("Path (" + apath + ") is not a directory!");
            } else {
                currentDir = apath.normalize();
            }
        }
    }

}
