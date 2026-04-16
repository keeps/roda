/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.roda.core.data.v2.ip.metadata.FileFormat;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * GWT widget that renders an email message file (.msg or .eml) entirely in the
 * browser without server-side conversion.
 *
 * <p>
 * Outlook {@code .msg} files are decoded with the
 * {@code @kenjiuno/msgreader} library (exposed on the page as
 * {@code $wnd.MSGReader}). RFC&nbsp;822 {@code .eml} files are parsed with
 * {@code eml-parse-js} (UMD bundle served from the WebJar, exposed as
 * {@code $wnd.EmlParseJs}). HTML bodies are sanitized with DOMPurify before
 * being inserted into the DOM.
 * </p>
 *
 * <p>
 * Required scripts in {@code Main.html} (all are browser IIFEs produced by
 * the {@code frontend-maven-plugin} build step or standard WebJars):
 * <ul>
 * <li>{@code webjars/dompurify/dist/purify.min.js} — sets
 * {@code window.DOMPurify}</li>
 * <li>{@code js/msgreader/MsgReader.js} — sets {@code window.MSGReader}</li>
 * <li>{@code js/emlparser/EmlParseJs.js} — sets {@code window.EmlParseJs}</li>
 * </ul>
 * </p>
 */
public class EmailViewer extends Composite {

  private final FlowPanel panel;
  private final Command onPreviewFailure;

  public EmailViewer(SafeUri downloadUri, FileFormat format, String filename, Command onPreviewFailure) {
    this.onPreviewFailure = onPreviewFailure;

    panel = new FlowPanel();
    initWidget(panel);
    setStyleName("viewRepresentationEmailFilePreview");

    final String fileUrl = downloadUri.asString();
    final String fmt = detectFormat(format, filename);

    panel.addAttachHandler(event -> {
      if (event.isAttached()) {
        renderEmail(panel.getElement(), fileUrl, fmt);
      } else {
        revokeObjectUrls();
      }
    });
  }

  /**
   * Determines the email sub-format from PRONOM ID, MIME type, or filename
   * extension.
   *
   * @return {@code "msg"} for Outlook MSG, {@code "eml"} for RFC 822
   */
  private static String detectFormat(FileFormat format, String filename) {
    if (format != null) {
      String pronom = format.getPronom();
      if ("fmt/952".equals(pronom)) {
        return "msg";
      }
      if ("fmt/950".equals(pronom)) {
        return "eml";
      }
      String mime = format.getMimeType();
      if ("application/vnd.ms-outlook".equals(mime)) {
        return "msg";
      }
      if ("message/rfc822".equals(mime)) {
        return "eml";
      }
    }
    if (filename != null && filename.toLowerCase().endsWith(".msg")) {
      return "msg";
    }
    return "eml";
  }

  /**
   * Called from JSNI when the email cannot be fetched or parsed. Delegates to
   * the {@code onPreviewFailure} command supplied by {@link BitstreamPreview}.
   */
  private void handleFailure() {
    onPreviewFailure.execute();
  }

  /**
   * Revokes all Blob object-URLs created during rendering to free memory.
   * Called when the widget is detached from the DOM.
   */
  private native void revokeObjectUrls() /*-{
    var urls = this.__emailViewerObjUrls;
    if (urls) {
      for (var i = 0; i < urls.length; i++) {
        $wnd.URL.revokeObjectURL(urls[i]);
      }
      this.__emailViewerObjUrls = [];
    }
  }-*/;

  /**
   * Fetches the email file and renders it inside {@code container}.
   *
   * <p>
   * {@code .msg} files are fetched as an {@code ArrayBuffer} via
   * {@code $wnd.XMLHttpRequest} (the host-page XHR constructor) so that
   * {@code xhr.response} lives in the same JavaScript realm as the MSGReader
   * bundle — avoiding cross-frame {@code instanceof ArrayBuffer} failures that
   * occur when the GWT module iframe's constructors differ from the host page's.
   * {@code .eml} files are fetched as plain text and parsed with eml-parse-js.
   * </p>
   *
   * <p>
   * The rendered view shows Subject, From, To, CC, Date, a sanitized body,
   * inline CID images resolved to data URIs, and a list of attachments with
   * download links.
   * </p>
   */
  private native void renderEmail(Element container, String url, String fmt) /*-{
    var self = this;
    self.__emailViewerObjUrls = [];
    var objUrls = self.__emailViewerObjUrls;

    function escapeHtml(str) {
      if (str == null) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    function onError(msg) {
      container.innerHTML =
        '<p class="errormessage">Unable to render email: ' + escapeHtml(String(msg)) + '</p>';
      self.@org.roda.wui.client.browse.EmailViewer::handleFailure()();
    }

    // Sanitize HTML bodies with DOMPurify; fall back to plain-text extraction.
    // ADD_DATA_URI_TAGS allows data: URIs on <img> src so that CID-resolved
    // inline attachments (replaced by data:image/...;base64,... before this
    // call) are not stripped by DOMPurify's default URI sanitisation.
    function sanitize(html) {
      if ($wnd.DOMPurify) {
        return $wnd.DOMPurify.sanitize(html, {
          FORBID_TAGS: ['script', 'style', 'base', 'link', 'meta'],
          ADD_DATA_URI_TAGS: ['img']
        });
      }
      // Fallback: strip all tags
      var tmp = $doc.createElement('div');
      tmp.innerHTML = html;
      return tmp.textContent || tmp.innerText || '';
    }

    function formatSize(bytes) {
      if (!bytes) return '';
      if (bytes >= 1048576) return (bytes / 1048576).toFixed(1) + ' MB';
      if (bytes >= 1024) return (bytes / 1024).toFixed(1) + ' KB';
      return bytes + ' B';
    }

    function buildHeaderField(label, value) {
      if (!value) return '';
      return '<div class="email-field">'
        + '<span class="email-label">' + label + ':</span>'
        + ' <span class="email-value">' + escapeHtml(value) + '</span>'
        + '</div>';
    }

    // eml-parse-js encodes attachment content with TextEncoder before storing it,
    // so att.data is a Uint8Array of UTF-8 bytes of the base64 string.
    // att.data64 is the base64 string decoded back via TextDecoder — use that.
    // Fallback: if data64 is absent, reconstruct the string from the byte array.
    function emlAttBase64(att) {
      if (att.data64 && typeof att.data64 === 'string') return att.data64;
      if (att.data) {
        // Convert Uint8Array of ASCII char codes back to the base64 string.
        var d = att.data;
        var s = '';
        var chunk = 32768;
        for (var i = 0; i < d.length; i += chunk) {
          s += String.fromCharCode.apply(null, d.subarray ? d.subarray(i, i + chunk) : Array.prototype.slice.call(d, i, i + chunk));
        }
        return s;
      }
      return null;
    }

    // Create a Blob object-URL from an EML attachment.
    // Returns null if the data is unavailable or conversion fails.
    function emlAttToObjectUrl(att, mimeType) {
      try {
        var b64 = emlAttBase64(att);
        if (!b64) return null;
        var binary = $wnd.atob(b64);
        var bytes = new $wnd.Uint8Array(binary.length);
        for (var i = 0; i < binary.length; i++) {
          bytes[i] = binary.charCodeAt(i);
        }
        var blob = new $wnd.Blob([bytes], {type: mimeType});
        var url = $wnd.URL.createObjectURL(blob);
        objUrls.push(url);
        return url;
      } catch (e) {
        return null;
      }
    }

    // Replace cid: references in an HTML body with data URIs built from the
    // parsed attachment list.  eml-parse-js stores the base64 string in
    // att.data64; att.id holds the Content-ID (with angle brackets).
    function resolveCids(html, attachments) {
      if (!html || !attachments || attachments.length === 0) return html;
      return html.replace(/cid:([^"'\s>)]+)/g, function(match, cid) {
        for (var i = 0; i < attachments.length; i++) {
          var att = attachments[i];
          var id = (att.contentId || att.id || '').replace(/^<|>$/g, '');
          if (id === cid) {
            var mime = ((att.contentType || att.mimeType || 'application/octet-stream')
                         .split(';')[0]).trim();
            var b64 = emlAttBase64(att);
            if (b64) {
              return 'data:' + mime + ';base64,' + b64;
            }
          }
        }
        return match;
      });
    }

    function renderParsed(subject, from, to, cc, date, bodyHtml, bodyText, attachments, isEml) {
      var html = '<div class="email-header">';
      html += buildHeaderField('Subject', subject);
      html += buildHeaderField('From', from);
      html += buildHeaderField('To', to);
      html += buildHeaderField('CC', cc);
      html += buildHeaderField('Date', date);
      html += '</div><hr class="email-divider"/>';

      if (bodyHtml) {
        // For EML, resolve inline CID images before sanitising.
        var resolvedHtml = isEml ? resolveCids(bodyHtml, attachments) : bodyHtml;
        html += '<div class="email-body">' + sanitize(resolvedHtml) + '</div>';
      } else if (bodyText) {
        html += '<div class="email-body"><pre class="email-body-text">'
          + escapeHtml(bodyText) + '</pre></div>';
      } else {
        html += '<div class="email-body email-body-empty">(no body)</div>';
      }

      // Build attachment list.  For EML, offer a download link built from the
      // base64 attachment data returned by eml-parse-js.
      if (attachments && attachments.length > 0) {
        html += '<div class="email-attachments">'
          + '<h4 class="email-attachments-title">Attachments (' + attachments.length + ')</h4>'
          + '<ul class="email-attachments-list">';
        for (var i = 0; i < attachments.length; i++) {
          var att = attachments[i];
          var name = att.fileName || att.name || 'attachment';
          var size = att.contentLength || att.size || 0;
          html += '<li><i class="fa fa-paperclip"></i> ';
          // _objUrl is pre-computed for MSG attachments; EML attachments are
          // converted on-the-fly from their embedded base64 data.
          var attObjUrl = att._objUrl || null;
          if (!attObjUrl && isEml && (att.data || att.data64)) {
            var mime = ((att.contentType || att.mimeType || 'application/octet-stream')
                         .split(';')[0]).trim();
            attObjUrl = emlAttToObjectUrl(att, mime);
          }
          if (attObjUrl) {
            html += '<a href="' + attObjUrl + '" download="' + escapeHtml(name) + '">'
              + escapeHtml(name) + '</a>';
          } else {
            html += escapeHtml(name);
          }
          if (size) {
            html += ' <span class="email-attachment-size">(' + formatSize(size) + ')</span>';
          }
          html += '</li>';
        }
        html += '</ul></div>';
      }

      container.innerHTML = html;
    }

    if (fmt === 'msg') {
      // window.MSGReader is the MsgReader constructor produced by the
      // frontend-maven-plugin esbuild step (entry.js → js/msgreader/MsgReader.js).
      var ReaderCls = $wnd.MSGReader;
      if (!ReaderCls) {
        onError('MSGReader not loaded — ensure the Maven build ran the bundle-msgreader-for-browser step.');
        return;
      }

      // Use $wnd.XMLHttpRequest so that xhr.response is an ArrayBuffer in the
      // host-page realm.  The MSGReader bundle's DataStream uses
      // `instanceof ArrayBuffer` with the host-page's ArrayBuffer constructor;
      // using the GWT frame's XHR would produce a cross-realm ArrayBuffer that
      // fails the instanceof check, causing "Unknown arrayBuffer".
      var xhr = new $wnd.XMLHttpRequest();
      xhr.open('GET', url, true);
      xhr.responseType = 'arraybuffer';

      xhr.onload = function() {
        if (xhr.status !== 200) {
          onError('HTTP ' + xhr.status);
          return;
        }
        try {
          // Pass the raw ArrayBuffer directly; it is already in the host-page
          // realm so DataStream's `instanceof ArrayBuffer` branch matches.
          var reader = new ReaderCls(xhr.response);
          var msg = reader.getFileData();

          var toList = [], ccList = [];
          if (msg.recipients) {
            for (var i = 0; i < msg.recipients.length; i++) {
              var r = msg.recipients[i];
              var addr = r.name
                ? r.name + (r.email ? ' <' + r.email + '>' : '')
                : (r.email || '');
              if (r.recipType === 'cc' || r.recipType === 'CC') {
                ccList.push(addr);
              } else {
                toList.push(addr);
              }
            }
          }

          var from = msg.senderName
            ? msg.senderName + (msg.senderEmail ? ' <' + msg.senderEmail + '>' : '')
            : (msg.senderEmail || '');

          var date = msg.date ? new $wnd.Date(msg.date).toLocaleString() : (msg.creationTime || '');

          // Pre-fetch attachment binary content from the MSG file.
          // reader.getAttachment(att) returns { fileName, content: Uint8Array }.
          // We create Blob object-URLs here while the reader is still in scope
          // and store them on the attachment objects for renderParsed to use.
          var rawAtts = msg.attachments || [];
          var processedAtts = [];
          for (var ai = 0; ai < rawAtts.length; ai++) {
            var rawAtt = rawAtts[ai];
            var pAtt = {
              fileName: rawAtt.fileName || rawAtt.name || 'attachment',
              name: rawAtt.fileName || rawAtt.name || 'attachment',
              size: rawAtt.size || 0,
              _objUrl: null
            };
            try {
              var attData = reader.getAttachment(rawAtt);
              if (attData && attData.content && attData.content.length > 0) {
                var attBlob = new $wnd.Blob([attData.content],
                  {type: 'application/octet-stream'});
                pAtt._objUrl = $wnd.URL.createObjectURL(attBlob);
                objUrls.push(pAtt._objUrl);
              }
            } catch (attErr) {
              // Leave _objUrl null; attachment will be listed without a link.
            }
            processedAtts.push(pAtt);
          }

          renderParsed(
            msg.subject || '',
            from,
            toList.join(', '),
            ccList.join(', '),
            date,
            msg.bodyHTML || null,
            msg.body || null,
            processedAtts,
            false
          );
        } catch (e) {
          onError(e.message || 'parse error');
        }
      };

      xhr.onerror = function() {
        onError('network error');
      };

      xhr.send();

    } else {
      // window.EmlParseJs is set by the UMD bundle served from the
      // github-com-MQpeng-eml-parse-js WebJar at lib/bundle.umd.js.
      var emlLib = $wnd.EmlParseJs;
      if (!emlLib) {
        onError('EmlParseJs not loaded — ensure js/emlparser/EmlParseJs.js is included (built by Maven frontend-maven-plugin).');
        return;
      }

      // Use $wnd.XMLHttpRequest for consistency with the MSG branch; text
      // responses have no realm issue but it is cleaner to keep it uniform.
      var xhrEml = new $wnd.XMLHttpRequest();
      xhrEml.open('GET', url, true);
      xhrEml.responseType = 'text';

      xhrEml.onload = function() {
        if (xhrEml.status !== 200) {
          onError('HTTP ' + xhrEml.status);
          return;
        }
        try {
          var readFn = emlLib.readEml || emlLib.read || emlLib;
          readFn(xhrEml.responseText, function(err, data) {
            if (err || !data) {
              onError(err ? (err.message || String(err)) : 'parse error');
              return;
            }

            function flattenAddrs(field) {
              if (!field) return '';
              if (typeof field === 'string') return field;
              // Single address object (name + email properties)
              if (typeof field === 'object' && !Array.isArray(field)) {
                return field.name ? field.name + (field.email ? ' <' + field.email + '>' : '')
                                  : (field.email || '');
              }
              if (Array.isArray(field)) {
                return field.map(function(f) {
                  if (typeof f === 'string') return f;
                  return f.name ? f.name + (f.email ? ' <' + f.email + '>' : '')
                                : (f.email || String(f));
                }).join(', ');
              }
              return String(field);
            }

            var date = '';
            if (data.date) {
              date = data.date instanceof $wnd.Date
                ? data.date.toLocaleString()
                : String(data.date);
            }

            renderParsed(
              data.subject || '',
              flattenAddrs(data.from),
              flattenAddrs(data.to),
              flattenAddrs(data.cc),
              date,
              data.html || null,
              data.text || null,
              data.attachments || [],
              true
            );
          });
        } catch (e) {
          onError(e.message || 'parse error');
        }
      };

      xhrEml.onerror = function() {
        onError('network error');
      };

      xhrEml.send();
    }
  }-*/;
}
