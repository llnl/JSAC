## RQ

**SUMMARY**

    Removes the seismic Q factor from spectral data.
****

**SYNTAX**

    RQ [Q v],[R v],[C v]
****

**INPUT**

    Q v:	Set quality factor to v.

    R v:	Set distance in km. to v.

    C v:	Set group velocity in km/sec to v
****

**DEFAULT VALUES**

    RQ Q 1. R 0. C 1.
****

**DESCRIPTION**

    The equation used to correct the amplitude is given below:

    AMP_corrected(F) = AMP_uncorrected(F) * Exp( (pi*R*F) / (Q*C) )

    where: F is the frequency in Hz. R is the distance in km. C is the group velocity in km/sec. Q is the the nondimensional quality factor.
****

**HEADER CHANGES**

    DEPMIN, DEPMAX, DEPMEN