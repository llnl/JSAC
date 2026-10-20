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
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.llnl.gnem.jsac.transfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.measure.Unit;

import com.isti.jevalresp.ResponseUnits;

import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;

/**
 *
 * @author dodge1
 */
public class PoleZeroMetadata {

    private final String network;
    private final String station;
    private final String location;
    private final String channel;
    private final String created;
    private final Double start;
    private final Double end;
    private final String description;
    private final Double latitude;
    private final Double longitude;
    private final Double elevation;
    private final Double depth;
    private final Double dip;
    private final Double azimuth;
    private final Double samplerate;
    private final String inputunit;
    private final String outputunit;
    private final String insttype;
    private final Double instgain;
    private final String comment;
    private final Double sensitivity;
    private final Double a0;

    public PoleZeroMetadata(String network,
            String station,
            String location,
            String channel,
            String created,
            Double start,
            Double end,
            String description,
            Double latitude,
            Double longitude,
            Double elevation,
            Double depth,
            Double dip,
            Double azimuth,
            Double samplerate,
            String inputunit,
            String outputunit,
            String insttype,
            Double instgain,
            String comment,
            Double sensitivity,
            Double a0) {
        this.network = network;
        this.station = station;
        this.location = location;
        this.channel = channel;
        this.created = created;
        this.start = start;
        this.end = end;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.depth = depth;
        this.dip = dip;
        this.azimuth = azimuth;
        this.samplerate = samplerate;
        this.inputunit = inputunit;
        this.outputunit = outputunit;
        this.insttype = insttype;
        this.instgain = instgain;
        this.comment = comment;
        this.sensitivity = sensitivity;
        this.a0 = a0;
    }

    public StreamKey getStreamKey() {
        if (network != null || station != null || location != null || channel != null) {
            return new StreamKey(network, station, channel, location);
        } else {
            return null;
        }
    }

    public String getNetwork() {
        return network;
    }

    public String getStation() {
        return station;
    }

    public String getLocation() {
        return location;
    }

    public String getChannel() {
        return channel;
    }

    public String getCreated() {
        return created;
    }

    public Double getStart() {
        return start;
    }

    public Double getEnd() {
        return end;
    }

    public String getDescription() {
        return description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getElevation() {
        return elevation;
    }

    public Double getDepth() {
        return depth;
    }

    public Double getDip() {
        return dip;
    }

    public Double getAzimuth() {
        return azimuth;
    }

    public Double getSamplerate() {
        return samplerate;
    }

    public String getInputunit() {
        return inputunit;
    }

    public String getOutputunit() {
        return outputunit;
    }

    public String getInsttype() {
        return insttype;
    }

    public Double getInstgain() {
        return instgain;
    }

    public String getComment() {
        return comment;
    }

    public Double getSensitivity() {
        return sensitivity;
    }

    public Double getA0() {
        return a0;
    }

    @Override
    public String toString() {
        return "PoleZeroMetadata{" + "network=" + network + ", station=" + station + ", location=" + location + ", channel=" + channel + ", created=" + created + ", start=" + start + ", end=" + end + ", description=" + description + ", latitude=" + latitude + ", longitude=" + longitude + ", elevation=" + elevation + ", depth=" + depth + ", dip=" + dip + ", azimuth=" + azimuth + ", samplerate=" + samplerate + ", inputunit=" + inputunit + ", outputunit=" + outputunit + ", insttype=" + insttype + ", instgain=" + instgain + ", comment=" + comment + ", sensitivity=" + sensitivity + ", a0=" + a0 + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.network);
        hash = 67 * hash + Objects.hashCode(this.station);
        hash = 67 * hash + Objects.hashCode(this.location);
        hash = 67 * hash + Objects.hashCode(this.channel);
        hash = 67 * hash + Objects.hashCode(this.created);
        hash = 67 * hash + Objects.hashCode(this.start);
        hash = 67 * hash + Objects.hashCode(this.end);
        hash = 67 * hash + Objects.hashCode(this.description);
        hash = 67 * hash + Objects.hashCode(this.latitude);
        hash = 67 * hash + Objects.hashCode(this.longitude);
        hash = 67 * hash + Objects.hashCode(this.elevation);
        hash = 67 * hash + Objects.hashCode(this.depth);
        hash = 67 * hash + Objects.hashCode(this.dip);
        hash = 67 * hash + Objects.hashCode(this.azimuth);
        hash = 67 * hash + Objects.hashCode(this.samplerate);
        hash = 67 * hash + Objects.hashCode(this.inputunit);
        hash = 67 * hash + Objects.hashCode(this.outputunit);
        hash = 67 * hash + Objects.hashCode(this.insttype);
        hash = 67 * hash + Objects.hashCode(this.instgain);
        hash = 67 * hash + Objects.hashCode(this.comment);
        hash = 67 * hash + Objects.hashCode(this.sensitivity);
        hash = 67 * hash + Objects.hashCode(this.a0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PoleZeroMetadata other = (PoleZeroMetadata) obj;
        if (!Objects.equals(this.network, other.network)) {
            return false;
        }
        if (!Objects.equals(this.station, other.station)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.channel, other.channel)) {
            return false;
        }
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.inputunit, other.inputunit)) {
            return false;
        }
        if (!Objects.equals(this.outputunit, other.outputunit)) {
            return false;
        }
        if (!Objects.equals(this.insttype, other.insttype)) {
            return false;
        }
        if (!Objects.equals(this.comment, other.comment)) {
            return false;
        }
        if (!Objects.equals(this.start, other.start)) {
            return false;
        }
        if (!Objects.equals(this.end, other.end)) {
            return false;
        }
        if (!Objects.equals(this.latitude, other.latitude)) {
            return false;
        }
        if (!Objects.equals(this.longitude, other.longitude)) {
            return false;
        }
        if (!Objects.equals(this.elevation, other.elevation)) {
            return false;
        }
        if (!Objects.equals(this.depth, other.depth)) {
            return false;
        }
        if (!Objects.equals(this.dip, other.dip)) {
            return false;
        }
        if (!Objects.equals(this.azimuth, other.azimuth)) {
            return false;
        }
        if (!Objects.equals(this.samplerate, other.samplerate)) {
            return false;
        }
        if (!Objects.equals(this.instgain, other.instgain)) {
            return false;
        }
        if (!Objects.equals(this.sensitivity, other.sensitivity)) {
            return false;
        }
        return Objects.equals(this.a0, other.a0);
    }

    public Unit<?> getUnits() {
        if (inputunit == null) {
            return null;
        }
        try {
            Unit<?> units = ResponseUnits.parse(inputunit.toLowerCase());
            return units;
        } catch (Exception ex) {
            System.out.println("Failed parsing unit string " + inputunit + "!");
            return null;
        }
    }

    public static PoleZeroMetadata maybeGetMetadata(String filename) throws FileNotFoundException {
        String network = null;
        String station = null;
        String location = null;
        String channel = null;
        String created = null;
        Double start = null;
        Double end = null;
        String description = null;
        Double latitude = null;
        Double longitude = null;
        Double elevation = null;
        Double depth = null;
        Double dip = null;
        Double azimuth = null;
        Double samplerate = null;
        String inputunit = null;
        String outputunit = null;
        String insttype = null;
        Double instgain = null;
        String comment = null;
        Double sensitivity = null;
        Double a0 = null;
        File file = new File(filename);
        boolean foundMetadata = false;
        try ( Scanner sc = new Scanner(file)) {

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains("NETWORK")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    network = tmp;
                } else if (line.contains("STATION")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    station = tmp;
                } else if (line.contains("LOCATION")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    location = tmp;
                } else if (line.contains("CHANNEL")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    channel = tmp;
                } else if (line.contains("CREATED")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    created = tmp;
                } else if (line.contains("START")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    TimeT startTime = convertToTime(tmp);
                    start = startTime != null ? startTime.getEpochTime() : null;
                } else if (line.contains("END")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    TimeT aTime = convertToTime(tmp);
                    end = aTime != null ? aTime.getEpochTime() : null;
                } else if (line.contains("DESCRIPTION")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    description = tmp;
                } else if (line.contains("LATITUDE")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    latitude = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("LONGITUDE")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    longitude = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("ELEVATION")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    elevation = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("DEPTH")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    depth = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("DIP")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    dip = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("AZIMUTH")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    azimuth = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("SAMPLE RATE")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    samplerate = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                } else if (line.contains("INPUT UNIT")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    inputunit = tmp;
                } else if (line.contains("OUTPUT UNIT")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    outputunit = tmp;
                } else if (line.contains("INSTTYPE")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    insttype = tmp;
                } else if (line.contains("INSTGAIN")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    if (tmp != null && !tmp.isEmpty()) {
                        StringTokenizer st2 = new StringTokenizer(tmp);
                        String tmp2 = st2.nextToken();
                        instgain = (tmp2 != null && !tmp2.isEmpty()) ? Double.parseDouble(tmp2) : null;
                    }
                } else if (line.contains("COMMENT")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    comment = tmp;
                } else if (line.contains("SENSITIVITY")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    if (tmp != null && !tmp.isEmpty()) {
                        StringTokenizer st2 = new StringTokenizer(tmp);
                        String tmp2 = st2.nextToken();
                        sensitivity = (tmp2 != null && !tmp2.isEmpty()) ? Double.parseDouble(tmp2) : null;
                    }
                } else if (line.contains("A0")) {
                    foundMetadata = true;
                    String tmp = getValue(line);
                    a0 = (tmp != null && !tmp.isEmpty()) ? Double.parseDouble(tmp) : null;
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed opening POLEZERO file: " + filename + " to extract metadata!");
            throw ex;
        }
        if (foundMetadata) {
            return new PoleZeroMetadata(network,
                    station,
                    location,
                    channel,
                    created,
                    start,
                    end,
                    description,
                    latitude,
                    longitude,
                    elevation,
                    depth,
                    dip,
                    azimuth,
                    samplerate,
                    inputunit,
                    outputunit,
                    insttype,
                    instgain,
                    comment,
                    sensitivity,
                    a0
            );
        }
        return null;
    }

    private static String getValue(String line) {
        int idx = line.indexOf(":");
        if (idx > 0) {
            return line.substring(idx + 1).trim();
        } else {
            return null;
        }
    }

    private static TimeT convertToTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(timeStr, ",:;.-T");
        if (st.countTokens() < 3) {
            return null;
        }
        int year = Integer.parseInt(st.nextToken());
        int month = Integer.parseInt(st.nextToken());
        int day = Integer.parseInt(st.nextToken());
        int hour = 0;
        int min = 0;
        int sec = 0;
        int msec = 0;
        if (st.hasMoreTokens()) {
            hour = Integer.parseInt(st.nextToken());
        }
        if (st.hasMoreTokens()) {
            min = Integer.parseInt(st.nextToken());
        }
        if (st.hasMoreTokens()) {
            sec = Integer.parseInt(st.nextToken());
        }
        if (st.hasMoreTokens()) {
            String tmp = st.nextToken();
            msec = Integer.parseInt(tmp.substring(0, Math.min(tmp.length(), 3)));
        }
        return new TimeT(year, month, day, hour, min, sec, msec);
    }

}
