var FOOTER_ADDED = false;

$(document).ready(function() {

	$(document).on('DOMNodeInserted', ".footer", function(e) {
		elem = e.target;
		if(!FOOTER_ADDED && $(elem).hasClass("footer")) {
			$.get("/version.json", function( data ) {
			      $(".footer").append("<div style='color:rgba(255, 255, 255, 0.5); float:right;' class='build_time'>Version build on " + data["git.build.time"] + "</div>");
			      FOOTER_ADDED = true;
			});
		}
	});
});