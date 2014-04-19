<%@ page language="java" 
		 contentType="text/html; charset=utf-8"
    	 pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
	<head>
		<title>Exam Timetable</title>
		<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
		<link rel="stylesheet" href="css/main.css" />
	</head>
	<body>
	
		<form id="input-form" action="process">
	
			<section id="section_1">
				<p class="justify">
					Before generating your timetable, the system needs to have certain input data.
					Please complete all the steps carefully. 
				</p>
				<p class="justify">
					Choose the start and end date for the entire exam period
				</p>
				
				<input id="startdate" type="text" name="startdate" placeholder="Start Date" />
				<input id="enddate" type="text" name="enddate" placeholder="End Date" />
				
				<p class="justify">
					Please check if you want to include Saturdays and if you want to have fixed timeslot
					settings for all the weekdays (Monday - Friday)
				</p>
				
				<label for="include_sats">Include Saturdays</label>
				<input id="include_sats" name="include_sats" type="checkbox">
				
				<br/>
				<label for="same_weekdays">Fixed Timeslots for weekdays</label>
				<input id="same_weekdays" name="same_weekdays" type="checkbox">
				
				<br/>
				<button id="next_btn" class="next_btns" type="button">Next</button>
			</section>
				
			<section id="weekday_section" style="display:none">
				<p class="justify">
					Choose the number of timeslots for all weekdays in the exam period and
					set the start and end time accordingly
				</p>
				
				<%int weekdayTimeslots = 5;%>
				<label for="weekday_ts">Weekday Timeslots</label>
				<select id="weekday_ts" name="weekday_ts">
 					<%
 					for (int i=0; i < weekdayTimeslots; i++)
 					{%>
 			 			<option value="<%=(i+1)%>"><%=(i+1)%></option>
					<%
					}%>
				</select>
				
				<table id="weekday_table">
					<tbody>
						<%
						for (int i=0; i < weekdayTimeslots; i++)
						{%>
							<tr id="timeslot<%=(i+1)%>_tr" class="weekday_trs">
								<td>
									<span><b>Timeslot <%=(i+1)%></b></span>
								</td>
								<td>
									<span>Start Time</span>
									<input id="ts<%=(i+1)%>_starttime" type="time" name="ts_w<%=(i+1)%>_starttime" placeholder="Start Time"/>
								</td>
								<td>
									<span>End Time</span>
									<input id="ts<%=(i+1)%>_endtime" type="time" name="ts_w<%=(i+1)%>_endtime" placeholder="End Time" />
								</td>
							</tr>
						<%
						}%>	
					</tbody>
				</table>
				
				<br/>
				<button id="next2_btn" class="next_btns" type="button">Next</button>
			</section>
		
			<section id="saturday_section" style="display:none">
				<p class="justify">
					Choose the number of timeslots you want for saturday and
					set the start and end time accordingly
				</p>
				
				<%int saturdayTimeslots = 5;%>
				<label for="saturday_ts">Saturday Timeslots</label>
				<select id="saturday_ts" name="saturday_ts">
 					<%
 					for (int i=0; i < saturdayTimeslots; i++)
 					{%>
 			 			<option value="<%=(i+1)%>"><%=(i+1)%></option>
					<%
					}%>
				</select>
				
				<table id="saturday_table">
					<tbody>
						<%
						for (int i=0; i < saturdayTimeslots; i++)
						{%>
							<tr id="timeslot<%=(i+1)%>_tr" class="saturday_trs">
								<td>
									<span><b>Timeslot <%=(i+1)%></b></span>
								</td>
								<td>
									<span>Start Time</span>
									<input id="ts<%=(i+1)%>_starttime" type="time" name="ts_s<%=(i+1)%>_starttime" placeholder="Start Time"/>
								</td>
								<td>
									<span>End Time</span>
									<input id="ts<%=(i+1)%>_endtime" type="time" name="ts_s<%=(i+1)%>_endtime" placeholder="End Time" />							
								</td>
							</tr>
						<%
						}%>
					</tbody>
				</table>
				
				<br/>
				<button id="done_btn" class="next_btns" type="submit">Done</button>
			</section>
			
			<section id="different_weekdays" style="display:none">
				
				<label for="first_option">Some weekdays have the same settings, others are different</label>
				<input id="first_option" name="first_option" type="checkbox">
				
				<label for="second_option">All weekdays have different settings from each other</label>
				<input id="second_option" name="second_option" type="checkbox">
				
				<label for="third_option">Specific dates are different</label>
				<input id="third_option" name="third_option" type="checkbox">	
			</section>
			
			<section id="first_option_section" style="display:none">
				
				<p class="justify">
					Choose which Weekdays fall under which group.
				</p>
				
				<label for="same_mondays">Same Mondays</label>
				<input id="same_mondays" name="same_mondays" type="checkbox">
				
				<label for="same_tuesdays">Same Tuesdays</label>
				<input id="same_tuesdays" name="same_tuesdays" type="checkbox">
				
				<label for="same_wednesdays">Same Wednesdays</label>
				<input id="same_wednesdays" name="same_wednesdays" type="checkbox">
				
				<label for="same_thursdays">Same Thursdays</label>
				<input id="same_thursdays" name="same_thursdays" type="checkbox">
				
				<label for="same_fridays">Same Fridays</label>
				<input id="same_fridays" name="same_fridays" type="checkbox">
			</section>
		</form>
		
		<script src="js/jquery-1.9.1.js"></script>
  		<script src="js/jquery-ui-1.10.3.js"></script>	
		<script>
		
			var includeSats = false;
			var sameWeekdays = false;
		
			$(document).ready(function()
			{
				$("#startdate, #enddate").datepicker(
				{
					changeYear : 'true',
					changeMonth: 'true',
					dateFormat: 'dd/mm/yy'
				});
		 	});
			
			function showSetTime(noOfTimeslots, tr_class)
			{
				$("." + tr_class).each(function(index, element) {
					if (index < noOfTimeslots)
					{
						$(element).css("display", "block");
					}
					else
					{
						$(element).css("display", "none");
					}
				});
			}
			
			$("#next2_btn").click(function()
			{
				$("#weekday_section").css("display", "none");
				
				if (includeSats)
					$("#saturday_section").css("display", "block");
				else
					$("#input-form").submit();
				
				// else done.. submit form
			});
			
			$("#next_btn").click(function()
			{
				$("#section_1").css("display", "none");
				
				includeSats = $("#include_sats").is(":checked");
				sameWeekdays = $("#same_weekdays").is(":checked");
				
				if (sameWeekdays)
				{
					$("#weekday_section").css("display", "block");
					
					if (!includeSats)
						$("#next2_btn").html("Done");
				}
				else
					$("#saturday_section").css("display", "block");
				
				// else if not same weekdays display section with different Monday to Friday
			});
			
			$("#saturday_ts").change(function()
			{
				showSetTime($(this).val(), "saturday_trs");
			});
			
			$("#weekday_ts").change(function()
			{
				showSetTime($(this).val(), "weekday_trs");
			});
			
			
		</script>
	</body>
</html>