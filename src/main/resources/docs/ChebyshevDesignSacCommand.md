## CHEBYSHEVDESIGN

**SUMMARY**

    For a range of orders, lists filter response at passband edge, passband ripple and response at stopband edge. The Chebyshev filter implementations in JSAC prioritize stopband rejection (attenuation) and transition bandwidth.

    This command calculates the deviation of the filter response from its ideal value (1.0) in the passband for a specified stopband attenuation and transition bandwidth and a range of possible orders.  The command reports the filter response at the passband edge (where the deviation is worst) for both types I and II filters and the passband ripple for type I filters for a range of orders.  Chebyshev type I filters ripple in the passband and are
    monotonic in the stopband.  

    The opposite is true for type II filters. Choose a filter order that produces an acceptable passband deviation (typically less than a few percent).  Try to
    minimize the filter order as very large orders lead to numerical inaccuracies and possibly instability.  If desired attenuation and passband deviation cannot be achieved with moderate orders (< ~ 9) think about using multiple passes with a lower order filter with a larger transition bandwidth or less stopband attenuation.

    Note:  The stopband edges specified in the IIR filtering command arguments may differ somewhat from the realized stopband edges.  This effect occurs because the digital filters are designed first as analog prototypes, which are then mapped to digital filters through a bilinear transformation, which entails a nonlinear frequency mapping.  So,for example, a lowpass Chebyshev type II with a cutoff of 10 Hz and a transition bandwidth of 0.5 will have its actual stopband edge at a frequency probably lower than 15 Hz.

**SYNTAX**

    [CH]EBYSHEV[D]ESIGN {[H]ELP}

    {[T]RANBW v}

    {[A]TTEN v}
****

**INPUT**

    HELP: Print this command description

    TRANBW v: Specify the Chebyshev transition bandwidth as v.

    ATTEN v: Specify the Chebyshev stopband attenuation factor as v.
