## PLOTRESP

**SUMMARY**
    Produce amplitude and phase plots for a specified instrument response . 

****
**SYNTAX**

   PLOTRESP (alt PR) {LIST_SERVICES}  SPECIFICATIONS

****
**INPUT**

    LIST_SERVICES:       List available services.
    SERVICE:             The service name. The FDSN service is default and always available. 
                         Other services may be available in your version. SERVICE
                         is "sticky". Once set there is no need to specify the service
                         until you decide to use a different service. 
    AGENCY:              The agency code. Only a single agency may be specified.
    NETWORK or NET:      The network code. Only a single code may be supplied.
    STATION or STA:      The station code. Only a single code may be supplied.
    CHANNEL or CHAN:     The channel code. Only a single code may be supplied.
    LOCID:               The location code. Only a single code may be supplied.
    FILE_NAME:           The name of a response file.
    NSAMP:               The number of time-domain samples used in generating the transfer function
    SAMPRATE:            The sample rate. This controls the maximum frequency displayed in the plots.
    UNWRAP:              When specified, the phase spectrum will be unwrapped.
    TIME:                An instant used for selecting a response epoch expressed as an epoch time.
    HTIME:               An alternate way to specify the time using one of the formats: "yyyyDDD"
                                                                                        "yyyy/MM/dd"
                                                                                        "yyyy/DDD:HH:mm:ss"
                                                                                        "yyyy/MM/dd:HH:mm:ss".

****
**DEFAULT VALUES**

    SERVICE  FDSN
    AGENCY   IRISDMC 
    TIME     The current time.
    UNWRAP   FALSE
    NSAMP    10000
    SAMPRATE 40


****
**DESCRIPTION**

    Produce amplitude and phase plots for a specified response. A file name
    may be specified directly using the file_name option. Otherwise the instrument 
    response is retrieved by means of a channel specification and time. The channel 
    specification depends on the service type. For the default FDSN service the 
    required elements are (agency, network code, station code, channel code
    and location code). Other services may have different requirements.
    

****
**EXAMPLES**

    pr LIST_SERVICES

    Retrieve the response from the  IRISDMC FDSN service for net = IU, station = ANMO
    channel = BHZ, location code = 00
       plotresp service FDSN agency IRISDMC net IU sta ANMO chan BHZ locid 00 samprate 40 nsamp 100000 time 1789034921

    Same as the previous, but with time in human readable format.
       plotresp service FDSN agency IRISDMC net IU sta ANMO chan BHZ locid 00 samprate 40 nsamp 100000 htime 2026/09/10


   Retrieve the response from the  IRISDMC FDSN service for net = IU, station = COL
   channel = VHN, location code = --
      plotresp agency IRISDMC net IU sta COL chan VHN locid -- samprate .1 nsamp 10000 time 788918400


****
