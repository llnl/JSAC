****
## Envelope

**SUMMARY**

    Computes the envelope function using a Hilbert transform.
****

**SYNTAX**

    ENVELOPE
****
**DESCRIPTION**

    This command computes the envelope function of the data in memory. The envelope is defined by the square root of x(n)^2 + y(n)^2, where x(n) is the original signal and y(n) its Hilbert transform (see HILBERT). 

    As with HILBERT, very long period data should be decimated (see DECIMATE) prior to processing.