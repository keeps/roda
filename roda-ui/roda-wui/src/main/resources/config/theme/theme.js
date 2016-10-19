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

	$(window).scroll(function () {
		// FIXME the math for the sidebar has to be reviewed
		if ($(".sidebar").length > 0 && 
				$(window).height() > $(".sticky-flow").height() + $(".footer").height() + $(".footerTop").height() + $(".sidebar").position().top + 40) {
		  $('.sidebar').addClass('sticky');
	    } else {
	      $('.sidebar').removeClass('sticky');
	    }
   	});
});