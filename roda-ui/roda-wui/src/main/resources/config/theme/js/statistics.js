$(function () {
    // select the target node
    var target = document.body;

    // create an observer instance
    var observer = new MutationObserver(function (mutations) {
        mutations.forEach(function (mutation) {
            console.log(mutation.type);
            $(".chart").each(function () {
                initChart(this);
            });
        });
    });

    // configuration of the observer:
    var config = {subtree: true, childList: true};

    // pass in the target node, as well as the observer options
    observer.observe(target, config);

    // later, you can stop observing
    // observer.disconnect();

    var executeFunctionByName = function (context, functionName/*, args */) {
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
    };

    var initChart = function (element) {
        if (element.chart == null) {
            var functionName = $(element).data("function");
            var functionParams = $(element).data("function-params");
            var params = functionParams.split(",").map(function (param) {
                return param.trim();
            });
            var chartOptions = executeFunctionByName(window, functionName, params);
            element.chart = new Chart(element, chartOptions);
            console.log("element.chart = ", element.chart);
        } else {
            // element.chart != null
            console.log("element.chart already exists");
        }
    };

});

function singleFacetPieChart(returnClass, facet) {
    var values = [];
    $.ajax({
        async: false,
        url: "/api/v1/index?returnClass=" + returnClass + "&facet=" + facet + "&start=0&limit=0&onlyActive=false"
    }).done(function (data) {
        values = data.facetResults[0].values;
    });
    return {
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
    };
}

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
