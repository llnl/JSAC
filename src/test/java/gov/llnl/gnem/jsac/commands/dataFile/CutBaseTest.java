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
public class CutBaseTest {

    public CutBaseTest() {
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
    public void testInitializeTwoNumbers() {
        System.out.println("initialize two numbers");
        String text = "CUT 20 30";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");

        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getStartReference().equals(pdw.getEndReference()));
        Assertions.assertTrue(pdw.getStartReference().equals("REFTIME"));
        Assertions.assertEquals(20.0, pdw.getStartOffset());
        Assertions.assertEquals(30.0, pdw.getEndOffset());
    }

    @Test
    public void testInitializeRefAndTwoNumbers() {
        System.out.println("initialize refname and two numbers");
        String text = "CUT B 20 30";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");

        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getStartReference().equals(pdw.getEndReference()));
        Assertions.assertTrue(pdw.getStartReference().equals("B"));
        Assertions.assertEquals(20.0, pdw.getStartOffset());
        Assertions.assertEquals(30.0, pdw.getEndOffset());
    }

    @Test
    public void testInitializeTwoRefsAndNoNumbers() {
        System.out.println("initialize two refnames and no numbers");
        String text = "CUT B T0";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");

        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getEndReference().equals("T0"));
        Assertions.assertTrue(pdw.getStartReference().equals("B"));
        Assertions.assertEquals(0.0, pdw.getStartOffset());
        Assertions.assertEquals(0.0, pdw.getEndOffset());
    }

    @Test
    public void testInitializeTwoRefsAndTwoNumbers() {
        System.out.println("initialize two refnames and two numbers");
        String text = "CUT B -23 T0 15";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");

        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getEndReference().equals("T0"));
        Assertions.assertTrue(pdw.getStartReference().equals("B"));
        Assertions.assertEquals(-23.0, pdw.getStartOffset());
        Assertions.assertEquals(15.0, pdw.getEndOffset());
    }

    @Test
    public void testInitializeSignal() {
        System.out.println("initialize two refnames and two numbers");
        String text = "CUT SIGNAL";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");

        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getEndReference().equals("F"));
        Assertions.assertTrue(pdw.getStartReference().equals("A"));
        Assertions.assertEquals(-1.0, pdw.getStartOffset());
        Assertions.assertEquals(1.0, pdw.getEndOffset());
    }

    @Test
    public void testInitializeBeginNPTS() {
        System.out.println("initialize with B offset in NPTS and no end cut");
        String text = "CUT B N 1000";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");
        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getEndReference().equals("E"));
        Assertions.assertTrue(pdw.getStartReference().equals("B"));
        Assertions.assertEquals(1000, pdw.getStartIntegerOffset());
        Assertions.assertEquals(0.0, pdw.getEndOffset());
    }

    @Test
    public void testInitializeBothNPTS() {
        System.out.println("initialize with B offset in NPTS and T0 ofset in npts");
        String text = "CUT B N 1000 T0 N 100";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");
        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);

        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getEndReference().equals("T0"));
        Assertions.assertTrue(pdw.getStartReference().equals("B"));
        Assertions.assertEquals(1000, pdw.getStartIntegerOffset());
        Assertions.assertEquals(100, pdw.getEndIntegerOffset());
    }

    @Test
    public void testInitializeBNoValueT1NPTS() {
        System.out.println("initialize with B no value and T0 ofset in npts");
        String text = "CUT B T0 N 100";
        String[] tokens = text.split("(\\s+)|(\\s*,\\s*)");

        PartialDataWindow pdw = new PartialDataWindow();
        PDWSetterBase instance = new PDWSetterBase("CUT", pdw);
        instance.initialize(tokens);

        Assertions.assertTrue(pdw.getEndReference().equals("T0"));
        Assertions.assertTrue(pdw.getStartReference().equals("B"));
        Assertions.assertEquals(0, pdw.getStartOffset());
        Assertions.assertEquals(100, pdw.getEndIntegerOffset());
    }

}
