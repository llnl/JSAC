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
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package gov.llnl.gnem.jsac.dataAccess.dataObjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;

/**
 *
 * @author dodge1
 */
public class SpectralDataTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/transfer/";
    private final SpectralData data;

    public SpectralDataTest() {
        String testSacFile = "fgseis.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        SacTraceData std = traces.get(0);
        std.FFT(SpectralData.PresentationFormat.RealImaginary);
        data = std.getSpectralData();
    }

    /**
     * Test of getDelfreq method, of class SpectralData.
     */
    @Test
    public void testGetDelfreq() {
        System.out.println("getDelfreq");
        double result = data.getDelfreq();
        assertEquals(0.09765625218278734, result, 0.0001);
    }

    /**
     * Test of getPresentationFormat method, of class SpectralData.
     */
    @Test
    public void testGetPresentationFormat() {
        System.out.println("getPresentationFormat");
        SpectralData.PresentationFormat expResult = PresentationFormat.RealImaginary;
        SpectralData.PresentationFormat result = data.getPresentationFormat();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOriginalNsamps method, of class SpectralData.
     */
    @Test
    public void testGetOriginalNsamps() {
        System.out.println("getOriginalNsamps");
        int expResult = 1000;
        int result = data.getOriginalNsamps();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMinFreq method, of class SpectralData.
     */
    @Test
    public void testGetMinFreq() {
        System.out.println("getMinFreq");
        Double result = data.getMinFreq();
        assertEquals(0.0, result, 0.0001);
    }

    /**
     * Test of getMaxPositiveFreq method, of class SpectralData.
     */
    @Test
    public void testGetMaxPositiveFreq() {
        System.out.println("getMaxPositiveFreq");
        Double expResult = 50.0;
        Double result = data.getMaxPositiveFreq();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of getSize method, of class SpectralData.
     */
    @Test
    public void testGetSize() {
        System.out.println("getSize");
        Integer expResult = 1024;
        Integer result = data.getSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of divOmega method, of class SpectralData.
     */
    @Test
    public void testDivOmega() {
        System.out.println("divOmega");
        SpectralData instance = new SpectralData(data);
        instance.divOmega();
        float[] result = instance.getAmplitudeArray();
        float[] truth = { 0.0F, 0.03897842F, 0.02454503F, 0.02324592F, 0.01750568F, 0.01012869F, 0.007293990F, 0.005082931F, 0.0005311262F, 0.005222498F, 0.006218750F, 0.01277142F, 0.003175353F,
                0.005945439F, 0.005006340F, 0.009765876F, 0.005463592F, 0.004546863F, 0.002812956F, 0.004694250F };

        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(truth[j], result[j], 0.00001);
        }

    }

    /**
     * Test of mulOmega method, of class SpectralData.
     */
    @Test
    public void testMulOmega() {
        System.out.println("mulOmega");
        SpectralData instance = new SpectralData(data);
        instance.mulOmega();
        float[] result = instance.getAmplitudeArray();
        float[] truth = { 0.0F, 0.01467520F, 0.03696437F, 0.07876787F, 0.1054529F, 0.09533516F, 0.09886158F, 0.09377135F, 0.01279787F, 0.1592660F, 0.2341332F, 0.5818141F, 0.1721529F, 0.3782948F,
                0.3694334F, 0.8272818F, 0.5265965F, 0.4947313F, 0.3431371F, 0.6380184F };

        for (int j = 0; j < truth.length; ++j) {
            Assertions.assertEquals(truth[j], result[j], 0.00001);
        }

    }

}
