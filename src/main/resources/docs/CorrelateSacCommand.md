## CORRELATE

**SUMMARY**

&#x20;   Computes auto- and cross-correlation functions to examine waveform similarity.

\---

**SYNTAX**

## &#x20;   CORRELATE {\[M]ASTER name|n}, {\[F]ILE path}, {\[N]ORMALIZED ON|OFF}

**INPUT**

&#x20;   \[M]ASTER name|n:  Select master waveform in data file list by name or number.  All waveforms are correlated against this one.

&#x20;   \[F]ILE   path:    Select master waveform from a file on disk.
&#x20;   \[N]ORMALIZED OFF: No normalization.
&#x20;   \[N]ORMALIZED ON:  Results are normalized between -1.0 and 1.0



\---

**DEFAULT VALUES**

&#x20;   CORRELATE MASTER 1 NORMALIZED ON

\---

**DESCRIPTION**

&#x20;   The JSAC implementation of this command compares a master signal with a collection of other signals (relatively short transients) to determine whether

&#x20;   the signal sources are closely related. One might read two transients to be compared, then correlate with NORMALIZED on to get an assessment of waveform similarity:

&#x20;   SAC> r a.sac b.sac

&#x20;   SAC> correlate MASTER 1 NORMALIZED ON



&#x20;   SAC> plot

&#x20;   This sequence of commands will result in an autocorrelation in the first trace in memory with a peak value of 1.0 and a cross-correlation in the second trace with a peak 










&#x20;   value ranging between -1.0 and 1.0, depending on the degree of similarity between the waveforms in a.sac and b.sac.

&#x20;   The correlations (whether auto- or cross-) are computed as:



&#x20;   corr( f, g, n ) = SUM\\\_m ( f\\\[m] g\\\[m-n] )

&#x20;   where f is the master waveform and g is any waveform among the signals in memory. If nf is the number of samples in f and ng is the number of samples 
&#x20;   in g, then the correlation has a total of nf+ng-1 samples.






&#x20;   Following execution, header value USER0 stores the lag (n) at which the maximum correlation is attained and USER1 stores the corresponding peak value.  The peak value and

&#x20;   lag are refined by maximizing a quadratic fit to three sequential samples centered upon the maximum computed correlation.  The refinement provides sub-sample accuracy to

&#x20;   the estimated lag.  Accordingly, for each signal, KUSER0 is set to "Lag" and KUSER1 is set to "Peak".



&#x20;   USER2 is set to the relative amplitude of each signal referenced to the master signal.  If the brackets <> indicate maximum correlation, then this value is <f,g>/<f,f>.

&#x20;   For each signal KUSER2 is set to "Relative".

\---

**HEADER CHANGES**

&#x20;   DEPMIN, DEPMAX, DEPMEN, NPTS, B, USER0, USER1, USER2, KUSER0, KUSER1, KUSER2

