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
package gov.llnl.gnem.jsac.commands.instrumentCorrection;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import llnl.gnem.core.util.SeriesMath;

/**
 *
 * @author dodge1
 */
public class TransferSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/transfer/";

    public TransferSacCommandTest() {
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
    public void testTransferDbaseEvalresp() {
        System.out.println("TransferDbaseEvalresp");
        SacDataModel.getInstance().clear();
        ClassLoader classLoader = getClass().getClassLoader();
        String testSacFile = "trans_dbase_evresp_input.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        File file = new File(classLoader.getResource(TEST_FILE_DIRECTORY + "1993.033.21.MHC.BHZ.--.6178620.resp").getFile());
        String command = String.format("transfer from evalresp s %s freqlimits 0.05 0.1 8 9", file.getAbsolutePath());

        runTransfer(command);
        SacTraceData transferred = SacDataModel.getInstance().getData().get(0);
        String sacTruth = "MHC_BHZ_evresp.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        float[] testValues = transferred.getData();
        float[] truthValues = truth.getData();

        double result = TestUtil.compareArraysNormalizedMeanAbsoluteError(testValues, truthValues);
        Assertions.assertEquals(0, result, 0.001, "TransferDbaseEvalresp");
    }

    @Test
    public void testTransferPoleZero() {
        System.out.println("TransferPoleZero");
        SacDataModel.getInstance().clear();
        ClassLoader classLoader = getClass().getClassLoader();
        String testSacFile = "impulse_delta_0.01_npts_10000.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        File file = new File(classLoader.getResource(TEST_FILE_DIRECTORY + "SAC_PZs_IU_YAK_BHZ_00_.pzf").getFile());
        String command = String.format("transfer from polezero subtype %s freqlimits 0.05 0.1 9 10", file.getAbsolutePath());
        runTransfer(command);
        SacTraceData transferred = SacDataModel.getInstance().getData().get(0);
        String sacTruth = "impulse_sacpzf.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        float[] testValues = transferred.getData();
        float[] truthValues = truth.getData();

        double result = TestUtil.compareArraysNormalizedMeanAbsoluteError(testValues, truthValues);
        Assertions.assertEquals(0, result, 0.001, "TransferPoleZero");

    }

    @Test
    public void testTransferToVel() {
        System.out.println("TransferToVel");
        SacDataModel.getInstance().clear();
        ClassLoader classLoader = getClass().getClassLoader();
        String testSacFile = "trans_test_to_vel_input.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        File file = new File(classLoader.getResource(TEST_FILE_DIRECTORY + "2013.148.00.YAK.BHZ.00.6733538.resp").getFile());
        String command = String.format("transfer from evalresp s %s freq 0.01 0.05 8 9 to vel", file.getAbsolutePath());
        runTransfer(command);
        SacTraceData transferred = SacDataModel.getInstance().getData().get(0);

        String sacTruth = "trans_test_to_vel_truth.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        truth.removeTrend();

        float[] testValues = transferred.getData();
        float[] truthValues = truth.getData();

        double result = TestUtil.compareArraysNormalizedMeanAbsoluteError(testValues, truthValues);
        Assertions.assertEquals(0, result, 0.001, "TransferToVel");

    }

    @Test
    public void testTransferToAcc() {
        System.out.println("TransferToAcc");
        SacDataModel.getInstance().clear();
        ClassLoader classLoader = getClass().getClassLoader();
        String testSacFile = "trans_test_to_vel_input.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        File file = new File(classLoader.getResource(TEST_FILE_DIRECTORY + "2013.148.00.YAK.BHZ.00.6733538.resp").getFile());
        String command = String.format("transfer from evalresp s %s freq 0.01 0.05 8 9 to acc", file.getAbsolutePath());
        runTransfer(command);
        SacTraceData transferred = SacDataModel.getInstance().getData().get(0);

        String sacTruth = "trans_test_to_acc_truth.sac";
        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);
        truth.removeTrend();

        float[] testValues = transferred.getData();
        SeriesMath.Taper(testValues, 1);
        float[] truthValues = truth.getData();
        SeriesMath.Taper(truthValues, 1);

        double result = TestUtil.compareArraysNormalizedMeanAbsoluteError(testValues, truthValues);
        Assertions.assertEquals(0, result, 0.001, "TransferToAcc");

    }

    private void runTransfer(String command) {
        String[] tokens = command.split("(\\s+)|(\\s*,\\s*)");

        TransferSacCommand instance = new TransferSacCommand();
        TransferSacCommand.resetDefaults();
        instance.initialize(tokens);
        instance.execute();

    }

}
