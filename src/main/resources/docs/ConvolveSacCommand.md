## CONVOLVE 

**SUMMARY**

    Compute the convolution of a pulse shape with all other time series in memory.
****
**SYNTAX**

    CONVOLVE {file} {PULSE n} {TRI v} {GAUSS v}
****
**INPUT**

    PULSE n:        The PULSE in filenumber n in memory from 1-N.

    file:           Convolve with data in file (not in memory)

    [TRI]ANGLE v:          Convolve with a triangle of half width v seconds

    GAUSS v:        Convolve with a Gaussian with standard deviation v seconds

    CENTERED ON|OFF Place zero time on center [ON} or beginning [OFF] of the pulse

    CONVOLVE PULSE 1 CENTERED OFF
****
**DESCRIPTION**

    In seismology, convolution is used in two ways:

    1. convolving a pulse with a time-series, and

    2. convolving a response with a time series. (Or, more typically, deconvolving a response from a time series.)

    The equation for convolution takes a simple form in both the time and frequency domains: (1) is more easily described in the time domain, while (2) in the frequency domain.  The CONVOLVE function here is oriented towards (1).  The TRANSFER command is more appropriate for (2).

    There are three ways to run CONVOLVE: (1) PULSE: read in N files and convolve one of them with all the others; (2) file: read in N files and convolve all of the waveforms with a waveform in a separate file; (3) using a PREDEFINED FUNCTION (see below).

    Prior to SAC v102.0, the PULSE method was the only option, so we start there.
    The discrete-time waveform specified by PULSE is convolved with all other time-series in memory using the following equation:

    conv( g, f ) = SUM_m ( g[m] f[n-m] )

    where f is the pulse waveform, g is any from among the other signals in memory and SUM_m refers to summation over the discrete time index m.  All signals in memory must have the same DELTA.  The number of points in f, the PULSE waveform, cannot be greater than the number of points in any other signal in memory.  

    If the numbers of points in f and g are nf and ng, then the number
    of points in the resulting convolved waveform is nf + ng - 1.  The start time for each convolved waveform will be the same as B(g).  Contouring on the pulse waveform eliminates time shifts introduced by convolution.  If CENTERED is OFF, B(f) = 0.0.

    CENTERED ON should be used only for time-symmetric pulses.  As an example, let synthetic.sac contain a synthetic time series created by a program such as WKBJ, consisting of spikes with time offsets, amplitudes and polarities representing phase arrivals.  To compare the synthetics with an observed seismogram, one could create a file p_arrival.sac containing a pulse waveform and perform the convolution with the commands:

    SAC> r p_arrival.sac synthetic.sac

    SAC> convolve PULSE 1 CENTERED OFF

    FILE OPTION

    Alternatively, given the same two files, one could obtain the same convolution result with the commands:

    SAC> r synthetic.sac

    SAC> convolve p_arrival.sac CENTER OFF

    PREDEFINED FUNCTIONS

    JSAC also supports the option of convolution with one of two predefined waveforms, using the TRI and GAUSS options.  These options automatically compute triangular and Gaussian functions with time samplings consistent with the waveforms in memory. When the TRI or GAUSS options are used, no pulse waveform file is read in;  the predefined function takes the place of f[n] in the convolution equation cited above.  Both functions are normalized such that the integrate to 1.0 and, thus, do not change amplitude or moment in the convolved result.  Both functions are symmetric.  For the GAUSS option, the sampled time history is e^(-0.5*(n*DELTA/v)^2), so that v is the standard deviation.

    Say one wants to look at a synthetic waveform in synthetic.sac simply as a spike series (no source pulse).  Most synthetics suffer from the Gibbs phenomenon and will exhibit a train of oscillating pulses.  Convolving the synthetic with one of these predefined functions will smooth out the oscillating pulses, resulting in a cleaner seismogram that is easier to interpret:

    SAC> r synthetic.sac

    SAC> convolve GAUSS 0.04 CENTERED ON

    or

    SAC> r synthetic.sac

    SAC> convolve TRI 0.04 CENTERED ON
****
**HEADER CHANGES**

    DEPMIN, DEPMAX, DEPMEN, NPTS