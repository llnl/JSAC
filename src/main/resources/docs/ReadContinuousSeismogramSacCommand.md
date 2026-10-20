## READCONTINUOUSSEISMOGRAM

**SUMMARY**

    Read seismogram data from a continuous waveform source.

****
**SYNTAX**

   READCONTINUOUSSEISMOGRAM (alt RCS) {MORE} {LIST_SERVICES} {LIST_AGENCIES} {LIST_NETWORKS} {LIST_STATIONS} SPECIFICATIONS

****
**INPUT**
    MORE: Place the new data files in memory AFTER the old ones. If this option 
            is omitted, the new data files REPLACE the old ones.

    LIST_TIME_FORMATS:   List available time formats for the START_DATE and END_DATE options.
    LIST_SERVICES:       List available services.
    LIST_AGENCIES:       For FDSN service (default) list available agencies having a DATASELECT service. 
                         For other services this will list all agencies in the source resource.
    LIST_NETWORKS:       For FDSN service (default) list available networks for the supplied agency 
                         having a DATASELECT service. For other services this will list all networks 
                         for the supplied agency in the source resource.
    LIST_STATIONS:       For FDSN service (default) list available stations for the supplied agency 
                         and network having a DATASELECT service. For other services this will list 
                         all stations for the supplied agency and network in the source resource.
    SERVICE:             The service name. The FDSN service is default and always available. 
                         Other services may be available in your version.
    AGENCY:              The agency code. Only a single agency may be specified.
    NETWORK:             The network code. Only a single code may be supplied.
    STATION or STA:      One or more station codes separated by white space. Wildcards (*) are allowed.
    STATION_BOX:         Specify a rectangular box enclosing stations of interest. 
                         Requires four parameters MIN_LAT MAX_LAT MIN_LON MAXLON in that order.
    STATION_CIRCLE:      Specify a circular region enclosing stations of interest. 
                         Requires three parameters CENTER_LAT CENTER_LON RADIUS (deg) in that order.
    STATION_CIRCLE_KM:   Specify a circular region  enclosing stations of interest. 
                         Requires three parameters CENTER_LAT CENTER_LON RADIUS (km) in that order.
    CHANNEL or CHAN:     One or more channel codes separated by white space. Wildcards (*) are allowed.
    LOCID:               One or more location codes. Wildcards (*) are allowed.
    START:               Segment start time expressed as an epoch time.
    START_DATE:          An alternate way to specify the start using a human-readable string.
                         Available Time Formats:
                            yyyyDDD
                            yyyy/MM/dd
                            yyyy/DDD:HH:mm:ss
                            yyyy/MM/dd:HH:mm:ss
                            yyyy/MM/dd-HH:mm:ss.SSS
    END:                 Segment end time expressed as an epoch time.
    END_DATE:            An alternate way to specify the end using a human-readable string.
                         Available Time Formats:
                            yyyyDDD
                            yyyy/MM/dd
                            yyyy/DDD:HH:mm:ss
                            yyyy/MM/dd:HH:mm:ss
                            yyyy/MM/dd-HH:mm:ss.SSS

    EVENT_ID:            A list of events for which data will be retrieved.
    EVENT_AGENCY:        The agency from which event information is to be retrieved (default is ISC).
    SRC_STA_RAD_DEG:     When EVENT_ID values are given this option can be used to select stations within the specified radius in degrees.
    SRC_STA_RAD_KM:      When EVENT_ID values are given this option can be used to select stations within the specified radius in km.
    MIN_DURATION_SECONDS:No window will be created shorter than this value.
    MAX_DURATION_SECONDS:No window will be created longer than this value.
    PRE_EVENT_SECONDS:   The number of seconds prior to the origin time to start the window.
    CUTOFF_VELOCITY:     Used to calculate the window end.

****
**DEFAULT VALUES**

    SERVICE FDSN
    AGENCY IRISDMC 
    CHAN *
    LOCID *
    EVENT_AGENCY ISC
    MIN_DURATION_SECONDS 500
    MAX_DURATION_SECONDS 25000
    PRE_EVENT_SECONDS 300
    CUTOFF_VELOCITY 1 (km/s)
    SRC_STA_RAD_DEG 30
    SRC_STA_RAD_KM 1000


****
**DESCRIPTION**

    This command reads waveform data from a specified waveform service to create
    one or more SAC files in memory. The default waveform service type is FDSN 
    which is always available. However other waveform services may be added
    as plugins. Use the LIST_SERVICES option to see the available service types.
    When more than 1 service is available use the service option to specify the 
    desired service.
    
    The command supports two modes: AD HOC and EVENT. 
    In AD HOC mode the user specifies the time window of the retrieved seismograms
    as well as other parameters controlling station and channel selection. In this
    mode the retrieved seismograms have common time windows and have station metadata
    (KNETWK, KSTNM, STLA, STLO , STEL, STDP, CMPAZ CMPINC, KCMPNM, KSTCMP) present in the headers. 

    In EVENT mode the user supplies one or more event ID strings in addition to station and
    channel restrictions. By default, the ID strings correspond to ISC public event ID values.
    However, you may supply ID strings from other agencies by specifying the agency
    using the EVENT_AGENCY option. Only a single event agency may be specified.
    Event ID strings may be obtained directly from agency websites or can be retrieved
    from within JSAC using the RetrieveEvents command. Seismograms retrieved in EVENT mode
    have time window limits determined automatically by JSAC. In addition to station
    metadata the header fields KEVNM IEVREG, EVLA, EVLO, EVDP, MAG,IMAGTYP, IMAGSRC, 
    IEVTYP, DIST,AZ, BAZ, GCARC are potentially set as well.
    

****
**EXAMPLES**

    rcs LIST_SERVICES
    rcs LIST_AGENCIES
    rcs LIST_NETWORKS IRISDMC
    rcs LIST_STATIONS IRISDMC US


    Retrieve waveforms from US network (hosted at IRISDMC) for station TPNV and for other stations
    with station codes starting with B. Restrict to the B*, H*, and S* channels and use a window 
    defined as (1777475172.0 to 1777475472.0) (300s long)
    rcs network US station B* TPNV channel B* H* S* locid * start 1777475172.0 end 1777475472.0
    Returns 21 files on 6/8/2026


    Retrieve waveforms from US network (hosted at IRISDMC) for station TPNV. Restrict to the
    B* channels and use a window defined as (2026/04/29-15:06:12.000 to 2026/04/29-15:11:12.000) (300s long)
    rcs network US  station TPNV channel B* start_date 2026/04/29-15:06:12.000 end_date 2026/04/29-15:11:12.000
    Returns 3 files on 6/8/2026


    Retrieve waveforms from US network (hosted at IRISDMC) within a lat-lon box (23.0, 45, -119, -75). 
    Restrict to the B* channels and use a window defined as (1777475172.0 to 1777475472.0) (300s long)
    rcs network US STATION_BOX 23.0  45  -119  -75  channel B* start 1777475172.0 end 1777475472.0
    Returns 43 files on 6/8/2026

    Retrieve waveforms from US network (hosted at IRISDMC) within 15 degrees of (35.0,-119). Restrict to
    the B* channels and use a window defined as (1777475172.0 to 1777475472.0) (300s long)
    rcs network US STATION_CIRCLE 35.0  -119  15.0  channel B* start 1777475172.0 end 1777475472.0
    Returns 42 files on 6/8/2026

    Retrieve waveforms from US network (hosted at IRISDMC) within 1500 km of (35.0,-119). Restrict to the
    B* channels and use a window defined as (1777475172.0 to 1777475472.0) (300s long)
    rcs network US STATION_CIRCLE_KM 35.0  -119  1500.0  channel B* start 1777475172.0 end 1777475472.0
    Returns 42 files on 6/8/2026

    For ISC eventID 645871038 retrieve all the vertical channels for stations in the IU network
    and with location code 00. Waveforms and metadata are from IRISDMC and event information from ISC.
    Returns 215 files on 6/8/2026
    rcs network IU event_agency ISC event_id 645871038  chan *Z locid 00

    For ISC eventID 645871090 retrieve BHZ-00 data for stations TATO and COLA
    rcs network IU event_agency ISC event_id 645871090 sta TATO COLA chan BHZ locid 00
    Returns 2 files on 6/8/2026

    For ISC eventID 645871090 retrieve all data for stations in the ORFEUS network NR using
    the ORFEUS dataselect service.
    rcs agency ORFEUS network NR event_agency ISC event_id 645871090
    Returns 2 files on 6/8/2026

    Same as the previous command but this time specify the service explicitly.
    rcs service FDSN agency ORFEUS network NR event_agency ISC event_id 645871090

****
