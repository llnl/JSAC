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

package gov.llnl.gnem.jsac.commands.unary;

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

public class DifSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/unary/";

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
    public void testDif() {
        compareCommand("difTruth.sac", "dif", "testDifOldSac");
    }

    @Test
    public void testDif2() {
        compareCommand("dif2truth.sac", "dif two", "testDif2OldSac");
    }

    @Test
    public void testDif3() {
        compareCommand("dif3truth.sac", "dif three", "testDif3OldSac");
    }

    @Test
    public void testDif5() {
        compareCommand("dif5truth.sac", "dif five", "testDif5OldSac");
    }

    private void compareCommand(String sacTruth, String cmdstring, String testName) {
        String testSacFile = "triangle.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        float tolerance = 0.001f;

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData result = lstd.get(0);

        SacTraceData truth = TestUtil.getTruth(sacTruth, TEST_FILE_DIRECTORY);

        TestUtil.runCommand(cmdstring, new DifSacCommand());

        TestUtil.compareTwoSacfileArraysExact(result, truth, testName, tolerance);

        //NPTS, B, E, DEPMIN, DEPMAX, DEPMEN
        SACHeader resultHeader = result.getSacFileHeader();
        SACHeader truthHeader = truth.getSacFileHeader();
        Assertions.assertEquals(truthHeader.getNpts(), resultHeader.getNpts());
        Assertions.assertEquals(truthHeader.getB(), resultHeader.getB(), tolerance);
        Assertions.assertEquals(truthHeader.getE(), resultHeader.getE(), tolerance);
        Assertions.assertEquals(truthHeader.getDepmin(), resultHeader.getDepmin(), tolerance);
        Assertions.assertEquals(truthHeader.getDepmax(), resultHeader.getDepmax(), tolerance);
        Assertions.assertEquals(truthHeader.getDepmen(), resultHeader.getDepmen(), tolerance);
    }
}
