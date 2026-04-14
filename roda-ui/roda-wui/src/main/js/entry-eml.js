// Entry point for the browser IIFE bundle of eml-parse-js.
// esbuild inlines all transitive deps (js-base64, @sinonjs/text-encoding).
// The resulting global is window.EmlParseJs = { readEml, ... }.
var emlParseJs = require('eml-parse-js');
var _origReadEml = emlParseJs.readEml;

// eml-parse-js v1.1.x has a bug in its HTML-body base64 auto-detection:
// it calls atob(htmlContent) on raw HTML.  After _tidyB64 strips all
// non-[A-Za-z0-9+/] characters, the resulting string's length is usually
// not divisible by 4, so atob throws InvalidCharacterError.  The library
// already catches this in a try/catch and falls back gracefully (email still
// renders correctly), but the catch block calls console.error, producing a
// noisy — and harmless — console warning.
//
// Fix: wrap readEml so that console.error is silenced for InvalidCharacterError
// during parsing.  The parse is fully synchronous so there is no window in
// which other legitimate errors would be accidentally suppressed.
emlParseJs.readEml = function readEml(eml, opts, cb) {
  // Mirror the library's own argument-shifting so that the two-argument form
  // readEml(eml, callback) works correctly when we forward the call with an
  // explicit third argument (the console.error-restoring wrapper).
  if (typeof opts === 'function' && typeof cb === 'undefined') {
    cb = opts;
    opts = null;
  }

  var origCE = console.error;
  console.error = function suppressAtobError() {
    var err = arguments[0];
    // Only suppress the known false-positive; let everything else through.
    if (err &&
        ((err instanceof Error && err.name === 'InvalidCharacterError') ||
         (typeof err === 'string' && err.indexOf('InvalidCharacterError') !== -1))) {
      return;
    }
    origCE.apply(console, arguments);
  };

  try {
    return _origReadEml.call(this, eml, opts, function () {
      console.error = origCE;        // restore before handing control to caller
      if (typeof cb === 'function') cb.apply(this, arguments);
    });
  } catch (e) {
    console.error = origCE;          // restore on unexpected exception too
    throw e;
  }
};

module.exports = emlParseJs;
