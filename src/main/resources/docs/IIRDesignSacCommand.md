## FILTERDESIGN

**SUMMARY**

    Produces a graphic display of an IIR filter's digital and analog prototype characteristics:  amplitude, phase, group delay and impulse response.
****

**SYNTAX**

    [F]ILTER[D]ESIGN [PRINT [pname]] [FILE [prefix]] [filter options] [delta]

    where 'filter options' are the same as those used in the LOWPASS, HIGHPASS, BANDPASS and BANDREJECT commands:

    {[L]OW[P]ASS | [H]IGH[P]ASS] | [B]AND[P]ASS | [B]AND[R]EJECT]} {[BU]TTERWORTH | C1 | C2}

    {[C]ORNERS v1 v2}

    {[N]POLES n}

    {[P]ASSES n}

    {[T]RANBW v}

    {[A]TTEN v}
****


**INPUT**

    PRINT {pname}:     Prints the resulting plot to the printer named by pname, or to the default printer if pname is not specified.

    FILE {prefix}      Writes three SAC files to disk.  These files contain the digital responses evaluated by this command:

    [prefix].spec      is of type IAMPH, and contains both the amplitude and phase information of the filter.

    [prefix].gd        is of type IGRP

    [prefix].imp       is of type ITIME and contains the impulse response.

    In each of these SAC files, the user header fields are set as follows:

    user0:             passband code

    1: lowpass

    2: highpass

    3: bandpass

    4: bandreject


    user1:             filter type code

    1: Butterworth

    3: Chebyshev Type 1

    4: Chebyshev Type 2

    user2:             number of poles

    user3:             number of passes

    user4:             transition bandwidth (used for Chebyshev types 1 and 2 filters

    user5:             attenuation (used for Chebyshev types 1 and 2 filters

    user6:             sampling interval (delta) in seconds

    user7:             first corner (UNDEFINED if lowpass)

    user8:             second corner (UNDEFINED if highpass)

    kuser0:            passband type (lowpass, highpass, bandpass, or bandrej)

    kuser1:            prototype (Butter, Bessel, C1, C2)
****

**DEFAULT VALUES**
        
    filter type        BUTTERWORTH

    bandpass type      LP

    CORNERS            1.0 5.0

    NPOLES             4

    PASSES             1

    TRANBW             0.5

    ATTEN              100.0

    delta              0.025
****

**DESCRIPTION**

    The FILTERDESIGN command is implemented through the OregonDSP package.  OregonDSP implements the standard recursive (IIR) filter designs through bilinear transformation of prototype analog filters.  These prototype filters are created in a two-step process.  A lowpass analog filter of the specified type and order (number of poles) is generated, then mapped to lowpass of specified cutoff, highpass, bandpass or bandreject analog filter using analog spectral transformations.

    Finally, the mapped analog prototype is transformed to a digital filter via the bilinear transformation for a specified sampling interval (rate).FILTERDESIGN displays digital filter responses as solid lines and analog responses as dashed lines. On color monitors, digital curves are blue, while analog curves are amber.
****

**HEADER CHANGES**

    None.