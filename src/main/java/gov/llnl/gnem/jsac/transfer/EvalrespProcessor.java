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
package gov.llnl.gnem.jsac.transfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.measure.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isti.jevalresp.ChannelMatchPolicy;
import com.isti.jevalresp.ResponseUnits;

import gov.llnl.gnem.response.ResponseMetaData;
import gov.llnl.gnem.response.ResponseType;
import gov.llnl.gnem.response.TransferData;
import gov.llnl.gnem.response.TransferFunctionProcessor;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;

/**
 *
 * @author dodge1
 */
public class EvalrespProcessor {

    private static final Logger log = LoggerFactory.getLogger(EvalrespProcessor.class);

    public static TransferData getTransferFunction(ResponseOptions responseOptions, StreamKey key, CssSeismogram s, ChannelMatchPolicy policy, Map<StreamKey, File> keyFileMap)
            throws IllegalStateException, FileNotFoundException {
        Unit<?> forcedInputUnits = ResponseUnits.NANOMETER;
      //  ResponseType responseType = responseOptions.
        String filename = responseOptions.getFilename();
        if (filename == null) {
            File tmp = keyFileMap.get(key);
            if (tmp != null) {
                filename = tmp.getAbsolutePath();
            }
        }
        if (filename != null) {
            ResponseType responseType = responseOptions.getResponseType();
            ResponseMetaData rmd = buildResponseMetadata(filename, forcedInputUnits, responseType);
            try {
                TransferData transferData = new TransferFunctionProcessor().getFromTransferFunction(
                        s.getNsamp(),
                            s.getSamprate(),
                            s.getTimeAsDouble(),
                            key.getNet(),
                            key.getSta(),
                            key.getChan(),
                            key.getLocationCode(),
                            rmd,
                            policy);
                return transferData;
            } catch (IOException ex) {
                log.warn("Failed retrieving FROM transfer function!");
                return null;
            }

        } else {
            log.warn("Filename was not specified!");
            return null;
        }
    }

    private static ResponseMetaData buildResponseMetadata(String filename, Unit<?> forcedInputUnits, ResponseType responseType) throws FileNotFoundException {

        Double startTime = TimeT.MIN_EPOCH_TIME;
        Double endTime = TimeT.MAX_EPOCH_TIME;
        ResponseMetaData rmd = new ResponseMetaData(filename, responseType, null, null, null, null, null, null, startTime, endTime, null, null);
        return rmd;
    }

}
