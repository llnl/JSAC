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

package gov.llnl.gnem.jsac.commands.unary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.TestUtil;
import gov.llnl.gnem.jsac.commands.filtering.ChebyshevParameters;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACAlphaReader;
import gov.llnl.gnem.jsac.io.SACFile;
import llnl.gnem.core.signalprocessing.filter.FilterDesign;

/**
 *
 * @author dodge1
 */
public class FilterSacCommandTest {

    public FilterSacCommandTest() {
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
    public void testBP_N_4_C_12_14() {
        System.out.println("testBP_N_4_C_12_14");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                SacTraceData std = new SacTraceData(Paths.get("."), sac);
                std.filter(FilterDesign.Butterworth, 4, PassbandType.BANDPASS, 12, 14, 0, 0, false);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.bp.n4.c.12_14.sacalpha")) {
                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);

                    TestUtil.compareTwoSacfileArraysExact(std, std2, "BP N 4 C 12 14", 0.001f);
                }
            }
        } catch (IOException ex) {

        }
    }

    @Test
    public void testBR_N_4_C_12_14() {
        System.out.println("testBR_N_4_C_12_14");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                SacTraceData std = new SacTraceData(Paths.get("."), sac);
                std.filter(FilterDesign.Butterworth, 4, PassbandType.BANDREJECT, 12, 14, 0, 0, false);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.br.n4.c.12_14.sacalpha")) {
                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);

                    TestUtil.compareTwoSacfileArraysExact(std, std2, "BR N 4 C 12 14", 0.001f);
                }
            }
        } catch (IOException ex) {

        }
    }

    @Test
    public void testHP_N_4_C_12() {
        System.out.println("testHP_N_4_C_12");

        try {
            // First emulate funcgen seis and differentiate the result.
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                SacTraceData std = new SacTraceData(Paths.get("."), sac);
                std.filter(FilterDesign.Butterworth, 4, PassbandType.HIGHPASS, 12, .4, 0, 0, false);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.hp.n4.c.12.sacalpha")) {
                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);

                    TestUtil.compareTwoSacfileArraysExact(std, std2, "HP N 4 C 12", 0.001f);
                }
            }
        } catch (IOException ex) {

        }
    }

    @Test
    public void testLP_N_4_C_12() {
        System.out.println("testLP_N_4_C_12");

        try {
            // First emulate funcgen seis and differentiate the result.
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                SacTraceData std = new SacTraceData(Paths.get("."), sac);
                std.filter(FilterDesign.Butterworth, 4, PassbandType.LOWPASS, .4, 12, 0, 0, false);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.lp.n4.c.12.sacalpha")) {
                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);

                    TestUtil.compareTwoSacfileArraysExact(std, std2, "LP N 4 C 12", 0.001f);
                }
            }
        } catch (IOException ex) {

        }
    }

    @Test
    public void testBP_C1_N_4_C_12_14_point5_100() {
        System.out.println("testBP_C1_N_4_C_12_14_point5_100");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                SacTraceData std = new SacTraceData(Paths.get("."), sac);
                double attenuation = 100;
                int order = 4;
                double transitionBW = 0.5;
                double[] cp = ChebyshevParameters.calculateEpsilon(attenuation, transitionBW, order);
                double epsilon = cp[0];

                std.filter(FilterDesign.Chebyshev1, 4, PassbandType.BANDPASS, 12, 14, epsilon, transitionBW, false);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.bp.c1.n4.c.12_14.tr.5.atten_100.sacalpha")) {
                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);

                    TestUtil.compareTwoSacfileArraysExact(std, std2, "BP_C1_N_4_C_12_14_point5_100", 0.001f);
                }
            }
        } catch (IOException ex) {

        }
    }

    @Test
    public void testBR_C1_N_4_C_12_14_point5_100() {
        System.out.println("testBR_C1_N_4_C_12_14_point5_100");

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            try (InputStream is = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seismogram.txt")) {
                SACFile sac = SACAlphaReader.readAlpha(is);
                SacTraceData std = new SacTraceData(Paths.get("."), sac);
                double attenuation = 100;
                int order = 4;
                double transitionBW = 0.5;
                double[] cp = ChebyshevParameters.calculateEpsilon(attenuation, transitionBW, order);
                double epsilon = cp[0];

                std.filter(FilterDesign.Chebyshev1, 4, PassbandType.BANDREJECT, 12, 14, epsilon, transitionBW, false);
                try (InputStream is2 = classLoader.getResourceAsStream("gov/llnl/gnem/jsac/commands/seis.br.c1.n4.c.12_14.tr.5.atten_100.sacalpha")) {
                    SACFile sac2 = SACAlphaReader.readAlpha(is2);
                    SacTraceData std2 = new SacTraceData(Paths.get("."), sac2);
                    TestUtil.compareTwoSacfileArraysExact(std, std2, "BP_C1_N_4_C_12_14_point5_100", 0.001f);
                }
            }
        } catch (IOException ex) {

        }
    }

}
