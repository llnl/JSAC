## DFIR

**SUMMARY**

Designs an FIR filter using the Parks-McClellan algorithm and writes the coefficients to a file.


\---

**SYNTAX**

&#x20;       DFIR    {LP  N Omega\_p Wp Omega\_s Ws}
                {HP  N Omega\_s Ws Omega\_p Wp}
                {BP  N Omega\_s1 Ws1 Omega\_p1 Omega\_p2 Wp Omega\_s2 Ws2}
                {DIFC N delta Omega\_p}
                {HILC N Omega\_p1 Omega\_p2}
                {DIFS N delta}
                {HILS N Omega\_p}
                {\[W]RITE fname}


\---

**INPUT**

&#x20;       LP  N Omega\_p Wp Omega\_s Ws:  Lowpass filter with passband edges at normalized frequencies 0 and Omega\_p and stopband edge at Omega\_s
          N:                          Filter order.  The filter has 2N+1 coefficients and is symmetric about index N counting from 0.
          Omega\_p:                    Normalized passband edge frequency (range 0:1).
          Wp:                         Passband design error weight.
          Omega\_s:                    Normalized stopband edge frequency.
          Ws:                         Stopband design error weight.
        HP  N Omega\_s Ws Omega\_p Wp:  Highpass filter with stopband edges at 0 and Omega\_s and passband edge at Omega\_p.
          N:                          Filter order.  The filter has 2N+1 coefficients and is symmetric about index N counting from 0.
          Omega\_s:                    Normalized stopband edge frequency.
          Ws:                         Stopband design error weight.
          Omega\_p:                    Normalized passband edge frequency (range 0:1).
          Wp:                         Passband design error weight.
        BP  N Omega\_s1 Ws1 Omega\_p1 Omega\_p2 Wp Omega\_s2 Ws2:  Bandpass filter.  0 < Omega\_s1 < Omega\_p1 < Omega\_p2 < Omega\_s2 < 1.
          N:                          Filter order.  The filter has 2N+1 coefficients and is symmetric about index N counting from 0.
          Omega\_s1:                   Lower stopband normalized edge frequency.
          Ws1:                        Lower stopband design error weight.
          Omega\_p1:                   Passband normalized lower edge frequency.
          Omega\_p2:                   Passband normalized upper edge frequency.
          Wp:                         Passband design error weight.
          Omega\_s2:                   Upper stopband normalized edge frequency.
          Ws2:                        Upper stopband design error weight.
        DIFC N delta Omega\_p:         Centered differentiator accurate between 0 and Omega\_p.
          N:                          Filter order.  The filter has 2N+1 coefficients and is antisymmetric about index N counting from 0.
          delta:                      Intended sampling interval.  The filter is scaled to a particular sampling rate.
          Omega\_p:                    Highest normalized frequency (0 < Omega\_p < 1) where the differentiator is accurate.  Beyond Omega\_p, the 
                                        filter response drops to zero at Omega = 1.
        HILC N Omega\_p1 Omega\_p2:     Centered Hilbert transform operator, accurate between Omega\_p1 and Omega\_p2 (0 < Omega\_p1 < Omega\_p2 < 1).
          N:                          Filter order.  The filter has 2N+1 coefficients and is antisymmetric about index N counting from 0.
          Omega\_p1:                   Low normalized frequency where the filter frequency response amplitude approximates 1.  It is 0 at 0.
          Omega\_p2:                   High normalized frequency where the filter frequency response amplitude approximates 1.  It is 0 at 1.
        DIFS N delta:                 Staggered differentiator.  The filter is accurate over the entire discrete frequency band.
          N:                          Filter order.  The filter has 2N coefficients and is antisymmetric about a point halfway between samples 
                                        N-1 and N.
          delta:                      Intended sampling interval.  The filter is scaled to a particular sampling rate.
        HILS N:                       Staggered Hilbert transform operator.  The filter is accurate from Omega\_p to 1.0.
          N:                          Filter order.  The filter has 2N coefficients and is antisymmetric about a point halfway between samples 
                                        N-1 and N.
          Omega\_p:                    Low end normalized frequency above which the operator is accurate.  The operator amplitude response is 0.0 
                                        at 0.0.
        \[W]RITE fname:                Write filter coefficients to file specified by fname (default FIR.txt).
          fname:                      File or path name.  May be an absolute or relative pathname, or the simple name of a file to appear in the 
                                        current working directory.


\---

**DEFAULT VALUES**

DFIR LP 30 0.25 1.0 0.4 1.0 W FIR.txt


\---

**DESCRIPTION**

&#x20;       This command designs equiripple finite impulse response (FIR) filters using the Parks-McClellan algorithms (which, in turn, use the Remez
          exchange algorithm.  The command designs seven different types of filters on a normalized frequency domain ranging from 0 (0 Hz) to 1
          (the folding frequency).  For example, if a lowpass filter has been designed with cutoff frequency 0.25 (one quarter of the folding
          frequency, then applied to a sequence sampled at 40 Hz (folding frequency 20 Hz), then the cutoff will occur at 5 Hz.  With the stopband 
          cutoff specified at 0.4, the applied stopband begins at 8 Hz, allowing the filter response a transition from approximately 1.0 to 0.0 
          between 5 and 8 Hz.  The coefficients are written to an ASCII file for later use with the FIR command, which actually applies the filter 
          to files in memory.

        The principle virtue of these filters is that they have linear phase, eliminating waveform distortion due to dispersion.  The impulse
          responses of the filters are symmetric, which can lead to precursory ringing of impulsive arrivals.  But the frequency responses of 
          these filters typically are much sharper than those of IIR filters, allowing better separation of signal components closely spaced in
          frequency.  They also allow better and explicit control of the filter response approximation error in both passbands and stop bands.
          Weights are specified to control the relative approximation errors in passbands and stopbands.  

        The filters are designed on a discrete-time frequency axis with pass and stop bands generally explicitly specified.  The default above,
          for example, is a lowpass filter with passband ranging from 0.0 to 0.25 and stopband ranging from 0.4 to 1.0.  The filter response
          nominally is 1.0 in the passband and 0.0 in the stopband, but ripples about these values because the response is a (cosine) polynomial.
          For a reasonable filter order (say 30), typically the ripples are not noticeable.  They become more significant the narrower the
          transition band (0.25 to 0.4 in the default).  The filter response in the transition band declines monotonically from around 1.0 at 0.25
          to around 0.0 at 0.4.  The narrower the transition band, the larger the order must be to maintain reasonable approximation errors.

        This command also designs differentiators and Hilbert transform operators.  These filters offer high-quality approximations of these ideal
          responses, typically accurate over wider frequency bands than simpler implementations (e.g. finite difference approximations of 
          differentiators).  The equiripple operators come in two flavors:  centered operators and staggered operators.  The centered operators
          have the advantage that the impulse response is antisymmetric about the center sample, meaning that a waveform filtered with one of these
          operators is not shifted and is directly comparable to the unfiltered waveform.  The staggered operators produce a half-sample shift, 
          which means a waveform filtered with one of these is offset slightly from the unfiltered waveform.  However, the staggered operators
          approximate ideal responses over a wider band or more closely with a smaller order.  For example the staggered differentiator is accurate
          over the entire band (0 to 1).

        For more detail on the design of these operators consult the following references:

          A Unified Approach to the Design Optimum Linear Phase FIR Digital Filter, James H. McClellan and Thomas W. Parks (1973),
            IEEE Transactions on Circuit Theory, Vol. CT-20, No. 6, pp. 697-701.

        and

          FIR Digital Filter Design Techniques Using Weighted Chebyshev Approximation, Lawrence R. Rabiner, James H. McClellan and
            Thomas W. Parks (1975), Proceedings of the IEEE, Vol. 63, No. 4, pp. 595-610.

        This Java implementation is part of the OregonDSP package from Deschutes Signal Processing LLC.


\---

**EXAMPLES**

Design a bandpass filter then apply to waveforms in memory:
 
          SAC> DFIR BP 30 0.1 10.0 0.15 0.25 1.0 0.3 10.0
          SAC> READ ARD1 ARD2 ARD3 ARD4 ARD5 ARD6 ARD7 ARD8 ARD9 
          SAC> FIR FIR.txt

        If the files are sampled at 40 sps, then the lower stopband ranges from 0.0 to 2.0 Hz, the passband ranges from 3.0 to 5.0 Hz
          and the upper stopband ranges from 6.0 to 20.0 Hz.  The weights have been specified so that the approximation error is 10
          times smaller in the stopbands than in the passband, ensuring good rejection of signal components in the stopbands.
 		
	Design a staggered differentiator, save it to a file, then apply it to one waveform:
 
          SAC> DFIR DIFS 30 0.025 W StaggeredDif.txt
          SAC> READ ARA0
          SAC> FIR StaggeredDif.txt

        This differentiator is accurate from 0.0 to 20.0 Hz, i.e. the whole band!

	Design a centered differentiator, save it to a file, then apply it to one waveform:
 
          SAC> DFIR DIFC 30 0.025 0.9 W CenteredDif.txt
          SAC> READ ARA0
          SAC> FIR CenteredDif.txt

         Note that this differentiator is accurate to 90% of the folding frequency, i.e. from 0.0 to 36.0 out of 40.0 Hz.


\---

**HEADER CHANGES**

None

