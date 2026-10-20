## WHITEN

**SUMMARY**

    Flattens the spectrum of the time series in memory using a prediction error filter designed from the data in memory.This implementation allows the filter coefficients to be saved in a SAC file on disk.
****


**SYNTAX**

    WHITEN {N} {FILTERDESIGN|FD} {COMMON}
****


**INPUT**

    N:                 Order of the filter (number of zeros in the predictor's transfer function).

    [F]ILTER[D]ESIGN:  Performs something akin to the filterdesign command.  Computes the impulse response, frequency response and group delay of the prediction error filter and writes these to files on disk in the current working directory with suffixes '.imp', '.spec' and '.gd' respectively.

    COMMON:    	       Uses a common prediction error filter for all sequences in memory.  This option is provided for ambient noise Greens function estimation applications.
****

**DEFAULT VALUES**

    1WHITEN 6
****

**DESCRIPTION**

    Flattens the spectrum of the time series in memory with a prediction error filter of the specified order (default 6). It estimates the coefficients of the prediction error filter from the autocorrelation function of the data, either trace by trace (default) or for all traces simultaneously (COMMON option), then applies the resulting prediction error filters to the data. 

    Prewhitening the data before spectral estimation or correlation reduces bias caused by spectral leakage (wherein energy from large peaks of the spectrum bleed into surrounding frequency bands) caused by finite windowing effects (see Jenkins and Watts, Spectral Analysis and Its Applications).  Prewhitening reduces the dynamic range of the spectrum (which reduces leakage).  When prewhitening for spectral estimation, remember to divide the spectral estimate by the squared magnitude of the frequency response of the prediction error filter.

    Note that the first N values of the trace will be lost in this translation and the time markers updated accordingly.
****

**HEADER CHANGES**

    DEPMIN, DEPMAX, DEPMEN, NPTS