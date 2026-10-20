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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import gov.llnl.gnem.jsac.io.enums.Body;
import gov.llnl.gnem.jsac.io.enums.DataQuality;
import gov.llnl.gnem.jsac.io.enums.DepVarType;
import gov.llnl.gnem.jsac.io.enums.EventType;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.io.enums.Iztype;
import gov.llnl.gnem.jsac.io.enums.MagSource;
import gov.llnl.gnem.jsac.io.enums.MagType;
import gov.llnl.gnem.jsac.io.enums.SyntheticsType;


public class SACHeaderIO {

    public static final int SAC_HEADER_BYTE_SIZE = 632;
    public static final int SAC_FOOTER_BYTE_SIZE = 176;
    private static final int NVHDR_OFFSET = 76;
    private static final int INTEGER_FIELDS_OFFSET = 70;
    private static final int CHARACTER_FIELDS_OFFSET = 440;

    // Undefined field values for use in files - "null" in SACHeader instance
    public static final float UNDEFINED_FLOAT = -12345.0f;
    public static final double UNDEFINED_DOUBLE = -12345.0;
    public static final int UNDEFINED_INTEGER = -12345;
    public static final String UNDEFINED_STRING8 = "-12345  ";
    public static final String UNDEFINED_STRING16 = "-12345          ";
    public static final String UNDEFINED_STRING16_ALT = "-12345  -12345  ";

    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    private final FileChannel channel;

    public SACHeaderIO(FileChannel channel) {
        this.channel = channel;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public SACHeader read() throws IOException {

        SACHeader retval = new SACHeader();

        ByteBuffer buffer = ByteBuffer.allocate(SAC_HEADER_BYTE_SIZE);
        channel.position(0);
        channel.read(buffer);

        buffer.flip();

        int version = buffer.getInt(NVHDR_OFFSET * 4);

        if (!(version == SACHeader.DEFAULT_NVHDR || version == SACHeader.IRIS_NVHDR)) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
            buffer.order(byteOrder);
        }

        FloatBuffer fbuffer = buffer.asFloatBuffer();

        retval.setDelta(readFloat(fbuffer));
        retval.setDepmin(readFloat(fbuffer));
        retval.setDepmax(readFloat(fbuffer));
        retval.setScale(readFloat(fbuffer));
        retval.setOdelta(readFloat(fbuffer));

        retval.setB(readFloat(fbuffer));
        retval.setE(readFloat(fbuffer));
        retval.setO(readFloat(fbuffer));
        retval.setA(readFloat(fbuffer));
        retval.setFmt(readFloat(fbuffer));

        Double[] t = new Double[10];
        for (int i = 0; i < 10; i++) {
            t[i] = readFloat(fbuffer);
        }
        retval.setT(t);

        retval.setF(readFloat(fbuffer));
        Double[] resp = new Double[10];
        for (int i = 0; i < 10; i++) {
            resp[i] = readFloat(fbuffer);
        }
        retval.setResp(resp);

        retval.setStla(readFloat(fbuffer));
        retval.setStlo(readFloat(fbuffer));
        retval.setStel(readFloat(fbuffer));
        retval.setStdp(readFloat(fbuffer));

        retval.setEvla(readFloat(fbuffer));
        retval.setEvlo(readFloat(fbuffer));
        retval.setEvel(readFloat(fbuffer));
        retval.setEvdp(readFloat(fbuffer));
        retval.setMag(readFloat(fbuffer));

        Double[] user = new Double[10];
        for (int i = 0; i < 10; i++) {
            user[i] = readFloat(fbuffer);
        }
        retval.setUser(user);

        retval.setDist(readFloat(fbuffer));
        retval.setAz(readFloat(fbuffer));
        retval.setBaz(readFloat(fbuffer));
        retval.setGcarc(readFloat(fbuffer));
        retval.setSb(readFloat(fbuffer));

        retval.setSdelta(readFloat(fbuffer));
        retval.setDepmen(readFloat(fbuffer));
        retval.setCmpaz(readFloat(fbuffer));
        retval.setCmpinc(readFloat(fbuffer));
        retval.setXminimum(readFloat(fbuffer));

        retval.setXmaximum(readFloat(fbuffer));
        retval.setYminimum(readFloat(fbuffer));
        retval.setYmaximum(readFloat(fbuffer));
        retval.setUnused6(readFloat(fbuffer));
        retval.setUnused7(readFloat(fbuffer));

        retval.setUnused8(readFloat(fbuffer));
        retval.setUnused9(readFloat(fbuffer));
        retval.setUnused10(readFloat(fbuffer));
        retval.setUnused11(readFloat(fbuffer));
        retval.setUnused12(readFloat(fbuffer));

        IntBuffer ibuffer = buffer.asIntBuffer();
        ibuffer.position(INTEGER_FIELDS_OFFSET);

        retval.setNzyear(readInteger(ibuffer));
        retval.setNzjday(readInteger(ibuffer));
        retval.setNzhour(readInteger(ibuffer));
        retval.setNzmin(readInteger(ibuffer));
        retval.setNzsec(readInteger(ibuffer));

        retval.setNzmsec(readInteger(ibuffer));
        retval.setNvhdr(readInteger(ibuffer));
        retval.setNorid(readInteger(ibuffer));
        retval.setNevid(readInteger(ibuffer));
        retval.setNpts(readInteger(ibuffer));

        retval.setNsnpts(readInteger(ibuffer));
        retval.setNwfid(readInteger(ibuffer));
        retval.setNxsize(readInteger(ibuffer));
        retval.setNysize(readInteger(ibuffer));
        retval.setUnused15(readInteger(ibuffer));

        retval.setIftype(FileType.getFileTypeFromCode(ibuffer.get()));
        retval.setIdep(DepVarType.getDepVarTypeFromCode(ibuffer.get()));
        retval.setIztype(Iztype.getIztype(ibuffer.get()));
        retval.setUnused16(readInteger(ibuffer));
        retval.setIinst(readInteger(ibuffer));

        retval.setIstreg(readInteger(ibuffer));
        retval.setIevreg(readInteger(ibuffer));
        retval.setIevtyp(EventType.getEventTypeFromCode(ibuffer.get()));
        retval.setIqual(DataQuality.getDataQualityFromCode(ibuffer.get()));
        retval.setIsynth(SyntheticsType.getSyntheticsTypeFromCode(ibuffer.get()));

        retval.setImagtyp(MagType.getMagtypeFromCode(ibuffer.get()));
        retval.setImagsrc(MagSource.getMagSourceFromCode(ibuffer.get()));
        retval.setIbody(Body.getBodyFromCode(ibuffer.get()));
        retval.setScenario(readInteger(ibuffer));
        retval.setUnused21(readInteger(ibuffer));

        retval.setUnused22(readInteger(ibuffer));
        retval.setUnused23(readInteger(ibuffer));
        retval.setUnused24(readInteger(ibuffer));
        retval.setUnused25(readInteger(ibuffer));
        retval.setUnused26(readInteger(ibuffer));

        retval.setLeven(readInteger(ibuffer));
        retval.setLpspol(readInteger(ibuffer));
        retval.setLovrok(readInteger(ibuffer));
        retval.setLcalda(readInteger(ibuffer));
        retval.setUnused27(readInteger(ibuffer));

        buffer.position(CHARACTER_FIELDS_OFFSET);

        retval.setKstnm(readString(buffer, 8));
        retval.setKevnm(readString(buffer, 16));

        retval.setKhole(readString(buffer, 8));
        retval.setKo(readString(buffer, 8));
        retval.setKa(readString(buffer, 8));

        String[] kt = new String[10];
        for (int i = 0; i < 10; i++) {
            kt[i] = readString(buffer, 8);
        }
        retval.setKt(kt);

        retval.setKf(readString(buffer, 8));
        retval.setKuser0(readString(buffer, 8));

        retval.setKuser1(readString(buffer, 8));
        retval.setKuser2(readString(buffer, 8));
        retval.setKcmpnm(readString(buffer, 8));

        retval.setKnetwk(readString(buffer, 8));
        retval.setKdatrd(readString(buffer, 8));
        retval.setKinst(readString(buffer, 8));

        if (retval.getNvhdr() == 7) {
            readFooter(retval);
            channel.position(SAC_HEADER_BYTE_SIZE);
        }

        return retval;
    }

    // For SACHeader version 7, read footer for more accurate versions of delta, b, e, o, a, t1 ... t9, 
    //   f, evlo, evla, stlo, stla, sb, sdelta
    private void readFooter(SACHeader header) throws IOException {

        long footer_offset = header.getNpts() * 4 + SAC_HEADER_BYTE_SIZE;

        ByteBuffer buffer = ByteBuffer.allocate(SAC_FOOTER_BYTE_SIZE);
        channel.position(footer_offset);
        channel.read(buffer);

        buffer.flip();

        buffer.order(byteOrder);

        DoubleBuffer dbuffer = buffer.asDoubleBuffer();

        header.setDelta(readDouble(dbuffer));
        header.setB(readDouble(dbuffer));
        header.setE(readDouble(dbuffer));
        header.setO(readDouble(dbuffer));
        header.setA(readDouble(dbuffer));

        Double[] T = new Double[10];
        for (int i = 0; i < 10; i++) {
            T[i] = readDouble(dbuffer);
        }
        header.setT(T);

        header.setF(readDouble(dbuffer));
        header.setEvlo(readDouble(dbuffer));
        header.setEvla(readDouble(dbuffer));
        header.setStlo(readDouble(dbuffer));
        header.setStla(readDouble(dbuffer));
        header.setSb(readDouble(dbuffer));
        header.setSdelta(readDouble(dbuffer));

    }

    private Double readDouble(DoubleBuffer db) {

        Double retval;

        double dbl = db.get();
        if (dbl == UNDEFINED_DOUBLE) {
            retval = null;
        } else {
            retval = dbl;
        }

        return retval;
    }

    private Double readFloat(FloatBuffer fb) {
        float flt = fb.get();
        return toFloat(flt);
    }

    private static Double toFloat(float flt) {
        Double retval;
        if (flt == UNDEFINED_FLOAT) {
            retval = null;
        } else {
            retval = (double) flt;
        }

        return retval;
    }

    private Integer readInteger(IntBuffer ib) {

        int i = ib.get();
        return toInteger(i);
    }

    private static Integer toInteger(int i) {
        Integer retval;
        if (i == UNDEFINED_INTEGER) {
            retval = null;
        } else {
            retval = i;
        }

        return retval;
    }

    private String readString(ByteBuffer buffer, int size) throws IOException {

        byte[] b = new byte[size];
        buffer.get(b);

        int i = 0;
        while (i < size) {

            if (b[i] == 0) {
                for (int j = i; j < size; j++) {
                    b[j] = 32;
                }
                break;
            }

            i++;
        }

        String retval = new String(b);
        if (retval.equals(UNDEFINED_STRING16) || retval.equals(UNDEFINED_STRING8) || retval.equals(UNDEFINED_STRING16_ALT)) {
            retval = null;
        }

        return retval;
    }

    public void write(SACHeader header) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(SAC_HEADER_BYTE_SIZE);

        FloatBuffer fbuffer = buffer.asFloatBuffer();

        writeFloat(fbuffer, header.getDelta());
        writeFloat(fbuffer, header.getDepmin());
        writeFloat(fbuffer, header.getDepmax());
        writeFloat(fbuffer, header.getScale());
        writeFloat(fbuffer, header.getOdelta());
        writeFloat(fbuffer, header.getB());
        writeFloat(fbuffer, header.getE());
        writeFloat(fbuffer, header.getO());
        writeFloat(fbuffer, header.getA());
        writeFloat(fbuffer, header.getFmt());

        Double[] tmp = header.getT();
        for (int i = 0; i < 10; i++) {
            writeFloat(fbuffer, tmp[i]);
        }

        writeFloat(fbuffer, header.getF());

        tmp = header.getResp();
        for (int i = 0; i < 10; i++) {
            writeFloat(fbuffer, tmp[i]);
        }

        writeFloat(fbuffer, header.getStla());
        writeFloat(fbuffer, header.getStlo());
        writeFloat(fbuffer, header.getStel());
        writeFloat(fbuffer, header.getStdp());
        writeFloat(fbuffer, header.getEvla());
        writeFloat(fbuffer, header.getEvlo());
        writeFloat(fbuffer, header.getEvel());
        writeFloat(fbuffer, header.getEvdp());
        writeFloat(fbuffer, header.getMag());

        tmp = header.getUser();
        for (int i = 0; i < 10; i++) {
            writeFloat(fbuffer, tmp[i]);
        }

        writeFloat(fbuffer, header.getDist());
        writeFloat(fbuffer, header.getAz());
        writeFloat(fbuffer, header.getBaz());
        writeFloat(fbuffer, header.getGcarc());
        writeFloat(fbuffer, header.getSb());
        writeFloat(fbuffer, header.getSdelta());
        writeFloat(fbuffer, header.getDepmen());
        writeFloat(fbuffer, header.getCmpaz());
        writeFloat(fbuffer, header.getCmpinc());
        writeFloat(fbuffer, header.getXminimum());
        writeFloat(fbuffer, header.getXmaximum());
        writeFloat(fbuffer, header.getYminimum());
        writeFloat(fbuffer, header.getYmaximum());
        writeFloat(fbuffer, header.getUnused6());
        writeFloat(fbuffer, header.getUnused7());
        writeFloat(fbuffer, header.getUnused8());
        writeFloat(fbuffer, header.getUnused9());
        writeFloat(fbuffer, header.getUnused10());
        writeFloat(fbuffer, header.getUnused11());
        writeFloat(fbuffer, header.getUnused12());

        IntBuffer ibuffer = buffer.asIntBuffer();
        ibuffer.position(INTEGER_FIELDS_OFFSET);

        writeInteger(ibuffer, header.getNzyear());
        writeInteger(ibuffer, header.getNzjday());
        writeInteger(ibuffer, header.getNzhour());
        writeInteger(ibuffer, header.getNzmin());
        writeInteger(ibuffer, header.getNzsec());
        writeInteger(ibuffer, header.getNzmsec());
        writeInteger(ibuffer, header.getNvhdr());
        writeInteger(ibuffer, header.getNorid());
        writeInteger(ibuffer, header.getNevid());
        writeInteger(ibuffer, header.getNpts());
        writeInteger(ibuffer, header.getNsnpts());
        writeInteger(ibuffer, header.getNwfid());
        writeInteger(ibuffer, header.getNxsize());
        writeInteger(ibuffer, header.getNysize());
        writeInteger(ibuffer, header.getUnused15());

        FileType iftype = header.getIftype();
        if (iftype == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, iftype.getCode());
        }

        DepVarType idep = header.getIdep();
        if (idep == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, idep.getCode());
        }

        Iztype iztype = header.getIztype();
        if (iztype == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, iztype.getCode());
        }

        writeInteger(ibuffer, header.getUnused16());
        writeInteger(ibuffer, header.getIinst());
        writeInteger(ibuffer, header.getIstreg());
        writeInteger(ibuffer, header.getIevreg());

        EventType ievtype = header.getIevtyp();
        if (ievtype == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, ievtype.getCode());
        }

        DataQuality iqual = header.getIqual();
        if (iqual == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, iqual.getCode());
        }

        SyntheticsType isynth = header.getIsynth();
        if (isynth == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, isynth.getCode());
        }

        MagType imagtyp = header.getImagtyp();
        if (imagtyp == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, imagtyp.getCode());
        }

        MagSource imagsrc = header.getImagsrc();
        if (imagsrc == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, imagsrc.getCode());
        }

        Body ibody = header.getIbody();
        if (ibody == null) {
            writeInteger(ibuffer, UNDEFINED_INTEGER);
        } else {
            writeInteger(ibuffer, ibody.getCode());
        }

        writeInteger(ibuffer, header.getScenario());
        writeInteger(ibuffer, header.getUnused21());
        writeInteger(ibuffer, header.getUnused22());
        writeInteger(ibuffer, header.getUnused23());
        writeInteger(ibuffer, header.getUnused24());
        writeInteger(ibuffer, header.getUnused25());
        writeInteger(ibuffer, header.getUnused26());
        writeInteger(ibuffer, header.getLeven());
        writeInteger(ibuffer, header.getLpspol());
        writeInteger(ibuffer, header.getLovrok());
        writeInteger(ibuffer, header.getLcalda());
        writeInteger(ibuffer, header.getUnused27());

        buffer.position(CHARACTER_FIELDS_OFFSET);

        writeString8(buffer, header.getKstnm());
        String kevnm = header.getKevnm();
        if (kevnm == null) {
            buffer.put(UNDEFINED_STRING16.getBytes());
        } else {
            String string = kevnm;
            if (string.length() < 16) {
                StringBuilder sb = new StringBuilder(string);
                while (sb.length() < 16) {
                    sb.append(' ');
                }
                string = sb.toString();
            } else if (string.length() > 16) {
                string = string.substring(0, 16);
            }

            buffer.put(string.getBytes());
        }
        writeString8(buffer, header.getKhole());
        writeString8(buffer, header.getKo());
        writeString8(buffer, header.getKa());

        String[] kt = header.getKt();
        for (int i = 0; i < kt.length; i++) {
            writeString8(buffer, kt[i]);
        }

        writeString8(buffer, header.getKf());
        writeString8(buffer, header.getKuser0());
        writeString8(buffer, header.getKuser1());
        writeString8(buffer, header.getKuser2());
        writeString8(buffer, header.getKcmpnm());
        writeString8(buffer, header.getKnetwk());
        writeString8(buffer, header.getKdatrd());
        writeString8(buffer, header.getKinst());

        channel.position(0);
        buffer.flip();
        channel.write(buffer);
    }

    public void writeFooter(SACHeader header) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(SAC_FOOTER_BYTE_SIZE);
        buffer.clear();
        DoubleBuffer dbuffer = buffer.asDoubleBuffer();

        writeDouble(dbuffer, header.getDelta());
        writeDouble(dbuffer, header.getB());
        writeDouble(dbuffer, header.getE());
        writeDouble(dbuffer, header.getO());
        writeDouble(dbuffer, header.getA());

        Double[] T = header.getT();
        for (int i = 0; i < 10; i++) {
            writeDouble(dbuffer, T[i]);
        }

        writeDouble(dbuffer, header.getF());
        writeDouble(dbuffer, header.getEvlo());
        writeDouble(dbuffer, header.getEvla());
        writeDouble(dbuffer, header.getStlo());
        writeDouble(dbuffer, header.getStla());
        writeDouble(dbuffer, header.getSb());
        writeDouble(dbuffer, header.getSdelta());

        channel.write(buffer);
    }

    private void writeDouble(DoubleBuffer db, Double D) {

        if (D != null) {
            db.put(D.doubleValue());
        } else {
            db.put(UNDEFINED_DOUBLE);
        }

    }

    private void writeFloat(FloatBuffer fb, Double D) {

        if (D != null) {
            fb.put(D.floatValue());
        } else {
            fb.put(UNDEFINED_FLOAT);
        }

    }

    private void writeInteger(IntBuffer ib, Integer I) {

        if (I != null) {
            ib.put(I.intValue());
        } else {
            ib.put(UNDEFINED_INTEGER);
        }

    }

    private void writeString8(ByteBuffer b, String string) {

        if (string != null) {
            if (string.length() < 8) {
                StringBuilder sb = new StringBuilder(string);
                while (sb.length() < 8) {
                    sb.append(' ');
                }
                string = sb.toString();
            } else if (string.length() > 8) {
                string = string.substring(0, 8);
            }
            b.put(string.getBytes());
        } else {
            b.put(UNDEFINED_STRING8.getBytes());
        }

    }

    static SACHeader produceHeaderFromStrings(List<String> headerLines) {
        SACHeader header = new SACHeader();
        List<Float> floatValues = new ArrayList<>();
        // Floats are in the first 14 lines...
        for (int j = 0; j < 14; ++j) {
            String line = headerLines.get(j).trim();
            String[] tokens = line.split("\\s+");
            for (String str : tokens) {
                floatValues.add(Float.parseFloat(str));
            }
        }
        setFloatHeaderValuesFromFloatArray(header, floatValues);
        List<Integer> intValues = new ArrayList<>();
        for (int j = 14; j < 22; ++j) {
            String line = headerLines.get(j).trim();
            String[] tokens = line.split("\\s+");
            for (String str : tokens) {
                intValues.add(Integer.parseInt(str));
            }
        }
        setIntHeaderValuesFromIntegerArray(header, intValues);
        String[] tmp = headerLines.get(22).trim().split("\\s+");
        header.setKstnm(getStringField(tmp[0]));
        header.setKevnm(getStringField(tmp[1]));
        List<String> kValues = new ArrayList<>();
        for (int j = 23; j < 30; ++j) {
            String line = headerLines.get(j).trim();
            String[] tokens = line.split("\\s+");
            for (String str : tokens) {
                kValues.add(str);
            }
        }
        setKHeaderValuesFromStringArray(header, kValues);
        return header;
    }

    private static String getStringField(String inString) {
        String tmp = inString.trim();
        return tmp.equals("-12345") ? null : tmp;
    }

    private static void setFloatHeaderValuesFromFloatArray(SACHeader retval, List<Float> floats) {
        int k = 0;
        retval.setDelta(toFloat(floats.get(k++)));
        retval.setDepmin(toFloat(floats.get(k++)));
        retval.setDepmax(toFloat(floats.get(k++)));
        retval.setScale(toFloat(floats.get(k++)));
        retval.setOdelta(toFloat(floats.get(k++)));

        retval.setB(toFloat(floats.get(k++)));
        retval.setE(toFloat(floats.get(k++)));
        retval.setO(toFloat(floats.get(k++)));
        retval.setA(toFloat(floats.get(k++)));
        retval.setFmt(toFloat(floats.get(k++)));

        Double[] t = new Double[10];
        for (int i = 0; i < 10; i++) {
            t[i] = toFloat(floats.get(k++));
        }
        retval.setT(t);

        retval.setF(toFloat(floats.get(k++)));
        Double[] resp = new Double[10];
        for (int i = 0; i < 10; i++) {
            resp[i] = toFloat(floats.get(k++));
        }
        retval.setResp(resp);

        retval.setStla(toFloat(floats.get(k++)));
        retval.setStlo(toFloat(floats.get(k++)));
        retval.setStel(toFloat(floats.get(k++)));
        retval.setStdp(toFloat(floats.get(k++)));

        retval.setEvla(toFloat(floats.get(k++)));
        retval.setEvlo(toFloat(floats.get(k++)));
        retval.setEvel(toFloat(floats.get(k++)));
        retval.setEvdp(toFloat(floats.get(k++)));
        retval.setMag(toFloat(floats.get(k++)));

        Double[] user = new Double[10];
        for (int i = 0; i < 10; i++) {
            user[i] = toFloat(floats.get(k++));
        }
        retval.setUser(user);

        retval.setDist(toFloat(floats.get(k++)));
        retval.setAz(toFloat(floats.get(k++)));
        retval.setBaz(toFloat(floats.get(k++)));
        retval.setGcarc(toFloat(floats.get(k++)));
        retval.setSb(toFloat(floats.get(k++)));

        retval.setSdelta(toFloat(floats.get(k++)));
        retval.setDepmen(toFloat(floats.get(k++)));
        retval.setCmpaz(toFloat(floats.get(k++)));
        retval.setCmpinc(toFloat(floats.get(k++)));
        retval.setXminimum(toFloat(floats.get(k++)));

        retval.setXmaximum(toFloat(floats.get(k++)));
        retval.setYminimum(toFloat(floats.get(k++)));
        retval.setYmaximum(toFloat(floats.get(k++)));
        retval.setUnused6(toFloat(floats.get(k++)));
        retval.setUnused7(toFloat(floats.get(k++)));

        retval.setUnused8(toFloat(floats.get(k++)));
        retval.setUnused9(toFloat(floats.get(k++)));
        retval.setUnused10(toFloat(floats.get(k++)));
        retval.setUnused11(toFloat(floats.get(k++)));
        retval.setUnused12(toFloat(floats.get(k++)));
    }

    private static void setIntHeaderValuesFromIntegerArray(SACHeader retval, List<Integer> ints) {
        int k = 0;
        retval.setNzyear(toInteger(ints.get(k++)));
        retval.setNzjday(toInteger(ints.get(k++)));
        retval.setNzhour(toInteger(ints.get(k++)));
        retval.setNzmin(toInteger(ints.get(k++)));
        retval.setNzsec(toInteger(ints.get(k++)));

        retval.setNzmsec(toInteger(ints.get(k++)));
        retval.setNvhdr(toInteger(ints.get(k++)));
        retval.setNorid(toInteger(ints.get(k++)));
        retval.setNevid(toInteger(ints.get(k++)));
        retval.setNpts(toInteger(ints.get(k++)));

        retval.setNsnpts(toInteger(ints.get(k++)));
        retval.setNwfid(toInteger(ints.get(k++)));
        retval.setNxsize(toInteger(ints.get(k++)));
        retval.setNysize(toInteger(ints.get(k++)));
        retval.setUnused15(toInteger(ints.get(k++)));

        retval.setIftype(FileType.getFileTypeFromCode(ints.get(k++)));
        retval.setIdep(DepVarType.getDepVarTypeFromCode(ints.get(k++)));
        retval.setIztype(Iztype.getIztype(ints.get(k++)));
        retval.setUnused16(toInteger(ints.get(k++)));
        retval.setIinst(toInteger(ints.get(k++)));

        retval.setIstreg(toInteger(ints.get(k++)));
        retval.setIevreg(toInteger(ints.get(k++)));
        retval.setIevtyp(EventType.getEventTypeFromCode(ints.get(k++)));
        retval.setIqual(DataQuality.getDataQualityFromCode(ints.get(k++)));
        retval.setIsynth(SyntheticsType.getSyntheticsTypeFromCode(ints.get(k++)));

        retval.setImagtyp(MagType.getMagtypeFromCode(ints.get(k++)));
        retval.setImagsrc(MagSource.getMagSourceFromCode(ints.get(k++)));
        retval.setIbody(Body.getBodyFromCode(ints.get(k++)));
        retval.setScenario(toInteger(ints.get(k++)));
        retval.setUnused21(toInteger(ints.get(k++)));

        retval.setUnused22(toInteger(ints.get(k++)));
        retval.setUnused23(toInteger(ints.get(k++)));
        retval.setUnused24(toInteger(ints.get(k++)));
        retval.setUnused25(toInteger(ints.get(k++)));
        retval.setUnused26(toInteger(ints.get(k++)));

        retval.setLeven(toInteger(ints.get(k++)));
        retval.setLpspol(toInteger(ints.get(k++)));
        retval.setLovrok(toInteger(ints.get(k++)));
        retval.setLcalda(toInteger(ints.get(k++)));
        retval.setUnused27(toInteger(ints.get(k++)));

    }

    private static void setKHeaderValuesFromStringArray(SACHeader retval, List<String> strings) {
        int k = 0;
        retval.setKhole(getStringField(strings.get(k++)));
        retval.setKo(getStringField(strings.get(k++)));
        retval.setKa(getStringField(strings.get(k++)));

        String[] kt = new String[10];
        for (int i = 0; i < 10; i++) {
            kt[i] = getStringField(strings.get(k++));
        }
        retval.setKt(kt);

        retval.setKf(getStringField(strings.get(k++)));
        retval.setKuser0(getStringField(strings.get(k++)));

        retval.setKuser1(getStringField(strings.get(k++)));
        retval.setKuser2(getStringField(strings.get(k++)));
        retval.setKcmpnm(getStringField(strings.get(k++)));

        retval.setKnetwk(getStringField(strings.get(k++)));
        retval.setKdatrd(getStringField(strings.get(k++)));
        retval.setKinst(getStringField(strings.get(k++)));
    }
}
