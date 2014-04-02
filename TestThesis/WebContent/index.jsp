<%@ page language="java" 
		 contentType="text/html; charset=utf-8"
    	 pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
	<head>
		<title>Exam Timetable</title>
		<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
	</head>
	<body>
		<form id="input-form" action="process">
			<input id="semester" class="textfields" type="text" name="semester" data-validation="required" placeholder="Semester e.g. 1 or 2" />
			
			<input id="startdate" type="text" name="startdate" placeholder="Start Date" data-validation="date" data-validation-format="dd/mm/yyyy" data-validation-error-msg="The correct date format is dd/mm/yyyy!"/>
			<input id="enddate" type="text" name="enddate" placeholder="End Date" data-validation="date" data-validation-format="dd/mm/yyyy" data-validation-error-msg="The correct date format is dd/mm/yyyy!"/>
			
			<input id="noOfWkDays" class="textfields" type="hidden" name="noOfWkDays" />
			<input id="noOfSats" class="textfields" type="hidden" name="noOfSats" />
			<input id="dayOfWeek" class="textfields" type="hidden" name="dayOfWeek" />
			<input id="daysAvailable" class="textfields" type="hidden" name="daysAvailable" />
				
			<button id="cont-btn" type="button"> Continue </button>
		</form>
		
		<script src="js/jquery-1.9.1.js"></script>
  		<script src="js/jquery-ui-1.10.3.js"></script>	
		<script>
		
			$(document).ready(function()
			{
				var startdate = new Date(); // start of exam period date
				var enddate = new Date(); // end of exam period date
				
				$("#startdate").datepicker(
				{
					changeYear : 'true',
					changeMonth: 'true',
					dateFormat: 'dd/mm/yy',
							
					onSelect: function(dateText) {   	        
						startdate = $(this).datepicker('getDate');
						
				        var seldate = startdate.toDateString();
				        seldate = seldate.split(' ');
				        var weekday= new Array();
				            weekday['Mon']="0";
				            weekday['Tue']="1";
				            weekday['Wed']="2";
				            weekday['Thu']="3";
				            weekday['Fri']="4";
				            weekday['Sat']="5";
				            weekday['Sun']="6";
				        
				        var dayOfWeek = weekday[seldate[0]];
				        $('#dayOfWeek').val(dayOfWeek);
					}
				});
				
				$("#enddate").datepicker(
				{
					changeYear : 'true',
					changeMonth: 'true',
					dateFormat: 'dd/mm/yy',
							
					onSelect: function(dateText) {				        						        
						enddate = $(this).datepicker('getDate');
					}
				});
				
				$("#cont-btn").click(function()
				{
					var daysInBetween = Math.ceil((enddate - startdate) / (1000 * 60 * 60 * 24)) + 1;
					var nsaturdays = Math.floor((daysInBetween + (startdate.getDay() + 5) % 7) / 7);
					var nsundays = Math.floor((daysInBetween + (startdate.getDay() + 6) % 7) / 7);
					
					var daysAvailable = daysInBetween - nsundays;
					var noOfWkDays = daysAvailable - nsaturdays;
					
					$("#noOfWkDays").val(noOfWkDays);
					$("#noOfSats").val(nsaturdays);
					
					$("#input-form").submit();
				});
									
				/*var myLanguage = 
				{
					errorTitle : 'Correct following errors to continue!',
					requiredFields : 'All fields are required!'
				};
				
				$("#cont-btn").click(function()
				{
					$.validate(
					{
						form : '#input-form',
						validateOnBlur : false, // disable validation when input looses focus
						errorMessagePosition : 'top', // Instead of 'element' which is default
						scrollToTopOnError : true, 
					 	language: myLanguage,
						onSuccess: function()
						{
							$("#input-form").submit();
						},
						onError: function()
						{
							return false;
						}
					});
				}); */
		 	});	
		</script>
	</body>
</html>