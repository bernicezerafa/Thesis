<%@ page contentType="application/json" 
		 import="com.dhtmlx.planner.*,servlets.*,events.EventsManager, helpers.SQLHelper, java.sql.Connection"
%>

<%= getEvents(request) %>
<%!
    String getEvents(HttpServletRequest request) throws Exception {
    	EventsManager evs = new EventsManager(request);
    	return evs.run();
  	}
%>