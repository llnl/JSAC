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
package gov.llnl.gnem.jsac.commands.binary;

import java.io.File;
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
import gov.llnl.gnem.jsac.io.SACHeader;

public class MulfSacCommandTest {
    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/binary/";

    public MulfSacCommandTest() {
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

    /**
     * Test of execute method, of class MulfSacCommand. Case tested here is two
     * files in memory, two multiplied newhdr off
     */
    @Test
    public void testExecuteRead2Mul2() {
        System.out.println("testExecuteRead2Mul2NewHdrOff");
        String testSacFile = "file1.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        SACHeader savedHdr = new SACHeader(SacDataModel.getInstance().getData().get(0).getSACHeader());
        testSacFile = "file2.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);

        testSacFile = "file3.sac";
        File file3 = TestUtil.getResourceFile(TEST_FILE_DIRECTORY, testSacFile);

        testSacFile = "file4.sac";
        File file4 = TestUtil.getResourceFile(TEST_FILE_DIRECTORY, testSacFile);

        String cmdstring = String.format("mulf newhdr off %s %s", file3.getAbsolutePath(), file4.getAbsolutePath());
        TestUtil.runCommand(cmdstring, new MulfSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        float tolerance = 0.001f;

        SacTraceData result = lstd.get(0);

        SACHeader compare = result.getSACHeader();
        compareHeaders(compare, savedHdr);

        String sacTruth = "mulf2p2OneTruth.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead2Mul2NewHdrOffFile1", tolerance);

        result = lstd.get(1);
        sacTruth = "mulf2p2TwoTruth.sac";
        truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead2Mul2NewHdrOffFile2", tolerance);
    }

    /**
     * Test of execute method, of class MulfSacCommand. Case tested here is
     * three files in memory, one multiplied newhdr off
     */
    @Test
    public void testExecuteRead3Mul1NewHdrOff() {
        System.out.println("testExecuteRead3Mul1NewHdrOff");
        String testSacFile = "file1.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        SACHeader savedHdr = new SACHeader(SacDataModel.getInstance().getData().get(0).getSACHeader());
        testSacFile = "file2.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        testSacFile = "file3.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);

        testSacFile = "file4.sac";
        File file = TestUtil.getResourceFile(TEST_FILE_DIRECTORY, testSacFile);

        String cmdstring = String.format("mulf %s", file.getAbsolutePath());
        TestUtil.runCommand(cmdstring, new MulfSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        float tolerance = 0.001f;

        SacTraceData result = lstd.get(0);

        SACHeader compare = result.getSACHeader();
        compareHeaders(compare, savedHdr);

        String sacTruth = "mulf3p1OneTruth.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead3Mul1NewHdrOffFile1", tolerance);

        result = lstd.get(1);
        sacTruth = "mulf3p1TwoTruth.sac";
        truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead3Mul1NewHdrOffFile2", tolerance);

        result = lstd.get(2);
        sacTruth = "mulf3p1ThreeTruth.sac";
        truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead3Mul1NewHdrOffFile2", tolerance);
    }

    /**
     * Test of execute method, of class MulfSacCommand. Case tested here is
     * three files in memory, one multiplied newhdr on
     */
    @Test
    public void testExecuteRead3Mul1NewHdr() {
        System.out.println("testExecuteRead3Mul1NewHdr");
        String testSacFile = "file1.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "file2.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        testSacFile = "file3.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);

        testSacFile = "file4.sac";
        File file = TestUtil.getResourceFile(TEST_FILE_DIRECTORY, testSacFile);

        String cmdstring = String.format("mulf newhdr on %s", file.getAbsolutePath());
        TestUtil.runCommand(cmdstring, new MulfSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        float tolerance = 0.001f;

        SacTraceData result = lstd.get(0);

        // Headers should match that of file that was argument to addf
        SACHeader compare = result.getSACHeader();
        SacTraceData lastArgFile = TestUtil.getTruth(testSacFile, TEST_FILE_DIRECTORY);
        compareHeaders(compare, lastArgFile.getSACHeader());

        String sacTruth = "mulf3p1OneTruth.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead3Mul1File1", tolerance);

        result = lstd.get(1);
        sacTruth = "mulf3p1TwoTruth.sac";
        truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead3Mul1File2", tolerance);

        result = lstd.get(2);
        sacTruth = "mulf3p1ThreeTruth.sac";
        truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        TestUtil.compareTwoSacfileArraysExact(result, truth, "testExecuteRead3Mul1File2", tolerance);
    }

    private void compareHeaders(SACHeader compare, SACHeader savedHdr) {
        //These three values must be updated to original because the addition has changed them.
        //The requirement for equality refers primarily to the file metadata.
        compare.setDepmin(savedHdr.getDepmin());
        compare.setDepmen(savedHdr.getDepmen());
        compare.setDepmax(savedHdr.getDepmax());
        Assertions.assertEquals(savedHdr, compare);
    }

}
