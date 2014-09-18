<%@ page language="java" 
		 contentType="text/html; charset=utf-8"
    	 pageEncoding="utf-8"
    	 import="java.text.DateFormat,
				 java.text.SimpleDateFormat,
				 java.util.Calendar,
				 java.util.Date,
				 java.sql.Connection,entities.Exam,
				 entities.TimetableEvent,
				 helpers.FileHelper,
				 helpers.SQLHelper,
				 com.dhtmlx.planner.data.DHXDataLoader.DHXDynLoadingMode"%>
<!DOCTYPE html>
<html>
	<head>
		<title> Exam Timetable </title>
		<link href="css/lightbox.css" rel="stylesheet" type="text/css">
		<link href="codebase/dhtmlxscheduler.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" href="//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css">
		<link rel="stylesheet" href="css/chosen/chosen.css" type="text/css">
		<link href="css/jquery.tagit.css" rel="stylesheet" type="text/css">
	</head>
	
	<body>
		<div id="dialog-move-confirm" title="Move Exams Together?">
  			<p>
  				<span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
  				This exam was required to be scheduled with <span id="exam_together"></span>. What do you
  				want to do?
  			</p>
		</div>
		
		<div id="add_drop_section" class="can_drag">
			<div id="switch_divs">
 				<h3>Add/Drop Student to Study Units</h3>
  				<div id="add_student_to_unit" class="accordion_div">
	   				<ol style="padding-left:20px">
 						<li>Type in a student ID, either for an existing student or a new student ID.</li>
  						<li>Confirm that student ID is correct. If this is an existing student, his study units 
    						will appear in the textbox below </li>
  						<li>Remove or Add study units accordingly for this student</li>
  						<li>Click on Add / Drop</li>
	   				</ol>
	   				<input id="choose_student" name="choose_student" class="accordion-elements" type="text" placeholder="Start typing student ID..." >
	   				<button type="button" id="confirm_student" class="lightbox-buttons">Confirm</button>
	   				
	   				<ul id="units_choice">
    					<!-- Existing list items will be pre-added to the tags -->
					</ul>					
 				</div>
			</div>
			<div id="add_drop_button_div">
				<button id="add_drop_btn" class="lightbox-buttons">Add/ Drop</button>
				<button id="cancel_add_drop_btn" class="lightbox-buttons"> Cancel </button>
			</div>
		</div>
		<div id="show_details_section" class="can_drag">
			<div id="switch_divs_details">
				<h3> 
					Students having clashes
					<span id="clash_summary" class="summary_report"> 0 students</span>
				</h3>
				<div id="clashing_exams" class="accordion_div_details">
				</div>
				<h3>
					Evening exams scheduled on weekday mornings
					<span id="evening_summary" class="summary_report">0 exams</span>
				</h3>
				<div id="evening_weekday" class="accordion_div_details">
				</div>
				<h3>
					Students having exams in the same day
					<span id="sameday_summary" class="summary_report">0 students</span>
				</h3>
				<div id="same_day_exams" class="accordion_div_details">
				</div>
				<h3>
					Students having exams on an evening (ending after 5PM) and next morning (starting before 12PM)
					<span id="eveningMorning_summary" class="summary_report">0 students</span>
				</h3>
				<div id="evening_morning" class="accordion_div_details">
				</div>		
				<h3>
					Students having two exams in two consecutive days
					<span id="twodays_summary" class="summary_report">0 students</span>	
				</h3>
				<div id="two_day_exams" class="accordion_div_details">
				</div>
				<h3>
					Students having three exams in three consecutive days
					<span id="threedays_summary" class="summary_report">0 students</span>	
				</h3>
				<div id="three_day_exams" class="accordion_div_details">
				</div>
				<h3>
					Exams with large number of students scheduled at the end of the timetable
					<span id="noofstudents_summary" class="summary_report">0 exams</span>		
				</h3>
				<div id="large_noofstudents" class="accordion_div_details">
				</div>
			</div>
			
			<div id="button-div-details">
				<input id="done_report" class="lightbox-buttons" type="button" value="Close">
			</div>
		</div>
		<div id="edit_students_section" class="can_drag">
			<b>Edit Students for this Study Unit</b>
			<p>
			Insert the student id's of the students you want to add or drop from this study unit and click on 
			Add/ Drop button accordingly.
			</p>
			<ul id="enrolled_students">
    			<!-- Existing list items will be pre-added to the tags -->
			</ul>
			
			<div id="edit_students_button_div">
				<button id="add_students_btn" class="lightbox-buttons">Add/ Drop</button>
				<button id="cancel_edit_students_btn" class="lightbox-buttons"> Cancel </button>
			</div>
		</div>
		<div id="custom_form" class="can_drag">
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
						<tr id="enrolled-students" class="field-rows">
							<td>Enrolled Students</td>
							<td colspan="3">
								<select id="students_in_exam" class="field-elements">
								</select>
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
								<input id="starttime" name="start_time" class="field-elements" type="time">
								<span id="span-dash"> - </span>
								<input id="endtime" name="end_time" class="field-elements" type="time">
							</td>
						</tr>
					</tbody>		
				</table>
				
				<input id="exam_period_start" name="exam_period_start" type="hidden" value="<%=request.getParameter("startdate")%>"/>
				<input id="event_id" name="event_id" type="hidden" />
												
				<div id="button-div">
					<input id="save-btn" class="lightbox-buttons" type="button" value="Save" onclick="save_form()">
					<input id="cancel-btn" class="lightbox-buttons" type="button" value="Cancel" onclick="close_form()">
					<input id="delete-btn" class="lightbox-buttons" type="button" value="Delete" onclick="delete_event()">
				</div>
			</form>
		</div>
		<div id="help_div" class="can_drag">
			<p>
				Colours on the calendar are used to easily show certain constraint violations present in the 
				timetable. The following is an explanation of each colour. Please note that there may be different
				shades of the same colours. The darker the shade is, the more students are being affected by that
				particular violation and vice versa. For more information about these constraints, click on 'Get 
				Details' button on the header of this page. Recommendations for where to move these exams for the
				most fair timetable are provided on hover of that exam in a tooltip.
			</p>
			<table style="border-spacing: 5px;">
				<tr>
					<td><div id="color_normal" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							This is the default scheduler colour
						</p>				
					</td>
				</tr>
				<tr>
					<td><div id="color_orchid" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							Searching for an exam from the text box at the top, selects that exam in this colour
						</p>				
					</td>
				</tr>
				<tr>
					<td><div id="color_salmon" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							Exams which have common students and are scheduled at the same time producing a clash.
							Once an exam is clicked, exams having students in common with that exam are highlighted 
							in this colour.
						</p>				
					</td>
				</tr>
				<tr>
					<td><div id="color_deyork" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							Exams which have common students and are scheduled in the same day
						</p>				
					</td>
				</tr>
				<tr>
					<td><div id="color_lightorange" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							Exams which have common students and are scheduled in two consecutive days
						</p>				
					</td>
				</tr>
				<tr>
					<td><div id="color_darkblue" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							Evening exams which are not scheduled during weekday evenings or saturday mornings
						</p>				
					</td>
				</tr>
				<tr>
					<td><div id="color_lightblue" class="color_div"></div></td>
					<td>
						<p style="display:inline">
							Exams which have common students and are scheduled in three consecutive days	
						</p>
					</td>
				</tr>
			</table>
			
			<div id="button-help-div">
				<input id="close_help_btn" class="lightbox-buttons" type="button" value="Close">
			</div>
		</div>
		<div id="header_div">
    		<input id="create_btn" class="lightbox-buttons" type="button" value="New Event" />
    		<input id="showstats_btn" class="lightbox-buttons" type="button" value="Show Details" />
    		<input id="refresh_btn" class="lightbox-buttons" type="button" value="Refresh" />
    		<input id="search_exam" name="search_exam" type="text" placeholder="Search Study Unit By Code..." />
    		<img id="help_icon" src="./images/help.png" height="25px" width="25px" />
    		<input id="export_png" class="lightbox-buttons" type="button" value="Print" />
			<input id="addDropStudents" class="lightbox-buttons" type="button" value="Add/Drop Students" />
		</div>
		<div id="scheduler_here" class="dhx_cal_container">
			<div class="dhx_cal_navline">
				<div class="dhx_cal_prev_button">&nbsp;</div>
				<div class="dhx_cal_next_button">&nbsp;</div>
				<div class="dhx_cal_date" style="left:-55px !important"></div>
				<div class="dhx_cal_tab" name="day_tab" style="right:204px;"></div>
				<div class="dhx_cal_tab" name="week_tab" style="right:140px;"></div>
				<div id="month_tab" class="dhx_cal_tab" name="month_tab" style="right:76px;"></div>
				<div id="grid_tab" class="dhx_cal_tab" name="grid_tab"></div>
				
				<div id="details_view">Details View</div>
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
		<div id="overlay_div"></div>
		<div id="footer_div">
			<table id="status_table">
				<tbody>
			   		<tr>
				    	<td class="color_cell"><div id="color_salmon" class="color_div"></div></td>
					    <td class="info_cell">Clashes</td>
					    <td class="status_cell" id="clash_status">0</td>
				    	<td class="color_cell"><div id="color_darkblue" class="color_div"></div></td>
				    	<td class="info_cell">Evening exams in the morning</td>
					 	<td class="status_cell" id="evening_status">0</td>
					 	<td class="color_cell"><div id="color_deyork" class="color_div"></div></td>
						<td class="info_cell">Same Day</td>
					 	<td class="status_cell" id="sameday_status">0</td>
						<td class="color_cell"><div id="color_lightorange" class="color_div"></div></td>
					 	<td class="info_cell">Two Consecutive Days</td>
					 	<td class="status_cell" id="twoday_status">0</td>
						<td class="color_cell"><div id="color_lightblue" class="color_div"></div></td>
					 	<td class="info_cell">Three Consecutive Days</td>
					 	<td class="status_cell" id="threeday_status">0</td>
					</tr>
				</tbody>
   			</table>
		</div>
		
		<script src="//code.jquery.com/jquery-1.10.2.js"></script>
  		<script src="//code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
		<script src="codebase/dhtmlxscheduler.js"></script>
  		<script src="codebase/ext/dhtmlxscheduler_minical.js"></script>
  		<script src="codebase/ext/dhtmlxscheduler_grid_view.js"></script>
 		<script src="codebase/ext/dhtmlxscheduler_tooltip.js"></script>
  		<script src="http://export.dhtmlx.com/scheduler/api.js"></script> 
  		<script src="js/chosen.jquery.js"></script>
  		<script src="js/jquery.iconfield.js"></script>
		<script src="js/tag-it.js"></script>
		 
		<script>
			
			$(function() 
			{			
				var format = scheduler.date.date_to_str("%d/%m/%Y %H:%i"); 
				var timeformat = scheduler.date.date_to_str("%H:%i");
				
				// initialize grid view
				scheduler.locale.labels.grid_tab = "Details";				
				scheduler.createGridView(
				{
				    name:"grid",
				    fields:
				    [    
				    	{id:"text", label:'Unit Code', sort:'str',  width:200},
				       	{id:"start_date", label:'Start Date', sort:'date', width:'*'},
				       	{id:"end_date", label:'End Date', sort:'date', width:'*'}
					]
				});
				scheduler.templates.grid_full_date;
				
				// fill in status info	
				updateStatusInfo();
				
				// tags plugin for adding and dropping students from units
		        $("#units_choice").tagit(
		        {
		            removeConfirmation: true,
		            allowSpaces: true,
		            placeholderText: "add/drop study units",
		        	tagSource: function(search, showChoices) 
		            {
		        		var that = this;
						                    	
		                $.ajax(
		                {
		                    url: "updateExam",
		                    data: search,
		                    dataType: "json",
		                    success: function(data) 
		                    {		                    	
		                    	var availableTags = new Array();
		                    	
		                    	$.each(data, function(k, v) {
						   		 	availableTags.push(v);
							   	});
		                    	
		                    	if (availableTags.length > 0) {
		                    		showChoices(that._subtractArray(availableTags, that.assignedTags()));
		                    	}
		                    }
		             	});
		            }
		        });
				
		     	// tags plugin for adding and dropping students from units
		     	$("#enrolled_students").tagit(
		     	{		     		
		            removeConfirmation: true,
		        	tagSource: function(search, showChoices) 
		            {
		        		var that = this;
								        		
		        		$.ajax(
        				{
        					url: "suggestStudents",
        					data: search,
        					dataType: "json",
        					success: function(data)
        					{
        						var availableTags = new Array();
    		                    	
    		                    $.each(data, function(k, v) {
    						   	 	availableTags.push(v);
    							});
    		                    	
    			                if (availableTags.length > 0) {
    			                  	showChoices(that._subtractArray(availableTags, that.assignedTags()));
    			                }        	                     
               				}
        				});
		             }
		        });
				
		        // expand and collapse divs
				$("#switch_divs").accordion();
				$("#switch_divs_details").accordion();
				
				// if report section empty, disable expand
				$("#switch_divs_details").on("accordionbeforeactivate", function(event, ui)
				{
				       if($.trim($(ui.newPanel).html()).length == 0)
				          event.preventDefault();
				});
				
				$("dhx_cal_cover").css("height", "100%");
				
				// all overlayed divs can be dragged
				$(".can_drag").draggable();
				
				// search select box
				$("#students_in_exam").chosen(
				{
					width: "100%",
					no_results_text: "No such student is in this exam!",
				});
				
				$(".ui-dialog-titlebar").prepend("<span class=\"ui-icon ui-icon-alert\" style=\"float:left; margin: 0 7px 0px 0;\"></span>");
				$(".chosen-single").css("width", "80%");
				$(".chosen-drop").css("width", "82.5%");
				$(".chosen-container").css("display", "inline-flex");
				$(".chosen-container").append("<input id=\"edit_students_btn\" type=\"image\" src=\"images/edit-icon.png\" name=\"edit_students\" width=\"25\" height=\"25\" style=\"margin-left:15px\">");
				
				// date picker jquery ui on start date in lightbox
				$("#start_date").datepicker(
				{
					dateFormat: 'dd/mm/yy',
					changeYear : 'true',
					changeMonth: 'true',
					yearRange: "-90:+0"
				});
				
				// for clickable search icons
				$('#view_as').iconfield(
				{
					 'image-url' : 'images/search.png',
					 'icon-cursor' : 'pointer',
					 'left' : false
				});
							
				$('#search_exam').iconfield(
				{
					 'image-url' : 'images/search.png',
					 'icon-cursor' : 'pointer',
				     'left' : false	
				});
				
				// specific scheudler settings
				scheduler.config.api_date = "%Y-%m-%d %H:%i";
				scheduler.config.details_on_dblclick = true;
				scheduler.config.details_on_create = true;
				scheduler.config.first_hour = "8";
				scheduler.config.last_hour = "22";
				scheduler.config.drag_lightbox = true;
				scheduler.config.show_loading = true;
				scheduler.config.mark_now = true;
				
				dhtmlXTooltip.config.className = 'dhtmlXTooltip tooltip'; 
				dhtmlXTooltip.config.timeout_to_display = 50; 
				dhtmlXTooltip.config.delta_x = 15; 
				dhtmlXTooltip.config.delta_y = -20;
				
				var title = "", noOfStudents = "", year = "", credits = "";
				var recommends = "";
				
				// tooltips on event hover
				scheduler.templates.tooltip_text = function(start,end,event) 
				{	
					$.ajax(
					{ 
					     type: 'GET', 
					     url: 'updateExam', 
					     data: 
					     { 
					    	 get_details: true,
					    	 tooltip: true,
					    	 event_id: event.id,
					    	 event_start: format(event.start_date),
					    	 event_end: format(event.end_date),
					   	 },
					   	 dataType: 'json',
					     success: function (data) 
					     {
					    	// set field values for tooltip from json object					    	
					    	title = data.title;
					    	noOfStudents = data.noOfStudents;
					    	year = data.year;
					    	credits = data.credits;
					    	
					    	var recommendations = data.recommendations;
							var count = 0;	
					    	
						    $.each(recommendations, function(key, value) 
						    {
						    	var recommend = "<br/><b>Recommend" + (key + 1) + "</b> " + value.start_date + " - " + value.end_date;
						    	
						    	if (count == 0)
						    		recommends = recommend;
						    	else recommends += recommend;						    	
								
						    	count++;
						    });
					     }
					});
					
					return  "<b>Unit Code:</b> " + event.text +
    						"<br/><b>Date:</b> " + format(start) + " - " + timeformat(end) +
   							"<br/><b>Title:</b> " + title + 
   							"<br/><b>Students:</b> " + noOfStudents +
   							"<br/><b>Years:</b> " + year + 
   							"<br/><b>Credits:</b> " + credits +
   							recommends;				
   				};
				
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
				
				// initialize scheduler and load events
				scheduler.init('scheduler_here', dateToPass, "week");
				scheduler.load("events.jsp", "json");
				
				var dp = new dataProcessor("events.jsp");
				dp.init(scheduler);
				
				// update event colours on move
				dp.attachEvent("onAfterUpdate", function(sid, action, tid, tag)
				{
	  		  		getEventColors();
					updateStatusInfo();
				});
				
				// add mini calendar to header and add its functionality (i.e. open calendar)
	  			$(".dhx_cal_navline").append("<div class='dhx_minical_icon' id='dhx_minical_icon'>&nbsp;</div>");
		  		$(".dhx_cal_date").css("left", "-70px");
		  		$("#grid_tab").css(
				{	
					'right': 'auto',
					'left': '197px'
				});
				
				$("#month_tab").css(
				{
					'border-top-right-radius': '0px',
					'border-bottom-right-radius': '0px'
				});
		  		
		  		// set mini calendar settings on icon click
		  		$("#dhx_minical_icon").click(function()
		  		{  	
	  		  		if (scheduler.isCalendarVisible())
	  		  		{
				    	scheduler.destroyCalendar();	    
	  		  		} 
	  		  		else 
	  		  		{
				        scheduler.renderCalendar(
				        {
				            position:"dhx_minical_icon",
				            date:scheduler._date,
				            navigation:true,
				            handler:function(date,calendar)
				            {
				                scheduler.setCurrentView(date);
				                scheduler.destroyCalendar();
				        	}
				   	 	});
					}
	  		  	});
			});
			
			function updateStatusInfo()
			{
				$.ajax(
				{
					url: 'getDetails',
					dataType: 'json',
					data:
					{
						'status_info': true 
					},
					success: function(status)
					{
						$("#clash_status").html(status.clashes);
						$("#evening_status").html(status.evening);
						$("#sameday_status").html(status.sameday);
						$("#twoday_status").html(status.twodays);
						$("#threeday_status").html(status.threedays);
					}
				});
			}
			
			$("#help_icon").click(function()
			{
				$("#help_div").slideToggle();
			});
			
			$("#close_help_btn").click(function()
			{
				$("#help_div").slideToggle();
			});
			
			$("#refresh_btn").click(function()
			{
				getEventColors();
			});
			
			// after all events load, set event colors
			scheduler.attachEvent("onXLE", function()
			{
				getEventColors();
			});
			
			function updateSchedulerColors()
			{
				var evs = scheduler.getEvents();
				
				$.each(evs, function(key, event)
				{
					event.color = "";
					scheduler.updateEvent(event.id);	
				});
			}
			
			var priorities = ["#E96D63", "#7FCA9F", "#85C1F5", "#F4BA70", "#05326D"];
			
			// update event colors according to violations
			function getEventColors()
			{
				updateSchedulerColors();
				
				$.ajax(
				{
					url: "getEvents",
					data:
					{ 
						getColors: true 
					},
					dataType: "json",
					success: function(constraintEventMap)
					{						
	                   	$.each(constraintEventMap, function(k, eventColorMap) 
	                   	{
	                   		$.each(eventColorMap, function(k, eventColorObj)
	                   		{
		                   		var color = eventColorObj.color;
		                   		var examsAffected = eventColorObj.exams;
		                   		
		                   		$.each(examsAffected, function(k, eventId) 
		        	            {
		                   			var event = scheduler.getEvent(eventId);
		                   			
		                   			if (event)
		                   			{
		                   				// if colour to be applied is more important than existing colour, apply it
		                   				if (event.color == "" || (priorities.indexOf(color) < priorities.indexOf(event.color)))
		                   				{
			                   				event.color = color;
		                   				}
		                   				
		                   				scheduler.updateEvent(eventId);
		                   			}
		        	            });
	                   		});
						});	
       				}
				});
			}
			
			// returns difference between two arrays
			// used for add/drop units
			Array.prototype.diff = function(a) 
			{
			    return this.filter(function(i) 
			    {
			    	return a.indexOf(i) < 0;
			    });
			};
			
			var studentExams = new Array();
			
			// if existing student, get his/her study units
			// else add new student
			$("#confirm_student").click(function()
			{
				studentExams = new Array();
				
				var studentID = $("#choose_student").val();
				$("#units_choice").tagit("removeAll");
				
				$.ajax(
				{
					url: "suggestStudents",
					data:
					{ 
						studentID: studentID 
					},
					dataType: "json",
					success: function(data)
					{
						if (data.insert)
						{
							alert("A new student with ID " + data.insert + " was added");
						}
						else
						{
	                    	$.each(data, function(k, v) 
	                    	{
	                    		studentExams.push(v);
	            		        $("#units_choice").tagit("createTag", v);
							});
						}
       				}
				});
			});
			
			$("#addDropStudents").click(function()
			{				
				$("#add_drop_section").slideToggle();
			});
			
			// remove all previous reports
			function emptyPreviousReports()
			{
				$(".accordion_div_details").each(function()
				{					
					$(this).empty();
					
					var span = $(this).prev().find(":last-child");
					
					var spanhtml = span.html();
					var label = spanhtml.split(" ").pop();
					span.html("0 " + label);
				});
			}
						
			var stringify_start = scheduler.date.date_to_str("%D %d/%m/%Y %H:%i"); 
			var stringify_end = scheduler.date.date_to_str("%H:%i");
			
			// get timetable details - report section
			$("#showstats_btn").click(function()
			{
				$.ajax(
				{
					url: 'getDetails',
					dataType: 'json',
					success: function(data)
					{
						emptyPreviousReports();
						
						console.log(data);
						$.each(data, function(key, violation)
						{
							var divId = "#" + violation.type;
							
							var previous = $(divId).prev().find(":last-child");
							
							if (divId != "#evening_weekday" && divId != "#large_noofstudents")
								previous.html(violation.noOfStudents + " students"); 
							else previous.html(violation.noOfStudents + " exams");
							
							var violations = violation.violations;
							
							$.each(violations, function(key, value)
							{
								var exams = value.exams;
								var examsPar = "<p> ";
								
								$.each(exams, function(index, exam)
								{
									var unitCode = exam.unitCode;
									var evening = exam.evening;
									
									var examUnitCode = "";
									
									if (evening == false)
										examUnitCode = "<b>" + unitCode + "</b>";
									else
										examUnitCode = "<b>" + unitCode + " Evening </b>";		
										
									var startdate = exam.eventStart;
									var endtime = exam.eventEnd;
									examsPar +=  examUnitCode + " (" + startdate  + "-" + endtime + ") and ";
								});
								
								examsPar = examsPar.substr(0, examsPar.lastIndexOf("and"));
								
								var thisNoOfStudents = value.thisNoOfStudents;
								examsPar += " - " + thisNoOfStudents + " students </p>";
								$(divId).append(examsPar);
								
								var studentsAffected = value.students;
								var studentsPar = "<p> ";
								
								$.each(studentsAffected, function(index, studentObj)
								{
									var studentId = studentObj.studentId;
									studentsPar += studentId + ", ";
								});
								
								studentsPar = studentsPar.substr(0, studentsPar.lastIndexOf(", "));
								studentsPar += "</p>";
								$(divId).append(studentsPar);
							});				
						});	
						
						openAccordionDiv();
					}
				});
				
				$("#show_details_section").slideToggle();
			});
			
			function openAccordionDiv()
			{
				var defaultActive = 0;
				var count = 0;
				$(".accordion_div_details").each(function()
				{
					var span = $(this).prev().find(":last-child");
					var spanhtml = span.html();
					var violationno = spanhtml.trim().substr(0, spanhtml.indexOf(" ") + 1);
					
					if (violationno != 0)
					{
						defaultActive = count;
						return;
					}
					
					count++;
				});
				
				$("#switch_divs_details").accordion("option", "active", defaultActive);
			}
			
			$("#done_report").click(function()
			{
				$("#show_details_section").slideToggle();
			});
			
			// add/ drop students from this particular unit - edit students section
			$("#add_students_btn").click(function()
			{
				var studentIds = $("#enrolled_students").tagit("assignedTags");
				var unit = $("#studyunit_code").val();
				var eveningVal = $('input:radio[name=evening]:checked').val();
				var evening = false;
				
				if (eveningVal == "yes")
					evening = true;
					
				$.ajax(
				{
					url: 'addDropStudent',
					data:
					{
						'studentIds': studentIds,
						'unit': unit,
						'evening': evening
					},
					dataType: 'json',
					success: function(data)
					{
						$.each(data.studentAddDrop, function(key, student)
						{
							var studentId = student.studentId;
							var drop = student.drop;
							
							if (drop == false)
							{
						    	$("#students_in_exam").append('<option value=' + studentId + '>' + studentId + '</option>');
						    	$("#students_in_exam").trigger("chosen:updated");								
							}
							else
							{
								$("#students_in_exam option[value='"+ studentId +"']").remove();
						    	$("#students_in_exam").trigger("chosen:updated");
							}
						});
												
						$("#edit_students_section").slideToggle();
						$("#enrolled_students").tagit("removeAll");
						getEventColors();
						updateStatusInfo();
					}
				});
			});
			
			$("#add_drop_btn").click(function()
			{
				// get tags currently in textbox
				var studentID = $("#choose_student").val(); 
				var units = $("#units_choice").tagit("assignedTags");
				
				var dropped = studentExams.diff(units);
				var added = units.diff(studentExams);
				
				$("#add_drop_section").slideToggle();
				
				$.ajax(
				{
					url: 'addDropStudent',
					data: 
					{
						'studentID': studentID,
						'dropped': dropped,
						'added': added
					},
					dataType: 'json',
					success: function(data)
					{
						getEventColors();		
						updateStatusInfo();
						$("#units_choice").tagit("removeAll");
						$("#choose_student").val();					
					}
				});
			});
			
			// minimize add drop section on cancel
			$("#cancel_add_drop_btn").click(function()
			{
				$("#add_drop_section").slideToggle();
			});
			
			$(document).on("click", "#edit_students_btn", function()
			{
				$("#edit_students_section").slideToggle();
			});
			
			$("#cancel_edit_students_btn").click(function()
			{
				$("#edit_students_section").slideToggle();
			});
			
  		  	var create_event = false;
			
			// on New Event button click, start lightbox
			$("#create_btn").click(function()
			{
				create_event = true;
				$("#enrolled-students").css("visibility", "hidden");
				$("#enrolled-students").css("line-height", "0px");
				
				scheduler.startLightbox(html("custom_form"));		
				clearLightbox();
				
				$("#custom_form").css("display", "block");
				$("#custom_form").css("left", "380px");
				$("#custom_form").css("top", "150px");
			});
			
			// export to png
			$("#export_png").click(function()
			{
				scheduler.exportToPNG(
				{
					name:"timetable.png",
					format:"A4",
					orientation:"landscape"
				});
			});
			
			var options = "";
			var lastValue = "";
			
			function viewAsSearch(studentID)
			{
				// view as student
				if (studentID.trim() != "")
				{
					scheduler.clearAll();
					scheduler.load("events.jsp?studentID=" + studentID);
				}
			}
			
			$('#view_as').on('iconfield.click', function()
			{
				viewAsSearch($(this).val());
			});
			
			// on keypress in view as field, get student id card numbers suggestions
			$("#view_as").on("change keyup paste", function() 
			{	
				var query = $(this).val();
			
				// if query has changed, is not empty and is a number
				if (query != lastValue && query != "" && !isNaN(query)) 
				{
					lastValue = query;
					getStudentIds(query, "view_as");
				}
			});
			
			// get all student id's to view timetable from their point of view
			function getStudentIds(query, id)
			{
				$.ajax(
				{ 
				   	 type: 'GET',
				     url: 'suggestStudents',
				     data: 
				     { 
				    	 term: query 
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
				   		 
				   		 $("#" + id).autocomplete({
				   	     	source: availableTags
				   	     });
				   	 }
				});
			}
			
			// get student suggestions for add/drop section
			$("#choose_student").on("change keyup paste", function()
			{
				var textbox = $(this);
				
				var query = textbox.val();
				var id = textbox.attr("id");
				
				if (query != lastValue && query != "" && !isNaN(query)) 
				{
					lastValue = query;
					getStudentIds(query, id);
				}
			});
			
			// when the user presses enter with an id card number, reload events with student id
			$("#view_as").keypress(function(e)
			{
				// if user pressed enter
				if (e.which == 13)
				{
					viewAsSearch($(this).val());
				}
			});
				
			// for suggestions with study units on typing in search textbox
			$("#search_exam").on("change keyup paste", function()
			{	
				var query = $(this).val();
					
				// if query has changed, is not empty and is a number
				if (query != lastValue && query != "") 
				{
					lastValue = query;
					
					$.ajax(
					{ 
					   	 type: 'GET', 
					     url: 'updateExam', 
					     data: 
					     {
					    	 studyunit_auto: true,
					    	 query: query,
					    	 semester: <%=FileHelper.getInputParameters().getSemester()%>
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
		
			// search for exam by unit code in timetable
			function searchExam(unitCode, evening) 
			{	
				$.ajax(
				{ 
				   	 type: 'GET', 
				     url: 'getEvents', 
				     data: 
				     {
				    	 unitCode: unitCode,
				    	 evening: evening
				   	 },
				  	 dataType: 'json',
				   	 success: function (id) 
				   	 {
						 var ev = scheduler.getEvent(id);
						 ev.color = "#DA70D6";
						 
						 scheduler.showLightbox(id);
						 scheduler.setCurrentView(ev.start_date, scheduler.getState().mode);
						 scheduler.updateEvent(id);
						 
						 return true;
				   	 }
				});
			}
			
			// if the user is searching for an exam by unit code
			// highlight event in orange and open its details
			$("#search_exam").keypress(function(e) 
			{	
				unitCode = $(this).val();
				var evening = false;
				
				if (unitCode.trim().indexOf("-") != -1)
				{
					unitCode = unitCode.substring(0, unitCode.indexOf(" ")).trim();
					evening = true;
				}
				
				// if user pressed enter
				if (e.which == 13)
				{
					searchExam(unitCode, evening);						
				}
			});
			
			// search for an exam from textbox in header
			$('#search_exam').on('iconfield.click', function()
			{	
				var unitcode = $(this).val();
				var evening = false;
				
				if (unitCode.trim().indexOf("-") != -1)
				{
					unitCode = unitCode.substring(0, unitCode.indexOf(" ")).trim();
					evening = true;
				}
				
				searchExam(unitcode, evening);
			});
			
			var colouredEvents = [];
			
			// this function is called to reset event colours when an event is clicked
			// and there are already coloured (clashed) exams
			function resetEventColours()
			{
				for (var i = 0; i < colouredEvents.length; i = i + 1)
				{	 
				   	var colouredEventObj = colouredEvents[i];
				   	var colouredEventId = colouredEventObj.id;
				 	var colouredEvent = scheduler.getEvent(colouredEventId);
				 	
				 	if (colouredEvent)
		   			{
				 		colouredEvent.color = colouredEventObj.evColor;
						scheduler.updateEvent(colouredEventId);
		   			}
				}
				
				colouredEvents = [];
			}
			
			function isContemporaneousExam(eventId) {

				var defer = $.Deferred();
				$.ajax(
				{ 
				   	 type: 'GET', 
				     url: 'updateExam', 
				     data: 
				     {
				    	 eventId: eventId,
				    	 isContemporaneous: true
				   	 },
				  	 dataType: 'json',
				   	 success: function (data) 
				   	 {
						if (data.examCodes) {
				   		 	defer.resolve(data);
						}
				   	 }
				});
				return defer.promise();
			}
			
			/* if move contemporaneous exams, alert user to choose 
			if move together or separately */
			function moveConfirmation(eventId) {	
				
				var defer = $.Deferred();					
				isContemporaneousExam(eventId).then(function(move) {
					
					if (move) {
						
						$('#exam_together').html("");
						
						var unitCodes = "";
						$.each(move.examCodes, function(k, examCode){
							unitCodes += examCode + " ";
						});
						
						$('#exam_together').html(unitCodes.trim());
												
						$('#dialog-move-confirm').dialog({
					    	resizable: false,
					    	width: 400,
					    	height: 200,
					    	autoOpen: true,
					    	modal: true,
					    	buttons: {
								'Move Separately': function() {
									$(this).dialog('close');
									defer.resolve('separately');
								},
						        'Move Together': function() {
						         	$(this).dialog('close');
						         	defer.resolve('together', move.eventIds);
					        	},
								Cancel: function() {
									$(this).dialog('close');
									defer.resolve(false);
								}
					      	}
					    });
					}
				});
				return defer.promise();
			}
			
			var eventBefore = {};
			scheduler.attachEvent("onBeforeEventChanged", function(ev, e, is_new, ev_old) {
				
				var state = scheduler.getState();
				eventBefore = ev_old;
				
				if (state.drag_mode === 'move') {
					
					moveConfirmation(ev.id).then(function(move, eventIds) {
						
						if (move === false) {
							ev.start_date = eventBefore.start_date; 
							ev.end_date = eventBefore.end_date; 
							
							scheduler.updateEvent(ev.id); // renders the updated event
							scheduler.updateView();
						
						} else if (move === 'together') {
								
							$.each(eventIds, function(k, eventId) {
								
								var otherEvent = scheduler.getEvent(eventId);
								
								otherEvent.start_date = ev.start_date; 
								otherEvent.end_date = ev.end_date; 
								
								scheduler.updateEvent(otherEvent.id); // renders the updated event
								scheduler.updateView();
							});
						}
					});
				}
				return true;
			});
			
			// on event changed, update event colours
			scheduler.attachEvent("onEventChanged", function(id,ev)
			{
				getEventColors();
				updateStatusInfo();
			});
			
			// change date on header to Details view on before view change
			scheduler.attachEvent("onBeforeViewChange", function(old_mode, old_date, mode, date)
			{    
				if (mode == "grid")
				{
					$("#details_view").css("display", "block");
					$("#dhx_minical_icon").css("display", "none");
				}
				else
				{
					$("#details_view").css("display", "none");
					$("#dhx_minical_icon").css("display", "block");
				}
				
			    return true;
			});
							
			// add logic to get exams that clash with this exam and color them in the
			// same color
			scheduler.attachEvent("onClick", function (id, e)
			{	
				resetEventColours();
				
				$.ajax(
				{ 
				   	 type: 'GET', 
				     url: 'getEvents', 
				     data: 
				     {
				    	 eventID: id
				   	 },
				  	 dataType: 'json',
				   	 success: function (data) 
				   	 {
				   		// return data event id's 
				 		$.each(data, function(k, eventid) 
				 		{ 	
				   			var event = scheduler.getEvent(eventid);
				   			
				   			if (event)
				   			{
				   				var obj = 
				   				{
				   					id: eventid,
				   					evColor: event.color
				   				};
				   				
					   			colouredEvents.push(obj);
					   			event.color = "#E96D63";
								scheduler.updateEvent(eventid);					   			
				   			}
						});
						return true;
				   	 }
				});
						
				return true;
			});
			
			// on click of a tab from filter year class
			// i.e. All, FullTime, Postgrad, Yr 1, 2, 3, or Evening
			$(".filter_year").click(function()
			{	
				var elem = $(this);
				
				// remove active class from the tab clicked before
				$(".filter_year").each(function()
				{	
					if ($(this) != elem && $(this).hasClass("active"))
					{
						$(this).removeClass("active");
					}
				});
				
				// add active class to tab clicked
				$(this).addClass("active");
				
				var id = $(this).attr("id");
				
				// load data according to which tab was clicked
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
			function reloadData(year) 
			{    
				scheduler.clearAll();
				
				if (year)
		     	    scheduler.load("events.jsp?year=" + year);
				else scheduler.load("events.jsp");
			}
					
			// helper to get elements by id from javascript
  		  	var html = function(id) { return document.getElementById(id); }; 
	  	
  		  	// add zero to date when it is less than 10 e.g. 6 becomes 06
  		  	function addZero(i)
  		  	{
				if (i<10) 
  		  		{
  		  			i="0" + i;
  		  		}
  				return i;
  			}
  		  	
  		  function setDate(dateInputId, timeInputId)
  		  {	
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
  		  	
  		  	function getDate(date, dateInputId, timeInputId)
  		  	{
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
  		  	
  		  	// clear lightbox values, set everything to default
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
			    $("#students_in_exam").empty();
			    $("#students_in_exam").trigger("chosen:updated");			    
	    	 	$("#starttime").val($("#starttime option:first").val());
	    	 	$("#endtime").val($("#endtime option:first").val());
  		  	}
			  		  	  		  	
  		  	scheduler.showLightbox = function(id) 
  		  	{
				var ev = scheduler.getEvent(id);
  				scheduler.startLightbox(id, html("custom_form"));
	  			var eventText = ev.text;
	
	  			if (eventText.trim() != "New event")
				{
	  				create_event = false;
				    $("#enrolled-students").css("visibility", "visible");
				    $("#enrolled-students").css("line-height", "45px");
				 	
				    $.ajax(
					{ 
					     type: 'GET', 
					     url: 'updateExam', 
					     data: 
					     { 
					    	 get_details: true,
					    	 exam_code: eventText,
					    	 event_id: id
					   	 },
					   	 dataType: 'json',
					     success: function (data) 
					     {					    	 
					    	// set field values for lightbox from json object					    	
					    	$("#studyunit_code").val(data.unitCode);
					    	$("#studyunit_title").val(data.title);
						    $("#department").val(data.department);
						    $("#exam_length").val(data.examLength);
						    $("#no_of_students").val(data.noOfStudents);
						    $("#exam_semester").val(data.semester);
						    $("#exam_year").val(data.year);
						    $("#exam_credits").val(data.credits);
						    $("#room").val(data.room);
						    						    
						    if (data.evening == true)
						    {
						   		$("#evening-course").prop("checked", true);
						    }
						    else if (data.evening == false)
							{
						   		$("#fulltime-course").prop("checked", true);	 
							}
						    
						    var students = data.students;
						    $("#students_in_exam").empty();
						    $("#students_in_exam").trigger("chosen:updated");
					    	$("#enrolled_students").tagit("removeAll");
									    
						    $.each(students, function(key, value) 
						    {	
						    	$("#students_in_exam").append('<option value=' + value.id + '>' + value.id + '</option>');
						    	$("#students_in_exam").trigger("chosen:updated");
						    });
					     }
					});
				}
	  			else
	  			{
	  				create_event = true;
				    $("#enrolled-students").css("visibility", "hidden");
				    $("#enrolled-students").css("line-height", "0px");
	  				clearLightbox();
	  			}
	  		
	  			$("#studyunit_code").focus();
	  			
		  		// fill in other fields from event object
	  			var startDate = ev.start_date;
	  			getDate(startDate, "start_date", "starttime");
	  			
	  			var endDate = ev.end_date;
	  			getDate(endDate, "start_date", "endtime");	  			
	  			
  		  	};
  		  	
  		  	function submit_lightbox()
  		  	{
  		  		$.ajax(
  		  		{
  		  			type: 'GET',
  		  		  	url: 'updateExam',
  		  		  	data: $('#form1').serialize(),
  		  		  	success: function()
  		  		  	{
  		  		  		close_form();
  		  		  	}
  		  		});
  		  	}
  		  	
  		  	// when save is clicked from lightbox
	  		function save_form() 
  		  	{
  		  		var ev = scheduler.getEvent(scheduler.getState().lightbox_id);
	  							
				// if event already exists, reset event fields
				if (ev)
				{
					ev.text =  $("#studyunit_code").val();
					ev.start_date = setDate("start_date", "starttime");
					ev.end_date = setDate("start_date", "endtime");	
				}
				
				if (!create_event)
				{					
		  			scheduler.endLightbox(true, html("custom_form"));
	  				submit_lightbox();
	  				getEventColors();
					updateStatusInfo();
				}	
	  			else
		  		{
	  				create_event = false;
		  			scheduler.endLightbox(true, html("custom_form"));
			  	}
  		  	}
  		  	
  		  	// this event is called after event id changes to auto generated id in database
  		  	// i.e. after timetable event is inserted
	  		scheduler.attachEvent("onEventIdChange", function(sid, tid) 
	  		{   
	  			$("#event_id").val(tid);
	  			submit_lightbox();
				
	  		    return true;
	  		});
  		  	  		  	
  		  	// close lightbox
	  		function close_form() 
  		  	{	
  		  		scheduler.endLightbox(false, html("custom_form"));
	  		}
	
  		  	// delete timetable event
	  		function delete_event() 
  		  	{
  		  		var event_id = scheduler.getState().lightbox_id;
	  			scheduler.deleteEvent(event_id);
	  		}
			
		</script>	
	</body>
</html>