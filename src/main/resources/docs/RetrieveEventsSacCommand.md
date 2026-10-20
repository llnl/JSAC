## RETRIEVEEVENTS

**SUMMARY**

    Retrieve events from an event service

**SYNTAX**

    [R]ETRIEVE[EV]ENTS {MORE} {AGENCY name} {LIST_RETRIEVED} {LIST_AGENCIES} {LATEST} 
        {ALL_AGENCIES} {TIME_LIMITS v1 v2} {DATE_LIMITS v1 v2} {DEPTH_LIMITS v1 v2} 
        {MAG_LIMITS v1 v2} {EVENT_BOX minLat maxLat minLon maxLon} 
	{EVENT_CIRCLE centerLat centerLon radiusDegrees} {EVENT_CIRCLE_KM centerLat centerLon radiusKm}
        {EVENT_ID v1 v2...vn}

**INPUT**

    MORE:
        Add newly-retrieved events to previously-retrieved events.

    AGENCY name:
        The agency from which data are to be retrieved (e.g., IRISDMC). Agency is sticky:
	Once set it remains the same within a single JSAC session.
	
    ALL_AGENCIES:
        Retrieve data from all agencies subject to restrictions specified in this command.

    LIST_RETRIEVED:
	Do not retrieve any events. Simply list the events already retrieved.
	
    LIST_AGENCIES:
	Do not retrieve any events. Simply list the available agencies.

    LIST_TIME_FORMATS:   
        List available time formats for the DATE_LIMITS option.

    LATEST:
    Restrict to events within the last 24 hours. Also, if no arguments are supplied to the
	REV command then the latest events for the current agency are retrieved.
	
    TIME_LIMITS v1 v2:
	Restrict event origin times to be between v1 and v2 where v1 and v2 are epoch times.

    DATE_LIMITS v1 v2:
	Restrict event origin times to be between v1 and v2 where v1 and v2 are Date-time strings.
	The strings must follow the format: yyyy/MM/dd-HH:mm:ss.SSS.

    DEPTH_LIMITS v1 v2:
	Restrict event origins to have depths between v1 and v2 where v1 and v2 are in km.
	
    MAG_LIMITS v1 v2:
	Restrict event origins to have magnitudes between v1 and v2.
	
    EVENT_BOX minLat maxLat minLon maxLon:
	Restrict event origins to have epicenters within the bounds of the rectangle.
	
    EVENT_CIRCLE centerLat centerLon radiusDegrees:
	Restrict event origins to have epicenters within the circle.
	
    EVENT_CIRCLE_KM centerLat centerLon radiusKm:
	Same as above but with the radius given in km.

    EVENT_ID ID1 ID2 and so on
        retrieve events using the event ID. Make sure the event ID and AGENCY are consistent.
	
**DEFAULT VALUES**	

	[R]ETRIEVE[EV]ENTS AGENCY ISC LATEST
    
**DESCRIPTION**	

	This command retrieves event summaries from an EVENT service. Retrieved
	events are stored in memory and may be used by the RCS command to retrieve
	event segmented waveforms.
	
**EXAMPLES**

	rev agency NEIC DATE_LIMITS 2026/05/22-00:00:00.000 2026/05/25-00:00:00.000
	1025 events retrieved in  0.2 seconds
	
	rev agency NEIC DATE_LIMITS 2026/05/22-00:00:00.000 2026/05/25-00:00:00.000 EVENT_CIRCLE 19.34 -155.843 10.0
	76 events retrieved in 23.9 seconds
	
	rev agency NEIC DATE_LIMITS 2026/05/22-00:00:00.000 2026/05/25-00:00:00.000 EVENT_CIRCLE 19.34 -155.843 10.0 DEPTH_LIMITS 0 39
	70 events retrieved in  9.0 seconds
	
	rev agency NEIC DATE_LIMITS 2026/05/22-00:00:00.000 2026/05/25-00:00:00.000 EVENT_CIRCLE 19.34 -155.843 10.0 DEPTH_LIMITS 0 39 MAG_LIMITS 4 8
	1 events retrieved in  0.1 seconds

	rev all_agencies latest mag_Limits 4.5 8
	32 events retrieved in 12.6 seconds

        rev agency ISC event_id 645872461 645872503 645872504
        3 events retrieved from 1 agencies in  3.3 seconds