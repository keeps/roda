/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * GWT widget that renders a (potentially multi-page) TIFF image onto a series
 * of HTML5 canvas elements using the UTIF.js library (exposed on the page as
 * {@code $wnd.UTIF}). Each IFD (page) in the TIFF file is rendered as an
 * individual canvas stacked vertically, matching the PDF-style layout used by
 * the PDF.js viewer.
 *
 * <p>
 * The file is fetched via XHR with {@code responseType = 'arraybuffer'} so
 * that the raw binary is available for UTIF to decode without server-side
 * conversion. Guards are applied against oversized images and excessive page
 * counts to prevent memory-exhaustion denial-of-service from malformed files.
 * </p>
 */
public class TiffViewer extends Composite {

  /**
   * Hard limit on the number of IFD pages rendered. Archival TIFFs rarely
   * exceed a few hundred pages; capping at 500 prevents DoS from pathological
   * files with thousands of IFDs.
   */
  private static final int MAX_PAGES = 500;

  /**
   * Hard limit on a single image dimension (width or height) in pixels. At
   * 16 384 px per side the RGBA buffer is exactly 1 GiB; images beyond this
   * threshold are skipped with an error label rather than crashing the browser
   * tab.
   */
  private static final int MAX_DIMENSION_PX = 16384;

  private final FlowPanel panel;

  public TiffViewer(SafeUri downloadUri) {
    panel = new FlowPanel();
    initWidget(panel);
    setStyleName("viewRepresentationTiffFilePreview");

    panel.addAttachHandler(event -> {
      if (event.isAttached()) {
        renderTiff(panel.getElement(), downloadUri.asString(), MAX_PAGES, MAX_DIMENSION_PX);
      }
    });
  }

  /**
   * Fetches the TIFF at {@code url} and renders every page onto its own
   * {@code <canvas>} element appended to {@code container}.
   *
   * <p>
   * Multi-page layout:
   * <ul>
   * <li>Each canvas is preceded by a {@code <div class="tiffPageLabel">}
   * showing "page / total" when the file has more than one page.</li>
   * <li>All canvases have {@code max-width:100%} so they scale down inside
   * any panel without horizontal overflow.</li>
   * </ul>
   * </p>
   *
   * <p>
   * Safety guards:
   * <ul>
   * <li>Total pages rendered is capped at {@code maxPages}.</li>
   * <li>Any single page with a dimension exceeding {@code maxDimPx} is
   * replaced by an error label and skipped.</li>
   * <li>All UTIF calls are wrapped in a try/catch; any decode or canvas
   * error replaces the container content with a plain error paragraph.</li>
   * </ul>
   * </p>
   */
  private static native void renderTiff(Element container, String url, int maxPages, int maxDimPx) /*-{
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'arraybuffer';

    xhr.onload = function() {
      if (xhr.status !== 200) {
        container.innerHTML =
          '<p class="errormessage">Unable to load TIFF image (HTTP ' + xhr.status + ').</p>';
        return;
      }

      try {
        var buffer  = xhr.response;
        var ifds    = $wnd.UTIF.decode(buffer);

        if (!ifds || ifds.length === 0) {
          container.innerHTML =
            '<p class="errormessage">Unable to decode TIFF: no image pages found.</p>';
          return;
        }

        var totalPages  = ifds.length;
        var renderPages = Math.min(totalPages, maxPages);
        var multiPage   = totalPages > 1;

        container.innerHTML = '';

        for (var i = 0; i < renderPages; i++) {
          $wnd.UTIF.decodeImage(buffer, ifds[i]);

          var w = ifds[i].width;
          var h = ifds[i].height;

          if (!w || !h || w > maxDimPx || h > maxDimPx) {
            var errDiv = $doc.createElement('div');
            errDiv.className = 'errormessage';
            errDiv.textContent =
              'Page ' + (i + 1) + ': dimensions not supported (' + w + '\u00d7' + h + ' px).';
            container.appendChild(errDiv);
            continue;
          }

          if (multiPage) {
            var label = $doc.createElement('div');
            label.className = 'tiffPageLabel';
            label.style.textAlign  = 'center';
            label.style.padding    = '4px 0';
            label.style.color      = '#666';
            label.style.fontSize   = '0.85em';
            label.textContent = (i + 1) + ' / ' + totalPages;
            container.appendChild(label);
          }

          var rgba      = $wnd.UTIF.toRGBA8(ifds[i]);
          var canvas    = $doc.createElement('canvas');
          canvas.width  = w;
          canvas.height = h;
          canvas.style.maxWidth  = '100%';
          canvas.style.display   = 'block';
          canvas.style.margin    = multiPage ? '0 auto 16px auto' : '0 auto';

          var ctx       = canvas.getContext('2d');
          var imageData = ctx.createImageData(w, h);
          imageData.data.set(rgba);
          ctx.putImageData(imageData, 0, 0);

          container.appendChild(canvas);
        }

        if (totalPages > maxPages) {
          var note = $doc.createElement('p');
          note.className = 'errormessage';
          note.textContent =
            'Showing first ' + maxPages + ' of ' + totalPages + ' pages.';
          container.appendChild(note);
        }
      } catch (e) {
        container.innerHTML =
          '<p class="errormessage">Unable to render TIFF image: ' + e.message + '</p>';
      }
    };

    xhr.onerror = function() {
      container.innerHTML =
        '<p class="errormessage">Unable to load TIFF image.</p>';
    };

    xhr.send();
  }-*/;
}
