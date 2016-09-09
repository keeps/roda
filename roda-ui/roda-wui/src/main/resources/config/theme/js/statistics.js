(function($) {

	$.fn.statistic = function() {
		var matchedObject = this;

		function init() {
			if (!matchedObject || matchedObject.length == 0) {
				return;
			}

			matchedObject.each(function() {
				var element = $(this);

				var view = element.data("view");
				var viewCallback;
				if (view == "chart") {
					var viewField = element.data("view-field");
					if (viewField == "facetResults") {
						viewCallback = initViewFacetChart;
					} else {
						console.log("Unknown view-field '" + viewField + "'");
					}
				} else if (view == "text") {
					viewCallback = initViewText;
				} else {
					viewCallback = function() {
					};
					console.log("Unknown view '" + view + "'");
				}

				var source = element.data("source");
				if (source == "index") {
					fetchIndexData(element, viewCallback);
				} else if (source == "function") {
					var customFunction = element.data("function");
					executeFunctionByName(window, customFunction, element,
							viewCallback);
				} else {
					console.log("Unknown data source '" + source + "'");
				}
			});
		}

		function fetchIndexData(element, viewCallback) {
			var returnClass = $(element).data("source-class");

			var filters = $(element).data("source-filters") ? $(element).data(
					"source-filters").split(/\s*[ ,]\s*/) : [];
			var filterParams = filters.map(function(filter) {
				return "filter=" + filter
			}).join("&");

			var facets = $(element).data("source-facets") ? $(element).data(
					"source-facets").split(/\s*[ ,]\s*/) : [];
			var facetParams = facets.map(function(facet) {
				return "facet=" + facet
			}).join("&");

			var start = $(element).data("source-start") || 0;
			var limit = $(element).data("source-limit") || 0;
			var onlyActive = $(element).data("source-onlyActive") || "false";

			$.ajax(
					{
						url : "/api/v1/index?returnClass=" + returnClass + "&"
								+ filterParams + "&" + facetParams + "&start="
								+ start + "&limit=" + limit + "&onlyActive="
								+ onlyActive
					}).done(function(data) {
				viewCallback(element, data);
			});
		}

		function initViewText(element, data) {
			element.text(data[element.data("view-field")]);
			element.parent().textfill({
				widthOnly : true
			});
		}

		function initViewFacetChart(element, data) {
			var chartOptionsCallback;
			var type = element.data("view-type");
			if (type == "function") {
				var customFacetFunction = element.data("view-type-function");
				chartOptionsCallback = functionByName(window,
						customFacetFunction);
			} else {
				var facetDataSourceCallbacks = {
					"line" : facetLineChartOptions,
					"bar" : facetBarChartOptions,
					"radar" : facetRadarChartOptions,
					"polarArea" : facetPolarAreaChartOptions,
					"pie" : facetPieChartOptions,
					"doughnut" : facetDoughnutChartOptions
				};
				if (type in facetDataSourceCallbacks) {
					chartOptionsCallback = facetDataSourceCallbacks[type];
				} else {
					chartOptionsCallback = function() {
					};
					console.log("Unknown view-type '" + type + "'");
				}
			}
			new Chart(element, chartOptionsCallback(data, element));
		}

		function facetLineChartOptions(data, element) {
			var options = facetCommonChartOptions("line", data, element);
			var baseColor = rgbRandomColor();
			var paleColor = rgbaRandomColorAsString($.extend(baseColor, {
				alpha : 0.5
			}));
			var opaqueColor = rgbaRandomColorAsString($.extend(baseColor, {
				alpha : 1
			}));
			options.data.datasets.forEach(function(dataset) {
				$.extend(dataset, {
					fill : false,
					lineTension : 0.1,
					backgroundColor : paleColor,
					borderColor : opaqueColor,
					borderCapStyle : 'butt',
					borderDash : [],
					borderDashOffset : 0.0,
					borderJoinStyle : 'miter',
					pointBackgroundColor : paleColor,
					pointBorderColor : opaqueColor,
					pointBorderWidth : 1,
					pointHoverRadius : 5,
					pointHoverBackgroundColor : paleColor,
					pointHoverBorderColor : opaqueColor,
					pointHoverBorderWidth : 2,
					pointRadius : 1,
					pointHitRadius : 10,
					spanGaps : false
				});
			});
			return options;
		}

		function facetBarChartOptions(data, element) {
			var options = facetCommonChartOptions("bar", data, element);
			options.data.datasets.forEach(function(dataset) {
				$.extend(dataset, {
					borderWidth : 1
				});
			});
			return options;
		}

		function facetRadarChartOptions(data, element) {
			var options = facetCommonChartOptions("radar", data, element);
			var baseColor = rgbRandomColor();
			var paleColor = rgbaRandomColorAsString($.extend(baseColor, {
				alpha : 0.2
			}));
			var opaqueColor = rgbaRandomColorAsString($.extend(baseColor, {
				alpha : 1
			}));
			options.data.datasets.forEach(function(dataset) {
				$.extend(dataset, {
					backgroundColor : paleColor,
					borderColor : opaqueColor,
					pointBackgroundColor : opaqueColor,
					pointBorderColor : "#fff",
					pointHoverBackgroundColor : "#fff",
					pointHoverBorderColor : opaqueColor
				});
			});
			return options;
		}

		function facetPolarAreaChartOptions(data, element) {
			return facetCommonChartOptions("polarArea", data, element);
		}

		function facetPieChartOptions(data, element) {
			return facetCommonChartOptions("pie", data, element);
		}

		function facetDoughnutChartOptions(data, element) {
			return facetCommonChartOptions("doughnut", data, element);
		}

		function facetCommonChartOptions(type, data, element) {
			var options = {};
			var facet = data && data.facetResults
					&& data.facetResults.length > 0 ? data.facetResults[0]
					: null;
			if (facet) {
				options = {
					type : type,
					data : {
						labels : facet.values.map(function(value) {
							return value.label;
						}),
						datasets : [ {
							label : facet.field,
							data : facet.values.map(function(value) {
								return value.count;
							}),
							backgroundColor : facet.values.map(function() {
								return rgbaRandomOpaqueColorAsString();
							})
						} ]

					},
					legend : {
						display : true,
						position : "bottom"
					}
				};

				if (type == "bar") {
					options.legend.display = false;
				}
			}

			return options;
		}

		function executeFunctionByName(context, functionName/* , args */) {
			var args = Array.prototype.slice.call(arguments, 2);
			if (args.length == 1 && Array.isArray(args[0])) {
				args = args[0];
			}
			functionByName(context, functionName).apply(context, args);
		}

		function functionByName(context, functionName) {
			var namespaces = functionName.split(".");
			var func = namespaces.pop();
			for (var i = 0; i < namespaces.length; i++) {
				context = context[namespaces[i]];
			}
			return context[func];
		}

		init();

		// returns the current context to the caller function/method
		// so that proper chaining may be applied to the context
		return this;
	};

	$(window).ready(
			function() {

				function recreateScriptTags(element) {
					var scriptNodes = element.getElementsByTagName('script');
					for (var i = 0; i < scriptNodes.length; i++) {
						var scriptNode = scriptNodes[i];
						var parent = scriptNode.parentElement;
						var newScriptNode = document.createElement('script');
						newScriptNode.async = scriptNode.async;
						newScriptNode.type = scriptNode.type;
						if (scriptNode.src) {
							newScriptNode.src = scriptNode.src;
						}
						newScriptNode.innerHTML = scriptNode.innerHTML;
						parent.insertBefore(newScriptNode, scriptNode);
						parent.removeChild(scriptNode);
					}
				}

				// select the target node
				var target = document.body;

				// create an observer instance
				var observer = new MutationObserver(function(mutations) {
					mutations.forEach(function(mutation) {
						// when the statistics page is added...
						var statistics = document.getElementById("statistics");
						if (statistics
								&& Array.from(mutation.addedNodes).indexOf(
										statistics) >= 0) {
							// Recreate the <script> elements to make them
							// "active"
							recreateScriptTags(statistics);
							// init statistic() plugin for existing .statistic
							// elements.
							$(".statistic").statistic();
						}
					});
				});

				// configuration of the observer:
				var config = {
					subtree : true,
					childList : true
				};

				// pass in the target node, as well as the observer options
				observer.observe(target, config);

				// later, you can stop observing
				// observer.disconnect();
			});

}(jQuery));

/**
 * Randomly generate an aesthetically-pleasing color palette.
 * 
 * @see http://stackoverflow.com/a/43235/2602440
 * @param mix
 * @returns {{red: number, green: number, blue: number}}
 */
function rgbRandomColor(mix) {
	// for pastel colors, mix with white:
	// mix = {red: 255, green: 255, blue: 255};

	var red = Math.random() * 256;
	var green = Math.random() * 256;
	var blue = Math.random() * 256;

	if (mix) {
		// mix the color
		red = (red + mix.red) / 2;
		green = (green + mix.green) / 2;
		blue = (blue + mix.blue) / 2;
	}

	return {
		red : Math.floor(red),
		green : Math.floor(green),
		blue : Math.floor(blue)
	};
}

/**
 * Randomly generate an aesthetically-pleasing color palette.
 * 
 * @see http://stackoverflow.com/a/43235/2602440
 * @param mix
 * @returns {{red: number, green: number, blue: number, alpha: number}}
 */
function rgbaRandomColor(mix) {
	if (!mix) {
		mix = {
			red : 255,
			green : 255,
			blue : 255,
			alpha : 1
		};
	}
	var color = rgbRandomColor(mix);
	var alpha = Math.random();
	// mix the color
	alpha = (alpha + mix.alpha) / 2;
	return $.extend(color, {
		alpha : Math.floor(alpha)
	});
}

function rgbaRandomColorAsString(color) {
	if (!color) {
		color = rgbaRandomColor();
	}
	return "rgba(" + color.red + ", " + color.green + ", " + color.blue + ", "
			+ color.alpha + ")";
}

function rgbaRandomOpaqueColorAsString() {
	var color = rgbRandomColor();
	color.alpha = 1;
	return rgbaRandomColorAsString(color);
}
