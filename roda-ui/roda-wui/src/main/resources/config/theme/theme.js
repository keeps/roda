var FOOTER_ADDED = false;
var SIDEBAR_ADDED = false;

$(document).ready(function() {

	$(document).on('DOMNodeInserted', ".footer", function(e) {
		elem = e.target;
		if(!FOOTER_ADDED && $(elem).hasClass("footer")) {
			$.get("/version.json", function( data ) {
			      $(".footer").append("<div style='color:#999; float:right;' class='build_time'>Version build on " + data["git.build.time"] + "</div>");
			      FOOTER_ADDED = true;
			});
		}
	});

	$(document).on('DOMNodeInserted', ".sidebar_actions_wrapper", function(e) {
		elem = e.target;
		if(!SIDEBAR_ADDED && $(elem).hasClass("sidebar_actions_wrapper")) {
			setFacetsWidth();
			SIDEBAR_ADDED = true;
		}
	});

	$(window).resize(function() {
		setFacetsWidth();
	});


	$(window).scroll(function () {
		if ($(".sidebar").length > 0 && $(window).height() > $(".sidebar_actions_wrapper").height() &&
				$(window).scrollTop() + $(".sidebar_actions_wrapper").height() < $(".sidebar").position().top + $(".sidebar").height()) {
	      $('.sidebar_actions_wrapper').addClass('sticky');
	  	  setFacetsWidth();
	    } else {
	      $('.sidebar_actions_wrapper').removeClass('sticky');
	    }
   	});
});


function setFacetsWidth() {
	if($('.sidebar_actions_wrapper').length > 0) {
		$('.sidebar_actions_wrapper').css('width', $(".sidebar").width());
	}
}
