## HIGHPASS 

**SUMMARY**

    Applies an IIR highpass filter.
****

**SYNTAX**

    [H]IGH[P]ASS {[BU]TTER|[BE]SSEL|C1|C2}
    
    {[C]ORNER v}

    {[N]POLES n}

    {[P]ASSES n}

    {[T]RANBW v}

    {[A]TTEN v}
****

**INPUT**

    BUTTER:	Apply a Butterworth filter.

    C1:	Apply a Chebyshev Type I filter.

    C2:	Apply a Chebyshev Type II filter.

    CORNERS v:	Set corner frequency to v.

    NPOLES n:	Set number of poles {range: 1-10}.

    PASSES n:	Set number of passes {n=1: causal, n=2: zero-phase}.

    TRANBW v:	Set the Chebyshev transition bandwidth to v.

    ATTEN v:	Set the Chebyshev attenuation factor to v.