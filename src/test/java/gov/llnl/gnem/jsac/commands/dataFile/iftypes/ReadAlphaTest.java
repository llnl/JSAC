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
package gov.llnl.gnem.jsac.commands.dataFile.iftypes;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.commands.dataFile.ReadAlphaSacCommand;
import gov.llnl.gnem.jsac.commands.fileSystem.CdSacCommand;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.enums.FileType;

/**
 *
 * @author dodge1
 */
public class ReadAlphaTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/iftypes/";

    public ReadAlphaTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        SacDataModel.getInstance().clear();
    }

    @AfterAll
    protected static void tearDownAfterClass() throws Exception {
        SacDataModel.getInstance().clear();
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    public void testReadIxy() {
        System.out.println("ReadIXY");
        TestUtil.runCommand("cd src/test/resources/" + TEST_FILE_DIRECTORY, new CdSacCommand());
        TestUtil.runCommand("read alpha itime.uneven.txt", new ReadAlphaSacCommand());
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        int count = traces.size();
        Assertions.assertEquals(1, count);

        SacTraceData std = traces.get(0);
        FileType type = std.getSACHeader().getIftype();
        Assertions.assertTrue(type == FileType.IXY);

        Assertions.assertTrue(std.getSACHeader().getLeven() == 0);

        SacPlotData spd = std.getPlotData();
        float[] xData = spd.getxValues();

        float[] xTruth = { 0.000000F, 0.010000F, 0.040000F, 0.070000F, 0.140000F, 0.150000F, 0.250000F, 0.270000F, 0.280000F, 0.290000F, 0.400000F, 0.510000F, 0.520000F, 0.630000F, 0.640000F,
                0.750000F, 0.760000F, 0.870000F, 0.880000F, 0.990000F };
        Assertions.assertArrayEquals(xTruth, xData);

        float[] yData = spd.getYValues();
        float[] yTruth = { 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 1.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F,
                0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F };

        Assertions.assertArrayEquals(yTruth, yData);
    }

    @Test
    public void testReadItimeEven() {
        System.out.println("ReadItimeUneven");
        TestUtil.runCommand("cd src/test/resources/" + TEST_FILE_DIRECTORY, new CdSacCommand());
        TestUtil.runCommand("read alpha itime.even.txt", new ReadAlphaSacCommand());
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        int count = traces.size();
        Assertions.assertEquals(1, count);

        SacTraceData std = traces.get(0);
        FileType type = std.getSACHeader().getIftype();
        Assertions.assertTrue(type == FileType.ITIME);

        Assertions.assertTrue(std.getSACHeader().getLeven() == 1);

        SacPlotData spd = std.getPlotData();

        float[] yData = spd.getYValues();
        float[] yTruth = { 0.000000F, 0.000000F, 0.000000F, 0.000000F, 1.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F, 0.000000F };

        Assertions.assertArrayEquals(yTruth, yData);
    }

    @Test
    public void testReadAmplPhase() {
        System.out.println("ReadAmplPhase");
        TestUtil.runCommand("cd src/test/resources/" + TEST_FILE_DIRECTORY, new CdSacCommand());
        TestUtil.runCommand("read alpha impulse_amp_phase.txt", new ReadAlphaSacCommand());
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        int count = traces.size();
        Assertions.assertEquals(1, count);

        SacTraceData std = traces.get(0);
        FileType type = std.getSACHeader().getIftype();
        Assertions.assertTrue(type == FileType.IAMPH);

        double delta = std.getDelta();
        Assertions.assertEquals(6.25, delta, 0.001);
        double b = std.getSACHeader().getB();
        Assertions.assertEquals(0.0, b, 0.001);

        float[] data = std.getSpectralData().getAmplitudeArray();

        float[] truth = { 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F, 0.01000000F,
                0.01000000F, 0.01000000F, 0.01000000F };
        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(truth[j], data[j], 0.00001);
        }

        data = std.getSpectralData().getPhaseArray();
        float[] truth2 = { 0.000000F, -1.570796F, 3.141593F, 1.570796F, 0.000000F, -1.570796F, 3.141593F, 1.570796F, 0.000000F, -1.570796F, -3.141593F, 1.570796F, -0.000000F, -1.570796F, -3.141593F,
                1.570796F };

        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(Math.cos(truth2[j]), Math.cos(data[j]), 0.00001);
        }

    }

    @Test
    public void testReadRealImag() {
        System.out.println("ReadRealImag");
        TestUtil.runCommand("cd src/test/resources/" + TEST_FILE_DIRECTORY, new CdSacCommand());
        TestUtil.runCommand("read alpha realImag.txt", new ReadAlphaSacCommand());
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        int count = traces.size();
        Assertions.assertEquals(1, count);

        SacTraceData std = traces.get(0);
        FileType type = std.getSACHeader().getIftype();
        Assertions.assertTrue(type == FileType.IRLIM);

        double delta = std.getDelta();
        Assertions.assertEquals(3.125000, delta, 0.001);
        double b = std.getSACHeader().getB();
        Assertions.assertEquals(0.0, b, 0.001);

        float[] data = std.getSpectralData().getRealArray();

        float[] truth = { 0.01000000F, -0.001950903F, -0.009238794F, 0.005555702F, 0.007071068F, -0.008314696F, -0.003826834F, 0.009807852F, 2.220446e-18F, -0.009807852F, 0.003826834F, 0.008314696F,
                -0.007071068F, -0.005555702F, 0.009238794F, 0.001950903F, -0.01000000F, 0.001950903F, 0.009238794F, -0.005555702F, -0.007071068F, 0.008314696F, 0.003826834F, -0.009807852F,
                2.220446e-18F, 0.009807852F, -0.003826834F, -0.008314696F, 0.007071068F, 0.005555702F, -0.009238794F, -0.001950903F };
        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(truth[j], data[j], 0.00001);
        }

        data = std.getSpectralData().getImagArray();
        float[] truth2 = { 0.000000F, -0.009807852F, 0.003826834F, 0.008314696F, -0.007071068F, -0.005555702F, 0.009238794F, 0.001950903F, -0.01000000F, 0.001950903F, 0.009238794F, -0.005555702F,
                -0.007071068F, 0.008314696F, 0.003826834F, -0.009807852F, 0.000000F, 0.009807852F, -0.003826834F, -0.008314696F, 0.007071068F, 0.005555702F, -0.009238794F, -0.001950903F, 0.01000000F,
                -0.001950903F, -0.009238794F, 0.005555702F, 0.007071068F, -0.008314696F, -0.003826834F, 0.009807852F };

        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(Math.cos(truth2[j]), Math.cos(data[j]), 0.00001);
        }

    }
}
