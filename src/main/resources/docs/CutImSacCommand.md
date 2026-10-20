## CUTLM

**SUMMARY**

    Cuts files in memory.
****
**SYNTAX**

    CUTIM pdw
****
**INPUT**

    pdw:	Partial Data Window. 

    It consists of a starting and a stopping value of the independent variable (usually time), which defines which segment of a file (or files) one wishes to retain. The most general form of a pde is : ref offset ref offset:, where ref is a reference value that is one of the following: B|E|O|A|F|Tn, where n=0,1...9.

    offset:	A positive or negative number which is added to the reference value (optional).
****
**DEFAULT VALUES**

    Start and stop reference values are required. See examples below for an exception.

    If the start or stop offset is omitted, it is assumed to be zero.
    