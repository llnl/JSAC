## READALPHA

**SUMMARY**

    Reads data from on disk into memory
****
**SYNTAX**

    READ [options] [filelist]

    where options is one or more of the following:

    MORE

    DIR CURRENT|name

    ALL options must precede any element in the filelist.
****

**INPUT**

    MORE:

    Place the new data files in memory AFTER the old ones. If this option is omitted, the new data files REPLACE the old ones.

    DIR CURRENT:

    Read all simple filenames (with or without wildcards) from the current directory. This is the directory from which you started SAC.

    DIR name:

    Read all simple filenames (with or without wildcards) from the directory called name. This may be a relative or absolute directory name.

    file:

    A legal filename. This may be a simple filename or a pathname. The pathname can be a relative or absolute one. See the DESCRIPTION and EXAMPLES sections below for more details.