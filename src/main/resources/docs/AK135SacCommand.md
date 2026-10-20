## AK135

**SUMMARY**

    Computes AK135 travel times for one or more phase(s) and sets the value in the specified field(s).
****
**SYNTAX**

    AK135  { phaseName name } { phaseName name } ...  fieldName
****
**DESCRIPTION**

    phaseName (or just name) is  a keyword that must be followed by one or more valid AK135 phase labels.

    fieldName:	One of T0 through T9.

    The number of requested phases should be <= 10 -i where Ti is the requested field"

**OPTIONAL**

    AK135 LIST will merely list the phases for which the model can compute travel times.
