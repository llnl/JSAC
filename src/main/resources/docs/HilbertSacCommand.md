## HILBERT

**SUMMARY**

    Applies a Hilbert transform to each data file in memory.
****
**SYNTAX**

    HILBERT { [SP]ECTRAL | FIR }
****
**INPUT**
                
    [SP]ECTRAL: Use the spectral Hilbert transform method.

    FIR: Apply a Hilbert transform by convolution with an FIR operator.
****

**DEFAULT VALUES**

    HILBERT SPECTRAL
****
**DESCRIPTION**

    Each trace in the data file list is replaced by its Hilbert transform, x(n).  

    Two transform implementations are available.  The first is the spectral method, which computes an FFT of the data, multiplies the transform by the (sampled) theoretical Hilbert response, then computes an inverse FFT to obtain a time-domain result.  The second is a time domain method which convolves the trace data with a 201-point FIR filter designed by the Parks-McClellan algorithm to closely approximate the Hilbert transform in the frequency domain.  

    Both methods have their plusses and minuses.  The spectral method is closer to the nominal response around 0 Hz and the folding frequency, but its implied response near 0 Hz and the folding frequency rings due to the Gibbs phenomenon.  This non-intuitive characteristic of the method occurs due to the fact that the method operates on a sampled version of the data spectrum rather than the continuous spectrum.  The method is exact on the sampled spectrum but not on the continuous spectrum.  

    By contrast the FIR method is not exact on the sampled spectrum (deviating significantly in the lowest 2.5% and highest 2.5% of the discrete-time spectrum).  But its continuous spectrum is well-behaved, without overshoot.  If the loss of the lowest 2.5% of the spectrum is a problem, one can decimate the data to
    move its spectrum higher into the mid-band where the FIR operator is highly accurate.