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

public class RMSSacCommandTest {

    private static final String TEST_FILE_DIRECTORY_1 = "gov/llnl/gnem/jsac/commands/transfer/";

    public RMSSacCommandTest() {
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
    public void testExecuteSignalOnly() {
        System.out.println("execute RMS signal-only");

        // Load the test file...
        String testSacFile = "fgseis.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY_1, true);

        // Then create a measurement window...
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW A 0 1";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);

        // execute the rms command...
        RMSSacCommand instance2 = new RMSSacCommand();
        cmd = "rms to user4";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance2.initialize(tokens);
        instance2.execute();

        List<SacTraceData> traces = SacDataModel.getInstance().getData();

        SacTraceData std = traces.get(0);

        double value = std.getSACHeader().getUser(4);
        Assertions.assertEquals(4.542869e-01, value, 0.001);
    }

    @Test
    public void testExecuteNoiseAndSignal() {
        System.out.println("execute RMS noise and signal");

        // Load the test file...
        String testSacFile = "fgseis.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY_1, true);

        // Then create a measurement window...
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW A 0 1";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);

        // execute the rms command...
        RMSSacCommand instance2 = new RMSSacCommand();
        cmd = "rms noise a -.5 0 to user5";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance2.initialize(tokens);
        instance2.execute();

        List<SacTraceData> traces = SacDataModel.getInstance().getData();

        SacTraceData std = traces.get(0);

        double value = std.getSACHeader().getUser(5);
        Assertions.assertEquals(4.420342e-01, value, 0.001);
    }

    @Test
    public void testExecuteNoiseTurnedOff() {
        System.out.println("execute RMS noise turned off");

        // Load the test file...
        String testSacFile = "fgseis.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY_1, true);

        // Then create a measurement window...
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW A 0 1";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);

        // execute the rms command...
        RMSSacCommand instance2 = new RMSSacCommand();
        cmd = "rms noise a -.5 0 to user5";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance2.initialize(tokens);
        instance2.execute();

        instance2 = new RMSSacCommand();
        cmd = "rms noise off";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance2.initialize(tokens);
        instance2.execute();

        List<SacTraceData> traces = SacDataModel.getInstance().getData();

        SacTraceData std = traces.get(0);

        double value = std.getSACHeader().getUser(5);
        Assertions.assertEquals(4.542869e-01, value, 0.001);
    }

}
