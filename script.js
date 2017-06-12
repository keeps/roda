// function to look for duplicate IDs
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

$(document).on('flatdoc:ready', function() {
  // get, transform and remove the markdown file identifiers
  $("span[data-markdown-file-placeholder]").each(function(i,e){
    e = $(e);
    e.parent().nextAll('h1').first().attr('data-markdown-file', e.attr('data-markdown-file-placeholder'));
    e.parent().remove();
  });

  // handle new headers
  $.each(
    $.grep(config.pages, function(v) {
      return (typeof v) === "object";
    }),
    function(index, v){
      var display = Object.keys(v)[0];
      var mds = v[display];

      childrenIDs = $.map(mds, function(md){
        return $("h1[data-markdown-file='"+md+"']").first().attr('id');
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


// set some defaults
$.ajaxSetup({ cache: false });
var urlStart = "https://raw.githubusercontent.com/keeps/roda/bf-dev/";
var jsonURL = urlStart + "roda-ui/roda-wui/src/main/resources/config/flatdoc.json";
//var jsonURL = 'http://localhost:8000/flatdoctest.json';

// define some globals
var config = {};
var menu = {};
var fileList = [];

// all set, go get'em!
$.getJSON( jsonURL, function( c ) {
  $.ajaxSetup({ cache: false });
  config = c;

  console.log("config: " + JSON.stringify(config, null, 4));

  /***** SET OPTIONS *************/

  // flattened list of markdown files
  fileList = $.map(
    $.map(config.pages, function(v){
      if(typeof v === "object"){
        // {key: [md1, md2, md3]} -> [md1, md2, md3]
        return v[Object.keys(v)[0]];
      }else{
        // md -> md
        return v;
      }
    }), function(v){return v}
  );

  // list of markdown files (as URLs) to process
  var urlFileList = $.map(fileList, function(md){
    // convert markdown filename to markdown URL
    return urlStart + config.base + md;
  });


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

  // make setMarkedOptions set all the options (not just highlight)
  Flatdoc.parser.setMarkedOptions = function(options) {
    marked.setOptions(options);
  };

  // extract the menu object as soon as Flatdoc generates it
  Flatdoc.parser.originalParse = Flatdoc.parser.parse;
  Flatdoc.parser.parse = function(source, highlight){
    var result = Flatdoc.parser.originalParse(source, highlight);
    menu = result.menu;
    console.log(result);
    return result;
  };

  // set marked options
  var markedOptions = {};

  markedOptions.highlight = function (code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(lang, code, true).value;
    } else {
      return hljs.highlightAuto(code).value;
    }
  };

  /*markedOptions.renderer = function(){
    var renderer = new marked.Renderer();
    renderer.heading = function(text, level, raw) {
      return '<h'
        + level
        + ' id="'
        + raw.toLowerCase().replace(/[^\w]+/g, '-')
        + '">'
        + text
        + '</h'
        + level
        + '>\n';
    };

    return renderer;
  }();*/

  // fetcher based on Flatdoc.file that injects a file identifier in a span element
  // on 'flatdoc:ready', the span element is removed and the file id is moved to the next H1 element
  identifiedFileFetcherCounter = 0;
  identifiedFileFetcher = function(url) {
    function loadData(locations, response, callback) {
      if (locations.length === 0)
        callback(null, response);
      else
        $.get(locations.shift())
          .fail(function(e) {
            callback(e, null);
          })
          .done(function (data) {
            response = response
                + '\n\n'
                + '<span data-markdown-file-placeholder="'+fileList[identifiedFileFetcherCounter]+'" />'
                + '\n\n'
                + data;
              identifiedFileFetcherCounter++;
            loadData(locations, response, callback);
          });
    }

    return function(callback) {
      loadData(url instanceof Array ?
        url : [url], '', callback);
    };
  };

  /***** FLATDOC CALL *************/
  Flatdoc.run({
    fetcher: identifiedFileFetcher(
      //'http://localhost:8000/test.md'
      urlFileList
    ),
    // originally was just highlighting, now it may be used to set all marked options
    highlight: markedOptions
  });
});
