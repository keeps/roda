$(function () {
    // select the target node
    var target = document.body;

    // create an observer instance
    var observer = new MutationObserver(function (mutations) {
        mutations.forEach(function (mutation) {
            $(".chart").each(function () {
                executeFunctionByName(window, $(this).data("function"), this);
            });
        });
    });

    // configuration of the observer:
    var config = {subtree: true, childList: true};

    // pass in the target node, as well as the observer options
    observer.observe(target, config);

    // later, you can stop observing
    // observer.disconnect();
});

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

function singleFacetPieChart(element) {
    if (element.chart == null) {
        var returnClass = $(element).data("class");
        var facet = $(element).data("facet");
        $.ajax({
            url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
        }).done(function (data) {
            var values = data.facetResults[0].values;
            element.chart = new Chart(element, {
                type: 'pie',
                data: {
                    labels: values.map(function (value) {
                        return value.label;
                    }),
                    datasets: [{
                        data: values.map(function (value) {
                            return value.count;
                        }),
                        backgroundColor: values.map(function () {
                            return rgbaRandomColor();
                        })
                    }]
                }
            });
        });
    } else {
        // element.chart != null
    }
}

function singleFacetLineChart(element) {
    if (element.chart == null) {
        var returnClass = $(element).data("class");
        var facet = $(element).data("facet");
        $.ajax({
            url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
        }).done(function (data) {
            var values = data.facetResults[0].values;
            element.chart = new Chart(element, {
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
        // element.chart != null
    }
}

function singleFacetBarChart(element) {
    if (element.chart == null) {
        var returnClass = $(element).data("class");
        var facet = $(element).data("facet");
        $.ajax({
            url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
        }).done(function (data) {
            var values = data.facetResults[0].values;
            element.chart = new Chart(element, {
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
        // element.chart != null
    }
}

/*
function multipleFacetsRadarChart(element) {
    if (element.chart == null) {
        var returnClass = $(element).data("class");
        var facets = $(element).data("facets").split(",");
        var facetParams = facets.map(function(facet) { return "facet=" + facet }).join("&");
        $.ajax({
            url: "/api/v1/index?returnClass=" + returnClass + "&" + facetParams + "&start=0&limit=0&onlyActive=false"
        }).done(function (data) {
            var values = data.facetResults[0].values;
            var datasets = data.facetResults.map(
                function(facetResult) {
                    return {

                    };
                }
            );
            element.chart = new Chart(element, {
                type: 'radar',
                data: {
                    labels: values.map(function (value) { return value.label; }),
                    datasets: datasets
                }
            });
        });
    } else {
        // element.chart != null
    }
}
*/

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
