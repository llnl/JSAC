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

package gov.llnl.gnem.jsac.io;

import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

/**
 *
 * @author dodge1
 */
public class SACFile {

    private final SACHeader header;
    private final float[] data1;
    private final float[] data2;

    public SACFile( SACHeader header, float[] data ) {
        this.header = header;
        this.data1  = data;
        this.data2  = null;
    }
    
    public SACFile( SACHeader header, float[] data1, float[] data2 ) {
    	this.header = header;
    	this.data1  = data1;
    	this.data2  = data2;
    }

    public SACHeader getHeader() {
        return header;
    }

    public float[] getData() {
        return data1 != null ? data1.clone() : null;
    }
    
    public float[] getDataBlock2() {
    	return data2 != null ? data2.clone() : null;
    }

    public CssSeismogram createSeismogram() {

        Long wfid = null;
        Integer tmpWfid = header.getNwfid();
        if (tmpWfid != null) {
            wfid = (long) tmpWfid;
        }
        String net = header.getKnetwk();
        String khole = header.getKhole();
        if (khole == null) {
            khole = "--";
        }
        String sta = header.getKstnm();
        String chan = header.getKcmpnm();
        if (chan != null && chan.length() > 3) {
            String tmp = chan.substring(3);
            chan = chan.substring(0, 3);
            if (tmp.length() == 2) {
                khole = tmp;
            } else if (tmp.length() > 2) {
                int n = tmp.length();
                khole = tmp.substring(n - 2);
            }
        }
        double samprate = 1.0;
        Double delta = header.getDelta();
        if (delta != null) {
            samprate = 1. / delta;
        }

        Double beginTime = header.getBeginTime();

        TimeT timet = beginTime != null ? new TimeT(beginTime) : new TimeT(0.0); // default to epoch time 0.

        double calib = 1.;
        double calper = 1.;

        StreamKey key = new StreamKey(net, sta, chan, khole);
        return new CssSeismogram(wfid, key, data1, samprate, timet, calib, calper);

    }

}
