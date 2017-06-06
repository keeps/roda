(function($) {

  Chart.defaults.global.defaultFontFamily = "Ubuntu";
  Chart.defaults.global.defaultFontColor = "#222";
  Chart.defaults.global.elements.arc.borderColor = "#fff";
  Chart.defaults.global.elements.arc.borderWidth = "1";

  $.fn.statistic = function() {
    var matchedObject = this;

    function init() {
      if (!matchedObject || matchedObject.length == 0) {
        return;
      }

      matchedObject.each(function() {
        var element = $(this);

        inheritDataAttributes(element);

        var view = element.data("view");
        var initView = function() {};
        if (view == "chart") {
          var viewField = element.data("view-field");
          if (viewField == "facetResults") {
            initView = initViewFacetChart;
          } else {
            console.log("Unknown view-field '" + viewField + "'");
          }
        } else if (view == "text") {
          initView = initViewText;
        } else if (view == "download") {
          initView = initViewDownload;
        } else {
          console.log("Unknown view '" + view + "'");
        }

        var source = element.data("source");
        var dataFunction = function() {};
        if (source == "index") {
          dataFunction = fetchIndexData;
        } else if (source == "function") {
          var customFunction = element.data("function");
          executeFunctionByName(window, customFunction, element);
        } else {
          console.log("Unknown data source '" + source + "'");
        }

        initView(element, dataFunction);

      });
    }

    function inheritDataAttributes(element) {
      var attributes = [
        "source", "source-class", "source-filters", "source-facets", "source-start", "source-limit", "source-only-active",
        "view", "view-field", "view-limit", "view-type", "view-filename", "view-type-function",
        "function"
      ];
      var ref = element.data("ref");
      if (ref) {
        var refElement = document.getElementById(ref);
        if (refElement) {
          refElement = $(refElement);
          attributes.forEach(function(attribute) {
            element.data(attribute, element.data(attribute) || refElement.data(attribute));
          });
        }
      }
    }

    function buildDataUrl(element) {
    	buildDataUrl(element, false);
    }

    function buildDataUrl(element, noLimits) {
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


      var onlyActive = $(element).data("source-only-active") || "false";
      var lang = document.locale;

      var pathname = window.location.pathname;
      var url;
      if(noLimits) {
    	  url = pathname + "api/v1/index?returnClass=" +
          returnClass + "&" +
          filterParams + "&" +
          facetParams +
          "&lang=" + lang +
          "&onlyActive=" + onlyActive;
      } else {
	      var start = $(element).data("source-start") || 0;
	      var limit = $(element).data("source-limit") || 0;
	      var facetLimit = $(element).data("view-limit") || 100;

	      url = pathname + "api/v1/index?returnClass=" +
	          returnClass + "&" +
	          filterParams + "&" +
	          facetParams +
	          "&start=" + start +
	          "&limit=" + limit +
	          "&facetLimit=" + facetLimit +
	          "&lang=" + lang +
	          "&onlyActive=" + onlyActive;
      }
      return url;
    }

    function fetchIndexData(element, viewCallback) {
      $.ajax({
        url: buildDataUrl(element)
      }).done(function(data) {
        viewCallback(element, data);
      });
    }

    function initViewText(element, dataFunction) {
      dataFunction(element, viewTextCallback);
    }

    function viewTextCallback(element, data) {
      element.text(data[element.data("view-field")]);
    }

    function initViewDownload(element, dataFunction) {
      var noLimits = true;
      var url = buildDataUrl(element, noLimits);
      if (element.data("view-field") == "facetResults") {
        url = url + "&exportFacets=true";
        var filename = element.data("view-filename");
        if (filename) {
          url = url + "&filename=" + filename;
        }
      }

      element.click(function() {
        var type = 'text/csv';
        $.ajax({
          accepts: {
            text: type
          },
          url: url,
          processData: false,
          dataType: 'text',
          success: function(data) {
            saveAs(new Blob([data], {
              type: type
            }), filename);
          }
        });
      });
    }

    function initViewFacetChart(element, dataFunction) {
      dataFunction(element, viewFacetChartCallback);
    }

    function viewFacetChartCallback(element, data) {
      var chartOptionsCallback;
      var type = element.data("view-type");
      if (type == "function") {
        var customFacetFunction = element.data("view-type-function");
        chartOptionsCallback = functionByName(window,
          customFacetFunction);
      } else {
        var facetDataSourceCallbacks = {
          "line": facetLineChartOptions,
          "bar": facetBarChartOptions,
          "radar": facetRadarChartOptions,
          "polarArea": facetPolarAreaChartOptions,
          "pie": facetPieChartOptions,
          "doughnut": facetDoughnutChartOptions,
          "horizontalBar": facetHorizontalBarChartOptions,
        };
        if (type in facetDataSourceCallbacks) {
          chartOptionsCallback = facetDataSourceCallbacks[type];
        } else {
          chartOptionsCallback = function() {};
          console.log("Unknown view-type '" + type + "'");
        }
      }
      new Chart(element, chartOptionsCallback(data, element));
    }

    function facetLineChartOptions(data, element) {
      var options = facetCommonChartOptions("line", data, element);
      var baseColor = rgbRandomColor();
      var paleColor = rgbaRandomColorAsString($.extend(baseColor, {
        alpha: 0.5
      }));
      var opaqueColor = rgbaRandomColorAsString($.extend(baseColor, {
        alpha: 1
      }));
      options.data.datasets.forEach(function(dataset) {
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
      });
      return options;
    }

    function facetBarChartOptions(data, element) {
      var options = facetCommonChartOptions("bar", data, element);
      options.data.datasets.forEach(function(dataset) {
        $.extend(dataset, {
          borderWidth: 1
        });
      });
      return options;
    }

    function facetHorizontalBarChartOptions(data, element) {
      var options = facetCommonChartOptions("horizontalBar", data, element);
      options.data.datasets.forEach(function(dataset) {
        $.extend(dataset, {
          borderWidth: 1
        });
      });
      return options;
    }

    function facetRadarChartOptions(data, element) {
      var options = facetCommonChartOptions("radar", data, element);
      var baseColor = rgbRandomColor();
      var paleColor = rgbaRandomColorAsString($.extend(baseColor, {
        alpha: 0.2
      }));
      var opaqueColor = rgbaRandomColorAsString($.extend(baseColor, {
        alpha: 1
      }));
      options.data.datasets.forEach(function(dataset) {
        $.extend(dataset, {
          backgroundColor: paleColor,
          borderColor: opaqueColor,
          pointBackgroundColor: opaqueColor,
          pointBorderColor: "#fff",
          pointHoverBackgroundColor: "#fff",
          pointHoverBorderColor: opaqueColor
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
      var labelFunction = $(element).data("label-function");
      var facet = data && data.facetResults && data.facetResults.length > 0 ? data.facetResults[0] : null;
      if (facet) {
        options = {
          type: type,
          data: {
            labels: facet.values.map(function(value) {
              if (labelFunction) {
                return executeFunctionByName(window, labelFunction, value.label);
              } else {
                return value.label;
              }
            }),
            datasets: [{
              label: facet.field,
              data: facet.values.map(function(value) {
                return value.count;
              }),
              backgroundColor: facet.values.map(function() {
                return rgbaRandomOpaqueColorAsString();
              })
            }]

          },
          options: {
            legend: {
              display: true,
              position: 'bottom',
              boxWidth: 10
            }
          }
        };

        if (type == "bar") {
          options.options.legend.display = false;
          options.options.scales = {
            yAxes: [{
              id: 'y-axis-0',
              ticks: {
                beginAtZero: true
              }
            }]
          };
        } else if (type == "horizontalBar") {
          options.options.legend.display = false;
          options.options.scales = {
            xAxes: [{
              id: 'x-axis-0',
              ticks: {
                beginAtZero: true
              }
            }]
          };
        }

      }

      if (facet.values.length == 0) {
        $(element).before('<i class="fa fa-pie-chart fa-4x" style="color: #ddd"></i>');
        $(element).hide();
      }

      return options;
    }

    function executeFunctionByName(context, functionName /* , args */ ) {
      var args = Array.prototype.slice.call(arguments, 2);
      if (args.length == 1 && Array.isArray(args[0])) {
        args = args[0];
      }
      return functionByName(context, functionName).apply(context, args);
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

	  $(document).on('DOMNodeInserted', ".chartjs-hidden-iframe", function(e) {
		elem = e.target;
		$(elem).attr("title", "statistic_frame");
      });

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
          if (statistics && Array.from(mutation.addedNodes).indexOf(
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
        subtree: true,
        childList: true
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
    red: Math.floor(red),
    green: Math.floor(green),
    blue: Math.floor(blue)
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
      red: 255,
      green: 255,
      blue: 255,
      alpha: 1
    };
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

/*** START ARRAY.FROM workaround ***/

if (!Array.from) {
  Array.from = (function () {
    var toStr = Object.prototype.toString;
    var isCallable = function (fn) {
      return typeof fn === 'function' || toStr.call(fn) === '[object Function]';
    };
    var toInteger = function (value) {
      var number = Number(value);
      if (isNaN(number)) { return 0; }
      if (number === 0 || !isFinite(number)) { return number; }
      return (number > 0 ? 1 : -1) * Math.floor(Math.abs(number));
    };
    var maxSafeInteger = Math.pow(2, 53) - 1;
    var toLength = function (value) {
      var len = toInteger(value);
      return Math.min(Math.max(len, 0), maxSafeInteger);
    };

    // The length property of the from method is 1.
    return function from(arrayLike/*, mapFn, thisArg */) {
      // 1. Let C be the this value.
      var C = this;

      // 2. Let items be ToObject(arrayLike).
      var items = Object(arrayLike);

      // 3. ReturnIfAbrupt(items).
      if (arrayLike == null) {
        throw new TypeError("Array.from requires an array-like object - not null or undefined");
      }

      // 4. If mapfn is undefined, then let mapping be false.
      var mapFn = arguments.length > 1 ? arguments[1] : void undefined;
      var T;
      if (typeof mapFn !== 'undefined') {
        // 5. else
        // 5. a If IsCallable(mapfn) is false, throw a TypeError exception.
        if (!isCallable(mapFn)) {
          throw new TypeError('Array.from: when provided, the second argument must be a function');
        }

        // 5. b. If thisArg was supplied, let T be thisArg; else let T be undefined.
        if (arguments.length > 2) {
          T = arguments[2];
        }
      }

      // 10. Let lenValue be Get(items, "length").
      // 11. Let len be ToLength(lenValue).
      var len = toLength(items.length);

      // 13. If IsConstructor(C) is true, then
      // 13. a. Let A be the result of calling the [[Construct]] internal method of C with an argument list containing the single item len.
      // 14. a. Else, Let A be ArrayCreate(len).
      var A = isCallable(C) ? Object(new C(len)) : new Array(len);

      // 16. Let k be 0.
      var k = 0;
      // 17. Repeat, while k < len… (also steps a - h)
      var kValue;
      while (k < len) {
        kValue = items[k];
        if (mapFn) {
          A[k] = typeof T === 'undefined' ? mapFn(kValue, k) : mapFn.call(T, kValue, k);
        } else {
          A[k] = kValue;
        }
        k += 1;
      }
      // 18. Let putStatus be Put(A, "length", len, true).
      A.length = len;
      // 20. Return A.
      return A;
    };
  }());
}

/*** END ARRAY.FROM workaround ***/
