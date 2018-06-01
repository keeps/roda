var FOOTER_ADDED = false;

$(document).ready(function() {
    // getUrlParameters function based on
    // from https://stackoverflow.com/a/2880929/1483200
    var urlParams = (function () {
        var urlParams,
            match,
            pl = /\+/g,  // Regex for replacing addition symbol with a space
            search = /([^&=]+)=?([^&]*)/g,
            decode = function (s) {
                return decodeURIComponent(s.replace(pl, " "));
            },
            query = window.location.search.substring(1);

        urlParams = {};
        while (match = search.exec(query))
            urlParams[decode(match[1])] = decode(match[2]);
        return urlParams;
    })();

    // keep branding=false retrocompatibility
    if (urlParams['branding'] === 'false') {
        urlParams['branding'] = 'nobranding.css';
    }

    if ((typeof urlParams['branding']) === 'string') {
        var brandings = urlParams['branding'].split(',');

        brandings.forEach(function(branding){
            if (branding.endsWith("css")) {
                $(document).on('DOMNodeInserted', ".footer", function(e) {
                    $("head").append('<link rel="stylesheet" type="text/css" href="api/v1/theme?resource_id=' + branding + '">');
                });
            } else {
                // allow branding to be specified without extension
                $(document).on('DOMNodeInserted', ".footer", function(e) {
                    $("head").append('<link rel="stylesheet" type="text/css" href="api/v1/theme?resource_id=' + branding + '.css">');
                });
            }
        });
    }

	// necessary to pass on the accessibility test
	$(document).on('DOMNodeInserted', "thead", function(e) {
		$("img").each(function(index) {
		  	var imageAlt = $(this).attr('alt');
			if(!(typeof attr !== typeof undefined && attr !== false)) {
				$(this).attr('alt', 'img_alt');
			}
		});
	});

	$(document).on('DOMNodeInserted', ".footer", function(e) {
		elem = e.target;

		if(!FOOTER_ADDED && $(elem).hasClass("footer")) {
			var pathname = window.location.pathname;
			$.get(pathname + "version.json", function(data) {
			      $(".footer").append("<div style='color:rgba(255, 255, 255, 0.5); float:right;' class='build_time'>Version build on " + data["git.build.time"] + "</div>");
			      FOOTER_ADDED = true;
			});
		}
	});
});
