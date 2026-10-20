/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.commands.dataFile;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class SynchronizeSacCommandTest {
    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/dataFile/";
    


    @Test
    public void testExecuteExampleFromManual() {
        System.out.println("testExecuteExampleFromManual");

        loadFirstTwoFiles();
        String cmdString = "synchronize";
        TestUtil.runCommand(cmdString, new SynchronizeSacCommand());
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        String value = lstd.get(0).getSACHeader().getKztime();
        Assertions.assertTrue(value.equals("10:40:20.000"));
        Double beginTime = lstd.get(0).getSACHeader().getB();
        Assertions.assertEquals(beginTime, -126.0, 0.001);
        beginTime = lstd.get(1).getSACHeader().getB();
        Assertions.assertEquals(beginTime, 0.0, 0.001);
    }

 
    @Test
    public void testExecuteExampleFromManualWithBeginOn() {
        System.out.println("testExecuteExampleFromManual");

        loadFirstTwoFiles();
        String cmdString = "synchronize begin on";
        TestUtil.runCommand(cmdString, new SynchronizeSacCommand());
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        String value = lstd.get(0).getSACHeader().getKztime();
        Assertions.assertTrue(value.equals("10:38:14.000"));
        Double beginTime = lstd.get(0).getSACHeader().getB();
        Assertions.assertEquals(beginTime, 0.0, 0.001);
        beginTime = lstd.get(1).getSACHeader().getB();
        Assertions.assertEquals(beginTime, 0.0, 0.001);
    }
   
    private void loadFirstTwoFiles() {
        String testSacFile = "sync_test_file1.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "sync_test_file2.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
    }

}
