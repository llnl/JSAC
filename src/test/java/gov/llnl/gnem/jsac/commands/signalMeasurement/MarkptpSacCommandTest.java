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
package gov.llnl.gnem.jsac.commands.signalMeasurement;

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

public class MarkptpSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/transfer/";

    public MarkptpSacCommandTest() {
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
     * Test of execute method, of class MarkptpSacCommand.
     */
    @Test
    public void testExecute() {
        System.out.println("Test markptp (MTW A -1 5) L 3. TO T7");

        // Load the test file...
        String testSacFile = "fgseis.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        List<SacTraceData> traces = SacDataModel.getInstance().getData();

        SacTraceData std = traces.get(0);

        // Then create a measurement window...
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW A -1 5";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);

        MarkptpSacCommand mPTPcmd = new MarkptpSacCommand();
        cmd = "L 3. TO T7";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        mPTPcmd.initialize(tokens);
        mPTPcmd.execute();

        double ptpmin = std.getSACHeader().getT(7);
        double ptpmax = std.getSACHeader().getT(8);
        String minStr = std.getSACHeader().getKt(7);
        String maxStr = std.getSACHeader().getKt(8);
        double ptpAmp = std.getSACHeader().getUser(0);
        String ptpStr = std.getSACHeader().getKuser0();
        Assertions.assertEquals(12.67, ptpmin, 0.01);
        Assertions.assertEquals(12.55, ptpmax, 0.01);
        Assertions.assertEquals(3.0899, ptpAmp, 0.01);

        Assertions.assertEquals("PTPMIN", minStr);
        Assertions.assertEquals("PTPMAX", maxStr);
        Assertions.assertEquals("PTPAMP", ptpStr);
    }

}
