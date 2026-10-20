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
package gov.llnl.gnem.jsac.io;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llnl.gnem.core.util.FileSystemException;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.util.FileUtil.DriveMapper;
import llnl.gnem.core.util.FileUtil.FileManager;
import llnl.gnem.core.waveform.io.BinaryData;
import llnl.gnem.core.waveform.io.BinaryDataReader;
import llnl.gnem.core.waveform.io.e1IO;
import llnl.gnem.core.waveform.io.s4IO;

/**
 *
 * @author dodge1
 */
public class Waveform {

    private static final String dbdataGrp = "210";
    private static final String sepChar = "/"; //Specify unix separator. This must be used even if code is running on Windows.

    private static final Logger log = LoggerFactory.getLogger(Waveform.class);
    
    public static BinaryData getSeismogramData(String fname, int foff, int nsamp, String datatype) throws Exception {
        BinaryDataReader bdr = BinaryDataReader.getReader(datatype);
        if (bdr != null) {
            return bdr.readData(fname, foff, nsamp);
        } else {
            throw new IllegalStateException("No BinaryDataReader was instantiated. Could not read data.");
        }

    }

    public static String writeDfile(String filename, int[] data, int numSamples) throws IOException {

        String datatype = "e1";
        if (data.length < 10000) { // Don't compress small files.
            datatype = "s4";
            s4IO.writeIntData(filename, data);
        } else {
            boolean wroteE1;
            try {
                //write file, read it and compare data. Return true if read matches input.
                wroteE1 = e1IO.WriteIntData(data, numSamples, filename);
            } catch (Exception e) {
                wroteE1 = false;
                log.trace("Failed writing e1 data!", e);
            }
            if (!wroteE1) {
                datatype = "s4";
                s4IO.writeIntData(filename, data);
            }
        }
        String os = System.getProperty("os.name");
        if (!os.contains("Windows")) {
            FileManager.updateFilePermissions(filename, dbdataGrp);
        }
        return datatype;
    }

    public static String makeSegmentOutputDirectory(long evid, TimeT evtime, String baseDirectory) throws IOException {

        String subdir = String.format("%04d%s%02d%s%09d%s", evtime.getYear(), sepChar, evtime.getMonth(), sepChar, evid, sepChar);
        String targetDir = baseDirectory + sepChar + subdir;
        String mappedDir = DriveMapper.getInstance().maybeMapPath(targetDir);
        File directory = new File(mappedDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new FileSystemException(String.format("Failed creating directory (%s)!", targetDir));
        }
        String os = System.getProperty("os.name");
        if (!os.contains("Windows")) {
            FileManager.updateDirPermissions(baseDirectory, subdir, dbdataGrp);
        }

        return targetDir;
    }

}
