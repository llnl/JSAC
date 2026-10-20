## DECIMATE ##

**SUMMARY**

Decimates (downsamples) data, after applying a default FIR anti-aliasing filter.

**** 
**SYNTAX**

[D]ECIMATE {n} { [F]ILTER {ON|OFF} } 
	 
****
**INPUT**

n                The decimation rate (ideally 2-7).
FILTER {ON|OFF}  Turn on or off preprocessing with an antialiasing filter.
        
****
**DEFAULT VALUES**

 DECIMATE 2 FILTER ON

****
**DESCRIPTION**

This command is used to downsample data after it has been read into memory. By default, a finite impulse 
response (FIR) filter is applied to the data as it is being decimated to prevent aliasing effects. These 
filters preserve phase information, but can produce undesirable ringing whenever the data contain discontinuities 
(as at the beginning and ending of a waveform), which should be checked by examining a plot of the data, following
decimation.  The ringing can be especially annoying when picking very impulsive, high-amplitude P phases.  Antialiasing 
can be overridden, but care should be taken to avoid aliasing artifacts, such as the introduction of discontinuities 
and/or phantom low-frequency signal components.  Again, examine the data for artifacts after unprotected decimation 
by plotting it before proceeding with further analysis.

Very high decimation rates (beyond about 7) should be avoided as these require very long anti-alias filters.  If 
such decimation is required, it is best to decimate the data in stages, using DECIMATE twice or more consecutively.
This command uses high-quality FIR filters designed by the Parks-McClellan algorithm.

****
**EXAMPLES**

This example effectively decimates the signals in two files by a factor of 3*5 = 15.

SAC>r file1 file2
SAC>decimate 3
SAC>decimate 5
 
****
**HEADER CHANGES**
 
NPTS, DELTA, E, DEPMIN, DEPMAX, DEPMEN
