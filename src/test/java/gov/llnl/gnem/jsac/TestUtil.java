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
package gov.llnl.gnem.jsac;

import java.io.File;
import java.util.List;
import java.net.URL;

import org.junit.jupiter.api.Assertions;

import gov.llnl.gnem.jsac.commands.Misc;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.io.SACDataEncoding;

/**
 *
 * @author dodge1
 */
public class TestUtil {

    public static final String SAC_TEST_FILES_DIRECTORY = "gov/llnl/gnem/jsac/commands/";

    public static double compareArraysNormalizedMeanAbsoluteError(float[] data, float[] data2) {
        int N = Math.min(data.length, data2.length);
        double cumulativeError = 0;
        double dataMin = Double.MAX_VALUE;
        double dataMax = -dataMin;
        for (int j = 0; j < N; ++j) {
            if (data[j] < dataMin) {
                dataMin = data[j];
            }
            if (data[j] > dataMax) {
                dataMax = data[j];
            }
            double diff = Math.abs(data[j] - data2[j]);
            cumulativeError += diff;
        }
        double range = dataMax - dataMin;
        if (range > 0) {
            double averageError = cumulativeError / N;
            return averageError / range;
        } else {
            throw new IllegalStateException("Data samples are all zero!");
        }
    }

    public static void loadTestSacFile(String sacFileName, String testFileDirectory, boolean clear) {
        loadFilePrivate(sacFileName, testFileDirectory, SACDataEncoding.BINARY, clear);
    }

    public static void loadTestSacAlphaFile(String sacFileName, String testFileDirectory) {
        loadFilePrivate(sacFileName, testFileDirectory, SACDataEncoding.ALPHANUMERIC, true);
    }

    private static void loadFilePrivate(String sacFileName, String testFileDirectory, SACDataEncoding encoding, boolean clear) {
        if (clear) {
            SacDataModel.getInstance().clear();
        }
        System.out.println("SAC File name: "+sacFileName);
        System.out.println("Diectory name: "+testFileDirectory);
        List<SacTraceData> results = SacDataModel.getInstance().getData();
        ClassLoader classLoader = SacDataModel.getInstance().getClass().getClassLoader();
        System.out.println("ClassLoader: " + classLoader);
        System.out.println("Getting file...");
        File file = getResourceFile(classLoader, testFileDirectory, sacFileName);
        SacTraceData std = Misc.createSacFile(file.toPath(), encoding);
        results.add(std);
        SacDataModel.getInstance().clear();
        SacDataModel.getInstance().addAll(results, 0.0);
    }

    public static File getResourceFile(ClassLoader classLoader, String testFileDirectory, String sacFileName) {
        String resourceName = String.format("%s%s", testFileDirectory, sacFileName);
        System.out.println("ResourceName = " + resourceName);
        URL url = classLoader.getResource(resourceName);
        System.out.println("URL = " +url);
        File file = new File(url.getFile());
        return file;
    }

    public static File getResourceFile(String testFileDirectory, String sacFileName) {
        ClassLoader classLoader = SacDataModel.getInstance().getClass().getClassLoader();
        File file = new File(classLoader.getResource(String.format("%s%s", testFileDirectory, sacFileName)).getFile());
        return file;
    }

    public static SacTraceData getTruth(String sacTruth, String testFileDirectory) {
        ClassLoader classLoader = SacDataModel.getInstance().getClass().getClassLoader();
        File file = new File(classLoader.getResource(testFileDirectory + sacTruth).getFile());
        SacTraceData truth = Misc.createSacFile(file.toPath(), SACDataEncoding.BINARY);
        return truth;
    }

    public static SacTraceData getTruthAlpha(String sacTruth, String testFileDirectory) {
        ClassLoader classLoader = SacDataModel.getInstance().getClass().getClassLoader();
        File file = new File(classLoader.getResource(testFileDirectory + sacTruth).getFile());
        SacTraceData truth = Misc.createSacFile(file.toPath(), SACDataEncoding.ALPHANUMERIC);
        return truth;
    }

    public static void runCommand(String cmdString, SacCommand command) {
        String[] tokens = TokenListParser.tokenizeString(cmdString);
        command.initialize(tokens);
        command.execute();
    }

    public static void weightedCompareArrays(SacTraceData result, SacTraceData truth, String testName, double tolerance) {
        float[] testValues = result.getData();
        float[] truthValues = truth.getData();

        double resultArray = TestUtil.compareArraysNormalizedMeanAbsoluteError(testValues, truthValues);
        Assertions.assertEquals(0, resultArray, tolerance, testName);
    }

    public static void compareTwoSacfileArraysExact(SacTraceData result, SacTraceData truth, String testName, float tolerance) {
        Assertions.assertEquals(truth.getData().length, result.getData().length, testName + "SameLength");
        Assertions.assertArrayEquals(truth.getData(), result.getData(), tolerance);
    }

    public static void compareSpectralDataExact(SpectralData truth, SpectralData test, float tolerance) {
        Assertions.assertEquals(truth.getSize(), test.getSize(), "Expect frequency counts to match");
        float maxdiff = 0f;
        float diff = 0f;
        for (int i = 0; i < truth.getSize(); i++) {
            diff = Math.abs(truth.getRealArray()[i] - test.getRealArray()[i]);
            diff = Math.max(diff, Math.abs(truth.getImagArray()[i] - test.getImagArray()[i]));
            maxdiff = Math.max(maxdiff, diff);
        }
        Assertions.assertTrue(maxdiff < tolerance, "Expected maximum difference in frequency arrays to be below tolerence.");
    }

}
