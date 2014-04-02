<%@ page language="java" 
		 contentType="text/html; charset=utf-8"
    	 pageEncoding="utf-8"
    	 import="java.text.DateFormat,
				 java.text.SimpleDateFormat,
				 java.util.Calendar,
				 java.util.Date,
				 entities.StudyUnit,
				 com.dhtmlx.planner.data.DHXDataLoader.DHXDynLoadingMode"%>

<!DOCTYPE html>
<html>
	<head>
		<title> Exam Timetable </title>
		<link href="css/lightbox.css" rel="stylesheet" type="text/css">
		<link href="codebase/dhtmlxscheduler.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" href="//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css">
	</head>

	<body>
		<div id="custom_form">
			<form id="form1" action="updateExam">
				<table id="form1-table">
					<tbody>
						<tr id="first-row" class="field-rows">
							<td>Study Unit Code</td>
							<td>
								<input id="studyunit_code" name="exam-code" type="text" class="field-elements">
							</td>
							<td>Department</td>
							<td>
								<select id="department" name="department" class="field-elements right-fields">
									<option value="CCE">CCE</option>
									<option value="CIS">CIS</option>
									<option value="CS">CS</option>
									<option value="MNE">MNE</option>
									<option value="ICS">ICS</option>
								</select>
							</td>
						</tr>
						<tr class="field-rows">
							<td>Study Unit Title</td>
							<td colspan="3">
								<input id="studyunit_title" name="exam-title" class="field-elements" type="text">
							</td>
						</tr>
						<tr class="field-rows">
							<td>Exam Length</td>
							<td>
								<input id="exam_length" name="exam-length" class="field-elements" type="text">				
							</td>
							<td>No. of Students</td>
							<td>
								<input id="no_of_students" name="no-of-students" class="field-elements right-fields" type="text">			
							</td>
						</tr>
						<tr class="field-rows">
							<td>Year</td>
							<td>
								<input id="exam_year" name="exam-year" class="field-elements" type="text">
							</td>
							<td>Semester</td>
							<td>
								<input id="exam_semester" name="exam-semester" class="field-elements right-fields" type="text">
							</td>
						</tr>
						<tr class="field-rows">
							<td>Credits</td>
							<td>
								<input id="exam_credits" name="exam-credits" class="field-elements" type="text">
							</td>
							<td>Evening</td>
							<td>
								<input id="evening-course" name="evening" type="radio" value="yes" style="margin-left:10px"/>
								<label for="evening-course">Yes</label>
								
								<input id="fulltime-course" name="evening" type="radio" value="no" checked="checked" style="margin-left:20px"/>
								<label for="fulltime-course">No</label>
							</td>
						</tr>
						<tr class="field-rows">
							<td>Room</td>
							<td colspan="3">
								<input id="room" name="room" class="field-elements" type="text">
							</td>
						</tr>
						<tr class="field-rows">
							<td>Time Period</td>
							<td colspan="3">
								<input id="start_date" name="start_date" class="field-elements" type="text" />
								<select id="starttime" name="start_time">
										<%
										DateFormat df = new SimpleDateFormat("HH:mm");
										
										Calendar start_time = Calendar.getInstance();
										start_time.set(Calendar.HOUR_OF_DAY, 8);
										start_time.set(Calendar.MINUTE, 0);
										start_time.set(Calendar.SECOND, 0);
										
										int startDate = start_time.get(Calendar.DATE);
										while (start_time.get(Calendar.HOUR_OF_DAY) != 22) {%>
											<option value="<%=df.format(start_time.getTime())%>">	
												<%=df.format(start_time.getTime())%>
											</option>
											<%
											start_time.add(Calendar.MINUTE, 5);
										}%>
										<option value="22:00">22:00</option>
								</select>
								<span id="span-dash">-</span>
								<!-- <input id="end_date" name="end_date" class="field-elements" type="text" />  -->
								<select id="endtime" name="end_time">
										<%
										DateFormat df2 = new SimpleDateFormat("HH:mm");
										
										Calendar startTime = Calendar.getInstance();
										startTime.set(Calendar.HOUR_OF_DAY, 8);
										startTime.set(Calendar.MINUTE, 0);
										startTime.set(Calendar.SECOND, 0);
										
										while (startTime.get(Calendar.HOUR_OF_DAY) != 22) {%>
											<option value="<%=df2.format(startTime.getTime())%>">	
												<%=df2.format(startTime.getTime())%>
											</option>
											<%
											startTime.add(Calendar.MINUTE, 5);
										}%>
										<option value="22:00">22:00</option>
								</select>
							</td>
						</tr>
					</tbody>		
				</table>
				
				<input id="exam_period_start" name="exam_period_start" type="hidden" value="<%=request.getParameter("startdate")%>"/>
				
				<div id="button-div">
					<input id="save-btn" class="lightbox-buttons" type="button" value="Save" onclick="save_form()">
					<input id="cancel-btn" class="lightbox-buttons" type="button" value="Cancel" onclick="close_form()">
					<input id="delete-btn" class="lightbox-buttons" type="button" value="Delete" onclick="delete_event()">
				</div>
			</form>
		</div>
		
		<div id="header_div">
    		<input id="create_btn" class="lightbox-buttons" type="button" value="New Event" />
    		<input id="search_exam" name="search_exam" type="text" placeholder="Search Study Unit By Code...">
		</div>
		
		<div id="scheduler_here" class="dhx_cal_container">
			<div class="dhx_cal_navline">
				<div class="dhx_cal_prev_button">&nbsp;</div>
				<div class="dhx_cal_next_button">&nbsp;</div>
				<div class="dhx_cal_date"></div>
				<div class="dhx_cal_tab" name="day_tab" style="right:204px;"></div>
				<div class="dhx_cal_tab" name="week_tab" style="right:140px;"></div>
				<div class="dhx_cal_tab" name="month_tab" style="right:76px;"></div>
				
    			<input id="view_as" name="view_as" type="text" placeholder="View As...">
  				
				<div id="switch_years_div">
					<div id="all_tab" class="filter_year active">All</div>
					<div id="fulltime_tab" class="filter_year">Full Time</div>
					<div id="year1_tab" class="filter_year">Year 1</div>  
	  				<div id="year2_tab" class="filter_year">Year 2</div>
					<div id="year3_tab" class="filter_year">Year 3</div>
					<div id="evening_tab" class="filter_year">Evening</div>
					<div id="postgrad_tab" class="filter_year">Postgrad</div>
				</div>
			</div>
			<div class="dhx_cal_header"></div>
			<div class="dhx_cal_data"></div>
		</div>
		
		<script src="//code.jquery.com/jquery-1.9.1.js"></script>
  		<script src="//code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
		<script src="codebase/dhtmlxscheduler.js"></script>
  		<script src="codebase/ext/dhtmlxscheduler_minical.js"></script>
  		<script src="codebase/ext/dhtmlxscheduler_grid_view.js"></script>
  		<script src="js/jquery.iconfield.js"></script>
		<script>
		
			$(function() {
				
				$("#custom_form").draggable();
				
				$("#start_date").datepicker(
				{
					dateFormat: 'dd/mm/yy',
					changeYear : 'true',
					changeMonth: 'true',
					yearRange: "-90:+0"
				});
				
				scheduler.config.api_date = "%Y-%m-%d %H:%i";
				scheduler.config.details_on_dblclick = true;
				scheduler.config.details_on_create = true;
				scheduler.config.first_hour = "8";
				scheduler.config.last_hour = "22";
				scheduler.config.drag_lightbox = true;
				scheduler.config.show_loading = true;
				scheduler.config.mark_now = true;
								
				<%
					String date = request.getParameter("startdate");
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					Date dateObj = sdf.parse(date);
					String newDateStr = sdf.format(dateObj);
					
					String day = newDateStr.substring(0, newDateStr.indexOf("/"));
					String monthStr = newDateStr.substring(newDateStr.indexOf("/") + 1, newDateStr.lastIndexOf("/"));
					int month = Integer.parseInt(monthStr);
					monthStr = Integer.toString(month -= 1);
					
					String year = newDateStr.substring(newDateStr.lastIndexOf("/") + 1, newDateStr.length());
				%>
				
				var dateToPass = new Date();
				dateToPass.setFullYear(<%=year%>, <%=monthStr%>, <%=day%>);
				
				scheduler.init('scheduler_here', dateToPass, "week");
				scheduler.load("events.jsp", "json");
				
				var dp = new dataProcessor("events.jsp");
				dp.init(scheduler);
				
				// add mini calendar to header and add its functionality (i.e. open calendar)
	  			$(".dhx_cal_navline").append("<div class='dhx_minical_icon' id='dhx_minical_icon'>&nbsp;</div>");
		  		$(".dhx_cal_date").css("left", "-100px");
				
		  		$("#dhx_minical_icon").click(function() {
		  		  	
	  		  		if (scheduler.isCalendarVisible()){
				        scheduler.destroyCalendar();
				    
	  		  		} else {
				        scheduler.renderCalendar({
				            position:"dhx_minical_icon",
				            date:scheduler._date,
				            navigation:true,
				            handler:function(date,calendar){
				                scheduler.setCurrentView(date);
				                scheduler.destroyCalendar();
				        	}
				   	 	});
					}
	  		  	});
			});
				
			$("#create_btn").click(function()
			{
				scheduler.startLightbox(html("custom_form"));
				clearLightbox();
				
				$("#custom_form").css("display", "block");
				$("#custom_form").css("left", "380px");
				$("#custom_form").css("top", "150px");
			});
			
			var options = "";
			var lastValue = "";
			
			$("#view_as").on("change keyup paste", function() 
			{	
				var query = $(this).val();
			
				// if query has changed, is not empty and is a number
				if (query != lastValue && query != "" && !isNaN(query)) 
				{
					lastValue = query;
					
					$.ajax({ 
					   	 type: 'GET', 
					     url: 'suggestStudents', 
					     data: 
					     { 
					    	 query: query 
					   	 },
					  	 dataType: 'json',
					   	 success: function (data) 
					   	 {
					   		 var availableTags = new Array();
					   		 var i=0;
					   		 
					   		 $.each(data, function(k, v) {			 
					   		   	availableTags[i] = v;
					   		  	i++;
					   		 });
					   		 
					   		 $("#view_as").autocomplete({
					   	        source: availableTags
					   	     });
					   	 }
					});
				}
			});
					
			$("#view_as").keypress(function(e){
				
				var studentID = $(this).val();
				
				// if user pressed enter
				if (e.which == 13)
				{
					viewAsStudent(studentID);	
				}
			});
			
			function viewAsStudent(studentID)
			{
				if (studentID.trim() != "")
				{
					scheduler.clearAll();
					scheduler.load("events.jsp?studentID=" + studentID);
				}
			}
			
			$("#search_exam").on("change keyup paste", function(){
				
				var query = $(this).val();
				
				// if query has changed, is not empty and is a number
				if (query != lastValue && query != "") 
				{
					lastValue = query;
					
					$.ajax({ 
					   	 type: 'GET', 
					     url: 'updateExam', 
					     data: 
					     {
					    	 studyunit_auto: true,
					    	 query: query 
					   	 },
					  	 dataType: 'json',
					   	 success: function (data) 
					   	 {
					   		 var availableTags = new Array();
					   		 var i=0;
					   		 
					   		 $.each(data, function(k, v) {			 
					   		   	availableTags[i] = v;
					   		  	i++;
					   		 });
					   		 
					   		 $("#search_exam").autocomplete({
					   	        source: availableTags
					   	     });
					   	 }
					});
				}
			});
		
			$("#search_exam").keypress(function(e) {
				
				unitCode = $(this).val();
				
				// if user pressed enter
				if (e.which == 13)
				{
					$.ajax({ 
					   	 type: 'GET', 
					     url: 'getEvents', 
					     data: 
					     {
					    	 unitCode: unitCode
					   	 },
					  	 dataType: 'json',
					   	 success: function (id) 
					   	 {
							 var ev = scheduler.getEvent(id);
							 ev.color = "#FF9933";
							 
							 scheduler.showLightbox(id);			 
							 return true;
					   	 }
					});								
				}
			});
			
			// apply css class to events
			scheduler.templates.event_class = function(start, end, ev){
			     return "event_colour";
			};
			
			// on before drag colour code clashes
			scheduler.attachEvent("onBeforeDrag", function (id, mode, e){
			    //any custom logic here
			    return true;
			});
			

			// add logic to get exams that clash with this exam and color them in the
			// same color
			scheduler.attachEvent("onClick", function (id, e){
				
				scheduler.setCurrentView();
				// redraw all events in the calendar in case another event was clicked before
				
				var ev = scheduler.getEvent(id);
				ev.color = "#FF9933";
				scheduler.updateEvent(id);
				
				$.ajax({ 
				   	 type: 'GET', 
				     url: 'getEvents', 
				     data: 
				     {
				    	 eventID: ev.id
				   	 },
				  	 dataType: 'json',
				   	 success: function (data) 
				   	 {
				   		 // return data event id's 
				   		 $.each(data, function(k, eventid) {			 
					     	
				   			var event = scheduler.getEvent(eventid);
				   			event.color = "#FF9933";
							scheduler.updateEvent(eventid);
					   	 });
				   		 
						 return true;
				   	 }
				});
				
				return true;
			});
			
			$(".filter_year").click(function() {
				
				var elem = $(this);
				
				$(".filter_year").each(function() {
					
					if ($(this) != elem && $(this).hasClass("active"))
					{
						$(this).removeClass("active");
					}
				});
				
				$(this).addClass("active");
				
				var id = $(this).attr("id");
				
				switch(id)
				{
					case "all_tab": reloadData(); break;
					case "fulltime_tab": reloadData("fulltime"); break;
					case "year1_tab": reloadData("1"); break;
					case "year2_tab": reloadData("2"); break;
					case "year3_tab": reloadData("3"); break;
					case "evening_tab": reloadData("evening"); break;
					case "postgrad_tab": reloadData("5"); break;
				}
			});
			
			//the reloadData() function clears the planner events and load new ones.
			function reloadData(year) { 
		        
				scheduler.clearAll();
				
				if (year)
		     	    scheduler.load("events.jsp?year=" + year);
				else scheduler.load("events.jsp");
			}
				
			$("#starttime, #endtime").change(function() {
				
				var timeChanged = $(this);
				var thisId = timeChanged.attr("id");
				var timeValue = timeChanged.val();
				var changeTimeValueId = "";
				
				if (thisId == "starttime")
				{
					changeTimeValueId = "endtime";
				}
				else if (thisId == "endtime")
				{
					changeTimeValueId = "starttime";
				}
					
				var examLength = $("#exam_length").val().trim();
				
				if (examLength == "")
				{
					$("#" + changeTimeValueId).val(addEventLength(timeValue, 2, changeTimeValueId));
				}
				else if (isNaN(examLength) == false)
				{
					$("#" + changeTimeValueId).val(addEventLength(timeValue, examLength, changeTimeValueId));
				}
			});
					
  		  	var html = function(id) { return document.getElementById(id); }; //just a helper
	  	
  		  	function addZero(i)
  		  	{
				if (i<10) 
  		  		{
  		  			i="0" + i;
  		  		}
  				return i;
  			}
  		  	
  		  function setDate(dateInputId, timeInputId) {
	  			
				var date1 = html(dateInputId).value;
	  			
	  			var day = date1.substring(0, date1.indexOf("/"));
	  			var month = date1.substring(date1.indexOf("/") + 1, date1.lastIndexOf("/"));
	  			var year = date1.substring(date1.lastIndexOf("/") + 1, date1.length);
	  			
	  			var time1 = html(timeInputId).value;
	  			
	  			var hours = time1.substring(0, time1.indexOf(":"));
	  			var minutes = time1.substring(time1.indexOf(":") + 1, time1.length);
	  			
	  			var date = new Date();
	  			date.setDate(day);
	  			date.setMonth(month - 1);
	  			date.setFullYear(year);
	  			
	  			date.setHours(hours);
	  			date.setMinutes(minutes);
	  			
	  			return date;
	  		};
  		  	
  		  	function getDate(date, dateInputId, timeInputId) {
	  		  	
  		  		var day = addZero(date.getDate());
	  			var month = addZero(date.getMonth() + 1);
	  			var year = date.getFullYear();
				
	  			var startDate = day + "/" + month + "/" + year;
	  			
	  			var hours = addZero(date.getHours());
				var minutes = addZero(date.getMinutes());
	  			
				var startTime = hours + ":" + minutes;
				
				$("#" + dateInputId).val(startDate);
				$("#" + timeInputId).val(startTime);
  		  	};
  		  	
  		  	function addEventLength(time, eventLength, id) {
	  		  	var hours = time.substring(0, time.indexOf(":"));
				hours = parseInt(hours);
	  		  	eventLength = parseInt(eventLength);
				
	  		  	if (id == "starttime")
	  		  		hours -= eventLength;
	  		  	else if (id == "endtime")
	  		  		hours += eventLength;
	  		  	
	  		  	var minutes = time.substring(time.indexOf(":") + 1, time.length);
				hours = addZero(hours);
	  		  	
				if (id == "endtime" && hours >= 22 && minutes >= 0)
				{
					time = "22:00";
  		  		}
				else if (id == "starttime" && hours < 8)
				{
					time = "08:00";
				}
				else
				{
					time = hours + ":" + minutes;	
				}
				
				return time;
  		  	}
  		  	
  		  	function clearLightbox()
  		  	{
  		  	 	$("#studyunit_code").val("");
				$("#studyunit_title").val("");
		 	    $("#department").val("");
		    	$("#exam_length").val("");
		    	$("#no_of_students").val("");
		     	$("#exam_semester").val("");
		     	$("#exam_year").val("");
		    	$("#exam_credits").val("");
		     	$("#room").val("");
	    	 	$("#fulltime-course").prop("checked", true);	 
	    	 	$("#start_date").val("");
	    	 	
	    	 	$("#starttime").val($("#starttime option:first").val());
	    	 	$("#endtime").val($("#endtime option:first").val());
  		  	}
  		  	
  		  	scheduler.showLightbox = function(id) {

  		  		var ev = scheduler.getEvent(id);
	  			scheduler.startLightbox(id, html("custom_form"));
				
	  			var eventText = ev.text;
	
	  			if (eventText.trim() != "New event")
				{			
		  			// get json request of studyunit details and fill in fields
					$.ajax({ 
					     type: 'GET', 
					     url: 'updateExam', 
					     data: 
					     { 
					    	 get_details: true,
					    	 unit_code: ev.text 
					   	 },
					   	 dataType: 'json',
					     success: function (data) 
					     {
					    	 $("#studyunit_title").val(data.title);
						     $("#department").val(data.department);
						     $("#exam_length").val(data.examLength);
						     $("#no_of_students").val(data.noOfStudents);
						     $("#exam_semester").val(data.semester);
						     $("#exam_year").val(data.year);
						     $("#exam_credits").val(data.credits);
						     $("#room").val(data.room);
						     
						     if (data.evening == "true")
						     {
						    	 $("#evening-course").prop("checked", true); 	 
						     }
						     else if (data.evening == "false")
						     {
						    	 $("#fulltime-course").prop("checked", true);	 
						     }
					     }
					});
				
	  				$("#studyunit_code").val(eventText);
				}
	  			else
	  			{
	  				clearLightbox();
	  			}
	  		
	  			$("#studyunit_code").focus();
	  			
		  		// fill in other fields from event object
	  			var startDate = ev.start_date;
	  			getDate(startDate, "start_date", "starttime");
	  			
	  			var endDate = ev.end_date;
	  			getDate(endDate, "start_date", "endtime");	  			
	  			
  		  	};
	  		
	  		function save_form() {
	  			var ev = scheduler.getEvent(scheduler.getState().lightbox_id);
				
	  			if (ev)
	  			{
	  				ev.text = html("studyunit_code").value;
	  				ev.start_date = setDate("start_date", "starttime");
					ev.end_date = setDate("start_date", "endtime");
					
	  				scheduler.endLightbox(true, html("custom_form"));
	  			}
	  			else
	  			{
	  				scheduler.addEvent({
	  				    start_date: setDate("start_date", "starttime"),
	  				    end_date: setDate("start_date", "endtime"),
	  				    text: html("studyunit_code").value
	  				});
	  			}
	  			
	  			$("#form1").submit();
	  		}
	  		
	  		function close_form() {
	  			scheduler.endLightbox(false, html("custom_form"));
	  		}
	
	  		function delete_event() {
	  			var event_id = scheduler.getState().lightbox_id;
	  			var ev = scheduler.getEvent(event_id);
	  			
	  			$.ajax({
				     type: 'GET', 
				     url: 'deleteExam', 
				     data: 
				     { 
				    	unit_code: ev.text,
				    	start_date: $("#exam_period_start").val()
				   	 },
				     success: function (data) 
				     {
				    	 scheduler.endLightbox(false, html("custom_form"));
				  		 scheduler.deleteEvent(event_id);
				     }
				});
	  		}
			
		</script>	
	</body>
</html>