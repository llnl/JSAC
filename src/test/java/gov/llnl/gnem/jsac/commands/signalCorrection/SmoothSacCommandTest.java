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
package gov.llnl.gnem.jsac.commands.signalCorrection;

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

public class SmoothSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/signalCorrection/";

    @BeforeAll
    public static void setUpClass() {
        SacDataModel.getInstance().clear();
    }

    @AfterAll
    protected static void tearDownAfterClass() throws Exception {
        SacDataModel.getInstance().clear();
    }

    @BeforeEach
    protected void setUp() throws Exception {
        SacDataModel.getInstance().clear();
    }

    @AfterEach
    protected void tearDown() throws Exception {
    }

    @Test
    public void defaultSmooth() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.002f;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("smooth h 1", new SmoothSacCommand());

        SacTraceData truth = TestUtil.getTruth("smoothTruth.sac", TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(truth, test, "defaultSmooth", tolerance);
    }

    @Test
    public void smoothHalfwidth20() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.1f;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("smooth halfwidth 20", new SmoothSacCommand());

        SacTraceData truth = TestUtil.getTruth("smoothHalfwidth20Truth.sac", TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(truth, test, "smoothHalfwidth20", tolerance);
    }

    @Test
    public void smoothH20() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.1f;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("smooth h 20", new SmoothSacCommand());

        SacTraceData truth = TestUtil.getTruth("smoothHalfwidth20Truth.sac", TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(truth, test, "smoothHalfwidth20", tolerance);
    }

    @Test
    public void defaultUnderspecifiedH() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);
            TestUtil.runCommand("smooth H", new SmoothSacCommand());
        });
    }

    @Test
    public void defaultUnderspecifiedHalfwidth() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);
            TestUtil.runCommand("smooth halfwidth", new SmoothSacCommand());
        });
    }
}
