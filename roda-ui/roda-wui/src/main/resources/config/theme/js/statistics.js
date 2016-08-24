(function($) {

    $.fn.chart = function() {
        var matchedObject = this;
        var sourceTypeFunction = {
            "facet": {
                "line": singleFacetLineChart,
                "bar": singleFacetBarChart,
                "radar": singleFacetRadarChart,
                "polarArea": singleFacetPolarAreaChart,
                "pie": singleFacetPieChart,
                "doughnut": singleFacetDoughnutChart
            }
        };

        function init() {
            if (!matchedObject || matchedObject.length == 0) {
                return;
            }

            // retrieves the reference to the various elements for which the
            // proper extension/plugin is going to be applied
            // var accordion = jQuery(".accordion", matchedObject);

            // runs the various component oriented extension/plugins, this
            // should changed the overall page behaviour
            // matchedObject.bsteps();
            // accordion.baccordion();

            matchedObject.each(function() {
                var element = $(this);
                if (element._chart == null) {
                    var customFunction = element.data("function");
                    if (customFunction) {
                        executeFunctionByName(window, customFunction, element);
                    } else {
                        var source = element.data("source");
                        var type = element.data("type");
                        sourceTypeFunction[source][type](element);
                    }
                }
            });
        }

        function singleFacetLineChart(element) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facet = $(element).data("facet");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    var values = data.facetResults[0].values;
                    element._chart = new Chart(element, {
                        type: 'line',
                        data: {
                            labels: values.map(function (value) { return value.label; }),
                            datasets: [
                                {
                                    label: facet,
                                    data: values.map(function (value) { return value.count; }),
                                    fill: false,
                                    lineTension: 0.1,
                                    backgroundColor: "rgba(75,192,192,0.4)",
                                    borderColor: "rgba(75,192,192,1)",
                                    borderCapStyle: 'butt',
                                    borderDash: [],
                                    borderDashOffset: 0.0,
                                    borderJoinStyle: 'miter',
                                    pointBorderColor: "rgba(75,192,192,1)",
                                    pointBackgroundColor: "#fff",
                                    pointBorderWidth: 1,
                                    pointHoverRadius: 5,
                                    pointHoverBackgroundColor: "rgba(75,192,192,1)",
                                    pointHoverBorderColor: "rgba(220,220,220,1)",
                                    pointHoverBorderWidth: 2,
                                    pointRadius: 1,
                                    pointHitRadius: 10,
                                    spanGaps: false
                                }
                            ]
                        }
                    });
                });
            } else {
                // element._chart != null
            }
        }

        function singleFacetBarChart(element) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facet = $(element).data("facet");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    var values = data.facetResults[0].values;
                    element._chart = new Chart(element, {
                        type: 'bar',
                        data: {
                            labels: values.map(function (value) {
                                return value.label;
                            }),
                            datasets: [{
                                label: facet,
                                data: values.map(function (value) {
                                    return value.count;
                                }),
                                backgroundColor: values.map(function () {
                                    return rgbaRandomColor();
                                }),
                                borderWidth: 1
                            }]
                        }
                    });
                });
            } else {
                // element._chart != null
            }
        }

        function singleFacetRadarChart(element) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facet = $(element).data("facet");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    var values = data.facetResults[0].values;
                    element._chart = new Chart(element, {
                        type: 'radar',
                        data: {
                            labels: values.map(function (value) {
                                return value.label;
                            }),
                            datasets: [
                                {
                                    label: facet,
                                    data: values.map(function (value) {
                                        return value.count;
                                    }),
                                    backgroundColor: "rgba(255,99,132,0.2)",
                                    borderColor: "rgba(255,99,132,1)",
                                    pointBackgroundColor: "rgba(255,99,132,1)",
                                    pointBorderColor: "#fff",
                                    pointHoverBackgroundColor: "#fff",
                                    pointHoverBorderColor: "rgba(255,99,132,1)"
                                }
                            ]
                        }
                    });
                });
            } else {
                // element._chart != null
            }
        }

        function singleFacetPolarAreaChart(element) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facet = $(element).data("facet");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    var values = data.facetResults[0].values;
                    element._chart = new Chart(element, {
                        type: 'polarArea',
                        data: {
                            labels: values.map(function (value) {
                                return value.label;
                            }),
                            datasets: [
                                {
                                    label: facet,
                                    data: values.map(function (value) {
                                        return value.count;
                                    }),
                                    backgroundColor: values.map(function () {
                                        return rgbaRandomColor();
                                    })
                                }
                            ]
                        }
                    });
                });
            } else {
                // element._chart != null
            }
        }

        function singleFacetPieChart(element) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facet = $(element).data("facet");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    var values = data.facetResults[0].values;
                    element._chart = new Chart(element, {
                        type: 'pie',
                        data: {
                            labels: values.map(function (value) {
                                return value.label;
                            }),
                            datasets: [
                                {
                                    label: facet,
                                    data: values.map(function (value) {
                                        return value.count;
                                    }),
                                    backgroundColor: values.map(function () {
                                        return rgbaRandomColor();
                                    })
                                }
                            ]
                        }
                    });
                });
            } else {
                // element._chart != null
            }
        }

        function singleFacetDoughnutChart(element) {
            if (element._chart == null) {
                var returnClass = $(element).data("class");
                var facet = $(element).data("facet");
                $.ajax({
                    url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
                }).done(function (data) {
                    var values = data.facetResults[0].values;
                    element._chart = new Chart(element, {
                        type: 'doughnut',
                        data: {
                            labels: values.map(function (value) {
                                return value.label;
                            }),
                            datasets: [
                                {
                                    label: facet,
                                    data: values.map(function (value) {
                                        return value.count;
                                    }),
                                    backgroundColor: values.map(function () {
                                        return rgbaRandomColor();
                                    })
                                }
                            ]
                        }
                    });
                });
            } else {
                // element._chart != null
            }
        }

        function executeFunctionByName(context, functionName/*, args */) {
            var args = Array.prototype.slice.call(arguments, 2);
            if (args.length == 1 && Array.isArray(args[0])) {
                args = args[0];
            }
            var namespaces = functionName.split(".");
            var func = namespaces.pop();
            for (var i = 0; i < namespaces.length; i++) {
                context = context[namespaces[i]];
            }
            return context[func].apply(context, args);
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
function randomColor(mix) {
    if (mix == null) {
        mix = {red: 255, green: 255, blue: 255};
    }
    var red = Math.random() * 256;
    var green = Math.random() * 256;
    var blue = Math.random() * 256;

    // mix the color
    if (mix != null) {
        red = (red + mix.red) / 2;
        green = (green + mix.green) / 2;
        blue = (blue + mix.blue) / 2;
    }

    return {red: Math.floor(red), green: Math.floor(green), blue: Math.floor(blue)};
}

function rgbaRandomColor() {
    var color = randomColor();
    return "rgba(" + color.red + ", " + color.green + ", " + color.blue + ", 1)";
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
                        backgroundColor: rgbaRandomColor(),
                        hoverBackgroundColor: rgbaRandomColor()
                    }
                ]
            }
        });
    } else {
        // element._chart != null
    }
}
