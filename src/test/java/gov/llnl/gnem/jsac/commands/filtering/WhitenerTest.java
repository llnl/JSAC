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
package gov.llnl.gnem.jsac.commands.filtering;

import static gov.llnl.gnem.jsac.commands.filtering.Whitener.levinsonsRecursion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.llnl.gnem.jsac.SacDataModel;

/**
 *
 * @author dodge1
 */
public class WhitenerTest {

    public WhitenerTest() {
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
     * Test of main method, of class Whitener.
     */
    @Test
    public void testAll() {
        System.out.println("testAll");

        double[] x = new double[100];

        for (int i = 0; i < 20; i++) {
            x[i] = 1.0;
        }

        double[] r = new double[11];

        int n = 11;

        for (int k = 0; k < 11; k++) {
            double tmp = 0.0;
            for (int i = 0; i < 100 - k; i++) {
                tmp += x[i] * x[i + k];
                r[k] = tmp;
            }
        }

        double[] a = new double[11];
        double[] reflect = new double[11];

        levinsonsRecursion(r, a, reflect, n);
        float[] c1 = { 20.00000f, 19.00000f, 18.00000f, 17.00000f, 16.00000f, 15.00000f, 14.00000f, 13.00000f, 12.00000f, 11.00000f, 10.00000f };
        float[] c2 = { 1.00000f, -0.96774f, 0.00000f, -0.00000f, -0.00000f, 0.00000f, -0.00000f, 0.00000f, 0.00000f, -0.00000f, 0.03226f };
        float[] c3 = { -0.95000f, 0.02564f, 0.02632f, 0.02703f, 0.02778f, 0.02857f, 0.02941f, 0.03030f, 0.03125f, 0.03226f, 0.00000f };

        for (int i = 0; i < n; i++) {
            Assertions.assertEquals(r[i], c1[i], 0.001, "r[" + i + "]");
            Assertions.assertEquals(a[i], c2[i], 0.001, "a[" + i + "]");
            Assertions.assertEquals(reflect[i], c3[i], 0.001, "reflect[" + i + "]");
        }
    }

}
