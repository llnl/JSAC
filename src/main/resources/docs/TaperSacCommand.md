## TAPER


**SUMMARY**

    Applies a symmetric taper to each end of data.
****

**SYNTAX**

    TAPER {TYPE HANNING|HAMMING|COSINE},{WIDTH v}
****

**INPUT**

    TYPE HANNING:	Apply a Hanning taper.

    TYPE HAMMING:	Apply a Hamming taper.

    TYPE COSINE:	Apply a cosine taper.

    WIDTH v:	Set the taper width on each end to v. This is a value between 0.0 and 0.5.
****

**DEFAULT VALUES**

    TAPER TYPE HANNING WIDTH 0.05
