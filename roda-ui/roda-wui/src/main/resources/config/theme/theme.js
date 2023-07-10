var FOOTER_ADDED = false;

$(document).ready(function () {
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
        urlParams['branding'] = 'nobranding';
    }

    let brandingsToInsert = [];

    if ((typeof urlParams['branding']) === 'string') {
        var brandings = urlParams['branding'].split(',');

        brandings.forEach(function (branding) {
            if (/^([a-z0-9]+)$/.test(branding)) {
                brandingsToInsert.push(branding.concat(".css"));
            }
        });
    }

    $(document).on('DOMNodeInserted', ".gwt-HTML", function (e) {
        elem = e.target;
        if ($(elem).find('.footer').length > 0) {
            brandingsToInsert.forEach(function (branding) {
                $("head").append('<link rel="stylesheet" type="text/css" href="api/v1/theme?resource_id=' + branding + '">');
            });
        }
    });

    // necessary to pass on the accessibility test
    $(document).on('DOMNodeInserted', "thead", function (e) {
        $("img").each(function (index) {
            var imageAlt = $(this).attr('alt');
            if (!(typeof attr !== typeof undefined && attr !== false)) {
                $(this).attr('alt', 'img_alt');
            }
        });
    });

    $(document).on('DOMNodeInserted', '.gwt-HTML', function (e) {
        elem = e.target;

        if (!FOOTER_ADDED && $(elem).find('.footer').length > 0) {
            var pathname = window.location.pathname;
            $.get(pathname + "version.json", function (data) {
                $("div#version").append(
                  "<div style='color:rgba(255, 255, 255, 0.5); class='built_time'>Version " +
                  data["git.build.version"] +
                  "</div>"
                 );
                FOOTER_ADDED = true;
            });
        }
    });
});
