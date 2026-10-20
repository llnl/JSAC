## YLIM


**SUMMARY**

    Determines the plot limits for the y axis.
****

**SYNTAX**

    YLIM {ON|OFF|ALL|min max|PM v ....}
****

**INPUT**

    {ON}:	Turn y limits option on, but don't change limits.

    OFF:	Turn y limits option off.

    ALL:	Scale y limits to the minimum and maximum of all files in memory.

    min max:	Turn fixed y option on and change limits to min and max.

    PM v:	Turn fixed y option on and change limits to minus and plus the absolute value of v. ,SKIP You may define different y limit options for each file in memory if you wish. The first entry in the command applies to the first file in memory, the second entry to the second file, etc. The last entry applies to the remainder of the files in memory.
****

**DEFAULT VALUES**

    YLIM OFF
