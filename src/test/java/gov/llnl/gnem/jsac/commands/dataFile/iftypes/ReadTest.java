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
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacPlotData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.enums.FileType;

/**
 *
 * @author dodge1
 */
public class ReadTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/iftypes/";

    public ReadTest() {
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
        String testSacFile = "ixy.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
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
    public void testReadItimeUneven() {
        System.out.println("ReadItimeUneven");
        String testSacFile = "itime.uneven.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        int count = traces.size();
        Assertions.assertEquals(1, count);

        SacTraceData std = traces.get(0);
        FileType type = std.getSACHeader().getIftype();
        Assertions.assertTrue(type == FileType.ITIME);

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
    public void testReadAmplPhase() {
        System.out.println("ReadAmplPhase");
        String testSacFile = "amphPhase.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        int count = traces.size();
        Assertions.assertEquals(1, count);

        SacTraceData std = traces.get(0);
        FileType type = std.getSACHeader().getIftype();
        Assertions.assertTrue(type == FileType.IAMPH);

        double delta = std.getDelta();
        Assertions.assertEquals(0.09765625, delta, 0.001);
        double b = std.getSACHeader().getB();
        Assertions.assertEquals(0.0, b, 0.001);

        float[] data = std.getSpectralData().getAmplitudeArray();

        float[] truth = { 0.9854721F, 0.02391686F, 0.03012128F, 0.04279056F, 0.04296539F, 0.03107443F, 0.02685322F, 0.02183193F, 0.002607160F, 0.02884036F, 0.03815777F, 0.08620087F, 0.02338047F,
                0.04742498F, 0.04300592F, 0.08988398F, 0.05363869F, 0.04742863F, 0.03106814F, 0.05472676F };
        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(truth[j], data[j], 0.00001);
        }

        data = std.getSpectralData().getPhaseArray();
        float[] truth2 = { 3.141593F, -0.1997031F, 0.9014801F, 0.1655050F, 0.2800555F, 1.130296F, 0.5879732F, 0.6534695F, -0.7312780F, -1.295282F, 1.231905F, 0.2789430F, -1.625351F, 1.972141F,
                2.261039F, 0.6382821F, -2.360692F, 1.188696F, 2.589292F, 0.4712695F };

        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(Math.cos(truth2[j]), Math.cos(data[j]), 0.00001);
        }

    }

    //    @Test
    //    public void testReadRealImag() {
    //        System.out.println("ReadRealImag");
    //        String testSacFile = "realImag.sac";
    //        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY);
    //        List<SacTraceData> traces = SacDataModel.getInstance().getData();
    //        int count = traces.size();
    //        Assertions.assertEquals(1, count);
    //
    //        SacTraceData std = traces.get(0);
    //        FileType type = std.getSACHeader().getIftype();
    //        Assertions.assertTrue(type == FileType.IRLIM);
    //
    //        double delta = std.getDelta();
    //        Assertions.assertEquals(0.09765625, delta, 0.001);
    //        double b = std.getSACHeader().getB();
    //        Assertions.assertEquals(0.0, b, 0.001);
    //
    //        float[] data = std.getTransform().getRealArray();
    //
    //        float[] truth = {-0.9854721F, 0.02344152F, 0.01868875F, 0.04220583F, 0.04129146F,
    //            0.01324988F, 0.02234366F, 0.01733410F, 0.001940567F, 0.007845781F,
    //            0.01268522F, 0.08286895F, -0.001274876F, -0.01852689F, -0.02738292F,
    //            0.07218766F, -0.03809848F, 0.01768473F, -0.02644892F, 0.04876114F};
    //        for (int j = 0; j < truth.length; ++j) {
    //            Assertions.assertEquals(truth[j], data[j], 0.00001);
    //        }
    //
    //        data = std.getTransform().getPhaseArray();
    //        float[] truth2 = {3.141593F, -0.1997031F, 0.9014801F, 0.1655050F, 0.2800555F,
    //            1.130296F, 0.5879732F, 0.6534695F, -0.7312780F, -1.295282F,
    //            1.231905F, 0.2789430F, -1.625351F, 1.972141F, 2.261039F,
    //            0.6382821F, -2.360692F, 1.188696F, 2.589292F, 0.4712695F};
    //
    //        for (int j = 0; j < truth.length; ++j) {
    //            Assertions.assertEquals(Math.cos(truth2[j]), Math.cos(data[j]), 0.00001);
    //        }
    //
    //    }
}
