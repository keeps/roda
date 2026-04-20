// Entry point for the browser IIFE bundle of @kenjiuno/msgreader.
// esbuild inlines all transitive deps (iconv-lite, @kenjiuno/decompressrtf).
// The resulting global is window.MSGReader (the MsgReader constructor directly).
var msgreader = require('@kenjiuno/msgreader');
var consts = require('@kenjiuno/msgreader/lib/const');

// Patch: PT_STRING8 ('001e') is incorrectly placed at FIELD level instead of
// inside TYPE_MAPPING in @kenjiuno/msgreader v1.11.0.  The decodeField function
// looks only in TYPE_MAPPING, so without this patch all PT_STRING8 fields
// (subject, senderName, senderEmail, body, …) are returned as raw Uint8Array
// instead of decoded strings.  esbuild module caching ensures this mutation is
// visible to the already-initialised MsgReader module.
if (consts && consts.default &&
    consts.default.MSG &&
    consts.default.MSG.FIELD &&
    consts.default.MSG.FIELD.TYPE_MAPPING) {
  consts.default.MSG.FIELD.TYPE_MAPPING['001e'] = 'string';
}

module.exports = msgreader.default;
