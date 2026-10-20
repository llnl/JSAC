## TRANSFER

**SUMMARY**

    Performs deconvolution to remove an instrument response and convolution to apply another instrument response.
****

**SYNTAX**

    [TRANS]FER {FROM type {options}} , {TO type {options}}

    {FREQlimits f1 f2 f3 f4}
****

**INPUT**

    FROM type:

    Available types are: EVALRESP, POLEZERO, CSS, DBASE, DATA_SERVICE, WA_OLD, WA_NEW

    Type options are:

    Network (Net) Use this network code in finding appropriate transfer function 

    Station (Sta) Use this station code in finding appropriate transfer function	

    Channel (chan) Use this channel code in finding appropriate transfer function

    Locid Use this location code (KHOLE) in finding appropriate transfer function

    TIME      	substitute reference Time specified as epoch time

    HTIME     	substitute reference Time specified as one of (YYYYDDD, YYYY/MM/DD, YYYY/DDD:HH:MM:SS, YYYY/MM/DD:HH:MM:SS)

    The CSS option requires the name of the response file to be specified immediately after the CSS keyword.

    The POLEZERO and EVALRESP options have a sub-option (SUBTYPE {S}) which should be followed by the name of the  file to use.

    If the SUBTYPE sub-option is not used the RESPONSE directory will be searched for files matching pattern "SAC_PZs_NET_STA_CHAN_LOCID_*" or "RESP.NET.STA.LOCID.CHAN"

    The RESPONSE is either in the current directory or a directory specified using the DIR sub-option.

    TO type: Insert the instrument type by convolution using spectral multiplication.

    FREQLIMITS:	Default is it is not used. 

    POLICY:	Controls the specificity of the query for response metadata. Allowable values are:

    AGENCY_NET_STA_CHAN_LOCID_TIME (ANSCLT)

    NET_STA_CHAN_LOCID_TIME (NSCLT)

    STA_CHAN_TIME (SCT)
****

**EXAMPLES**

    POLEZERO: Name the polezero file using the SUBTYPE (S) option:
        transfer from polezero subtype polezero-file.pz freqlimits 0.05 0.1 9 10

    Search a named directory (sacpzf) for SAC Polezero files named like:SAC_PZs_NETWORK_STATION_CHANNEL_LOCID_* 
        transfer from polezero dir sacpzf to none 

    Search the current directory for SAC Polezero files named like:SAC_PZs_NETWORK_STATION_CHANNEL_LOCID_* but override the net-sta-chan-locid values in the header(s) 
       transfer from polezero  network IU station YAK channel BHZ locid 00 freq .01 .05 8 9  to none

    EVALRESP: Name the evalresp file using the SUBTYPE (S) option: 
        transfer from evalresp s 2013.148.00.YAK.BHZ.00.6733538.resp freq 0.01 .05 8 9  to none

    Search a named directory (evresp) for EVALRESP files named like: RESP.NET.STA.LOCID.CHAN
        transfer from evalresp dir evresp s RESP.IU.ANMO.00.BHZ freq .01 .05 8 9  to none

    Search the current directory for evalresp files named like: RESP.NET.STA.LOCID.CHAN but override the net-sta-chan-locid and the reftime values in the header(s) 
        transfer from evalresp network IU station ANMO channel BHZ locid 00    htime 2019001 freq .01 .05 8 9  to none 

    CSS: This option applies to response files from the USNDC such as (FAP,PAZ,PAZFIR)

    CSS: Transfer from a named CSS response 
        transfer from css 1989.025.00.YKW2.be.--.1.resp  freq .01 .05 8 9

        Transfer from a named CSS response to a named POLEZERO response
        transfer from css cmg3t+q330_lb100c@1 to polezero subtype polezero-file.pz

    DATA_SERVICE: Transfer the files in memory using response data retrieved from a station service.
    For most services this option requires that all of 
        (AGENCY KNETWK, KSTNM, KCMPNM, and KHOLE) be populated.
    For data retrieved using the RCS command these fields will usually be set.
        transfer from DATA_SERVICE

    WA_OLD: Transfer from or to the Wood-Anderson response used in SAC-2000
        POLES 2
        -6.28318 4.71239
        -6.28318 -4.71239
        ZEROS 2
        CONSTANT 2800.0
        transfer to WA_OLD

    WA_NEW: Transfer from or to the Wood-Anderson response determined by Uhrhammer and Collins (1990, BSSA 80, 702-716)
        POLES 2
        -5.49779 5.60886
        -5.49779 -5.60886
        ZEROS 2
        CONSTANT 2080.0
        transfer to WA_NEW
 
   DBASE: Looks up the correct response in the associated database table using metadata in the SAC header. This option can only be used in the FROM direction and requires a provider plugin installed and available for the DBASE processing type. transfer from dbase freqlimits 0.05 0.1 9 10
****

**DEFAULT VALUES**

    TRANS FROM NONE TO NONE POLICY ANSCLT
****

**DESCRIPTION**

    The default input and output "instrument" in TRANSFER is displacement, which in SAC is designated as NONE. Hence, if a FROM type or a TO type is not specified, SAC assumes it to be NONE. If the output instrument is NONE, IDEP in the SAC header is set to DISPLACEMENT (NM) - SAC's convention for displacement. If TRANSFER uses TO VEL or TO ACC, the header variable IDEP is changed accordingly for all waveforms in memory.
