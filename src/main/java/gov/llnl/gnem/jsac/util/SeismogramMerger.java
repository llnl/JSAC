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
package gov.llnl.gnem.jsac.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.waveform.merge.MergeException;
import llnl.gnem.core.waveform.merge.WaveformMerger;
import llnl.gnem.core.waveform.qc.DataDefect;
import llnl.gnem.core.waveform.qc.DataGap;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;


public class SeismogramMerger {
    
    public static CssSeismogram mergeWaveforms(Collection<CssSeismogram> waveforms) throws MergeException {
        boolean ignoreMergeError = true;
        boolean ignoreMismatchedSamples = true;
        Map<Double, CssSeismogram> waveformMap = new TreeMap<>(); //The TreeMap will put the seismograms into time order.
        for (CssSeismogram waveform : waveforms) {
            waveformMap.put(waveform.getTimeAsDouble(), waveform);
        }
        CssSeismogram result = null;
        Double lastSample = null;
        Collection<DataDefect> defects = new ArrayList<>();

        //Iterate through the returned segments merging them into a single result waveform.
        for (Double startTime : waveformMap.keySet()) {
            CssSeismogram waveform = waveformMap.get(startTime);
            if (result == null) {
                result = waveform;
                lastSample = result.getEndtimeAsDouble();
            } else {
                double firstSample = waveform.getTimeAsDouble();
                double gap = (firstSample - lastSample) * waveform.getSamprate();
                long nsamplesInGap = Math.round(gap);
                if (nsamplesInGap > 1) {
                    DataGap dataGap = new DataGap(new Epoch(lastSample, firstSample));
                    defects.add(dataGap);
                }

                result = WaveformMerger.mergeWaveforms(waveform, result, ignoreMergeError, ignoreMismatchedSamples);
                result = new CssSeismogram(result, null, null, defects);
                lastSample = result.getEndtimeAsDouble();
            }

        }

        return result;

    }

}
