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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;

public class InterpolateSacCommandTest {

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
    public void defaultInterpolate() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.01f;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        TestUtil.runCommand("INTERPOLATE DELTA 0.025", new InterpolateSacCommand());

        SacTraceData truth = TestUtil.getTruth("interpTruth.sac", TEST_FILE_DIRECTORY);

        //Account for rounding differences between old SAC
        float[] oldsacData = truth.getData();
        float[] truncatedData = new float[test.getData().length];
        for (int i = 0; i < truncatedData.length; i++) {
            truncatedData[i] = oldsacData[i];
        }
        truth.setData(truncatedData);

        TestUtil.compareTwoSacfileArraysExact(truth, test, "defaultInterpolate", tolerance);
    }
}
