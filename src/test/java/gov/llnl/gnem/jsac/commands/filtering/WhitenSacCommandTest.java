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
package gov.llnl.gnem.jsac.commands.filtering;

import java.util.Arrays;
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

public class WhitenSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/filtering/";

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
    public void defaultWhiten() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.0001f;
        int order = 6;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        final int npts = test.getSacFileHeader().getNpts();
        final Double depMax = test.getSacFileHeader().getDepmax();
        final Double depMen = test.getSacFileHeader().getDepmen();
        final Double depMin = test.getSacFileHeader().getDepmin();

        TestUtil.runCommand("whiten", new WhitenSacCommand());

        SacTraceData truth = TestUtil.getTruth("whitenTruth.sac", TEST_FILE_DIRECTORY);

        //New whitening drops N samples off the front of the series so we need to clip
        //the same amount off the old SAC trace to compare them.
        truth.setData(Arrays.copyOfRange(truth.getData(), order, truth.getData().length));

        TestUtil.compareTwoSacfileArraysExact(truth, test, "defaultWhiten", tolerance);
        Assertions.assertNotEquals(npts, test.getSacFileHeader().getNpts(), "Expected npts header to be changed.");
        Assertions.assertNotEquals(depMin, test.getSacFileHeader().getDepmin(), "Expected depmin header to be changed.");
        Assertions.assertNotEquals(depMen, test.getSacFileHeader().getDepmen(), "Expected depmen header to be changed.");
        Assertions.assertNotEquals(depMax, test.getSacFileHeader().getDepmax(), "Expected depmax header to be changed.");
    }

    @Test
    public void whitenCommon() {
        //Common is new so this is just a pure regression test.
        TestUtil.loadTestSacFile("funcgenSeismogram.sac", TestUtil.SAC_TEST_FILES_DIRECTORY, true);
        TestUtil.loadTestSacFile("impulse.sac", TestUtil.SAC_TEST_FILES_DIRECTORY, false);
        TestUtil.loadTestSacFile("random.sac", TestUtil.SAC_TEST_FILES_DIRECTORY, false);

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("whiten common", new WhitenSacCommand());

        SacTraceData truth = TestUtil.getTruth("whitenCommonTruth.sac", TEST_FILE_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(truth, test, "whitenCommon", 0.001f);
    }
}
