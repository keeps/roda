// look for duplicate IDs
var hasDuplicateIDs = function () {
  var ids = {};
  var found = false;
  $('[id]').each(function() {
    if (this.id && ids[this.id]) {
      found = true;
      console.warn('Duplicate ID #'+this.id);
    }
    ids[this.id] = 1;
  });
  if (found){
    console.error('Duplicate IDs have been found!');
    return true;
  }else{
    return false;
  }
};

var urlStart = "https://raw.githubusercontent.com/keeps/roda/bf-dev/";
var config = {};

$(document).on('flatdoc:ready', function() {

  // handle new headers
  $.each(
    $.grep(config.pages, function(v) {
      return (typeof v) === "object";
    }),
    function(index, v){
      var display = Object.keys(v)[0];
      var values = v[display];

      // get IDs from config
      var childrenIDs = $.map(values, function(v){
        return v[Object.keys(v)[0]];
      });

      // build and add this:
      // <li class="level-1"><span class="level-1">'+display+'</span></li>
      var previous = $("#"+childrenIDs[0]+"-item").closest("li.level-1");

      var header = jQuery('<span/>', {
          class: 'level-1',
          text: display
      });

      var li = jQuery('<li/>', {
          class: 'level-1'
      });

      $(li).append($(header));
      li = $(li).insertBefore($(previous));

      $.each(childrenIDs, function(i, id){
        $("li#" + id + "-item").detach().appendTo(li);
      });

      // handle children indent
      $.each(childrenIDs, function(i, id){
        //var id = "acknowledgements";

        // indent children 1 time
        $("#" + id + "-item .level-3").addClass("level-3-4");
        $("#" + id + "-item .level-3-4").removeClass("level-3");
        $("#" + id + "-item .level-3-4").addClass("level-4");
        $("#" + id + "-item .level-3-4").removeClass("level-3-4");

        $("#" + id + "-item .level-2").addClass("level-2-3");
        $("#" + id + "-item .level-2-3").removeClass("level-2");
        $("#" + id + "-item .level-2-3").addClass("level-3");
        $("#" + id + "-item .level-2-3").removeClass("level-2-3");

        $("#" + id + "-item .level-1").addClass("level-1-2");
        $("#" + id + "-item .level-1-2").removeClass("level-1");
        $("#" + id + "-item .level-1-2").addClass("level-2");
        $("#" + id + "-item .level-1-2").removeClass("level-1-2");
      });
    }
  );

  // handle markdown URLs
  var markdownLinkRegex = /^(?:(?![a-zA-Z]+:\/\/))(?:(?![#/]))(.*?\\.md)$/;
  $('a').each(function(i, a){
    var $a = $(a);
    var link = $a.attr('href');
    if(typeof link === 'string' && link.match(markdownLinkRegex)){
      link = link.replace(markdownLinkRegex, urlStart + config.base + "$1");
      $a.attr('href', link);
    }
  });

  // look for duplicate IDs
  hasDuplicateIDs();
});

$.ajaxSetup({ cache: false });

var jsonURL = urlStart + "roda-ui/roda-wui/src/main/resources/config/flatdoc.json";
//var jsonURL = 'http://localhost:8000/flatdoctest.json';

$.getJSON( jsonURL, function( c ) {
  $.ajaxSetup({ cache: false });
  config = c;

  console.log("config: " + JSON.stringify(config, null, 4));

  /*
  config.pages = [
    {
      'top': [
        {'Acknowledgements.md': 'acknowledgements'},
        {'CAS.md': 'cas'}
      ]
    }
  ];
  */

  var md2url = function(md){
    return urlStart + config.base + md;
  }

  // make mangle also handle image URLs
  var imageLinkRegex = /^(images\/.*?)$/;
  Flatdoc.transformer.originalMangle = Flatdoc.transformer.mangle;
  Flatdoc.transformer.mangle = function($content) {
    // do whatever it is it should be doing
    Flatdoc.transformer.originalMangle($content);
    // do our thing: handle image URLs
    $content.find('img').each(function(i, img){
      var $img = $(img);
      var link = $img.attr('src');
      if(typeof link === 'string' && link.match(imageLinkRegex)){
        link = link.replace(imageLinkRegex, urlStart + config.base + "$1");
        $img.attr('src', link);
      }
    });
  };

  Flatdoc.run({
    fetcher: Flatdoc.file(//'http://localhost:8000/test.md'
      $.map(
        $.map(config.pages, function(v){
          if(typeof v === "object"){
            // {key: [{md1: id1}, {md2: id2}, {md3: id3}]} -> [URLmd1, URLmd2, URLmd3]
            return $.map(v[Object.keys(v)[0]], function(mdAndId){
              // {md: id} -> URLmd
              return md2url(Object.keys(mdAndId)[0]);
            });
          }else{
            // md -> URLmd
            return md2url(v);
          }
        }), function(v){return v}
      )
    ),
    highlight: function (code, lang) {
      if (lang && hljs.getLanguage(lang)) {
        return hljs.highlight(lang, code, true).value;
      } else {
        return hljs.highlightAuto(code).value;
      }
    }
  });
});
