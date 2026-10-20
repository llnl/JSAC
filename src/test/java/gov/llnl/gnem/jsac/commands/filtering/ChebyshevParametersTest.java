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

import static gov.llnl.gnem.jsac.commands.filtering.ChebyshevParameters.c1;
import static gov.llnl.gnem.jsac.commands.filtering.ChebyshevParameters.c2;
import static gov.llnl.gnem.jsac.commands.filtering.ChebyshevParameters.calculateEpsilon;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevI;
import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevII;
import com.oregondsp.signalProcessing.filter.iir.AnalogPrototype;

import gov.llnl.gnem.jsac.SacDataModel;

public class ChebyshevParametersTest {

    public ChebyshevParametersTest() {
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
     * Test of calculateEpsilon method, of class ChebyshevParameters.
     */
    @Test
    public void testAll() {
        System.out.println("testAll");
        double A = 100.0;
        double TBW = 0.5;
        int order = 6;

        double[] params = calculateEpsilon(A, TBW, order);

        double eps = params[0];

        Assertions.assertEquals(eps, 0.621, 0.001, "eps");

        double omegaR = 1.0 + TBW;

        Assertions.assertEquals(c1(order, eps, 1.0), 0.8495, 0.001, "c1(1)");
        Assertions.assertEquals(c2(order, eps, omegaR, 1.0), 0.8495, 0.001, "c2(1)");

        Assertions.assertEquals(c1(order, eps, omegaR), 0.01, 0.001, "c1(omegaR)");
        Assertions.assertEquals(c2(order, eps, omegaR, omegaR), 0.01, 0.001, "c2(omegaR)");

        AnalogChebyshevI ACI = new AnalogChebyshevI(order, eps);
        AnalogPrototype proto = ACI;

        AnalogPrototypeInspector inspector = new AnalogPrototypeInspector(proto);

        Assertions.assertEquals(inspector.getMagnitude(1.0), 0.8495, 0.001, "inspector.getMagnitude( 1.0 )");
        Assertions.assertEquals(inspector.getMagnitude(omegaR), 0.00999, 0.001, "inspector.getMagnitude( omegaR )");

        eps = 1.0 / Math.sqrt(A * A - 1.0);

        AnalogChebyshevII ACII = new AnalogChebyshevII(order, eps);
        proto = ACII.lptolp(omegaR);

        inspector = new AnalogPrototypeInspector(proto);
        System.out.println("Chebyshev II epsilon: " + eps);
        Assertions.assertEquals(inspector.getMagnitude(1.0), 0.8495, 0.001, "inspector.getMagnitude( 1.0 )");
        Assertions.assertEquals(inspector.getMagnitude(omegaR), 0.00999, 0.001, "inspector.getMagnitude( omegaR )");
    }

}
