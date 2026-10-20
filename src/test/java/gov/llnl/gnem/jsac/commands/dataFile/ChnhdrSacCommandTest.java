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
package gov.llnl.gnem.jsac.commands.dataFile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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
import gov.llnl.gnem.jsac.io.enums.Body;
import gov.llnl.gnem.jsac.io.enums.DataQuality;
import gov.llnl.gnem.jsac.io.enums.DepVarType;
import gov.llnl.gnem.jsac.io.enums.EventType;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.io.enums.MagSource;
import gov.llnl.gnem.jsac.io.enums.MagType;
import gov.llnl.gnem.jsac.io.enums.SyntheticsType;
import llnl.gnem.core.io.SAC.Iztype;
import llnl.gnem.core.util.TimeT;

public class ChnhdrSacCommandTest {

    private static final String TEST_FILE_DIRECTORY = "gov/llnl/gnem/jsac/commands/dataFile/";

 

    /**
     * Test of execute method, of class ChnhdrSacCommand changing one header
     * value for two out of four files.
     */
    @Test
    public void testExecuteChangeOneVarTwoFiles() {
        System.out.println("testExecuteChangeOneVarTwoFiles");

        load4Files();
        String cmdString = "ch file 2 4 KEVNM 'LA goes under'";
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        String value = lstd.get(0).getSACHeader().getKevnm();
        Assertions.assertFalse(value.equals("LA goes under"));
        value = lstd.get(1).getSACHeader().getKevnm();
        Assertions.assertTrue(value.equals("LA goes under"));
        value = lstd.get(2).getSACHeader().getKevnm();
        Assertions.assertFalse(value.equals("LA goes under"));
        value = lstd.get(3).getSACHeader().getKevnm();
        Assertions.assertTrue(value.equals("LA goes under"));
    }

    /**
     * Test of execute method, of class ChnhdrSacCommand un-defining one header
     * value.
     */
    @Test
    public void testExecuteUndef1() {
        System.out.println("testExecuteUndef1");
        loadOneFile();
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData std = lstd.get(0);
        std.getSACHeader().setA(10.0);
        String cmdString = "ch A UNDEF";
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Double v = std.getSACHeader().getA();
        Assertions.assertTrue(v == null);
    }

    /**
     * Test of execute method, of class ChnhdrSacCommand using GMT option.
     */
    @Test
    public void testExecuteAllT() {
        System.out.println("testExecuteAllT");
        loadOneFile();

        // Start with no reftime and header O not set...
        TimeT refTime = new TimeT(1982, 123, 13, 37, 10, 103);
        String cmdString = "CHNHDR O GMT 1982 123 13 37 10 103";
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData std = lstd.get(0);
        std.getSACHeader().setT(0, 10.0);

        cmdString = "ch allt 5";
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());

        Double v = std.getSACHeader().getB();
        Assertions.assertEquals(v, 5.0, 0.001);

        v = std.getSACHeader().getO();
        Assertions.assertEquals(v, 5.0, 0.001);
        v = std.getSACHeader().getE();
        Assertions.assertEquals(v, 14.99, 0.001);
        v = std.getSACHeader().getT(0);
        Assertions.assertEquals(v, 15, 0.001);

        v = std.getSACHeader().getReferenceTime();
        Assertions.assertEquals(v, refTime.getEpochTime() - 5, 0.001);
    }

    /**
     * Test of execute method, of class ChnhdrSacCommand using GMT option.
     */
    @Test
    public void testExecutesetRefTime() {
        System.out.println("testExecutesetRefTime");
        loadOneFile();

        // Start with no reftime and header O not set...
        TimeT refTime = new TimeT(1982, 123, 13, 37, 10, 103);
        String cmdString = "CHNHDR O GMT 1982 123 13 37 10 103";
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SacTraceData std = lstd.get(0);
        Double v = std.getSACHeader().getReferenceTime();
        Assertions.assertEquals(v, refTime.getEpochTime(), 0.001);

        // Now set T0 to 10 relative to the current reference time.
        // also reset the reference time of the origin to 10 seconds later.
        std.getSACHeader().setT(0, 10.0);
        cmdString = "CHNHDR O GMT 1982 123 13 37 20 103";
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());

        v = std.getSACHeader().getReferenceTime();
        Assertions.assertEquals(v, refTime.getEpochTime() + 10, 0.001);

        v = std.getSACHeader().getT(0);
        Assertions.assertEquals(v, 0.0, 0.001);
    }

    /**
     * Test of execute method, of class ChnhdrSacCommand changing every header
     * value.
     */
    @Test
    public void testExecuteChangeAll() {
        System.out.println("testExecuteChangeAll");
        loadOneFile();
        List<SacTraceData> lstd = SacDataModel.getInstance().getData();
        SACHeader header = lstd.get(0).getSACHeader();
        List<String> fields = new ArrayList<>(SACHeader.getSettableFields());
        Collections.sort(fields);
        double testDouble = 22.0;
        int testInt = 42;
        String testString = "testStr";
        Body testBody = Body.IEARTH;
        DepVarType testDepVarType = DepVarType.IACC;
        EventType testEventType = EventType.ICHEM;
        FileType testFileType = FileType.IAMPH;
        MagSource testMagSource = MagSource.IBRK;
        MagType testMagType = MagType.IMB;
        DataQuality testDataQuality = DataQuality.IDROP;
        SyntheticsType testSyntheticsType = SyntheticsType.IRLDTA;
        Iztype testIztype = Iztype.IA;
        for (String field : fields) {
            Type type = SACHeader.getTypeForField(field);
            String typeName = type.getTypeName();
            if (typeName.equals("java.lang.Double")) {
                testChangeSingleDouble(field, testDouble, header);
            } else if (typeName.contains("Body")) {
                testChangeSingleBody(field, testBody, header);
            } else if (typeName.contains("DepVarType")) {
                testChangeSingleDepVarType(field, testDepVarType, header);
            } else if (typeName.equals("java.lang.Integer")) {
                testChangleSingleInt(field, testInt, header);
            } else if (typeName.equals("java.lang.String")) {
                testChangeSingleString(field, testString, header);
            } else if (typeName.contains("EventType")) {
                testChangeEventType(field, testEventType, header);
            } else if (typeName.contains("FileType")) {
                testChangeFileType(field, testFileType, header);
            } else if (typeName.contains("MagSource")) {
                testChangeMagSource(field, testMagSource, header);
            } else if (typeName.contains("MagType")) {
                testChangeMagType(field, testMagType, header);
            } else if (typeName.contains("DataQuality")) {
                testChangeDataQuality(field, testDataQuality, header);
            } else if (typeName.contains("SyntheticsType")) {
                testChangeSyntheticsType(field, testSyntheticsType, header);
            } else if (typeName.contains("Iztype")) {
                testChangeIzType(field, testIztype, header);
            } else {
                Assertions.fail("Unexpected field: " + field + "!");
            }
        }
    }

    private void testChangeIzType(String field, Iztype testIztype, SACHeader header) {
        String cmdString = "ch " + field + " " + testIztype.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof Iztype) {
            Iztype dvt = (Iztype) obj;
            Assertions.assertTrue(dvt == Iztype.IA);
        }
    }

    private void testChangeSyntheticsType(String field, SyntheticsType testSyntheticsType, SACHeader header) {
        String cmdString = "ch " + field + " " + testSyntheticsType.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof SyntheticsType) {
            SyntheticsType dvt = (SyntheticsType) obj;
            Assertions.assertTrue(dvt == SyntheticsType.IRLDTA);
        }
    }

    private void testChangeDataQuality(String field, DataQuality testDataQuality, SACHeader header) {
        String cmdString = "ch " + field + " " + testDataQuality.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof DataQuality) {
            DataQuality dvt = (DataQuality) obj;
            Assertions.assertTrue(dvt == DataQuality.IDROP);
        }
    }

    private void testChangeMagType(String field, MagType testMagType, SACHeader header) {
        String cmdString = "ch " + field + " " + testMagType.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof MagType) {
            MagType dvt = (MagType) obj;
            Assertions.assertTrue(dvt == MagType.IMB);
        }
    }

    private void testChangeMagSource(String field, MagSource testMagSource, SACHeader header) {
        String cmdString = "ch " + field + " " + testMagSource.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof MagSource) {
            MagSource dvt = (MagSource) obj;
            Assertions.assertTrue(dvt == MagSource.IBRK);
        }
    }

    private void testChangeFileType(String field, FileType testFileType, SACHeader header) {
        String cmdString = "ch " + field + " " + testFileType.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof FileType) {
            FileType dvt = (FileType) obj;
            Assertions.assertTrue(dvt == FileType.IAMPH);
        }
    }

    private void testChangeEventType(String field, EventType testEventType, SACHeader header) {
        String cmdString = "ch " + field + " " + testEventType.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof EventType) {
            EventType dvt = (EventType) obj;
            Assertions.assertTrue(dvt == EventType.ICHEM);
        }
    }

    private void testChangeSingleString(String field, String testString, SACHeader header) {
        String cmdString = "ch " + field + " " + testString;
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof String) {
            String v = (String) obj;
            Assertions.assertTrue(v.equals(testString));
        }
    }

    private void testChangleSingleInt(String field, int testInt, SACHeader header) {
        String cmdString = "ch " + field + " " + testInt;
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof Integer) {
            Integer v = (Integer) obj;
            Assertions.assertTrue(v == testInt);
        }
    }

    private void testChangeSingleDepVarType(String field, DepVarType testDepVarType, SACHeader header) {
        String cmdString = "ch " + field + " " + testDepVarType.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof DepVarType) {
            DepVarType dvt = (DepVarType) obj;
            Assertions.assertTrue(dvt == testDepVarType);
        }
    }

    private void testChangeSingleBody(String field, Body testBody, SACHeader header) {
        String cmdString = "ch " + field + " " + testBody.name();
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof Body) {
            Body body = (Body) obj;
            Assertions.assertTrue(body == testBody);
        }
    }

    private void testChangeSingleDouble(String field, double testDouble, SACHeader header) {
        String cmdString = "ch " + field + " " + testDouble;
        TestUtil.runCommand(cmdString, new ChnhdrSacCommand());
        Object obj = header.getValue(field);
        if (obj == null) {
            Assertions.fail("Got null header value!");
        }
        if (obj instanceof Double) {
            Double v = (Double) obj;
            Assertions.assertEquals(testDouble, v, 0.001, field);
        }
    }

    private void load4Files() {
        String testSacFile = "file1.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
        testSacFile = "file2.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        testSacFile = "file3.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);
        testSacFile = "file4.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, false);

    }

    private void loadOneFile() {
        String testSacFile = "file1.sac";
        TestUtil.loadTestSacFile(testSacFile, TEST_FILE_DIRECTORY, true);
    }

}
