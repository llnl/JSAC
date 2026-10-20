## BEAM

**SUMMARY**

        Computes a beam using all data files in SAC memory.

**** 
**SYNTAX**

        BEAM    {[B]EARING  b}
                {[V]ELOCITY v}
                {[REF]ERENCE lat lon {el}}
                {EC         anginc survel}
                {[W]RITE    fname}
                {ADAPTIVE {m {niter}}}
        INPUT
                BEARING  b:               Set bearing to b, in degrees east from north.
                VELOCITY v:               Set velocity to v, in kilometers/second.
                [REF]ERENCE lat lon {el}  Specifies the latitude, longitude and, optionally, the elevation of the 
                                          beam reference point.
                  lat:                        Latitude of the beam reference point.
                  lon:                        Corresponding longitude.
                  el:                         Optional beam reference elevation in meters(positive is up).
                REFSTA station            Specifies a station's location as the beam reference point.
                  station:                    Name of the station.
                EC anginc survel:         Elevation correction:
                  anginc:                     Angle of incidence in degrees from the z axis. Vertical incidence 
                                              is 0.0.
                  survel:                     Surface medium velocity in kilometers/second.
                WRITE    fname:	          Write beam to file.
                  fname:                      File or path name.  May be an absolute or relative pathname, or the 
                                              simple name of a 
                                              file to appear in the current working directory.
                ADAPTIVE {m {niter}}      Use Hishashi Kobayashi's iterative solution to Capon's adaptive beamformer.
                  m                           Optional filter half-size;  the number of filter coefficients is 2*m+1 
                                              per data channel.
                                              The default is 50.
                  niter                       Optional number of iterations.  The default is m/10.

****
**DEFAULT VALUES**

        BEAM B 0.0 V 99999.0 EC 33.0 6.0 W beam

****
**DESCRIPTION**

This command calculates a beam from the files read into memory.  It does not alter existing data in JSAC memory, so may be 
called repeatedly to construct a beam spread by writing the resulting beams to different files.  The default behavior uses 
the centroid of the stations with data in memory as the array beam reference point.  This behavior maybe overridden in one 
of two ways:  by using the REFERENCE option to specify an alternative latitude, longitude and, optionally, elevation as the 
reference point, or by using the REFSTA option to specify the location of one station with data in memory as the reference 
point.  The following header values must be populated for all files in memory:

    stla
    stlo
    stel
    kstnm
    kcmpnm

The default beam is an infinite velocity beam suitable for teleseismic arrivals.

This command also implements the Kobayashi iterative solution to Capon's adaptive beamformer.  Adaptive beamformers apply 
FIR filters to each channel of the aligned waveforms prior to summing.  The filters are designed to minimize the energy of 
the beam subject to the constraint that signals arriving from the specified direction and velocity are passed without 
distortion.  Use this option to suppress coherent interference that is significantly more powerful than the target signal.  
Coherent interference could be aftershocks from a source unrelated to the target or highly directional background noise 
from an industrial facility.  The size of the filters (which are two-sided) can be controlled with an optional parameter
(m) and the number of iterations with a second optional parameter (niter).  Generally good performance can be obtained 
with 5 to 10 iterations. This option produces multiple beam files, one for each iteration, so that the improvement with 
iteration can be gauged.  The file names are as specified with the WRITE option, but with suffixes indicating the iteration 
number (e.g. beam_00, beam_01, ...).  The first beam written is identical to the conventional stack, as obtained without 
the ADAPTIVE option.

****
**EXAMPLES**

    Default usage:
 
        SAC> READ ARAO ARC1 ARC2 ARC3ARC4 ARC5 ARC6 ARC7
        SAC> READ MORE ARD1 ARD2 ARD3ARD4 ARD5 ARD6 ARD7 ARD8 ARD9 
        SAC> BEAM B 130.0 V 8.3 W Pn_beam
 		
    Specifying a beam referenced to the center element of the array:
 
        SAC> BEAM B 130.0 V 4.5 REFSTA ARA0 W Sn_beam

    Specifying an adaptive beam with 201 filter coefficients per channel and 20 iterations:

        SAC> BEAM ADAPTIVE 100 20 B 356.0 V 17.0

****
**HEADER CHANGES**

        None