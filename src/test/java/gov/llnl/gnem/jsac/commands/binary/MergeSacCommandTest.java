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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;

/**
 *
 * @author dodge1
 */
public class MergeSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/merge/";

    public MergeSacCommandTest() {
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
    public void testMergeWithOverlap() {
        System.out.println("MergeWithOverlap");
        String testSacFile = "RIYD.BHE.0_300.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "RIYD.BHE.290_600.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        String cmdstring = String.format("merge");
        TestUtil.runCommand(cmdstring, new MergeSacCommand());

        SacTraceData merged = SacDataModel.getInstance().getData().get(0);

        String sacTruth = "merge1.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(merged, truth, "MergeWithOverlap", 0.001f);
    }

    @Test
    public void testMergeWithContiguousSegments() {
        System.out.println("MergeWithContiguousSegments");
        String testSacFile = "RIYD.BHE.610_1000.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "RIYD.BHE.1000_1500.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        String cmdstring = String.format("merge");
        TestUtil.runCommand(cmdstring, new MergeSacCommand());

        SacTraceData merged = SacDataModel.getInstance().getData().get(0);

        String sacTruth = "merge2.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(merged, truth, "MergeWithContiguousSegments", 0.001f);
    }

    @Test
    public void testMergeWithZeroFilledGap() {
        System.out.println("MergeWithZeroFilledGap");
        String testSacFile = "RIYD.BHE.290_600.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "RIYD.BHE.610_1000.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        String cmdstring = String.format("merge");
        TestUtil.runCommand(cmdstring, new MergeSacCommand());

        SacTraceData merged = SacDataModel.getInstance().getData().get(0);

        String sacTruth = "merge3.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        TestUtil.compareTwoSacfileArraysExact(merged, truth, "MergeWithZeroFilledGap", 0.001f);
    }

}
