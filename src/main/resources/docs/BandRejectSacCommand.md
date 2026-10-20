## BANDREJECT


**SUMMARY**

    Applies an IIR band reject filter.
****
**SYNTAX**

    [B]AND[R]EJ {[BU]TTER|[BE]SSEL|C1|C2}

    {[C]ORNERS v1 v2}

    {[N]POLES n}

    {[P]ASSES n}

    {[T]RANBW v}

    {[A]TTEN v}
****
**INPUT**

    BUTTER:	Apply a Butterworth filter.

    C1:	Apply a Chebyshev Type I filter.

    C2:	Apply a Chebyshev Type II filter.

    CORNERS v1 v2:	Set corner frequencies to v1 and v2.

    NPOLES n:	Set number of poles {range: 1-10}.

    PASSES n:	Set number of passes {n=1: causal, n=2: zero-phase}.

    TRANBW v:	Set the Chebyshev transition bandwidth to v.

    ATTEN v:	Set the Chebyshev attenuation factor to v.
****
**DEFAULT VALUES**

    BANDREJECT BUTTER CORNER 0.1 0.4 NPOLES 2 PASSES 1 TRANBW 0.3 ATTEN 30.