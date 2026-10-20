## CUT  

**SUMMARY**

    Defines the amount of a data file to be read. As discussed below, CUT can be preceded by CUTERR, which controls errors if the chosen amount includes times outside B to E. The syntax will then be CUTERR {CUTERR option} ; CUT {CUT options}. CUT does not act on data currently in memory: a call to CUT must be followed by a READ or FUNCGEN to take effect. 
**** 
**SYNTAX**

    CUT {ON|OFF|pdw|SIGNAL}
****

**INPUT**

    ON:	Turn cut option on but don't change pdw (see below)

    OFF:	Turn cut option off.

    pdw:	Turn cut option on and enter/change pdw.

    A pdw is a partial data window. It consists of a starting and a stopping value of the independent variable, usually time, which defines the segment of a file one wishes to read. The most general form of a pdw is ref offset ref offset, where ref is a number or a reference value that is one of the following: B|E|O|A|F|Tn, where n=0,1...9, and N, the number of points. The reference values are defined in SAC data file format and reviewed below.

    offset:	A positive or negative number that is added to the reference value.

    SIGNAL:	Equivalent to typing: A -1 F +1.
****
**DEFAULT VALUES**

    CUT OFF (equivalent to CUT b e)

    CUTERR FILLZ
****

**DESCRIPTION**

    The CUT command simply sets cut points and does not change the file in memory. For the command to take effect, CUT must be followed by a READ. This is in contrast with command CUTIM, which carries out cut (or cuts) on the data currently in memory.

    If the start or stop offset is omitted it is assumed to be zero. If the start reference value is omitted it is assumed to be zero. If the stop reference value is omitted it is assumed to be the same as the start reference value.

    With CUT off, the entire file is read. With CUT on, only that portion of the file between the starting and stopping cut values is read. These are values in terms of the independent variable in the data file, normally time.  The following header variables are used to represent certain values of the independent variable:

    B:	Disk file beginning value

    E:	Disk file ending value

    O:	Event origin time

    A:	First arrival time

    F:	Signal end time

    Tn:	User defined time picks (n = 0,1...9)
