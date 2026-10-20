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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.measure.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isti.jevalresp.ChannelMatchPolicy;
import com.isti.jevalresp.ResponseUnits;
import com.isti.jevalresp.UnitsStatus;

import gov.llnl.gnem.response.ResponseMetaData;
import gov.llnl.gnem.response.ResponseMetadataExtension;
import gov.llnl.gnem.response.ResponseType;
import gov.llnl.gnem.response.ResponseUnitsException;
import gov.llnl.gnem.response.TransferData;
import gov.llnl.gnem.response.TransferFunctionProcessor;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.util.FileUtil.DriveMapper;
import llnl.gnem.core.waveform.seismogram.CssSeismogram;


public class PoleZeroProcessor {
    private static final Logger log = LoggerFactory.getLogger(PoleZeroProcessor.class);

    public static TransferData getTransferFunction(ResponseOptions poleZeroOptions, CssSeismogram s, ChannelMatchPolicy policy)
            throws IllegalStateException, FileNotFoundException {
        Unit<?> forcedInputUnits = ResponseUnits.NANOMETER;
        ResponseType responseType = ResponseType.SACPZF;
        String filename = poleZeroOptions.getFilename();
        if (filename != null) {
            ResponseMetaData rmd = buildResponseMetadata(filename, forcedInputUnits, responseType);
            try {
                TransferData transferData = new TransferFunctionProcessor().getFromTransferFunction(
                        s.getNsamp(),
                            s.getSamprate(),
                            s.getTimeAsDouble(),
                            null,
                            null,
                            null,
                            null,
                            rmd,
                            policy);
                if (forcedInputUnits != null && !forcedInputUnits.equals(transferData.getOriginalUnits().getInputUnits())) {
                    ResponseUnits ru = new ResponseUnits(forcedInputUnits, transferData.getOriginalUnits().getUnitObj(), UnitsStatus.FORCED_VALUE);
                    transferData.setForcedUnits(ru);
                }
                try {
                    transferData.maybeTransformData(forcedInputUnits);
                } catch (ResponseUnitsException ex) {
                    LoggerFactory.getLogger(TransferFunctionProcessor.class).trace(ex.toString(), ex);
                }

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

    private static ResponseMetaData buildResponseMetadata(String filename, Unit<?> forcedInputUnits, gov.llnl.gnem.response.ResponseType responseType) throws FileNotFoundException {

        String tmp = DriveMapper.getInstance().maybeMapPath(filename);
        PoleZeroMetadata pzm = PoleZeroMetadata.maybeGetMetadata(tmp);
        long responseId = -1L;
        Unit<?> analysisDerivedUnits = null;
        Double medianResidualThisResponse = null;
        Double residualStdThisResponse = null;
        Integer evaluationsThisResponse = null;
        String failureProblemDetail = null;
        Double startTime = TimeT.MIN_EPOCH_TIME;
        Double endTime = TimeT.MAX_EPOCH_TIME;
        Double instrumentSampleRate = null;
        if (pzm != null) {
            Unit<?> tmpUnit = pzm.getUnits();
            if (tmpUnit != null) {
                forcedInputUnits = tmpUnit;
            }
            startTime = pzm.getStart();
            if (startTime == null) {
                startTime = TimeT.MIN_EPOCH_TIME;
            }
            endTime = pzm.getEnd();
            if (endTime == null) {
                endTime = TimeT.MAX_EPOCH_TIME;
            }
            instrumentSampleRate = pzm.getSamplerate();
        }
        ResponseMetadataExtension rme = new ResponseMetadataExtension(responseId,
                                                                      responseType,
                                                                      forcedInputUnits,
                                                                      analysisDerivedUnits,
                                                                      medianResidualThisResponse,
                                                                      residualStdThisResponse,
                                                                      evaluationsThisResponse,
                                                                      failureProblemDetail);
        ResponseMetaData rmd = new ResponseMetaData(filename, responseType, null, null, null, null, null, null, startTime, endTime, rme, instrumentSampleRate);
        return rmd;
    }

}
