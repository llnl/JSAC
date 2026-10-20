## INTERPOLATE

****

**SUMMARY**

    Interpolates evenly-spaced data to a new sampling rate. Interpolate can also be used with unevenly-spaced data. If DELTA and NPTS are both specified DELTA takes priority and NPTS will be ignored.
****

**SYNTAX**

    INTERPOLATE {DELTA v} {NPTS n} {BEGIN v}
****

**INPUT**

    DELTA v:	Set new sampling rate to v. The time range (E-B) is not changed, so NPTS is changed. 

    However, E will be changed so that it is a multiple of DELTA from b.

    NPTS n:		Force the number of points in interpolated file to be n. The time range (E-B) is not changed, so DELTA is changed. BEGIN v:	Start interpolation at v. This value becomes the begin time of the interpolated file. BEGIN can be used with either DELTA or NPTS.
****

**DEFAULT VALUES**

    The time series is unchanged.