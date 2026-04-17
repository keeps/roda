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
 * Outlook {@code .msg} files are decoded with the {@code @kenjiuno/msgreader}
 * library (exposed on the page as {@code $wnd.MSGReader}). RFC&nbsp;822
 * {@code .eml} files are parsed with {@code eml-parse-js} (UMD bundle served
 * from the WebJar, exposed as {@code $wnd.EmlParseJs}). HTML bodies are
 * sanitized with DOMPurify before being inserted into the DOM.
 * </p>
 *
 * <h3>Privacy-first image blocking</h3>
 * <p>
 * External images (src starting with {@code http}) are blocked by default for
 * senders not in the user's trusted-sender list (stored in
 * {@code localStorage} under the key {@code roda_email_trusted_senders}). A
 * {@code DOMPurify.addHook('afterSanitizeAttributes', …)} intercepts each
 * {@code <img>} during sanitisation: the original {@code src} is moved to
 * {@code data-blocked-src} and replaced with an SVG placeholder that preserves
 * the image's original {@code width}/{@code height} so the layout does not
 * collapse. Detected external domains are collected and displayed to the user
 * in a banner above the email body.
 * </p>
 *
 * <h3>Sandboxed iframe body</h3>
 * <p>
 * The sanitized body HTML is rendered inside a sandboxed {@code <iframe>}
 * using the {@code srcdoc} attribute with
 * {@code sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin"}.
 * The {@code allow-same-origin} token lets the parent frame access
 * {@code contentDocument} to restore blocked images. When the user confirms
 * image loading, the blocked {@code src} values are restored inside the
 * existing {@code srcdoc} iframe and the iframe is then swapped for a
 * {@code blob:} URL iframe. A {@code blob:} URL frame has its own opaque
 * origin and does not inherit the parent page's Content-Security-Policy,
 * ensuring that external images are not blocked by CSP after the user
 * explicitly opts in.
 * </p>
 *
 * <p>
 * Required scripts in {@code Main.html}:
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
   */
  private native void renderEmail(Element container, String url, String fmt) /*-{
    var self = this;
    self.__emailViewerObjUrls = [];
    var objUrls = self.__emailViewerObjUrls;

    // ── Utilities ──────────────────────────────────────────────────────────

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

    function formatSize(bytes) {
      if (!bytes) return '';
      if (bytes >= 1048576) return (bytes / 1048576).toFixed(1) + ' MB';
      if (bytes >= 1024)    return (bytes / 1024).toFixed(1)    + ' KB';
      return bytes + ' B';
    }

    function buildHeaderField(label, value) {
      if (!value) return '';
      return '<div class="email-field">'
        + '<span class="email-label">' + label + ':</span>'
        + ' <span class="email-value">' + escapeHtml(value) + '</span>'
        + '</div>';
    }

    // ── Trusted-sender helpers (localStorage) ──────────────────────────────

    function getTrustedSenders() {
      try {
        var raw = $wnd.localStorage.getItem('roda_email_trusted_senders');
        return raw ? JSON.parse(raw) : [];
      } catch (e) { return []; }
    }

    function isTrustedSender(email) {
      if (!email) return false;
      var lc = email.toLowerCase();
      var list = getTrustedSenders();
      for (var i = 0; i < list.length; i++) {
        if (list[i] === lc) return true;
      }
      return false;
    }

    function trustSender(email) {
      if (!email) return;
      try {
        var lc = email.toLowerCase();
        var list = getTrustedSenders();
        for (var i = 0; i < list.length; i++) {
          if (list[i] === lc) return;
        }
        list.push(lc);
        $wnd.localStorage.setItem('roda_email_trusted_senders', JSON.stringify(list));
      } catch (e) {}
    }

    // Extract bare email address from "Display Name <addr>" or plain addr.
    function extractEmail(addr) {
      if (!addr) return '';
      var m = String(addr).match(/<([^>]+)>/);
      return m ? m[1].toLowerCase().trim() : String(addr).toLowerCase().trim();
    }

    function extractDomain(imageUrl) {
      var m = String(imageUrl).match(/^https?:\/\/([^\/?\#]+)/i);
      return m ? m[1] : imageUrl;
    }

    // ── Placeholder SVG for blocked images ─────────────────────────────────
    // Preserves original width/height so layout does not collapse.

    function makePlaceholder(w, h) {
      var pw = (w && parseInt(w, 10) > 0) ? Math.min(parseInt(w, 10), 600) : 40;
      var ph = (h && parseInt(h, 10) > 0) ? Math.min(parseInt(h, 10), 400) : 40;
      // Grey rect + crossed lines to mimic a "broken image" icon
      var cx = pw / 2, cy = ph / 2;
      var r  = Math.min(pw, ph) * 0.18;
      var svg = '<svg xmlns="http://www.w3.org/2000/svg" width="' + pw + '" height="' + ph + '">'
        + '<rect width="100%" height="100%" fill="#e8e8e8" rx="3"/>'
        + '<rect x="' + (cx - r) + '" y="' + (cy - r) + '"'
        +   ' width="' + (r * 2) + '" height="' + (r * 2) + '"'
        +   ' fill="none" stroke="#bbb" stroke-width="1.5" rx="2"/>'
        + '<line x1="' + (cx - r * 0.6) + '" y1="' + (cy - r * 0.6) + '"'
        +      ' x2="' + (cx + r * 0.6) + '" y2="' + (cy + r * 0.6) + '"'
        +      ' stroke="#bbb" stroke-width="1.5"/>'
        + '<line x1="' + (cx + r * 0.6) + '" y1="' + (cy - r * 0.6) + '"'
        +      ' x2="' + (cx - r * 0.6) + '" y2="' + (cy + r * 0.6) + '"'
        +      ' stroke="#bbb" stroke-width="1.5"/>'
        + '</svg>';
      return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
    }

    // ── DOMPurify-hook sanitiser with optional image blocking ──────────────
    //
    // Uses DOMPurify.addHook / removeHooks so the hook is only active during
    // this single sanitise call.  detectedDomains is a plain-object set
    // (keys are domain strings, values true).

    function sanitizeBody(html, senderKey, detectedDomains) {
      if (!$wnd.DOMPurify) {
        var tmp = $doc.createElement('div');
        tmp.innerHTML = html;
        return tmp.textContent || tmp.innerText || '';
      }

      if (!isTrustedSender(senderKey)) {
        $wnd.DOMPurify.addHook('afterSanitizeAttributes', function(node) {
          if (node.nodeName !== 'IMG') return;
          var src = node.getAttribute('src');
          if (!src || !/^https?:\/\//i.test(src)) return;
          // Preserve original dimensions so the layout does not collapse.
          var w = node.getAttribute('width')  || '';
          var h = node.getAttribute('height') || '';
          detectedDomains[extractDomain(src)] = true;
          node.setAttribute('data-blocked-src', src);
          node.setAttribute('src', makePlaceholder(w, h));
          if (w) node.setAttribute('width',  w);
          if (h) node.setAttribute('height', h);
        });
      }

      var clean = $wnd.DOMPurify.sanitize(html, {
        FORBID_TAGS:       ['script', 'style', 'base', 'link', 'meta'],
        ADD_DATA_URI_TAGS: ['img'],
        ADD_ATTR:          ['data-blocked-src']
      });

      if (!isTrustedSender(senderKey)) {
        $wnd.DOMPurify.removeHooks('afterSanitizeAttributes');
      }

      return clean;
    }

    // ── srcdoc iframe ──────────────────────────────────────────────────────
    //
    // sandbox="… allow-same-origin" is required so the parent frame can
    // access contentDocument to restore blocked images.  Scripts inside the
    // iframe are still forbidden (no allow-scripts).

    function buildBodyIframe(bodyContent) {
      var fullDoc = '<!DOCTYPE html><html><head><meta charset="utf-8">'
        + '<style>'
        + 'html,body{margin:0;padding:8px;box-sizing:border-box;}'
        + 'body{font-family:Arial,sans-serif;font-size:14px;'
        +      'word-wrap:break-word;line-height:1.5;color:#222;}'
        + 'img{max-width:100%;height:auto;display:inline-block;}'
        + 'a{color:#0066cc;word-break:break-all;}'
        + '</style></head><body>' + bodyContent + '</body></html>';

      var iframe = $doc.createElement('iframe');
      iframe.className = 'email-body-iframe';
      iframe.setAttribute('sandbox',
        'allow-popups allow-popups-to-escape-sandbox allow-same-origin');
      iframe.setAttribute('srcdoc', fullDoc);
      iframe.style.width  = '100%';
      iframe.style.border = '1px solid #eee';
      iframe.style.minHeight = '80px';
      iframe.style.display = 'block';

      iframe.addEventListener('load', function() {
        try {
          var body = iframe.contentDocument.body;
          if (body) iframe.style.height = (body.scrollHeight + 24) + 'px';
        } catch (e) {}
      });

      return iframe;
    }

    // ── Image restoration ──────────────────────────────────────────────────
    //
    // Step 1 — restore data-blocked-src → src inside the existing srcdoc
    //          iframe (requires allow-same-origin).
    // Step 2 — swap the srcdoc iframe for a blob: URL iframe.
    //
    // A blob: URL frame has its own opaque origin and does NOT inherit the
    // parent page's Content-Security-Policy, so external images that would
    // be blocked by CSP in the srcdoc frame load freely after the swap.

    function restoreImages(iframe) {
      try {
        var doc = iframe.contentDocument || iframe.contentWindow.document;

        // Restore every blocked image src.
        var blocked = doc.querySelectorAll('img[data-blocked-src]');
        for (var i = 0; i < blocked.length; i++) {
          blocked[i].setAttribute('src', blocked[i].getAttribute('data-blocked-src'));
          blocked[i].removeAttribute('data-blocked-src');
        }

        // Serialise the now-restored document and create a blob URL.
        // A blob: URL frame does not inherit the parent CSP, so external
        // images are no longer blocked after the navigation.
        var restoredHtml = doc.documentElement.outerHTML;
        var blob = new $wnd.Blob([restoredHtml], {type: 'text/html'});
        var blobUrl = $wnd.URL.createObjectURL(blob);
        objUrls.push(blobUrl);

        // Build replacement iframe (no allow-same-origin needed for blob frames).
        var newIframe = $doc.createElement('iframe');
        newIframe.className = iframe.className;
        newIframe.setAttribute('sandbox', 'allow-popups allow-popups-to-escape-sandbox');
        newIframe.style.cssText = iframe.style.cssText;
        newIframe.src = blobUrl;

        // Mirror the current height; images will adjust naturally once loaded.
        newIframe.style.height = iframe.style.height || '400px';

        if (iframe.parentNode) {
          iframe.parentNode.replaceChild(newIframe, iframe);
        }
      } catch (e) {
        // Fallback: plain src restore in the srcdoc frame (may still be
        // blocked by CSP but better than nothing).
        try {
          var doc2 = iframe.contentDocument || iframe.contentWindow.document;
          var blocked2 = doc2.querySelectorAll('img[data-blocked-src]');
          for (var j = 0; j < blocked2.length; j++) {
            blocked2[j].setAttribute('src', blocked2[j].getAttribute('data-blocked-src'));
            blocked2[j].removeAttribute('data-blocked-src');
          }
        } catch (e2) {}
      }
    }

    // ── Trust modal ────────────────────────────────────────────────────────

    function showTrustModal(domains, senderKey, iframe, banner) {
      var overlay = $doc.createElement('div');
      overlay.className = 'email-trust-modal-overlay';

      var modal = $doc.createElement('div');
      modal.className = 'email-trust-modal';

      var items = '';
      for (var i = 0; i < domains.length; i++) {
        items += '<li>' + escapeHtml(domains[i]) + '</li>';
      }

      modal.innerHTML =
          '<h4 class="email-trust-modal-title">External images blocked</h4>'
        + '<p class="email-trust-modal-desc">'
        + 'Images from the following domains are blocked to protect your privacy:</p>'
        + '<ul class="email-trust-modal-domains">' + items + '</ul>'
        + '<div class="email-trust-modal-actions">'
        + '<button class="email-trust-btn email-trust-btn-cancel">Cancel</button>'
        + '<button class="email-trust-btn email-trust-btn-once">Load once</button>'
        + '<button class="email-trust-btn email-trust-btn-always">Always trust sender</button>'
        + '</div>';

      overlay.appendChild(modal);
      $doc.body.appendChild(overlay);

      function closeModal() {
        if (overlay.parentNode) overlay.parentNode.removeChild(overlay);
      }

      // Close on backdrop click.
      overlay.addEventListener('click', function(e) {
        if (e.target === overlay) closeModal();
      });

      modal.querySelector('.email-trust-btn-cancel')
           .addEventListener('click', closeModal);

      modal.querySelector('.email-trust-btn-once')
           .addEventListener('click', function() {
             restoreImages(iframe);
             if (banner && banner.parentNode) banner.parentNode.removeChild(banner);
             closeModal();
           });

      modal.querySelector('.email-trust-btn-always')
           .addEventListener('click', function() {
             trustSender(senderKey);
             restoreImages(iframe);
             if (banner && banner.parentNode) banner.parentNode.removeChild(banner);
             closeModal();
           });
    }

    // ── Blocking banner ────────────────────────────────────────────────────

    function buildBanner(iframe, senderKey, detectedDomains) {
      var domains = [];
      for (var k in detectedDomains) {
        if (Object.prototype.hasOwnProperty.call(detectedDomains, k)) domains.push(k);
      }
      if (domains.length === 0) return;

      var banner = $doc.createElement('div');
      banner.className = 'email-image-banner';

      var text = $doc.createElement('span');
      text.className = 'email-image-banner-text';
      text.innerHTML = '<i class="fa fa-eye-slash"></i> External images from '
        + domains.length + ' domain' + (domains.length === 1 ? '' : 's') + ' blocked.';

      var btn = $doc.createElement('button');
      btn.className = 'email-image-banner-btn';
      btn.textContent = 'Show images';

      banner.appendChild(text);
      banner.appendChild(btn);

      // Insert banner immediately before the body iframe.
      if (iframe.parentNode) {
        iframe.parentNode.insertBefore(banner, iframe);
      }

      btn.addEventListener('click', function() {
        showTrustModal(domains, senderKey, iframe, banner);
      });
    }

    // ── EML attachment helpers ─────────────────────────────────────────────

    // eml-parse-js encodes attachment content with TextEncoder before storing it,
    // so att.data is a Uint8Array of UTF-8 bytes of the base64 string.
    // att.data64 is the base64 string decoded back via TextDecoder — use that.
    function emlAttBase64(att) {
      if (att.data64 && typeof att.data64 === 'string') return att.data64;
      if (att.data) {
        var d = att.data, s = '', chunk = 32768;
        for (var i = 0; i < d.length; i += chunk) {
          s += String.fromCharCode.apply(null,
            d.subarray ? d.subarray(i, i + chunk)
                       : Array.prototype.slice.call(d, i, i + chunk));
        }
        return s;
      }
      return null;
    }

    function emlAttToObjectUrl(att, mimeType) {
      try {
        var b64 = emlAttBase64(att);
        if (!b64) return null;
        var binary = $wnd.atob(b64);
        var bytes  = new $wnd.Uint8Array(binary.length);
        for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
        var blob   = new $wnd.Blob([bytes], {type: mimeType});
        var u      = $wnd.URL.createObjectURL(blob);
        objUrls.push(u);
        return u;
      } catch (e) { return null; }
    }

    // ── CID resolver ──────────────────────────────────────────────────────

    function resolveCids(html, attachments) {
      if (!html || !attachments || attachments.length === 0) return html;
      return html.replace(/cid:([^"'\s>)]+)/g, function(match, cid) {
        for (var i = 0; i < attachments.length; i++) {
          var att = attachments[i];
          var id  = (att.contentId || att.id || '').replace(/^<|>$/g, '');
          if (id === cid) {
            var mime = ((att.contentType || att.mimeType || 'application/octet-stream')
                         .split(';')[0]).trim();
            var b64 = emlAttBase64(att);
            if (b64) return 'data:' + mime + ';base64,' + b64;
          }
        }
        return match;
      });
    }

    // ── Main render ────────────────────────────────────────────────────────

    function renderParsed(subject, from, to, cc, date, bodyHtml, bodyText,
                          attachments, isEml, senderKey) {
      // Header
      var headerDiv = $doc.createElement('div');
      headerDiv.className = 'email-header';
      headerDiv.innerHTML = buildHeaderField('Subject', subject)
        + buildHeaderField('From',    from)
        + buildHeaderField('To',      to)
        + buildHeaderField('CC',      cc)
        + buildHeaderField('Date',    date);
      container.appendChild(headerDiv);

      var divider = $doc.createElement('hr');
      divider.className = 'email-divider';
      container.appendChild(divider);

      // Body
      var detectedDomains = {};
      var bodyContent;

      if (bodyHtml) {
        var resolvedHtml = isEml ? resolveCids(bodyHtml, attachments) : bodyHtml;
        bodyContent = sanitizeBody(resolvedHtml, senderKey, detectedDomains);
      } else if (bodyText) {
        bodyContent = '<pre style="white-space:pre-wrap;font-family:monospace;'
          + 'font-size:13px;margin:0;">' + escapeHtml(bodyText) + '</pre>';
      } else {
        bodyContent = '<p style="color:#999;font-style:italic;">(no body)</p>';
      }

      var iframe = buildBodyIframe(bodyContent);
      container.appendChild(iframe);

      // Banner appears between the header block and the iframe (inserted before
      // iframe by buildBanner), so append iframe first then let buildBanner
      // splice the banner in.
      buildBanner(iframe, senderKey, detectedDomains);

      // Attachments
      if (attachments && attachments.length > 0) {
        var attDiv = $doc.createElement('div');
        attDiv.className = 'email-attachments';
        var attHtml = '<h4 class="email-attachments-title">Attachments ('
          + attachments.length + ')</h4>'
          + '<ul class="email-attachments-list">';
        for (var i = 0; i < attachments.length; i++) {
          var att    = attachments[i];
          var name   = att.fileName || att.name || 'attachment';
          var size   = att.contentLength || att.size || 0;
          attHtml += '<li><i class="fa fa-paperclip"></i> ';
          var attUrl = att._objUrl || null;
          if (!attUrl && isEml && (att.data || att.data64)) {
            var mime = ((att.contentType || att.mimeType || 'application/octet-stream')
                         .split(';')[0]).trim();
            attUrl = emlAttToObjectUrl(att, mime);
          }
          if (attUrl) {
            attHtml += '<a href="' + attUrl + '" download="' + escapeHtml(name) + '">'
              + escapeHtml(name) + '</a>';
          } else {
            attHtml += escapeHtml(name);
          }
          if (size) {
            attHtml += ' <span class="email-attachment-size">('
              + formatSize(size) + ')</span>';
          }
          attHtml += '</li>';
        }
        attHtml += '</ul>';
        attDiv.innerHTML = attHtml;
        container.appendChild(attDiv);
      }
    }

    // ── MSG branch ─────────────────────────────────────────────────────────

    if (fmt === 'msg') {
      var ReaderCls = $wnd.MSGReader;
      if (!ReaderCls) {
        onError('MSGReader not loaded — ensure the Maven build ran the '
          + 'bundle-msgreader-for-browser step.');
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
        if (xhr.status !== 200) { onError('HTTP ' + xhr.status); return; }
        try {
          var reader = new ReaderCls(xhr.response);
          var msg    = reader.getFileData();

          var toList = [], ccList = [];
          if (msg.recipients) {
            for (var i = 0; i < msg.recipients.length; i++) {
              var r    = msg.recipients[i];
              var addr = r.name
                ? r.name + (r.email ? ' <' + r.email + '>' : '')
                : (r.email || '');
              if (r.recipType === 'cc' || r.recipType === 'CC') ccList.push(addr);
              else toList.push(addr);
            }
          }

          var from = msg.senderName
            ? msg.senderName + (msg.senderEmail ? ' <' + msg.senderEmail + '>' : '')
            : (msg.senderEmail || '');

          var date = msg.date
            ? new $wnd.Date(msg.date).toLocaleString()
            : (msg.creationTime || '');

          // Pre-fetch attachment binary content from the MSG file.
          // reader.getAttachment(att) returns { fileName, content: Uint8Array }.
          var rawAtts = msg.attachments || [], processedAtts = [];
          for (var ai = 0; ai < rawAtts.length; ai++) {
            var rawAtt = rawAtts[ai];
            var pAtt = {
              fileName: rawAtt.fileName || rawAtt.name || 'attachment',
              name:     rawAtt.fileName || rawAtt.name || 'attachment',
              size:     rawAtt.size || 0,
              _objUrl:  null
            };
            try {
              var attData = reader.getAttachment(rawAtt);
              if (attData && attData.content && attData.content.length > 0) {
                var attBlob = new $wnd.Blob([attData.content],
                  {type: 'application/octet-stream'});
                pAtt._objUrl = $wnd.URL.createObjectURL(attBlob);
                objUrls.push(pAtt._objUrl);
              }
            } catch (attErr) { // leave _objUrl null
            }
            processedAtts.push(pAtt);
          }

          var senderKey = msg.senderEmail
            ? msg.senderEmail.toLowerCase()
            : extractEmail(from);

          renderParsed(
            msg.subject   || '',
            from,
            toList.join(', '),
            ccList.join(', '),
            date,
            msg.bodyHTML  || null,
            msg.body      || null,
            processedAtts,
            false,
            senderKey
          );
        } catch (e) { onError(e.message || 'parse error'); }
      };

      xhr.onerror = function() { onError('network error'); };
      xhr.send();

    // ── EML branch ─────────────────────────────────────────────────────────

    } else {
      var emlLib = $wnd.EmlParseJs;
      if (!emlLib) {
        onError('EmlParseJs not loaded — ensure js/emlparser/EmlParseJs.js is '
          + 'included (built by Maven frontend-maven-plugin).');
        return;
      }

      var xhrEml = new $wnd.XMLHttpRequest();
      xhrEml.open('GET', url, true);
      xhrEml.responseType = 'text';

      xhrEml.onload = function() {
        if (xhrEml.status !== 200) { onError('HTTP ' + xhrEml.status); return; }
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
              if (typeof field === 'object' && !Array.isArray(field)) {
                return field.name
                  ? field.name + (field.email ? ' <' + field.email + '>' : '')
                  : (field.email || '');
              }
              if (Array.isArray(field)) {
                return field.map(function(f) {
                  if (typeof f === 'string') return f;
                  return f.name
                    ? f.name + (f.email ? ' <' + f.email + '>' : '')
                    : (f.email || String(f));
                }).join(', ');
              }
              return String(field);
            }

            // Extract raw sender email for trust comparisons.
            var senderKey = '';
            if (data.from) {
              if (typeof data.from === 'object' && !Array.isArray(data.from)
                  && data.from.email) {
                senderKey = data.from.email.toLowerCase().trim();
              } else if (Array.isArray(data.from) && data.from.length > 0
                         && data.from[0] && data.from[0].email) {
                senderKey = data.from[0].email.toLowerCase().trim();
              } else {
                senderKey = extractEmail(flattenAddrs(data.from));
              }
            }

            var date = '';
            if (data.date) {
              date = (data.date instanceof $wnd.Date)
                ? data.date.toLocaleString()
                : String(data.date);
            }

            renderParsed(
              data.subject      || '',
              flattenAddrs(data.from),
              flattenAddrs(data.to),
              flattenAddrs(data.cc),
              date,
              data.html         || null,
              data.text         || null,
              data.attachments  || [],
              true,
              senderKey
            );
          });
        } catch (e) { onError(e.message || 'parse error'); }
      };

      xhrEml.onerror = function() { onError('network error'); };
      xhrEml.send();
    }
  }-*/;
}
