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
package gov.llnl.gnem.jsac.io.gse;

import java.util.List;

/**
 *
 * @author dodge1
 */
public class CheckSumComputer {

    public static int compute_checksum(List<Integer> rawSignal) {
        int numSamples = rawSignal.size();
        int sampleValue;
        int modulo;
        int checksum;
        int MODULO_VALUE = 100000000;
        checksum = 0;
        modulo = MODULO_VALUE;
        for (int j = 0; j < numSamples; j++) {
            /* check on sample value overflow */
            sampleValue = rawSignal.get(j);
            if (Math.abs(sampleValue) >= modulo) {
                sampleValue = sampleValue - (sampleValue / modulo) * modulo;
            }
            /* add the sample value to the checksum */
            checksum += sampleValue;
            /* apply modulo division to the checksum */
            if (Math.abs(checksum) >= modulo) {
                checksum = checksum - (checksum / modulo) * modulo;
            }
        }
        /* compute absolute value of the checksum */
        return Math.abs(checksum);
    }

}
