
$(document).ready(function() {
	
	$(document).on('DOMNodeInserted', ".sidebar_actions_wrapper", function(e) {
		if($(this).hasClass("sidebar_actions_wrapper")) {
			setFacetsWidth();
		}
	});

	
	$(document).on('DOMNodeInserted', ".footer", function(e) {
		$.get("/version.json", function( data ) {
		      $(".footer").append("<div style='color:#999; float:right;' class='build_time'>Version build on " + data["git.build.time"] + "</div>");
		});
	});

	
	$(window).resize(function() {
		setFacetsWidth();
	});
	
	
	$(window).scroll(function () {
	if ($(window).scrollTop() + $(".sidebar_actions_wrapper").height() < $(".sidebar").position().top + $(".sidebar").height() ) {
      $('.sidebar_actions_wrapper').addClass('sticky');
  	  setFacetsWidth();
    } else {
      $('.sidebar_actions_wrapper').removeClass('sticky');
    }
  });
});


function setFacetsWidth() {
	$('.sidebar_actions_wrapper').css('width', $(".sidebar").width());
}