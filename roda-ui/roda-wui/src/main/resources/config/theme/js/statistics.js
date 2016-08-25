(function($) {

    $.fn.chart = function() {
        var matchedObject = this;

        function init() {
            if (!matchedObject || matchedObject.length == 0) {
                return;
            }

            matchedObject.each(function() {
                var element = $(this);
                if (element._chart == null) {
                    var source = element.data("source");
                    if (source == "facet") {
                        initFacetChart(element);
                    } else if (source == "function") {
                        var customFunction = element.data("function");
                        executeFunctionByName(window, customFunction, element);
                    } else {
                        console.log("Unknown data source '"+source+"'");
                    }
                }
            });
        }

        function initFacetChart(element) {
            var facetCallback;
            var customFacetFunction = element.data("facet-function");
            if (customFacetFunction) {
                facetCallback = functionByName(window, customFacetFunction);
            } else {
                var type = element.data("type");
                var facetDataSourceCallbacks = {
                    "line": facetLineChartOptions,
                    "bar": facetBarChartOptions,
                    "radar": facetRadarChartOptions,
                    "polarArea": facetPolarAreaChartOptions,
                    "pie": facetPieChartOptions,
                    "doughnut": facetDoughnutChartOptions
                };
                facetCallback = facetDataSourceCallbacks[type];
            }
            facetsChart(element, facetCallback);
        }

        function facetsChart(element, dataSourceCallback) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facets = $(element).data("facets").split(/\s*[ ,]\s*/);
                var facetParams = facets.map(function(facet) { return "facet=" + facet }).join("&");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&" + facetParams + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    element._chart = new Chart(element, dataSourceCallback(data.facetResults, element));
                });
            }
        }

        function facetLineChartOptions(facetResults, element){
            var options = facetCommonChartOptions("line", facetResults, element);
            var baseColor = rgbRandomColor();
            var paleColor = rgbaRandomColorAsString($.extend(baseColor, { alpha: 0.5 }));
            var opaqueColor = rgbaRandomColorAsString($.extend(baseColor, { alpha: 1 }));
            options.data.datasets.forEach(
                function(dataset){
                    $.extend(dataset, {
                        fill: false,
                        lineTension: 0.1,
                        backgroundColor: paleColor,
                        borderColor: opaqueColor,
                        borderCapStyle: 'butt',
                        borderDash: [],
                        borderDashOffset: 0.0,
                        borderJoinStyle: 'miter',
                        pointBackgroundColor: paleColor,
                        pointBorderColor: opaqueColor,
                        pointBorderWidth: 1,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: paleColor,
                        pointHoverBorderColor: opaqueColor,
                        pointHoverBorderWidth: 2,
                        pointRadius: 1,
                        pointHitRadius: 10,
                        spanGaps: false
                    });
                }
            );
            return options;
        }

        function facetBarChartOptions(facetResults, element){
            var options = facetCommonChartOptions("bar", facetResults, element);
            options.data.datasets.forEach(
                function(dataset){
                    $.extend(dataset, {
                        borderWidth: 1
                    });
                }
            );
            return options;
        }

        function facetRadarChartOptions(facetResults, element){
            var options = facetCommonChartOptions("radar", facetResults, element);
            var baseColor = rgbRandomColor();
            var paleColor = rgbaRandomColorAsString($.extend(baseColor, { alpha: 0.2 }));
            var opaqueColor = rgbaRandomColorAsString($.extend(baseColor, { alpha: 1 }));
            options.data.datasets.forEach(
                function(dataset){
                    $.extend(dataset, {
                        backgroundColor: paleColor,
                        borderColor: opaqueColor,
                        pointBackgroundColor: opaqueColor,
                        pointBorderColor: "#fff",
                        pointHoverBackgroundColor: "#fff",
                        pointHoverBorderColor: opaqueColor
                    });
                }
            );
            return options;
        }

        function facetPolarAreaChartOptions(facetResults, element){
            return facetCommonChartOptions("polarArea", facetResults, element);
        }

        function facetPieChartOptions(facetResults, element){
            return facetCommonChartOptions("pie", facetResults, element);
        }

        function facetDoughnutChartOptions(facetResults, element){
            return facetCommonChartOptions("doughnut", facetResults, element);
        }

        function facetCommonChartOptions(type, facetResults, element){
            var options = {};
            var facet = facetResults && facetResults.length > 0 ? facetResults[0] : null;
            if (facet) {
                options = {
                    type: type,
                    data: {
                        labels: facet.values.map(function (value) {
                            return value.label;
                        }),
                        datasets: [
                            {
                                label: facet.field,
                                data: facet.values.map(function (value) {
                                    return value.count;
                                }),
                                backgroundColor: facet.values.map(function () {
                                    return rgbaRandomOpaqueColorAsString();
                                })
                            }
                        ]
                    }
                };
            }
            return options;
        }

        function executeFunctionByName(context, functionName/*, args */) {
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

    $(window).ready(function() {
        // select the target node
        var target = document.body;

        // create an observer instance
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                // when the statistics page is added, init chart() plugin for existing .chart elements.
                var statistics = document.getElementById("statistics");
                if (statistics && Array.from(mutation.addedNodes).indexOf(statistics) >= 0) {
                    $(".chart").chart();
                }
            });
        });

        // configuration of the observer:
        var config = {subtree: true, childList: true};

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
    if (!mix) {
        mix = {red: 255, green: 255, blue: 255};
    }
    var red = Math.random() * 256;
    var green = Math.random() * 256;
    var blue = Math.random() * 256;

    // mix the color
    red = (red + mix.red) / 2;
    green = (green + mix.green) / 2;
    blue = (blue + mix.blue) / 2;

    return {
        red: Math.floor(red),
        green: Math.floor(green),
        blue: Math.floor(blue),
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
        mix = {red: 255, green: 255, blue: 255, alpha: 1};
    }
    var color = rgbRandomColor(mix);
    var alpha = Math.random();
    // mix the color
    alpha = (alpha + mix.alpha) / 2;
    return $.extend(color, {
        alpha: Math.floor(alpha)
    });
}

function rgbaRandomColorAsString(color) {
    if (!color) {
        color = rgbaRandomColor();
    }
    return "rgba(" + color.red + ", " + color.green + ", " + color.blue + ", " + color.alpha + ")";
}

function rgbaRandomOpaqueColorAsString() {
    var color = rgbRandomColor();
    color.alpha = 1;
    return rgbaRandomColorAsString(color);
}

function facetCustomDataHandlerChartOptions(facetResults, element){
    var options = {};
    var facet = facetResults && facetResults.length > 0 ? facetResults[0] : null;
    if (facet) {
        options = {
            type: "pie",
            data: {
                labels: facet.values.map(function (value) {
                    return value.label;
                }),
                datasets: [
                    {
                        label: facet.field,
                        data: facet.values.map(function (value) {
                            return value.count;
                        }),
                        backgroundColor: facet.values.map(function () {
                            return rgbaRandomOpaqueColorAsString();
                        })
                    }
                ]
            },
            options: {
                cutoutPercentage: 90
            }
        };
    }
    return options;
}

function customDataBubbleChart(element) {
    if (element._chart == null) {
        element._chart = new Chart(element, {
            type: 'bubble',
            data: {
                datasets: [
                    {
                        label: "Test dataset",
                        data: [-3,-2,-1,0,1,2,3].map(function (value) {
                            return {
                                x: value,
                                y: value * value,
                                r: Math.abs(value) * 10
                            };
                        }),
                        backgroundColor: rgbaRandomOpaqueColorAsString(),
                        hoverBackgroundColor: rgbaRandomOpaqueColorAsString()
                    }
                ]
            }
        });
    }
}
