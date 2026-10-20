## DIF

**SUMMARY**

    Differentiates the data in memory.  A variety of differentiators are provided as options, including locally (around 0 Hz) accurate approximations from a Taylor series expansion, and wideband differentiators designed with the Parks McClellan algorithm.
****
**SYNTAX**

    DIF { TWO | THREE | FIVE | [EQ]UIRIPPLE }
****
**INPUT**

    TWO:         Apply a two-point difference operator.

    THREE:       Apply a three-point difference operator.

    FIVE:        Apply a four-point difference operator.

    EQUIRIPPLE   Apply a two-sided, 81 point Parks-McClellan differentiator.
****
**DEFAULT VALUES**

    DIF TWO
****
**DESCRIPTION**

    The two-point algorithm is:
    OUT(J) = (DATA(J+1) - DATA(J)) / DELTA
    The last output point is not defined by this algorithm.  It also is not a centered algorithm.  SAC takes care of these problems by decreasing the number of points in the file (NPTS) by one and by increasing the begin time (B) by half the sampling interval (DELTA/2).

    The three-point (centered 2-point) algorithm is: OUT(J) = 1/2 * (DATA(J+1) - DATA(J-1)) / DELTA The first and last output points are not defined by this algorithm.  SAC decreases NPTS by 2 and increases B by DELTA.

    The five-point (centered 3-point) algorithm is: OUT(J) = 2/3 * (DATA(J+1) - DATA(J-1)) / DELTA  -  1/12 * (DATA(J+2) - DATA(J-2)) / DELTA The first two and last two output points are not defined by this algorithm. 

    SAC applies the three-point operator to the second points from each end, decreases NPTS by 2, and increases B by DELTA. SAC then applies an 81 point symmetric (centered) FIR diffentiator, designed with the Parks-McClellan algorithm, that has equiripple error as a fraction of the nominal response over 90% of the frequency band.  This implementation pads the data with zeros on either side, filters the data, then crops it back to the original length. 

    Consequently, there are no changes to NPTS, B and E with this option.
****
**HEADER CHANGES**

    NPTS, B, E, DEPMIN, DEPMAX, DEPMEN