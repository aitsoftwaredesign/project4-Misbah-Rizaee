<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript">
window.onload = function() {
 
var dataPoints = [];
 
var chart = new CanvasJS.Chart("chartContainer", {
	animationEnabled: true,
	zoomEnabled: true,
	title: {
		text: "eBay Inc. Stock Price - 2017"
	},
	axisX: {
		crosshair: {
			enabled: true,
      snapToDataPoint: true,
			valueFormatString: "DD MMM"
		}
	},
	axisY: {
		title: "Closing Price (in USD)",
		crosshair: {
			enabled: true,
			snapToDataPoint: true,
			valueFormatString: "$#,##0.00"
		}
	},
	data: [{
		type: "line",
		xValueFormatString: "DD MMM",
		yValueFormatString: "$#,##0.00",
		xValueType: "dateTime",
		dataPoints: dataPoints
	}]
});
 
function addData(data) {
	for (var i = 0; i < data.length; i++) {
		dataPoints.push({
			x: new Date(data[i].x),
			y: data[i].y
		});
	}
	chart.render();
}
 
$.getJSON("/CanvasJS_Spring_MVC_Charts/restfull-service/eBay-inc-stock-price.json", addData);
 
}
</script>
</head>
<body>
	<div id="chartContainer" style="height: 370px; width: 100%;"></div>
	<script src="https://canvasjs.com/assets/script/jquery-1.11.1.min.js"></script>
	<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
</body>
</html>                