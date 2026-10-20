## NBFK

**SUMMARY**

    Computes the narrowband frequency-wavenumber (FK) spectral estimate, using all data files in SAC memory.

****
**SYNTAX**

    NBFK {PDS|MLM}
         {[N]ORMALIZE}
         {EPS v}
         {[F]REQUENCY f}
         {[S]LOWNESS v}
         {[G]RID m {n}}
         {[L]EVELS n}
         {DB}
         {[T]ITLE text}
         {[WIN]DOW v1 v2}
         {[W]RITE fname}
         {CONTOUR_LINE_COLOR {CLC} v}
         {CONTOUR_TEXT_COLOR {CTC} v}
         {CONTOUR_TEXT_BACKGROUND {CTB} v}
         {CONTOUR_LINE_THICKNESS {CLT} v}
         {PLOT_OPTIONS v}

****
**INPUT**

     PDS:               Use conventional power spectrum.

     MLM:               Use maximum likelihood (Capon) estimator for power spectrum.

     [N]ORMALIZE:       Scale the covariance matrix rows  and columns by the
                        square root of their diagonal element.
                        Helpful when signal amplitudes vary significantly 
                        among channels.

     EPS v:             Regularization quantity for the spectral 
                        covariance matrices.
                        Diagonal matrix entries are scaled by (1.0 + v).

     [F]REQUENCY f	Temporal frequency f of FK spectrum evaluation.	

     [S]LOWNESS v:      Bounding (maximum) slowness of the calculated spectrum.

     [G]RID m {n}:      Grid type for spectrum evaluation and plot.
       m {n}:             Grid size parameters.  If one parameter only the grid
                          is an m x m Cartesian grid.
                          If two parameters, the grid is polar with m samples in azimuth and n in slowness.

     [L]EVELS n:        Specifies the number (n) of contour levels.

     DB                 Indicates log scaling of the spectrum in decibels.

     [T]ITLE text       Plot title.

     [WIN]DOW  v1 v2:   Specifies a window other than the whole waveform 
                        (which is the default).
       v1:                Beginning time of the window in seconds 
                          past the file start.
       v2:                Ending time of the window in seconds 
                          past the file start.

     [W]RITE   fname:   Write the FK spectrum to a SAC xyz file in 
                        rectangular sampling.
       fname:             File or path name.  May be an absolute or 
                          relative pathname. 
						  
     CONTOUR_LINE_COLOR {CLC} color:Set the color of contour lines in the FK plot.
		color:			one of WHITE,RED,ORANGE,PINK,GREEN,YELLOW,
		                        BLUE,MAGENTA,CYAN,BLACK,DARK_GRAY,GRAY,LIGHT_GRAY,PINK

     CONTOUR_TEXT_COLOR {CTC} color:Set the color of contour line label text in the FK plot.   
		color:			one of WHITE,RED,ORANGE,PINK,GREEN,YELLOW,
		                        BLUE,MAGENTA,CYAN,BLACK,DARK_GRAY,GRAY,LIGHT_GRAY,PINK

     CONTOUR_TEXT_BACKGROUND {CTB} color:Set the color of contour line label text in the FK plot.   
		color:			one of WHITE,RED,ORANGE,PINK,GREEN,YELLOW,
		                        BLUE,MAGENTA,CYAN,BLACK,DARK_GRAY,GRAY,LIGHT_GRAY,PINK

     CONTOUR_LINE_THICKNESS {CLT} value:Set the thickness of contour lines in the FK plot.   
		value:						   integer from 1 to 10

     PLOT_OPTIONS value:                The plot can be rendered as a heat map, a contour plot or both.
	    value:                      One of CONTOURS_ONLY, HEAT_MAP_ONLY, or CONTOURS_AND_HEAT_MAP

****
**DEFAULT VALUES**

    NBFK  PDS  SLOWNESS 0.5  FREQUENCY 2.0  NORMALIZE  EPS 0.001  RECTANGULAR 200  LEVELS 11 CLC BLUE CTC BLACK CTB WHITE PLOT_OPTIONS CONTOURS_AND_HEAT_MAP

****
**DESCRIPTION**

    This command calculates a broadband (stacked over frequency) FK spectrum from the files read into memory.  
    The following header values must be populated for all files in memory: 
        stla
        stlo
        stel
        kstnm
        kcmpnm

****
**EXAMPLES**

    Default usage:

    SAC> READ ARAO ARC1 ARC2 ARC3 ARC4 ARC5 ARC6 ARC7
    SAC> READ MORE ARD1 ARD2 ARD3 ARD4 ARD5 ARD6 ARD7 ARD8 ARD9 
    SAC> NBFK

****
**HEADER CHANGES**

    none
****
