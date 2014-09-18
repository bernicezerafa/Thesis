<%@ page language="java" 
		 contentType="text/html; charset=utf-8"
    	 pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
	<head>
		<title> Exam Timetable </title>
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
					Please check if you want to include Saturdays between these two dates.
				</p>
				
				<label for="include_sats">Include Saturdays</label>
				<input id="include_sats" name="include_sats" type="checkbox">				
				<br/>
				<div class="button_divs">
					<button id="next_btn" class="next_btns" type="button">Next</button>
				</div>
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
				<div class="button_divs">
					<button id="back_btn" class="next_btns" type="button">Back</button>
					<button id="next2_btn" class="next_btns" type="button">Done</button>
				</div>
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
				<div class="button_divs">
					<button id="back2_btn" class="next_btns" type="button">Back</button>
					<button id="done_btn" class="next_btns" type="button">Done</button>
				</div>
			</section>
			
		</form>
		
		<div class="modal">
			<p id="loading-msg"> 
				Generating Timetable. Please Wait...
			</p>
		</div>
		<div id="overlay_div"></div>
		
		<script src="//code.jquery.com/jquery-1.10.2.js"></script>
  		<script src="//code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
		<script>
		
			var includeSats = false;
			
			$(function()
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
				
			// section 1, go to weekday section
			$("#next_btn").click(function()
			{
				$("#section_1").css("display", "none");
				includeSats = $("#include_sats").is(":checked");
				$("#weekday_section").css("display", "block");
				
				if (!includeSats)
					$("#next2_btn").html("Done");
				else
					$("#next2_btn").html("Next");
			});
			
			// weekday section, if include sat go to saturday, else submit
			$("#next2_btn").click(function()
			{				
				if ($(this).html() == "Done")
				{
					$("#done_btn").trigger("click");
				}
				else
				{
					$("#weekday_section").css("display", "none");
					$("#saturday_section").css("display", "block");
				}
			});
			
			$("#back_btn").click(function()
			{
				$("#weekday_section").css("display", "none");
				$("#section_1").css("display", "block");	
			});
			
			$("#back2_btn").click(function()
			{
				$("#saturday_section").css("display", "none");
				$("#weekday_section").css("display", "block");
			});
			
			$body = $("body");
			
			$("#done_btn").click(function()
			{
				if (includeSats)
					$("#saturday_section").css("display", "none");
				else 
					$("#weekday_section").css("display", "none");
				
				$.ajax(
				{
					type: 'POST',
					url: 'process',
					data: $('#input-form').serialize(),
					beforeSend: function()
					{
						$body.addClass("loading"); 
				    	$("#overlay_div").css("display", "block");
					},
					success: function()
					{
						$body.removeClass("loading"); 
				    	$("#overlay_div").css("display", "none");
				    	
				    	window.location.href = "http://localhost:8080/thesis/timetable.jsp?startdate=" + $("#startdate").val()
					}
				});	
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