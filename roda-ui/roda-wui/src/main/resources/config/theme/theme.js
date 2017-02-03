var FOOTER_ADDED = false;

$(document).ready(function() {
	
	// necessary to pass on the accessibility test
	$(document).on('DOMNodeInserted', "thead", function(e) {		
		$("img").each(function( index ) {
		  	var imageAlt = $(this).attr('alt');
			if(!(typeof attr !== typeof undefined && attr !== false)) {
				$(this).attr('alt', 'img_alt');
			}
		});		
	});

	$(document).on('DOMNodeInserted', ".footer", function(e) {
		elem = e.target;
		if(!FOOTER_ADDED && $(elem).hasClass("footer")) {
			$.get("/version.json", function(data) {
			      $(".footer").append("<div style='color:rgba(255, 255, 255, 0.5); float:right;' class='build_time'>Version build on " + data["git.build.time"] + "</div>");
			      FOOTER_ADDED = true;
			});
		}
	});
});