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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.util.PartialDataWindow;

/**
 *
 * @author dodge1
 */
public class MTWSacCommandTest {

    public MTWSacCommandTest() {
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
     * Test of execute method, of class MTWSacCommand.
     */
    @Test
    public void testOne() {
        System.out.println("MTW B 0 30");
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW B 0 30";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        PartialDataWindow pdw = instance.getPdw();
        Assertions.assertEquals("B", pdw.getStartReference());
        Assertions.assertEquals("B", pdw.getEndReference());
        Assertions.assertEquals(0.0, pdw.getStartOffset());
        Assertions.assertEquals(30.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

    @Test
    public void testTwo() {
        System.out.println("A -10 30");
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW A -10 30";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        PartialDataWindow pdw = instance.getPdw();
        Assertions.assertEquals("A", pdw.getStartReference());
        Assertions.assertEquals("A", pdw.getEndReference());
        Assertions.assertEquals(-10.0, pdw.getStartOffset());
        Assertions.assertEquals(30.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

    @Test
    public void testThree() {
        System.out.println("T3 -1 T7");
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW T3 -1 T7";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        PartialDataWindow pdw = instance.getPdw();
        Assertions.assertEquals("T3", pdw.getStartReference());
        Assertions.assertEquals("T7", pdw.getEndReference());
        Assertions.assertEquals(-1.0, pdw.getStartOffset());
        Assertions.assertEquals(0.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

    @Test
    public void testFour() {
        System.out.println("30.2 48");
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW 30.2 48";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        PartialDataWindow pdw = instance.getPdw();
        Assertions.assertEquals("REFTIME", pdw.getStartReference());
        Assertions.assertEquals("REFTIME", pdw.getEndReference());
        Assertions.assertEquals(30.2, pdw.getStartOffset());
        Assertions.assertEquals(48.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

    @Test
    public void testFive() {
        System.out.println("B N 2048");
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW B N 2048";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        PartialDataWindow pdw = instance.getPdw();
        Assertions.assertEquals("B", pdw.getStartReference());
        Assertions.assertEquals("E", pdw.getEndReference());
        Assertions.assertEquals(0.0, pdw.getStartOffset());
        Assertions.assertEquals(2048, pdw.getStartIntegerOffset());
        Assertions.assertEquals(0.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

    @Test
    public void testSix() {
        System.out.println("Test ON/OFF");
        MTWSacCommand instance = new MTWSacCommand();
        PartialDataWindow pdw = instance.getPdw();
        String cmd = "MTW 10 20";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        cmd = "MTW off";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);

        Assertions.assertFalse(pdw.isEnabled());
        cmd = "MTW on";
        tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);

        Assertions.assertEquals("REFTIME", pdw.getStartReference());
        Assertions.assertEquals("REFTIME", pdw.getEndReference());
        Assertions.assertEquals(10.0, pdw.getStartOffset());
        Assertions.assertEquals(20.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

    @Test
    public void testSeven() {
        System.out.println("MTW T4 T5");
        MTWSacCommand instance = new MTWSacCommand();
        String cmd = "MTW T4 T5";
        String[] tokens = cmd.split("(\\s+)|(\\s*,\\s*)");
        instance.initialize(tokens);
        PartialDataWindow pdw = instance.getPdw();
        Assertions.assertEquals("T4", pdw.getStartReference());
        Assertions.assertEquals("T5", pdw.getEndReference());
        Assertions.assertEquals(0.0, pdw.getStartOffset());
        Assertions.assertEquals(0.0, pdw.getEndOffset());
        Assertions.assertTrue(pdw.isEnabled());
    }

}
