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

import static gov.llnl.gnem.jsac.io.SACHeaderIO.UNDEFINED_FLOAT;
import static gov.llnl.gnem.jsac.io.SACHeaderIO.UNDEFINED_INTEGER;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;
import gov.llnl.gnem.jsac.io.enums.Body;
import gov.llnl.gnem.jsac.io.enums.DataQuality;
import gov.llnl.gnem.jsac.io.enums.DepVarType;
import gov.llnl.gnem.jsac.io.enums.EventType;
import gov.llnl.gnem.jsac.io.enums.FileType;
import gov.llnl.gnem.jsac.io.enums.Iztype;
import gov.llnl.gnem.jsac.io.enums.MagSource;
import gov.llnl.gnem.jsac.io.enums.MagType;
import gov.llnl.gnem.jsac.io.enums.SyntheticsType;
import java.util.Iterator;
import java.util.LinkedHashSet;

import llnl.gnem.core.util.Epoch;
import llnl.gnem.core.util.StreamKey;
import llnl.gnem.core.util.TimeT;
import llnl.gnem.core.util.Geometry.EModel;

//Doubles instead of doubles
//Enums for various itypes
//Separate reading/writing from container class
//accessors and mutators
//getting reference time as Epoch Time - double
public class SACHeader {

    private static final Logger log = LoggerFactory.getLogger(SACHeader.class);

    // Default header version number
    public static final int DEFAULT_NVHDR = 6;
    public static final int IRIS_NVHDR = 7;

    private Double a; // first arrival time
    private Double az; // s-r azimuth, degrees
    private Double b; // RD initial value, time
    private Double baz; // s-r backazimuth, degrees
    private Double cmpaz; // component azimuth, degrees
    private Double cmpinc; // component inclination, degrees
    private Double delta; // RF time increment, seconds
    private Double depmax; // maximum amplitude
    private Double depmen; // mean value
    private Double depmin; // minimum amplitude
    private Double dist; // s-r distance, km
    private Double e; // RD final value, time
    private Double evdp; // event depth
    private Double evel; // event elevation
    private Double evla; // event latitude
    private Double evlo; // event longitude
    private Double f; // event end sec < nz.
    private Double fmt; // internal use
    private Double gcarc; // s-r distance, degrees
    private DepVarType idep;
    private Iztype iztype;
    private Integer ievreg;
    private EventType ievtyp;
    private FileType iftype = FileType.ITIME;
    private Integer iinst;
    private MagSource imagsrc;
    private MagType imagtyp;
    private Integer istreg;
    private DataQuality iqual;
    private SyntheticsType isynth;
    private String ka;
    private String kcmpnm;
    private String kdatrd;
    private String kevnm;
    private String kf;
    private String khole;
    private String kinst;
    private String knetwk;
    private String ko;
    private String kstnm;
    private String[] kt = new String[10];
    private String kuser0;
    private String kuser1;
    private String kuser2;
    private Integer lcalda;
    private Integer leven;
    private Integer lovrok;
    private Integer lpspol;
    private Double mag; // magnitude
    private Integer nevid;
    private Integer norid;
    private Integer npts;
    private Integer nsnpts;
    private Integer nvhdr;
    private Integer nwfid;
    private Integer nxsize;
    private Integer nysize;
    private Integer nzhour;
    private Integer nzjday;
    private Integer nzmin;
    private Integer nzmsec;
    private Integer nzsec;
    private Integer nzyear;
    private Double o; // event start, sec < nz
    private Double odelta; // observed time increment
    private Double[] resp = new Double[10]; // instrument response param
    private Double sb; // internal use
    private Double scale; // amplitude scale factor
    private Double sdelta; // internal use
    private Double stdp; // station depth
    private Double stel; // station elevation
    private Double stla; // station latitude
    private Double stlo; // station longitude
    private Double[] t = new Double[10]; // user defined time picks
    private Double unused10;
    private Double unused11;
    private Double unused12;
    private Integer unused15;
    private Integer unused16;
    private Body ibody;
    private Integer scenario;
    private Integer unused21;
    private Integer unused22;
    private Integer unused23;
    private Integer unused24;
    private Integer unused25;
    private Integer unused26;
    private Integer unused27;
    private Double unused6;
    private Double unused7;
    private Double unused8;
    private Double unused9;
    private Double[] user = new Double[10]; // user variables
    private Double xmaximum;
    private Double xminimum;
    private Double ymaximum;
    private Double yminimum;

    // Default header constructor
    public SACHeader() {
        nvhdr = DEFAULT_NVHDR;
    }

    public SACHeader(SACHeader old) {
        a = old.a;
        az = old.az;
        b = old.b;
        baz = old.baz;
        cmpaz = old.cmpaz;
        cmpinc = old.cmpinc;
        delta = old.delta;
        depmax = old.depmax;
        depmen = old.depmen;
        depmin = old.depmin;
        dist = old.dist;
        e = old.e;
        evdp = old.evdp;
        evel = old.evel;
        evla = old.evla;
        evlo = old.evlo;
        f = old.f;
        fmt = old.fmt;
        gcarc = old.gcarc;
        idep = old.idep;
        iztype = old.iztype;
        ievreg = old.ievreg;
        ievtyp = old.ievtyp;
        iftype = old.iftype;
        iinst = old.iinst;
        imagsrc = old.imagsrc;
        imagtyp = old.imagtyp;
        istreg = old.istreg;
        iqual = old.iqual;
        isynth = old.isynth;
        ka = old.ka;
        kcmpnm = old.kcmpnm;
        kdatrd = old.kdatrd;
        kevnm = old.kevnm;
        kf = old.kf;
        khole = old.khole;
        kinst = old.kinst;
        knetwk = old.knetwk;
        ko = old.ko;
        kstnm = old.kstnm;
        kuser0 = old.kuser0;
        kuser1 = old.kuser1;
        kuser2 = old.kuser2;
        lcalda = old.lcalda;
        leven = old.leven;
        lovrok = old.lovrok;
        lpspol = old.lpspol;
        mag = old.mag;
        nevid = old.nevid;
        norid = old.norid;
        npts = old.npts;
        nsnpts = old.nsnpts;
        nvhdr = old.nvhdr;
        nwfid = old.nwfid;
        nxsize = old.nxsize;
        nysize = old.nysize;
        nzhour = old.nzhour;
        nzjday = old.nzjday;
        nzmin = old.nzmin;
        nzmsec = old.nzmsec;
        nzsec = old.nzsec;
        nzyear = old.nzyear;
        o = old.o;
        odelta = old.odelta;
        sb = old.sb;
        scale = old.scale;
        sdelta = old.sdelta;
        stdp = old.stdp;
        stel = old.stel;
        stla = old.stla;
        stlo = old.stlo;
        unused10 = old.unused10;
        unused11 = old.unused11;
        unused12 = old.unused12;
        unused15 = old.unused15;
        unused16 = old.unused16;
        ibody = old.ibody;
        scenario = old.scenario;
        unused21 = old.unused21;
        unused22 = old.unused22;
        unused23 = old.unused23;
        unused24 = old.unused24;
        unused25 = old.unused25;
        unused26 = old.unused26;
        unused27 = old.unused27;
        unused6 = old.unused6;
        unused7 = old.unused7;
        unused8 = old.unused8;
        unused9 = old.unused9;
        xmaximum = old.xmaximum;
        xminimum = old.xminimum;
        ymaximum = old.ymaximum;
        yminimum = old.yminimum;
        kt = old.kt.clone();
        resp = old.resp.clone();
        t = old.t.clone();
        user = old.user.clone();
    }

    public void print(PrintStream ps) {

        String fmt1 = "%s: %10.4f, %s: %10.4f, %s: %10.4f, %s: %10.4f, %s: %10.4f\n";
        String fmt2 = "%s: %10d, %s: %10d, %s: %10d, %s: %10d, %s: %10d\n";
        String fmt3 = "%s: %8s, %s: %8s, %s: %8s\n";
        String fmt4 = "%s: %10s, %s: %10s, %s: %10s, %s: %10d, %s: %10d\n";
        String fmt5 = "%s: %10d, %s: %10d, %s: %10s, %s: %10s, %s: %10s\n";

        String iftypeS = null;
        if (iftype != null) {
            iftypeS = iftype.name();
        }
        String idepS = null;
        if (idep != null) {
            idepS = idep.name();
        }
        String iztypeS = null;
        if (iztype != null) {
            iztypeS = iztype.name();
        }
        String ievtypS = null;
        if (ievtyp != null) {
            ievtypS = ievtyp.name();
        }
        String iqualS = null;
        if (iqual != null) {
            iqualS = iqual.name();
        }
        String isynthS = null;
        if (isynth != null) {
            isynthS = isynth.name();
        }
        String imagtypS = null;
        if (imagtyp != null) {
            imagtypS = imagtyp.name();
        }
        String imagsrcS = null;
        if (imagsrc != null) {
            imagsrcS = imagsrc.name();
        }
        String ibodyS = null;
        if (ibody != null) {
            ibodyS = ibody.name();
        }

        ps.printf(fmt1, "   delta", delta, "  depmin", depmin, "  depmax", depmax, "   scale", scale, "  odelta", odelta);
        ps.printf(fmt1, "       b", b, "       e", e, "       o", o, "       a", a, "     fmt", fmt);
        ps.printf(fmt1, "      t0", t[0], "      t1", t[1], "      t2", t[2], "      t3", t[3], "      t4", t[4]);
        ps.printf(fmt1, "      t5", t[5], "      t6", t[6], "      t7", t[7], "      t8", t[8], "      t9", t[9]);
        ps.printf(fmt1, "       f", f, "   resp0", resp[0], "   resp1", resp[1], "   resp2", resp[2], "   resp3", resp[3]);
        ps.printf(fmt1, "   resp4", resp[4], "   resp5", resp[5], "   resp6", resp[6], "   resp7", resp[7], "   resp8", resp[8]);
        ps.printf(fmt1, "   resp9", resp[9], "    stla", stla, "    stlo", stlo, "    stel", stel, "    stdp", stdp);
        ps.printf(fmt1, "    evla", evla, "    evlo", evlo, "    evel", evel, "    evdp", evdp, "     mag", mag);
        ps.printf(fmt1, "   user0", user[0], "   user1", user[1], "   user2", user[2], "   user3", user[3], "   user4", user[4]);
        ps.printf(fmt1, "   user5", user[5], "   user6", user[6], "   user7", user[7], "   user8", user[8], "   user9", user[9]);
        ps.printf(fmt1, "    dist", dist, "      az", az, "     baz", baz, "   gcarc", gcarc, "      sb", sb);
        ps.printf(fmt1, "  sdelta", sdelta, "  depmen", depmen, "   cmpaz", cmpaz, "  cmpinc", cmpinc, "xminimum", xminimum);
        ps.printf(fmt1, "xmaximum", xmaximum, "yminimum", yminimum, "ymaximum", ymaximum, " unused6", unused6, " unused7", unused7);
        ps.printf(fmt1, " unused8", unused8, " unused9", unused9, "unused10", unused10, "unused11", unused11, "unused12", unused12);
        ps.printf(fmt2, "  nzyear", nzyear, "  nzjday", nzjday, "  nzhour", nzhour, "   nzmin", nzmin, "   nzsec", nzsec);
        ps.printf(fmt2, "  nzmsec", nzmsec, "   nvhdr", nvhdr, "   norid", norid, "   nevid", nevid, "    npts", npts);
        ps.printf(fmt2, "  nsnpts", nsnpts, "   nwfid", nwfid, "  nxsize", nxsize, "  nysize", nysize, "unused15", unused15);
        ps.printf(fmt4, "  iftype", iftypeS, "    idep", idepS, "  iztype", iztypeS, "unused16", unused16, "   iinst", iinst);
        ps.printf(fmt5, "  istreg", istreg, "  ievreg", ievreg, "  ievtyp", ievtypS, "   iqual", iqualS, "  isynth", isynthS);
        ps.printf(fmt4, " imagtyp", imagtypS, " imagsrc", imagsrcS, "   ibody", ibodyS, "scenario", scenario, "unused21", unused21);
        ps.printf(fmt2, "unused22", unused22, "unused23", unused23, "unused24", unused24, "unused25", unused25, "unused26", unused26);
        ps.printf(fmt2, "   leven", leven, "  lpspol", lpspol, "  lovrok", lovrok, "  lcalda", lcalda, "unused27", unused27);
        ps.printf("%s: %8s, %s: %16s\n", "   kstnm", kstnm, "   kevnm", kevnm);
        ps.printf(fmt3, "   khole", khole, "      ko", ko, "      ka", ka);
        ps.printf(fmt3, "     kt0", kt[0], "     kt1", kt[1], "     kt2", kt[2]);
        ps.printf(fmt3, "     kt3", kt[3], "     kt4", kt[4], "     kt5", kt[5]);
        ps.printf(fmt3, "     kt6", kt[6], "     kt7", kt[7], "     kt8", kt[8]);
        ps.printf(fmt3, "     kt9", kt[9], "      kf", kf, "  kuser0", kuser0);
        ps.printf(fmt3, "  kuser1", kuser1, "  kuser2", kuser2, "  kcmpnm", kcmpnm);
        ps.printf(fmt3, "  knetwk", knetwk, "  kdatrd", kdatrd, "   kinst", kinst);

    }

    public List<Float> getFloatsForWriting() {
        List<Float> floatBuffer = new ArrayList<>();
        floatBuffer.add(formatFloat(getDelta()));
        floatBuffer.add(formatFloat(getDepmin()));
        floatBuffer.add(formatFloat(getDepmax()));
        floatBuffer.add(formatFloat(getScale()));
        floatBuffer.add(formatFloat(getOdelta()));
        floatBuffer.add(formatFloat(getB()));
        floatBuffer.add(formatFloat(getE()));
        floatBuffer.add(formatFloat(getO()));
        floatBuffer.add(formatFloat(getA()));
        floatBuffer.add(formatFloat(getFmt()));

        Double[] tmp = getT();
        for (int i = 0; i < 10; i++) {
            floatBuffer.add(formatFloat(tmp[i]));
        }

        floatBuffer.add(formatFloat(getF()));

        tmp = getResp();
        for (int i = 0; i < 10; i++) {
            floatBuffer.add(formatFloat(tmp[i]));
        }

        floatBuffer.add(formatFloat(getStla()));
        floatBuffer.add(formatFloat(getStlo()));
        floatBuffer.add(formatFloat(getStel()));
        floatBuffer.add(formatFloat(getStdp()));
        floatBuffer.add(formatFloat(getEvla()));
        floatBuffer.add(formatFloat(getEvlo()));
        floatBuffer.add(formatFloat(getEvel()));
        floatBuffer.add(formatFloat(getEvdp()));
        floatBuffer.add(formatFloat(getMag()));

        tmp = getUser();
        for (int i = 0; i < 10; i++) {
            floatBuffer.add(formatFloat(tmp[i]));
        }

        floatBuffer.add(formatFloat(getDist()));
        floatBuffer.add(formatFloat(getAz()));
        floatBuffer.add(formatFloat(getBaz()));
        floatBuffer.add(formatFloat(getGcarc()));
        floatBuffer.add(formatFloat(getSb()));
        floatBuffer.add(formatFloat(getSdelta()));
        floatBuffer.add(formatFloat(getDepmen()));
        floatBuffer.add(formatFloat(getCmpaz()));
        floatBuffer.add(formatFloat(getCmpinc()));
        floatBuffer.add(formatFloat(getXminimum()));
        floatBuffer.add(formatFloat(getXmaximum()));
        floatBuffer.add(formatFloat(getYminimum()));
        floatBuffer.add(formatFloat(getYmaximum()));
        floatBuffer.add(formatFloat(getUnused6()));
        floatBuffer.add(formatFloat(getUnused7()));
        floatBuffer.add(formatFloat(getUnused8()));
        floatBuffer.add(formatFloat(getUnused9()));
        floatBuffer.add(formatFloat(getUnused10()));
        floatBuffer.add(formatFloat(getUnused11()));
        floatBuffer.add(formatFloat(getUnused12()));
        return floatBuffer;
    }

    private float formatFloat(Double v) {
        return v == null ? UNDEFINED_FLOAT : (float) (double) v;
    }

    public List<Integer> getIntsForWriting() {
        List<Integer> intBuffer = new ArrayList<>();
        intBuffer.add(formatInt(getNzyear()));
        intBuffer.add(formatInt(getNzjday()));
        intBuffer.add(formatInt(getNzhour()));
        intBuffer.add(formatInt(getNzmin()));
        intBuffer.add(formatInt(getNzsec()));
        intBuffer.add(formatInt(getNzmsec()));
        intBuffer.add(formatInt(getNvhdr()));
        intBuffer.add(formatInt(getNorid()));
        intBuffer.add(formatInt(getNevid()));
        intBuffer.add(formatInt(getNpts()));
        intBuffer.add(formatInt(getNsnpts()));
        intBuffer.add(formatInt(getNwfid()));
        intBuffer.add(formatInt(getNxsize()));
        intBuffer.add(formatInt(getNysize()));
        intBuffer.add(formatInt(getUnused15()));

        FileType iftypeL = getIftype();
        if (iftypeL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(iftypeL.getCode()));
        }

        DepVarType idepL = getIdep();
        if (idepL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(idepL.getCode()));
        }

        Iztype iztypeL = getIztype();
        if (iztypeL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(iztypeL.getCode()));
        }

        intBuffer.add(formatInt(getUnused16()));
        intBuffer.add(formatInt(getIinst()));
        intBuffer.add(formatInt(getIstreg()));
        intBuffer.add(formatInt(getIevreg()));

        EventType ievtype = getIevtyp();
        if (ievtype == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(ievtype.getCode()));
        }

        DataQuality iqualL = getIqual();
        if (iqualL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(iqualL.getCode()));
        }

        SyntheticsType isynthL = getIsynth();
        if (isynthL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(isynthL.getCode()));
        }

        MagType imagtypL = getImagtyp();
        if (imagtypL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(imagtypL.getCode()));
        }

        MagSource imagsrcL = getImagsrc();
        if (imagsrcL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(imagsrcL.getCode()));
        }

        Body ibodyL = getIbody();
        if (ibodyL == null) {
            intBuffer.add(formatInt(UNDEFINED_INTEGER));
        } else {
            intBuffer.add(formatInt(ibodyL.getCode()));
        }

        intBuffer.add(formatInt(getScenario()));
        intBuffer.add(formatInt(getUnused21()));
        intBuffer.add(formatInt(getUnused22()));
        intBuffer.add(formatInt(getUnused23()));
        intBuffer.add(formatInt(getUnused24()));
        intBuffer.add(formatInt(getUnused25()));
        intBuffer.add(formatInt(getUnused26()));
        intBuffer.add(formatInt(getLeven()));
        intBuffer.add(formatInt(getLpspol()));
        intBuffer.add(formatInt(getLovrok()));
        intBuffer.add(formatInt(getLcalda()));
        intBuffer.add(formatInt(getUnused27()));
        return intBuffer;
    }

    private int formatInt(Integer v) {
        return v == null ? SACHeaderIO.UNDEFINED_INTEGER : (int) v;
    }

    // Getters
    public Double getA() {
        return a;
    }

    public Double getAz() {
        return az;
    }

    public Double getB() {
        return b;
    }

    public Double getBaz() {
        return baz;
    }

    public Double getCmpaz() {
        return cmpaz;
    }

    public Double getCmpinc() {
        return cmpinc;
    }

    public Double getDelta() {
        return delta;
    }

    public Double getDepmax() {
        return depmax;
    }

    public Double getDepmen() {
        return depmen;
    }

    public Double getDepmin() {
        return depmin;
    }

    public Double getDist() {
        return dist;
    }

    public Double getE() {
        return e;
    }

    public Double getEvdp() {
        return evdp;
    }

    public Double getEvel() {
        return evel;
    }

    public Double getEvla() {
        return evla;
    }

    public Double getEvlo() {
        return evlo;
    }

    public Double getF() {
        return f;
    }

    public Double getFmt() {
        return fmt;
    }

    public Double getGcarc() {
        return gcarc;
    }

    public DepVarType getIdep() {
        return idep;
    }

    public Iztype getIztype() {
        return iztype;
    }

    public Integer getIevreg() {
        return ievreg;
    }

    public EventType getIevtyp() {
        return ievtyp;
    }

    public FileType getIftype() {
        return iftype;
    }

    public Integer getIinst() {
        return iinst;
    }

    public MagSource getImagsrc() {
        return imagsrc;
    }

    public MagType getImagtyp() {
        return imagtyp;
    }

    public Integer getIstreg() {
        return istreg;
    }

    public DataQuality getIqual() {
        return iqual;
    }

    public SyntheticsType getIsynth() {
        return isynth;
    }

    public String getKa() {
        return getTrimmedString(ka);
    }

    public String getKcmpnm() {
        return getTrimmedString(kcmpnm);
    }

    public String getKdatrd() {
        return getTrimmedString(kdatrd);
    }

    public String getKevnm() {
        return getTrimmedString(kevnm);
    }

    public String getKf() {
        return getTrimmedString(kf);
    }

    public String getKhole() {
        return getTrimmedString(khole);
    }

    public String getKinst() {
        return getTrimmedString(kinst);
    }

    public String getKnetwk() {
        return getTrimmedString(knetwk);
    }

    public String getKo() {
        return getTrimmedString(ko);
    }

    public String getKstnm() {
        return getTrimmedString(kstnm);
    }

    public String[] getKt() {
        return kt;
    }

    public String getKt(int index) {
        if (index >= 0 && index < kt.length) {
            return getTrimmedString(kt[index]);
        }
        return null;
    }

    public String getKuser0() {
        return getTrimmedString(kuser0);
    }

    public String getKuser1() {
        return getTrimmedString(kuser1);
    }

    public String getKuser2() {
        return getTrimmedString(kuser2);
    }

    /*
    Certain plotting functions may require traces to be shifted by some amount.
    In order to properly report the reference time for plot text fields, all functons
    that return a relative time as a String must be corrected by that amount.
     */
    public String getKzdate(double refTimeAdjustment) {
        Double refTime = getReferenceTime();
        if (refTime == null) {
            return null;
        } else {
            TimeT time = new TimeT(refTime + refTimeAdjustment);
            return time.toString("MMM dd (DDD), yyyy").toUpperCase();
        }
    }

    public String getKzdate() {
        return getKzdate(0.0);
    }

    /*
    Certain plotting functions may require traces to be shifted by some amount.
    In order to properly report the reference time for plot text fields, all functons
    that return a relative time as a String must be corrected by that amount.
     */
    public String getKztime(double refTimeAdjustment) {
        Double refTime = getReferenceTime();
        if (refTime == null) {
            return null;
        } else {
            TimeT time = new TimeT(refTime + refTimeAdjustment);
            return time.toString("HH:mm:ss.SSS");
        }
    }

    public String getKztime() {
        return getKztime(0.0);
    }

    public Integer getLcalda() {
        return lcalda;
    }

    public Integer getLeven() {
        return leven;
    }

    public boolean isEvenlySampled() {
        return leven != null && leven == 1;
    }

    public Integer getLovrok() {
        return lovrok;
    }

    public Integer getLpspol() {
        return lpspol;
    }

    public Double getMag() {
        return mag;
    }

    public Integer getNevid() {
        return nevid;
    }

    public Integer getNorid() {
        return norid;
    }

    public Integer getNpts() {
        return npts;
    }

    public Integer getNsnpts() {
        return nsnpts;
    }

    public Integer getNvhdr() {
        return nvhdr;
    }

    public Integer getNwfid() {
        return nwfid;
    }

    public Integer getNxsize() {
        return nxsize;
    }

    public Integer getNysize() {
        return nysize;
    }

    public Integer getNzhour() {
        return nzhour;
    }

    public Integer getNzjday() {
        return nzjday;
    }

    public Integer getNzmin() {
        return nzmin;
    }

    public Integer getNzmsec() {
        return nzmsec;
    }

    public Integer getNzsec() {
        return nzsec;
    }

    public Integer getNzyear() {
        return nzyear;
    }

    public Double getO() {
        return o;
    }

    public Double getOdelta() {
        return odelta;
    }

    public Double[] getResp() {
        return resp;
    }

    public Double getResp(int index) {
        if (index >= 0 && index < resp.length) {
            return resp[index];
        }
        return null;
    }

    public Double getUser(int index) {
        if (index >= 0 && index < user.length) {
            return user[index];
        }
        return null;
    }

    public Double getSb() {
        return sb;
    }

    public Double getScale() {
        return scale;
    }

    public Double getSdelta() {
        return sdelta;
    }

    public Double getStdp() {
        return stdp;
    }

    public Double getStel() {
        return stel;
    }

    public Double getStla() {
        return stla;
    }

    public Double getStlo() {
        return stlo;
    }

    public Double[] getT() {
        return t;
    }

    public Double getT(int index) {
        if (index >= 0 && index < t.length) {
            return t[index];
        }
        return null;
    }

    public Double getUnused10() {
        return unused10;
    }

    public Double getUnused11() {
        return unused11;
    }

    public Double getUnused12() {
        return unused12;
    }

    public Integer getUnused15() {
        return unused15;
    }

    public Integer getUnused16() {
        return unused16;
    }

    public Body getIbody() {
        return ibody;
    }

    public Integer getScenario() {
        return scenario;
    }

    public Integer getUnused21() {
        return unused21;
    }

    public Integer getUnused22() {
        return unused22;
    }

    public Integer getUnused23() {
        return unused23;
    }

    public Integer getUnused24() {
        return unused24;
    }

    public Integer getUnused25() {
        return unused25;
    }

    public Integer getUnused26() {
        return unused26;
    }

    public Integer getUnused27() {
        return unused27;
    }

    public Double getUnused6() {
        return unused6;
    }

    public Double getUnused7() {
        return unused7;
    }

    public Double getUnused8() {
        return unused8;
    }

    public Double getUnused9() {
        return unused9;
    }

    public Double[] getUser() {
        return user;
    }

    public Double getXmaximum() {
        return xmaximum;
    }

    public Double getXminimum() {
        return xminimum;
    }

    public Double getYmaximum() {
        return ymaximum;
    }

    public Double getYminimum() {
        return yminimum;
    }

    private String getTrimmedString(String in) {
        return in != null ? in.trim() : null;
    }

    private String trimString(String in) {
        return in != null ? in.trim() : null;
    }

    public Double getReferenceTime() {

        if (nzyear == null || nzjday == null || nzhour == null || nzmin == null || nzsec == null || nzmsec == null) {
            return null;
        } else {
            TimeT tmp = new TimeT(nzyear, nzjday, nzhour, nzmin, nzsec, nzmsec);
            return tmp.getEpochTime();
        }

    }

    public void setReferenceTime(Double refTime) {
        if (refTime == null) {
            nzyear = null;
            nzjday = null;
            nzhour = null;
            nzmin = null;
            nzsec = null;
            nzmsec = null;
        } else {
            TimeT tmp = new TimeT(refTime);
            nzyear = tmp.getYear();
            nzjday = tmp.getJDay();
            nzhour = tmp.getHour();
            nzmin = tmp.getMinute();
            nzsec = tmp.getSec();
            double seconds = tmp.getSecond();
            double frac = seconds - nzsec;
            nzmsec = (int) Math.round(frac * 1000);
        }
    }

    public void setReferenceTime(int year, int jday, int hour, int min, int sec, int msec) {
        nzyear = year;
        nzjday = jday;
        nzhour = hour;
        nzmin = min;
        nzsec = sec;
        nzmsec = msec;
    }

    public void shiftAllT(double value) {
        if (b != null) {
            b += value;
        }
        if (e != null) {
            e += value;
        }
        if (o != null) {
            o += value;
        }
        if (a != null) {
            a += value;
        }
        if (f != null) {
            f += value;
        }
        for (int j = 0; j < t.length; ++j) {
            if (t[j] != null) {
                t[j] += value;
            }
        }
    }

    public void setAllT(double value) {
        if (b != null) {
            b += value;
        }
        if (e != null) {
            e += value;
        }
        if (o != null) {
            o += value;
        }
        if (a != null) {
            a += value;
        }
        if (f != null) {
            f += value;
        }
        for (int j = 0; j < t.length; ++j) {
            if (t[j] != null) {
                t[j] += value;
            }
        }
        Double refTime = getReferenceTime();
        if (refTime != null) {
            refTime -= value;
        } else {
            refTime = 0d;
        }
        setReferenceTime(refTime);
    }

    public Instant getReferenceTimeAsInstant() {
        return Instant.ofEpochMilli(Math.round(getReferenceTime() * 1000.0));
    }

    public Double getBeginTime() {

        Double retval = getReferenceTime();
        if (retval != null) {
            retval += b;
        }

        return retval;
    }

    public Instant getBeginTimeAsInstant() {
        return Instant.ofEpochMilli(Math.round(getBeginTime() * 1000.0));
    }

    public StreamKey getStreamKey() {
        return new StreamKey(getKnetwk(), getKstnm(), getKcmpnm(), getKhole());
    }

    public Double getSamprate() {
        Double d = getDelta();
        return d != null && d != 0 ? 1.0 / d : null;
    }

    // Setters
    public void setA(Double a) {
        this.a = a;
    }

    public void setAz(Double az) {
        this.az = az;
    }

    public void setB(Double b) {
        this.b = b;
    }

    public void setBaz(Double baz) {
        this.baz = baz;
    }

    public void setCmpaz(Double cmpaz) {
        this.cmpaz = cmpaz;
    }

    public void setCmpinc(Double cmpinc) {
        this.cmpinc = cmpinc;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public void setDepmax(Double depmax) {
        this.depmax = depmax;
    }

    public void setDepmen(Double depmen) {
        this.depmen = depmen;
    }

    public void setDepmin(Double depmin) {
        this.depmin = depmin;
    }

    public void setDist(Double dist) {
        this.dist = dist;
    }

    public void setE(Double e) {
        this.e = e;
    }

    public void setEvdp(Double evdp) {
        this.evdp = evdp;
    }

    public void setEvel(Double evel) {
        this.evel = evel;
    }

    public void setEvla(Double evla) {
        this.evla = evla;
        maybeUpdateDistAz();
    }

    public void setEvlo(Double evlo) {
        this.evlo = evlo;
        maybeUpdateDistAz();
    }

    public void setF(Double f) {
        this.f = f;
    }

    public void setFmt(Double fmt) {
        this.fmt = fmt;
    }

    public void setGcarc(Double gcarc) {
        this.gcarc = gcarc;
    }

    public void setIdep(DepVarType idep) {
        this.idep = idep;
    }

    public void setIztype(Iztype iztype) {
        this.iztype = iztype;
    }

    public void setIevreg(Integer ievreg) {
        this.ievreg = ievreg;
    }

    public void setIevtyp(EventType ievtyp) {
        this.ievtyp = ievtyp;
    }

    public void setIftype(FileType iftype) {
        this.iftype = iftype;
    }

    public void setIinst(Integer iinst) {
        this.iinst = iinst;
    }

    public void setImagsrc(MagSource imagsrc) {
        this.imagsrc = imagsrc;
    }

    public void setImagtyp(MagType imagtyp) {
        this.imagtyp = imagtyp;
    }

    public void setIstreg(Integer instreg) {
        this.istreg = instreg;
    }

    public void setIqual(DataQuality iqual) {
        this.iqual = iqual;
    }

    public void setIsynth(SyntheticsType isynth) {
        this.isynth = isynth;
    }

    public void setKa(String ka) {
        this.ka = trimString(ka);
    }

    public void setKcmpnm(String kcmpnm) {
        this.kcmpnm = trimString(kcmpnm);
    }

    public void setKdatrd(String kdatrd) {
        this.kdatrd = trimString(kdatrd);
    }

    public void setKdatrd() {
        TimeT time = new TimeT(); // current instant
        kdatrd = time.toString("yyyyMMdd");
    }

    public void setKevnm(String kevnm) {
        this.kevnm = trimString(kevnm);
    }

    public void setKf(String kf) {
        this.kf = trimString(kf);
    }

    public void setKhole(String khole) {
        this.khole = trimString(khole);
    }

    public void setKinst(String kinst) {
        this.kinst = trimString(kinst);
    }

    public void setKnetwk(String knetwk) {
        this.knetwk = trimString(knetwk);
    }

    public void setKo(String ko) {
        this.ko = trimString(ko);
    }

    public void setKstnm(String kstnm) {
        this.kstnm = trimString(kstnm);
    }

    public void setKt(String[] kt) {
        this.kt = kt;
    }

    public void setKt(int j, String value) {
        if (j >= 0 && j < 10) {
            kt[j] = trimString(value);
        }
    }

    public void setKuser0(String kuser0) {
        this.kuser0 = trimString(kuser0);
    }

    public void setKuser1(String kuser1) {
        this.kuser1 = trimString(kuser1);
    }

    public void setKuser2(String kuser2) {
        this.kuser2 = trimString(kuser2);
    }

    public void setLcalda(Integer lcalda) {
        this.lcalda = lcalda;
    }

    public void setLeven(Integer leven) {
        this.leven = leven;
    }

    public void setLovrok(Integer lovrok) {
        this.lovrok = lovrok;
    }

    public void setLpspol(Integer lpspol) {
        this.lpspol = lpspol;
    }

    public void setMag(Double mag) {
        this.mag = mag;
    }

    public void setNevid(Integer nevid) {
        this.nevid = nevid;
    }

    public void setNorid(Integer norid) {
        this.norid = norid;
    }

    public void setNpts(Integer npts) {
        this.npts = npts;
    }

    public void setNsnpts(Integer nsnpts) {
        this.nsnpts = nsnpts;
    }

    public void setNvhdr(Integer nvhdr) {
        this.nvhdr = nvhdr;
    }

    public void setNwfid(Integer nwfid) {
        this.nwfid = nwfid;
    }

    public void setNxsize(Integer nxsize) {
        this.nxsize = nxsize;
    }

    public void setNysize(Integer nysize) {
        this.nysize = nysize;
    }

    public void setNzhour(Integer nzhour) {
        this.nzhour = nzhour;
    }

    public void setNzjday(Integer nzjday) {
        this.nzjday = nzjday;
    }

    public void setNzmin(Integer nzmin) {
        this.nzmin = nzmin;
    }

    public void setNzmsec(Integer nzmsec) {
        this.nzmsec = nzmsec;
    }

    public void setNzsec(Integer nzsec) {
        this.nzsec = nzsec;
    }

    public void setNzyear(Integer nzyear) {
        this.nzyear = nzyear;
    }

    public void setO(Double o) {
        this.o = o;
    }

    public void setOdelta(Double odelta) {
        this.odelta = odelta;
    }

    public void setResp(Double[] resp) {
        this.resp = resp;
    }

    public void setResp(int index, Double value) {
        if (index >= 0 && index < 10) {
            resp[index] = value;
        }
    }

    public void setUser(int index, Double value) {
        if (index >= 0 && index < 10) {
            user[index] = value;
        }
    }

    public void setSb(Double sb) {
        this.sb = sb;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public void setSdelta(Double sdelta) {
        this.sdelta = sdelta;
    }

    public void setStdp(Double stdp) {
        this.stdp = stdp;
    }

    public void setStel(Double stel) {
        this.stel = stel;
    }

    public void setStla(Double stla) {
        this.stla = stla;
        maybeUpdateDistAz();
    }

    public void setStlo(Double stlo) {
        this.stlo = stlo;
        maybeUpdateDistAz();
    }

    public void setT(Double[] t) {
        this.t = t;
    }

    public void setT(int index, Double v) {
        if (index >= 0 && index < 10) {
            t[index] = v;
        }
    }

    public void setUnused10(Double unused10) {
        this.unused10 = unused10;
    }

    public void setUnused11(Double unused11) {
        this.unused11 = unused11;
    }

    public void setUnused12(Double unused12) {
        this.unused12 = unused12;
    }

    public void setUnused15(Integer unused15) {
        this.unused15 = unused15;
    }

    public void setUnused16(Integer unused16) {
        this.unused16 = unused16;
    }

    public void setIbody(Body ibody) {
        this.ibody = ibody;
    }

    public void setScenario(Integer scenario) {
        this.scenario = scenario;
    }

    public void setUnused21(Integer unused21) {
        this.unused21 = unused21;
    }

    public void setUnused22(Integer unused22) {
        this.unused22 = unused22;
    }

    public void setUnused23(Integer unused23) {
        this.unused23 = unused23;
    }

    public void setUnused24(Integer unused24) {
        this.unused24 = unused24;
    }

    public void setUnused25(Integer unused25) {
        this.unused25 = unused25;
    }

    public void setUnused26(Integer unused26) {
        this.unused26 = unused26;
    }

    public void setUnused27(Integer unused27) {
        this.unused27 = unused27;
    }

    public void setUnused6(Double unused6) {
        this.unused6 = unused6;
    }

    public void setUnused7(Double unused7) {
        this.unused7 = unused7;
    }

    public void setUnused8(Double unused8) {
        this.unused8 = unused8;
    }

    public void setUnused9(Double unused9) {
        this.unused9 = unused9;
    }

    public void setUser(Double[] user) {
        this.user = user;
    }

    public void setXmaximum(Double xmaximum) {
        this.xmaximum = xmaximum;
    }

    public void setXminimum(Double xminimum) {
        this.xminimum = xminimum;
    }

    public void setYmaximum(Double ymaximum) {
        this.ymaximum = ymaximum;
    }

    public void setYminimum(Double yminimum) {
        this.yminimum = yminimum;
    }

    public boolean hasEventInfo() {
        return (evla != null && evlo != null);
    }

    public boolean hasStationInfo() {
        return (stla != null && stlo != null);
    }

    public Double getBackAzimuth() {
        return hasEventInfo() && hasStationInfo() ? EModel.getAzimuthWGS84(stla, stlo, evla, evlo) : null;
    }

    public Double getDistanceKm() {
        return hasEventInfo() && hasStationInfo() ? EModel.getDistanceWGS84(stla, stlo, evla, evlo) : null;
    }

    public Double getDistanceDeg() {
        return hasEventInfo() && hasStationInfo() ? EModel.getDeltaWGS84(stla, stlo, evla, evlo) : null;
    }

    public void maybeUpdateDistAz() {
        if (hasEventInfo() && hasStationInfo()) {
            dist = EModel.getDistanceWGS84(stla, stlo, evla, evlo);
            baz = EModel.getAzimuthWGS84(stla, stlo, evla, evlo);
            az = EModel.getAzimuthWGS84(evla, evlo, stla, stlo);
            gcarc = EModel.getDeltaWGS84(stla, stlo, evla, evlo);
        }
    }

    public String getTString(int idx) {
        String markername = String.format("T%dMARKER", idx);
        Double tt = t[idx];
        if (tt == null) {
            return String.format("%8s =  UNDEFINED", markername);
        }
        String k = kt[idx];
        if (k == null) {
            return String.format("%8s =  %-7.2f", markername, tt);
        } else {
            return String.format("%8s =  %-7.2f          (%s)", markername, tt, k.trim());
        }

    }

    private static final Map<String, Method> NAME_SETTER_MAP = new HashMap<>();
    private static final Map<String, Method> NAME_GETTER_MAP = new HashMap<>();
    private static final Set<String> RESTRICTED_HEADER_VARIABLES = new HashSet<>();
    private static final Set<String> SETTABLE_FIELDS = new HashSet<>();
    private static final Set<String> TIME_OFFSET_FIELDS = new LinkedHashSet<>();
    private static final Set<String> UNEXPORTED_FIELDS = new HashSet<>();

    public static Collection<String> getSettableFields() {
        return new ArrayList<>(SETTABLE_FIELDS);
    }

    static {
        UNEXPORTED_FIELDS.add("LOG");
        UNEXPORTED_FIELDS.add("NAME_SETTER_MAP");
        UNEXPORTED_FIELDS.add("NAME_GETTER_MAP");
        UNEXPORTED_FIELDS.add("RESTRICTED_HEADER_VARIABLES");
        UNEXPORTED_FIELDS.add("SETTABLE_FIELDS");
        UNEXPORTED_FIELDS.add("UNEXPORTED_FIELDS");
        UNEXPORTED_FIELDS.add("DEFAULT_NVHDR");
        UNEXPORTED_FIELDS.add("IRIS_NVHDR");
        UNEXPORTED_FIELDS.add("KDATRD");
        UNEXPORTED_FIELDS.add("KT");
        UNEXPORTED_FIELDS.add("T");
        UNEXPORTED_FIELDS.add("USER");
        UNEXPORTED_FIELDS.add("RESP");
        UNEXPORTED_FIELDS.add("TIME_OFFSET_FIELDS");

        Class<SACHeader> c = SACHeader.class;
        Method[] methods = c.getDeclaredMethods();
        for (Method m : methods) {
            String tmp = m.getName().toUpperCase();
            if (tmp.indexOf("SET") == 0) {
                NAME_SETTER_MAP.put(tmp.substring(3), m);
            } else if (tmp.indexOf("GET") == 0) {
                NAME_GETTER_MAP.put(tmp.substring(3), m);
            }
        }

        String[] tmp = {"NVHDR", "NPTS", "LCALDA", "LOVROK"};
        RESTRICTED_HEADER_VARIABLES.addAll(Arrays.asList(tmp));

        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            String tmpf = f.getName().toUpperCase();
            if (!RESTRICTED_HEADER_VARIABLES.contains(tmpf) && !UNEXPORTED_FIELDS.contains(tmpf)) {
                SETTABLE_FIELDS.add(tmpf);
            }
        }
        String[] tmp2 = {"B", "E", "O", "A", "F", "T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9"};
        TIME_OFFSET_FIELDS.addAll(Arrays.asList(tmp2));
    }

    public static boolean isValidTimeOffsetFieldName(String name) {
        return TIME_OFFSET_FIELDS.contains(name.toUpperCase());
    }

    public static String getTimeOffsetFieldNames() {
        StringBuilder sb = new StringBuilder();
        int n = TIME_OFFSET_FIELDS.size();
        Iterator<String> it = TIME_OFFSET_FIELDS.iterator();
        int j = 1;
        while (it.hasNext()) {
            sb.append(it.next());
            if (j++ < n) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static Type getTypeForField(String fieldName) {
        Method m = NAME_SETTER_MAP.get(fieldName.toUpperCase());
        if (m != null) {
            Type[] types = m.getGenericParameterTypes();
            if (types != null && types.length == 1) {
                return types[0];
            }
        }
        return null;
    }

    public static boolean isSettableField(String field) {
        return SETTABLE_FIELDS.contains(field);
    }

    public void setHeaderValue(String fieldName, Object value) {
        if (value == null || fieldName == null || fieldName.isEmpty()) {
            return;
        }
        fieldName = fieldName.toUpperCase();
        if (RESTRICTED_HEADER_VARIABLES.contains(fieldName)) {
            log.warn("Cannot change header field: {}", fieldName);
            return; // Not allowed to change these with CHNHDR command.
        }

        if (value instanceof TimeT && TIME_OFFSET_FIELDS.contains(fieldName)) {
            double change;
            TimeT tmp = (TimeT) value;
            Double currentRefTime = getReferenceTime();
            if (currentRefTime != null) { // Shift all offsets by difference in reftime except for the target field which is set to zero.
                change = tmp.getEpochTime() - currentRefTime;
                setReferenceTime(tmp.getEpochTime());
                setOneFieldValue(fieldName, 0);
            } else { // No current reftime so just set reftime, set target field to zero and leave the rest.
                setReferenceTime(tmp.getEpochTime());
                change = 0.0;
                setOneFieldValue(fieldName, change);
                return;
            }

            // If value is not zero then update as required B, E, O, A, F, and Tn
            if (change != 0.0) {
                for (String aName : TIME_OFFSET_FIELDS) {
                    if (!aName.equalsIgnoreCase(fieldName)) {
                        Double v = (Double) getValue(aName);
                        if (v != null) {
                            setHeaderValue(aName, v - change);
                        }
                    }
                }

            }
            return;
        }
        String name = fieldName.toUpperCase();
        if (maybeSetKT(name, value)) {
            return;
        } else if (maybeSetT(name, value)) {
            return;
        } else if (maybeSetResp(name, value)) {
            return;
        } else if (maybeSetUser(name, value)) {
            return;
        }
        setOneFieldValue(name, value);
    }

    private void setOneFieldValue(String name, Object value) {
        Method m = NAME_SETTER_MAP.get(name);
        if (m != null) {
            Type[] types = m.getGenericParameterTypes();
            if (types != null && types.length == 1) {
                try {
                    Type type = types[0];
                    if (value.getClass().equals(type)) {
                        try {
                            m.invoke(this, value);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            log.warn("Failed to change header value for : {}", name);
                        }
                    } else if (value instanceof UndefHeaderValue) {
                        m.invoke(this, (Object) null);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.warn("Failed to change header value for : {}", name);
                }
            }
        }
    }

    private boolean maybeSetKT(String name, Object value) throws NumberFormatException {
        if (name.length() == 3 && name.indexOf("KT") == 0) {
            String tmp = name.substring(2);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                if (value instanceof UndefHeaderValue) {
                    setKt(index, null);
                } else if (value instanceof String) {
                    this.setKt(index, (String) value);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean maybeSetT(String name, Object value) throws NumberFormatException {
        if (name.length() == 2 && name.indexOf("T") == 0) {
            String tmp = name.substring(1);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                if (value instanceof UndefHeaderValue) {
                    setT(index, null);
                } else if (value instanceof Double) {
                    setT(index, (Double) value);
                    return true;
                } else if (value instanceof Float) {
                    float v = (Float) value;
                    double d = v;
                    setT(index, d);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean maybeSetResp(String name, Object value) throws NumberFormatException {
        if (name.length() == 5 && name.indexOf("RESP") == 0) {
            String tmp = name.substring(4);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                if (value instanceof UndefHeaderValue) {
                    setResp(index, null);
                } else if (value instanceof Double) {
                    this.setResp(index, (Double) value);
                    return true;
                } else if (value instanceof Float) {
                    float v = (Float) value;
                    double d = v;
                    setResp(index, d);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean maybeSetUser(String name, Object value) throws NumberFormatException {
        if (name.length() == 5 && name.indexOf("USER") == 0) {
            String tmp = name.substring(4);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                if (value instanceof UndefHeaderValue) {
                    setUser(index, null);
                } else if (value instanceof Double) {
                    this.setUser(index, (Double) value);
                    return true;
                } else if (value instanceof Float) {
                    float v = (Float) value;
                    double d = v;
                    setUser(index, d);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isHeaderField(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        String name = fieldName.toUpperCase();
        if (name.equals("T")) {
            return false;
        } else if (name.equals("KT")) {
            return false;
        } else if (name.equals("RESP")) {
            return false;
        } else if (name.equals("USER")) {
            return false;
        }
        if (name.length() == 3 && name.indexOf("KT") == 0) {
            String tmp = name.substring(2);
            if (NumberUtils.isParsable(tmp)) {
                return true;
            }
        }

        if (name.length() == 2 && name.indexOf("T") == 0) {
            String tmp = name.substring(1);
            if (NumberUtils.isParsable(tmp)) {
                return true;
            }
        }
        if (name.length() == 5 && name.indexOf("RESP") == 0) {
            String tmp = name.substring(4);
            if (NumberUtils.isParsable(tmp)) {
                return true;
            }
        }
        if (name.length() == 5 && name.indexOf("USER") == 0) {
            String tmp = name.substring(4);
            if (NumberUtils.isParsable(tmp)) {
                return true;
            }
        }
        if (name.equals("KZDATE")) {
            return true;
        }
        if (name.equals("KZTIME")) {
            return true;
        }

        if (name.equals("KSTCMP")) {
            return true;
        }
        Method m = NAME_GETTER_MAP.get(name);
        return m != null;
    }

    public Object getValue(String fieldName) {
        return getValue(fieldName, 0.0);
    }

    /*
    For certain plotting purposes it may be necessary to report relative time Strings
    that have been adjusted to account for traces shifted in time in the plot axis.
     */
    public Object getValue(String fieldName, double refTimeAdjustment) {
        if (fieldName == null || fieldName.isEmpty()) {
            return null;
        }

        String name = fieldName.toUpperCase();
        if (name.length() == 3 && name.indexOf("KT") == 0) {
            String tmp = name.substring(2);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                return getKt(index);
            }
        }
        if (name.length() == 2 && name.indexOf("T") == 0) {
            String tmp = name.substring(1);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                return getT(index);
            }
        }
        if (name.length() == 5 && name.indexOf("RESP") == 0) {
            String tmp = name.substring(4);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                return getResp(index);
            }
        }
        if (name.length() == 5 && name.indexOf("USER") == 0) {
            String tmp = name.substring(4);
            if (NumberUtils.isParsable(tmp)) {
                int index = Integer.parseInt(tmp);
                return getUser(index);
            }
        }
        if (name.equals("KZDATE")) {
            return getKzdate(refTimeAdjustment);
        }
        if (name.equals("KZTIME")) {
            return getKztime(refTimeAdjustment);
        }

        if (name.equals("KSTCMP")) {
            String s0 = knetwk;
            if (s0 == null) {
                s0 = "";
            }
            String s1 = kstnm;
            if (s1 == null) {
                s1 = "";
            }
            String s2 = kcmpnm;
            if (s2 == null) {
                s2 = "";
            }
            String s3 = khole;
            if (s3 == null) {
                s3 = "";
            }
            String result = String.format("%8s%8s%8s%8s", s0, s1, s2, s3);
            return result.trim();
        }

        Method m = NAME_GETTER_MAP.get(name);
        if (m != null) {

            try {
                return m.invoke(this);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.warn("Failed to get header value for : {}", name);
            }

        }
        return null;
    }

    public void updateForFFT(SpectralData result) {
        sb = b;
        nsnpts = npts;
        sdelta = delta;
        b = result.getMinFreq();
        e = result.getMaxPositiveFreq();
        npts = result.getSize();
        delta = result.getDelfreq();
        if (result.getPresentationFormat() == PresentationFormat.AmplitudePhase) {
            iftype = FileType.IAMPH;
        } else {
            iftype = FileType.IRLIM;
        }
    }

    public void updateForIFFT() {
        b = sb;
        npts = nsnpts;
        delta = sdelta;
        e = b + (npts - 1) * delta;
        iftype = FileType.ITIME;
    }

    public String createFileName() {
        StringBuilder stringBuilder = new StringBuilder();

        if (knetwk != null) {
            stringBuilder.append(knetwk).append(".");
        }
        if (kstnm != null) {
            stringBuilder.append(kstnm).append(".");
        }
        if (kcmpnm != null) {
            stringBuilder.append(kcmpnm).append(".");
        }

        if (khole != null) {
            stringBuilder.append(khole).append(".");
        }
        stringBuilder.append(getReferenceTime());
        return stringBuilder.toString();
    }

    public Epoch getEpoch() {
        Double refTime = getReferenceTime();
        if (refTime == null) {
            refTime = 0.0;
        }
        if (b == null || e == null) {
            return null;
        }
        double begin = refTime + b;
        double end = refTime + e;
        return new Epoch(begin, end);
    }

    public static class UndefHeaderValue { // Used during parsing CHNHDR to flag fields to be undefined.

        public static String getFlagName() {
            return "UNDEF";
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.a);
        hash = 97 * hash + Objects.hashCode(this.az);
        hash = 97 * hash + Objects.hashCode(this.b);
        hash = 97 * hash + Objects.hashCode(this.baz);
        hash = 97 * hash + Objects.hashCode(this.cmpaz);
        hash = 97 * hash + Objects.hashCode(this.cmpinc);
        hash = 97 * hash + Objects.hashCode(this.delta);
        hash = 97 * hash + Objects.hashCode(this.depmax);
        hash = 97 * hash + Objects.hashCode(this.depmen);
        hash = 97 * hash + Objects.hashCode(this.depmin);
        hash = 97 * hash + Objects.hashCode(this.dist);
        hash = 97 * hash + Objects.hashCode(this.e);
        hash = 97 * hash + Objects.hashCode(this.evdp);
        hash = 97 * hash + Objects.hashCode(this.evel);
        hash = 97 * hash + Objects.hashCode(this.evla);
        hash = 97 * hash + Objects.hashCode(this.evlo);
        hash = 97 * hash + Objects.hashCode(this.f);
        hash = 97 * hash + Objects.hashCode(this.fmt);
        hash = 97 * hash + Objects.hashCode(this.gcarc);
        hash = 97 * hash + Objects.hashCode(this.idep);
        hash = 97 * hash + Objects.hashCode(this.iztype);
        hash = 97 * hash + Objects.hashCode(this.ievreg);
        hash = 97 * hash + Objects.hashCode(this.ievtyp);
        hash = 97 * hash + Objects.hashCode(this.iftype);
        hash = 97 * hash + Objects.hashCode(this.iinst);
        hash = 97 * hash + Objects.hashCode(this.imagsrc);
        hash = 97 * hash + Objects.hashCode(this.imagtyp);
        hash = 97 * hash + Objects.hashCode(this.istreg);
        hash = 97 * hash + Objects.hashCode(this.iqual);
        hash = 97 * hash + Objects.hashCode(this.isynth);
        hash = 97 * hash + Objects.hashCode(this.ka);
        hash = 97 * hash + Objects.hashCode(this.kcmpnm);
        hash = 97 * hash + Objects.hashCode(this.kdatrd);
        hash = 97 * hash + Objects.hashCode(this.kevnm);
        hash = 97 * hash + Objects.hashCode(this.kf);
        hash = 97 * hash + Objects.hashCode(this.khole);
        hash = 97 * hash + Objects.hashCode(this.kinst);
        hash = 97 * hash + Objects.hashCode(this.knetwk);
        hash = 97 * hash + Objects.hashCode(this.ko);
        hash = 97 * hash + Objects.hashCode(this.kstnm);
        hash = 97 * hash + Arrays.deepHashCode(this.kt);
        hash = 97 * hash + Objects.hashCode(this.kuser0);
        hash = 97 * hash + Objects.hashCode(this.kuser1);
        hash = 97 * hash + Objects.hashCode(this.kuser2);
        hash = 97 * hash + Objects.hashCode(this.lcalda);
        hash = 97 * hash + Objects.hashCode(this.leven);
        hash = 97 * hash + Objects.hashCode(this.lovrok);
        hash = 97 * hash + Objects.hashCode(this.lpspol);
        hash = 97 * hash + Objects.hashCode(this.mag);
        hash = 97 * hash + Objects.hashCode(this.nevid);
        hash = 97 * hash + Objects.hashCode(this.norid);
        hash = 97 * hash + Objects.hashCode(this.npts);
        hash = 97 * hash + Objects.hashCode(this.nsnpts);
        hash = 97 * hash + Objects.hashCode(this.nvhdr);
        hash = 97 * hash + Objects.hashCode(this.nwfid);
        hash = 97 * hash + Objects.hashCode(this.nxsize);
        hash = 97 * hash + Objects.hashCode(this.nysize);
        hash = 97 * hash + Objects.hashCode(this.nzhour);
        hash = 97 * hash + Objects.hashCode(this.nzjday);
        hash = 97 * hash + Objects.hashCode(this.nzmin);
        hash = 97 * hash + Objects.hashCode(this.nzmsec);
        hash = 97 * hash + Objects.hashCode(this.nzsec);
        hash = 97 * hash + Objects.hashCode(this.nzyear);
        hash = 97 * hash + Objects.hashCode(this.o);
        hash = 97 * hash + Objects.hashCode(this.odelta);
        hash = 97 * hash + Arrays.deepHashCode(this.resp);
        hash = 97 * hash + Objects.hashCode(this.sb);
        hash = 97 * hash + Objects.hashCode(this.scale);
        hash = 97 * hash + Objects.hashCode(this.sdelta);
        hash = 97 * hash + Objects.hashCode(this.stdp);
        hash = 97 * hash + Objects.hashCode(this.stel);
        hash = 97 * hash + Objects.hashCode(this.stla);
        hash = 97 * hash + Objects.hashCode(this.stlo);
        hash = 97 * hash + Arrays.deepHashCode(this.t);
        hash = 97 * hash + Objects.hashCode(this.unused10);
        hash = 97 * hash + Objects.hashCode(this.unused11);
        hash = 97 * hash + Objects.hashCode(this.unused12);
        hash = 97 * hash + Objects.hashCode(this.unused15);
        hash = 97 * hash + Objects.hashCode(this.unused16);
        hash = 97 * hash + Objects.hashCode(this.ibody);
        hash = 97 * hash + Objects.hashCode(this.scenario);
        hash = 97 * hash + Objects.hashCode(this.unused21);
        hash = 97 * hash + Objects.hashCode(this.unused22);
        hash = 97 * hash + Objects.hashCode(this.unused23);
        hash = 97 * hash + Objects.hashCode(this.unused24);
        hash = 97 * hash + Objects.hashCode(this.unused25);
        hash = 97 * hash + Objects.hashCode(this.unused26);
        hash = 97 * hash + Objects.hashCode(this.unused27);
        hash = 97 * hash + Objects.hashCode(this.unused6);
        hash = 97 * hash + Objects.hashCode(this.unused7);
        hash = 97 * hash + Objects.hashCode(this.unused8);
        hash = 97 * hash + Objects.hashCode(this.unused9);
        hash = 97 * hash + Arrays.deepHashCode(this.user);
        hash = 97 * hash + Objects.hashCode(this.xmaximum);
        hash = 97 * hash + Objects.hashCode(this.xminimum);
        hash = 97 * hash + Objects.hashCode(this.ymaximum);
        hash = 97 * hash + Objects.hashCode(this.yminimum);
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
        final SACHeader other = (SACHeader) obj;
        if (!Objects.equals(this.ka, other.ka)) {
            return false;
        }
        if (!Objects.equals(this.kcmpnm, other.kcmpnm)) {
            return false;
        }
        if (!Objects.equals(this.kdatrd, other.kdatrd)) {
            return false;
        }
        if (!Objects.equals(this.kevnm, other.kevnm)) {
            return false;
        }
        if (!Objects.equals(this.kf, other.kf)) {
            return false;
        }
        if (!Objects.equals(this.khole, other.khole)) {
            return false;
        }
        if (!Objects.equals(this.kinst, other.kinst)) {
            return false;
        }
        if (!Objects.equals(this.knetwk, other.knetwk)) {
            return false;
        }
        if (!Objects.equals(this.ko, other.ko)) {
            return false;
        }
        if (!Objects.equals(this.kstnm, other.kstnm)) {
            return false;
        }
        if (!Objects.equals(this.kuser0, other.kuser0)) {
            return false;
        }
        if (!Objects.equals(this.kuser1, other.kuser1)) {
            return false;
        }
        if (!Objects.equals(this.kuser2, other.kuser2)) {
            return false;
        }
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        if (!Objects.equals(this.az, other.az)) {
            return false;
        }
        if (!Objects.equals(this.b, other.b)) {
            return false;
        }
        if (!Objects.equals(this.baz, other.baz)) {
            return false;
        }
        if (!Objects.equals(this.cmpaz, other.cmpaz)) {
            return false;
        }
        if (!Objects.equals(this.cmpinc, other.cmpinc)) {
            return false;
        }
        if (!Objects.equals(this.delta, other.delta)) {
            return false;
        }
        if (!Objects.equals(this.depmax, other.depmax)) {
            return false;
        }
        if (!Objects.equals(this.depmen, other.depmen)) {
            return false;
        }
        if (!Objects.equals(this.depmin, other.depmin)) {
            return false;
        }
        if (!Objects.equals(this.dist, other.dist)) {
            return false;
        }
        if (!Objects.equals(this.e, other.e)) {
            return false;
        }
        if (!Objects.equals(this.evdp, other.evdp)) {
            return false;
        }
        if (!Objects.equals(this.evel, other.evel)) {
            return false;
        }
        if (!Objects.equals(this.evla, other.evla)) {
            return false;
        }
        if (!Objects.equals(this.evlo, other.evlo)) {
            return false;
        }
        if (!Objects.equals(this.f, other.f)) {
            return false;
        }
        if (!Objects.equals(this.fmt, other.fmt)) {
            return false;
        }
        if (!Objects.equals(this.gcarc, other.gcarc)) {
            return false;
        }
        if (this.idep != other.idep) {
            return false;
        }
        if (this.iztype != other.iztype) {
            return false;
        }
        if (!Objects.equals(this.ievreg, other.ievreg)) {
            return false;
        }
        if (this.ievtyp != other.ievtyp) {
            return false;
        }
        if (this.iftype != other.iftype) {
            return false;
        }
        if (!Objects.equals(this.iinst, other.iinst)) {
            return false;
        }
        if (this.imagsrc != other.imagsrc) {
            return false;
        }
        if (this.imagtyp != other.imagtyp) {
            return false;
        }
        if (!Objects.equals(this.istreg, other.istreg)) {
            return false;
        }
        if (this.iqual != other.iqual) {
            return false;
        }
        if (this.isynth != other.isynth) {
            return false;
        }
        if (!Arrays.deepEquals(this.kt, other.kt)) {
            return false;
        }
        if (!Objects.equals(this.lcalda, other.lcalda)) {
            return false;
        }
        if (!Objects.equals(this.leven, other.leven)) {
            return false;
        }
        if (!Objects.equals(this.lovrok, other.lovrok)) {
            return false;
        }
        if (!Objects.equals(this.lpspol, other.lpspol)) {
            return false;
        }
        if (!Objects.equals(this.mag, other.mag)) {
            return false;
        }
        if (!Objects.equals(this.nevid, other.nevid)) {
            return false;
        }
        if (!Objects.equals(this.norid, other.norid)) {
            return false;
        }
        if (!Objects.equals(this.npts, other.npts)) {
            return false;
        }
        if (!Objects.equals(this.nsnpts, other.nsnpts)) {
            return false;
        }
        if (!Objects.equals(this.nvhdr, other.nvhdr)) {
            return false;
        }
        if (!Objects.equals(this.nwfid, other.nwfid)) {
            return false;
        }
        if (!Objects.equals(this.nxsize, other.nxsize)) {
            return false;
        }
        if (!Objects.equals(this.nysize, other.nysize)) {
            return false;
        }
        if (!Objects.equals(this.nzhour, other.nzhour)) {
            return false;
        }
        if (!Objects.equals(this.nzjday, other.nzjday)) {
            return false;
        }
        if (!Objects.equals(this.nzmin, other.nzmin)) {
            return false;
        }
        if (!Objects.equals(this.nzmsec, other.nzmsec)) {
            return false;
        }
        if (!Objects.equals(this.nzsec, other.nzsec)) {
            return false;
        }
        if (!Objects.equals(this.nzyear, other.nzyear)) {
            return false;
        }
        if (!Objects.equals(this.o, other.o)) {
            return false;
        }
        if (!Objects.equals(this.odelta, other.odelta)) {
            return false;
        }
        if (!Arrays.deepEquals(this.resp, other.resp)) {
            return false;
        }
        if (!Objects.equals(this.sb, other.sb)) {
            return false;
        }
        if (!Objects.equals(this.scale, other.scale)) {
            return false;
        }
        if (!Objects.equals(this.sdelta, other.sdelta)) {
            return false;
        }
        if (!Objects.equals(this.stdp, other.stdp)) {
            return false;
        }
        if (!Objects.equals(this.stel, other.stel)) {
            return false;
        }
        if (!Objects.equals(this.stla, other.stla)) {
            return false;
        }
        if (!Objects.equals(this.stlo, other.stlo)) {
            return false;
        }
        if (!Arrays.deepEquals(this.t, other.t)) {
            return false;
        }
        if (!Objects.equals(this.unused10, other.unused10)) {
            return false;
        }
        if (!Objects.equals(this.unused11, other.unused11)) {
            return false;
        }
        if (!Objects.equals(this.unused12, other.unused12)) {
            return false;
        }
        if (!Objects.equals(this.unused15, other.unused15)) {
            return false;
        }
        if (!Objects.equals(this.unused16, other.unused16)) {
            return false;
        }
        if (this.ibody != other.ibody) {
            return false;
        }
        if (!Objects.equals(this.scenario, other.scenario)) {
            return false;
        }
        if (!Objects.equals(this.unused21, other.unused21)) {
            return false;
        }
        if (!Objects.equals(this.unused22, other.unused22)) {
            return false;
        }
        if (!Objects.equals(this.unused23, other.unused23)) {
            return false;
        }
        if (!Objects.equals(this.unused24, other.unused24)) {
            return false;
        }
        if (!Objects.equals(this.unused25, other.unused25)) {
            return false;
        }
        if (!Objects.equals(this.unused26, other.unused26)) {
            return false;
        }
        if (!Objects.equals(this.unused27, other.unused27)) {
            return false;
        }
        if (!Objects.equals(this.unused6, other.unused6)) {
            return false;
        }
        if (!Objects.equals(this.unused7, other.unused7)) {
            return false;
        }
        if (!Objects.equals(this.unused8, other.unused8)) {
            return false;
        }
        if (!Objects.equals(this.unused9, other.unused9)) {
            return false;
        }
        if (!Arrays.deepEquals(this.user, other.user)) {
            return false;
        }
        if (!Objects.equals(this.xmaximum, other.xmaximum)) {
            return false;
        }
        if (!Objects.equals(this.xminimum, other.xminimum)) {
            return false;
        }
        if (!Objects.equals(this.ymaximum, other.ymaximum)) {
            return false;
        }
        return Objects.equals(this.yminimum, other.yminimum);
    }

}
