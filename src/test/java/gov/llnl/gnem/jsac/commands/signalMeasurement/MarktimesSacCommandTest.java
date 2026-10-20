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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;

public class MarktimesSacCommandTest {

    private static final Logger log = LoggerFactory.getLogger(MarktimesSacCommandTest.class);

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/signalMeasurement/";

    @BeforeEach
    public void setUp() {
        SacDataModel.getInstance().clear();
    }

    @AfterEach
    public void tearDown() {
        SacDataModel.getInstance().clear();
    }

    @Test
    public void testMarkTimes() {
        double tolerance = 0.01;
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 2. 3. 4. 5. 6.  DISTANCE HEADER ORIGIN HEADER TO T0", new MarktimesSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        SacTraceData truth = TestUtil.getTruth("marktimesDefaultTruth.sac", TEST_FILE_DIRECTORY);

        SACHeader testHeader = test.getSACHeader();
        SACHeader truthHeader = truth.getSACHeader();
        compareMarktimesHeaders(tolerance, testHeader, truthHeader);
    }

    @Test
    public void testDistanceSpecified() {
        double tolerance = 0.01;
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 2. 3. 4. 5. 6.  DISTANCE 102.3 ORIGIN HEADER TO T0", new MarktimesSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        SacTraceData truth = TestUtil.getTruth("marktimesDistTruth.sac", TEST_FILE_DIRECTORY);

        SACHeader testHeader = test.getSACHeader();
        SACHeader truthHeader = truth.getSACHeader();
        compareMarktimesHeaders(tolerance, testHeader, truthHeader);
    }

    @Test
    public void testOriginSpecified() {
        double tolerance = 0.01;
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 2. 3. 4. 5. 6. DISTANCE HEADER ORIGIN 5.1 TO T0", new MarktimesSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        SacTraceData truth = TestUtil.getTruth("marktimesVelTruth.sac", TEST_FILE_DIRECTORY);

        SACHeader testHeader = test.getSACHeader();
        SACHeader truthHeader = truth.getSACHeader();
        compareMarktimesHeaders(tolerance, testHeader, truthHeader);
    }

    @Test
    public void testGMTSpecified() {
        double tolerance = 0.01;
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 2. 3. 4. 5. 6.  DISTANCE HEADER ORIGIN GMT 1981 88 11 03 45 10 TO T0", new MarktimesSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        SacTraceData truth = TestUtil.getTruth("marktimesGMTTruth.sac", TEST_FILE_DIRECTORY);

        SACHeader testHeader = test.getSACHeader();
        SACHeader truthHeader = truth.getSACHeader();
        compareMarktimesHeaders(tolerance, testHeader, truthHeader);
    }

    @Test
    public void testUTCSpecified() {
        double tolerance = 0.01;
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 2. 3. 4. 5. 6.  DISTANCE HEADER ORIGIN UTC 1981 88 11 03 45 10 TO T0", new MarktimesSacCommand());

        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData test = lstd.get(0);

        SacTraceData truth = TestUtil.getTruth("marktimesGMTTruth.sac", TEST_FILE_DIRECTORY);

        SACHeader testHeader = test.getSACHeader();
        SACHeader truthHeader = truth.getSACHeader();
        compareMarktimesHeaders(tolerance, testHeader, truthHeader);
    }

    @Test
    public void testTooManyVelocties() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 0. 1. 2. 3. 4. 5. 6. 7. 8. 9. 10. 11. 12. DISTANCE HEADER ORIGIN HEADER TO T0", new MarktimesSacCommand());
        SacTraceData test = SacDataModel.getInstance().getData().get(0);
        String[] testValues = test.getSACHeader().getKt();
        for (String testVal : testValues) {
            Assertions.assertNotNull(testVal, "Expect all 10 entries to be non-null");
        }
    }

    @Test
    public void testToNonZero() {
        String testSacFile = "funcgenSeismogram.sac";
        TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

        TestUtil.runCommand("MARKTIMES VELOCITIES 99. DISTANCE HEADER ORIGIN HEADER TO T9", new MarktimesSacCommand());
        SacTraceData test = SacDataModel.getInstance().getData().get(0);
        String[] testValues = test.getSACHeader().getKt();
        Assertions.assertEquals(99d, Double.valueOf(testValues[9]), "Expected last header value is set");
    }

    @Test
    public void testUnderspecifiedGMT() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

            TestUtil.runCommand("MARKTIMES VELOCITIES 2. DISTANCE HEADER ORIGIN UTC 1981 88 11 03 45 TO T0", new MarktimesSacCommand());
        });
    }

    @Test
    public void testInvalidDistance() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

            TestUtil.runCommand("MARKTIMES VELOCITIES 2. DISTANCE -1 ORIGIN HEADER TO T0", new MarktimesSacCommand());
        });
    }

    @Test
    public void testInvalidDistanceType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

            TestUtil.runCommand("MARKTIMES VELOCITIES 2. DISTANCE UNK ORIGIN HEADER TO T0", new MarktimesSacCommand());
        });
    }

    @Test
    public void testInvalidTime() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

            TestUtil.runCommand("MARKTIMES VELOCITIES 2. DISTANCE HEADER ORIGIN UNK TO T0", new MarktimesSacCommand());
        });
    }

    @Test
    public void testToNonexistant() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            String testSacFile = "funcgenSeismogram.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

            TestUtil.runCommand("MARKTIMES VELOCITIES 99. DISTANCE HEADER ORIGIN HEADER TO T11", new MarktimesSacCommand());
        });
    }

    @Test
    public void testNoHeaderDistance() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            String testSacFile = "triangle.sac";
            TestUtil.loadTestSacFile(testSacFile, TestUtil.SAC_TEST_FILES_DIRECTORY, true);

            TestUtil.runCommand("MARKTIMES VELOCITIES 0. 1. 2. 3. 4. 5. 6. 7. 8. 9. 10. 11. 12. DISTANCE HEADER ORIGIN HEADER TO T0", new MarktimesSacCommand());
        });
    }

    private void compareMarktimesHeaders(double tolerance, SACHeader testHeader, SACHeader truthHeader) {

        //Old SAC did not trim whitespace on strings so we need to clean that up before we compare
        for (int i = 0; i < truthHeader.getKt().length; i++) {
            truthHeader.setKt(i, StringUtils.trim(truthHeader.getKt(i)));
        }
        Assertions.assertArrayEquals(truthHeader.getKt(), testHeader.getKt(), "Expect KT values to match");
        for (int i = 0; i < testHeader.getT().length; i++) {
            Double truthT = truthHeader.getT(i);
            if (truthT != null) {
                Double testT = testHeader.getT(i);
                if (testT != null) {
                    Assertions.assertEquals(truthT.doubleValue(), testT.doubleValue(), tolerance, "Expected T" + i + " values to match.");
                } else {
                    Assertions.fail("Expected test value at index " + i + " but was [null]");
                }
            }
        }
    }
}
