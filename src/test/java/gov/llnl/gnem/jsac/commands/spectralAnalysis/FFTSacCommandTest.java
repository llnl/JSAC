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

package gov.llnl.gnem.jsac.commands.spectralAnalysis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.io.SACAlphaReader;
import gov.llnl.gnem.jsac.io.SACFile;

/**
 *
 * @author dodge1
 */
public class FFTSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/spectralAnalysis/";

    public FFTSacCommandTest() {
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
    public void testFFT() {
        System.out.println("testFFT");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.fft.am.sacalpha")) {
                    SacTraceData std = new SacTraceData(Paths.get("."), sac);
                    std.FFT(SpectralData.PresentationFormat.AmplitudePhase);
                    float[] jsac = std.getSpectralData().getPositiveFreqAmplitudeArray();

                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);
                    float[] data2 = std2.getData();

                    //We drop the zero frequency from the oldsac result since
                    // jsac is strictly positive here
                    float[] oldsac = new float[data2.length - 1];
                    System.arraycopy(data2, 1, oldsac, 0, oldsac.length);

                    Assertions.assertArrayEquals(jsac, oldsac, 0.001f, "testFFT");
                }
            }
        } catch (IOException ex) {

        }
    }

    @Test
    public void testFFTSpectral() {
        String testSacFile = "triangle.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.001f;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);
        TestUtil.runCommand("fft", new FFTSacCommand());

        SacTraceData truth = TestUtil.getTruth("fftTruth.sac", TEST_FILE_DIRECTORY);

        SpectralData jsac = test.getSpectralData();
        SpectralData oldsac = truth.getSpectralData();

        TestUtil.compareSpectralDataExact(oldsac, jsac, tolerance);
    }
}
