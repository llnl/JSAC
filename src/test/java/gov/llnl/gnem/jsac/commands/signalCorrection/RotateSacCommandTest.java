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
import gov.llnl.gnem.jsac.commands.dataFile.CutImSacCommand;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

public class RotateSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/rotate/";

    public RotateSacCommandTest() {
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
    public void testRotateThrough30() {
        System.out.println("rotate through 30");

        // Load the test file...
        String testSacFile = "RIYD.BHN.1997059125720";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "RIYD.BHE.1997059125720";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        PartialDataWindow pdw = CutImSacCommand.getPartialDataWindow();
        pdw.setStartReference("reftime");
        pdw.setEndReference("reftime");
        pdw.setStartOffset(0.0);
        pdw.setEndOffset(500.0);
        SacDataModel.getInstance().cutFilesInMemory();

        RotateSacCommand instance = new RotateSacCommand();
        String cmd = "rotate through 30";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        instance.execute();

        // Get the result
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        SacTraceData rotatedNorth = traces.get(0);
        SacTraceData rotatedEast = traces.get(1);

        // Now get results computed with old SAC
        String truthRotatedNorth = "RIYD.30.BHN.truth.sac";
        TestUtil.loadTestSacFile(truthRotatedNorth, TEST_FILE_DIRECTORY, true);
        traces = SacDataModel.getInstance().getData();
        SacTraceData truthNorth = traces.get(0);
        Assertions.assertArrayEquals(truthNorth.getData(), rotatedNorth.getData(), 0.5F);
        String truthRotatedEast = "RIYD.30.BHE.truth.sac";
        TestUtil.loadTestSacFile(truthRotatedEast, TEST_FILE_DIRECTORY, true);
        traces = SacDataModel.getInstance().getData();
        SacTraceData truthEast = traces.get(0);
        Assertions.assertArrayEquals(truthEast.getData(), rotatedEast.getData(), 0.5F);
    }

    @Test
    public void testRotateToGCP() {
        System.out.println("rotate to GCP");

        // Load the test file...
        String testSacFile = "RIYD.BHN.1997059125720";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "RIYD.BHE.1997059125720";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        PartialDataWindow pdw = CutImSacCommand.getPartialDataWindow();
        pdw.setStartReference("reftime");
        pdw.setEndReference("reftime");
        pdw.setStartOffset(0.0);
        pdw.setEndOffset(500.0);
        SacDataModel.getInstance().cutFilesInMemory();

        RotateSacCommand instance = new RotateSacCommand();
        String cmd = "rotate to GCP ";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        instance.execute();

        // Get the result
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        SacTraceData rotatedNorth = traces.get(0);
        SacTraceData rotatedEast = traces.get(1);

        // Now get results computed with old SAC
        String truthRotatedNorth = "RIYD.BHN.GCP.sac";
        TestUtil.loadTestSacFile(truthRotatedNorth, TEST_FILE_DIRECTORY, true);
        traces = SacDataModel.getInstance().getData();
        SacTraceData truthNorth = traces.get(0);
        Assertions.assertArrayEquals(truthNorth.getData(), rotatedNorth.getData(), 5F);
        String truthRotatedEast = "RIYD.BHE.GCP.sac";
        TestUtil.loadTestSacFile(truthRotatedEast, TEST_FILE_DIRECTORY, true);
        traces = SacDataModel.getInstance().getData();
        SacTraceData truthEast = traces.get(0);
        Assertions.assertArrayEquals(truthEast.getData(), rotatedEast.getData(), 5F);
    }

    @Test
    public void testRotateToGCPReversed() {
        System.out.println("rotate to GCP (Reversed)");

        // Load the test file...
        String testSacFile = "RIYD.BHN.1997059125720";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "RIYD.BHE.1997059125720";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        PartialDataWindow pdw = CutImSacCommand.getPartialDataWindow();
        pdw.setStartReference("reftime");
        pdw.setEndReference("reftime");
        pdw.setStartOffset(0.0);
        pdw.setEndOffset(500.0);
        SacDataModel.getInstance().cutFilesInMemory();

        RotateSacCommand instance = new RotateSacCommand();
        String cmd = "rotate to GCP reversed ";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        instance.execute();

        // Get the result
        List<SacTraceData> traces = SacDataModel.getInstance().getData();
        SacTraceData rotatedNorth = traces.get(0);
        SacTraceData rotatedEast = traces.get(1);

        // Now get results computed with old SAC
        String truthRotatedNorth = "RIYD.BHN.GCP.reversed.sac";
        TestUtil.loadTestSacFile(truthRotatedNorth, TEST_FILE_DIRECTORY, true);
        traces = SacDataModel.getInstance().getData();
        SacTraceData truthNorth = traces.get(0);
        Assertions.assertArrayEquals(truthNorth.getData(), rotatedNorth.getData(), 5F);
        String truthRotatedEast = "RIYD.BHE.GCP.reversed.sac";
        TestUtil.loadTestSacFile(truthRotatedEast, TEST_FILE_DIRECTORY, true);
        traces = SacDataModel.getInstance().getData();
        SacTraceData truthEast = traces.get(0);
        Assertions.assertArrayEquals(truthEast.getData(), rotatedEast.getData(), 5F);
    }

}
