## PLOTSP

**SUMMARY**

    Plots spectral data in several different formats.
****

**SYNTAX**


    PLOTSP {type},{mode}

    where type is one of the following:

    ASIS | RLIM | AMPH | RL | IM | AM | PH

    and mode is one of the following:

    LINLIN | LINLOG | LOGLIN | LOGLOG
****


**INPUT**

    ASIS:	Plot components in their present format.

    RLIM:	Plot real and imaginary components.

    AMPH:	Plot amplitude and phase components.

    RL:	Plot real component only.

    IM:	Plot imaginary component only.

    AM:	Plot amplitude component only.

    PH:	Plot phase component only.

    LINLIN:	Set x-y scaling mode to linear-linear.

    LINLOG:	Set x-y scaling mode to linear-logarithmic.

    LOGLIN:	Set x-y scaling mode to logarithmic-linear.

    LOGLOG:	Set x-y scaling mode to logarithmic-logarithmic.

****

**DEFAULT VALUES**

    PLOTSP ASIS LOGLOG