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

import java.util.Arrays;

import com.oregondsp.signalProcessing.fft.RDFT;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.signalprocessing.Sequence;

public class StackedCorrelator {

    private final float[] master;
    private final int numberOfWindows;
    private final int windowLength;
    private int mOverlap;
    private int sOverlap;

    private RDFT fft;

    public StackedCorrelator( float[] master, boolean normalize, int windowLength, int numberOfWindows, int slaveLength ) {

        this.master = master;
        this.numberOfWindows = numberOfWindows;
        this.windowLength = windowLength;

        if (master.length < windowLength) {
            throw new IllegalStateException( "Length of master sequence is smaller than specified window length" );
        }

        if (slaveLength < windowLength) {
            throw new IllegalStateException( "Length of second sequence is smaller than specified window length" );
        }

        mOverlap = (int) Math.ceil((double) (windowLength * numberOfWindows - master.length) / (double) (numberOfWindows - 1));

        sOverlap = (int) Math.ceil((double) (windowLength * numberOfWindows - slaveLength) / (double) (numberOfWindows - 1));

        int nfft = 32;
        int log2nfft = 5;

        while (nfft < windowLength) {
            nfft *= 2;
            log2nfft++;
        }

        fft = new RDFT(log2nfft);

    }

    public void correlate(SacTraceData std) {

        int nfft = fft.getFFTSize();

        float[] accumulator = new float[nfft];
        float[] m = new float[nfft];
        float[] s = new float[nfft];
        float[] M = new float[nfft];
        float[] S = new float[nfft];

        float[] slave = std.getData();

        Arrays.fill(accumulator, 0.0f);

        int mptr = 0;
        int sptr = 0;

        for (int iw = 0; iw < numberOfWindows; iw++) {
            Arrays.fill(m, 0.0f);
            Arrays.fill(s, 0.0f);
            System.arraycopy(master, mptr, m, 0, windowLength);
            System.arraycopy(slave, sptr, s, 0, windowLength);

            fft.evaluate(m, M);
            fft.evaluate(s, S);

            RDFT.dftProduct(M, S, -1.0f);

            for (int i = 0; i < nfft; i++) {
                accumulator[i] += S[i];
            }

            mptr += windowLength - mOverlap;
            sptr += windowLength - sOverlap;
        }

        Arrays.fill(M, 0.0f);
        fft.evaluateInverse(accumulator, M);

        Sequence.cshift(M, nfft / 2);

        SACHeader header = std.getSACHeader();
        header.setB(header.getB() - nfft / 2 * header.getDelta());

        std.setData(M);
    }

}
