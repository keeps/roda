$.get("/version.json", function( data ) {
    setTimeout(function() {
      $(".footer").append("<div style='color:#999; float:right;' class='build_time'>Version build on " + data["git.build.time"] + "</div>");
    }, 500);
});