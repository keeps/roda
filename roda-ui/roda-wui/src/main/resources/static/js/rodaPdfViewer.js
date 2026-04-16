/**
 * RODA PDF Viewer
 *
 * Manual canvas + TextLayer rendering using only build/pdf.mjs (no PDFViewer
 * component).  Pages flow in normal document order so the browser's own
 * window scroll moves through them; the toolbar and search bar are sticky.
 * In fullscreen the viewer element becomes the scroll container instead.
 *
 * Search highlighting is done directly in the DOM text layers (no PDFFindController).
 * All styles are scoped inside .rodaPdfViewer in main.gss — pdf_viewer.css is
 * NOT loaded globally, which avoids the :root variable pollution.
 */
(function (global) {
  'use strict';

  /* Map<HTMLElement, ViewerState> */
  var viewers = new Map();
  var _lib = null;   /* cached build/pdf.mjs module */

  /* ------------------------------------------------------------------ */
  /* Library loading                                                      */
  /* ------------------------------------------------------------------ */

  async function getLib(baseUrl) {
    if (_lib) return _lib;
    _lib = await import(baseUrl + 'webjars/pdfjs-dist/build/pdf.mjs');
    _lib.GlobalWorkerOptions.workerSrc =
      baseUrl + 'webjars/pdfjs-dist/build/pdf.worker.mjs';
    return _lib;
  }

  /* ------------------------------------------------------------------ */
  /* State factory                                                        */
  /* ------------------------------------------------------------------ */

  function newState(viewerEl, fileUrl, baseUrl) {
    return {
      viewerEl:            viewerEl,
      fileUrl:             fileUrl,
      baseUrl:             baseUrl,
      /* document */
      pdfDoc:              null,
      scale:               1.5,
      rotation:            0,
      currentPage:         1,
      totalPages:          0,
      /* lazy rendering */
      renderTasks:         new Map(),   /* pageNum → RenderTask */
      renderedPages:       new Set(),
      pageEls:             [],          /* index = pageNum - 1 */
      pageObserver:        null,
      /* search */
      searchQuery:         null,
      searchMarks:         [],          /* [{pageNum, el}] */
      searchIndex:         -1,
      searchDebounceTimer: null,
      /* sidebar */
      sidebarMode:         null,
      thumbEls:            new Map(),
      thumbRenderTasks:    new Map(),
      thumbObserver:       null,
      /* presentation mode */
      presentationMode:    false,
      keyHandler:          null,
      /* fullscreen */
      fsChangeHandler:     null,
      fullscreenBtnEl:     null,
      /* DOM refs */
      toolbarEl:           null,
      pagesEl:             null,
      sidebarEl:           null,
      sidebarContentEl:    null,
      pageInputEl:         null,
      pageCountEl:         null,
      searchBarEl:         null,
      searchInputEl:       null,
      searchCountEl:       null,
      scrollHandler:       null,
    };
  }

  /* ------------------------------------------------------------------ */
  /* DOM helpers                                                          */
  /* ------------------------------------------------------------------ */

  function mk(tag, cls) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    return e;
  }

  function icon(faClass) {
    var i = document.createElement('i');
    i.className = 'fa ' + faClass;
    i.setAttribute('aria-hidden', 'true');
    return i;
  }

  function toolbarBtn(faClass, title, onClick) {
    var btn = mk('button', 'rodaPdfBtn');
    btn.type = 'button';
    btn.title = title;
    btn.appendChild(icon(faClass));
    btn.addEventListener('click', onClick);
    return btn;
  }

  /* ------------------------------------------------------------------ */
  /* DOM construction                                                     */
  /* ------------------------------------------------------------------ */

  function buildDOM(state) {
    var viewer = state.viewerEl;

    /* Loading indicator (removed once doc loads) */
    var loading = mk('div', 'rodaPdfLoading');
    loading.textContent = 'Loading document\u2026';
    viewer.appendChild(loading);

    /* ---- Toolbar ---- */
    var toolbar = mk('div', 'rodaPdfToolbar');

    /* Group 1: sidebar toggles */
    var g1 = mk('div', 'rodaPdfBtnGroup');
    g1.appendChild(toolbarBtn('fa-th-large', 'Show thumbnails',
      function () { toggleSidebar(state, 'thumbs'); }));
    g1.appendChild(toolbarBtn('fa-list', 'Show outline / index',
      function () { toggleSidebar(state, 'outline'); }));

    /* Group 2: page navigation */
    var g2 = mk('div', 'rodaPdfBtnGroup');
    var pageInput = mk('input', 'rodaPdfPageInput');
    pageInput.type  = 'text';
    pageInput.title = 'Page number';
    pageInput.addEventListener('change', function () {
      var n = parseInt(pageInput.value, 10);
      if (!isNaN(n)) goToPage(state, n);
    });
    var pageCount = mk('span', 'rodaPdfPageCount');
    g2.appendChild(toolbarBtn('fa-chevron-left',  'Previous page', function () { prevPage(state); }));
    g2.appendChild(pageInput);
    g2.appendChild(pageCount);
    g2.appendChild(toolbarBtn('fa-chevron-right', 'Next page',     function () { nextPage(state); }));

    /* Group 3: search (before zoom) */
    var g3 = mk('div', 'rodaPdfBtnGroup');
    g3.appendChild(toolbarBtn('fa-search', 'Search in document',
      function () { toggleSearch(state); }));

    /* Group 4: zoom */
    var g4 = mk('div', 'rodaPdfBtnGroup');
    g4.appendChild(toolbarBtn('fa-search-minus', 'Zoom out',     function () { zoomOut(state); }));
    g4.appendChild(toolbarBtn('fa-search-plus',  'Zoom in',      function () { zoomIn(state); }));
    g4.appendChild(toolbarBtn('fa-arrows-h',     'Fit to width', function () { fitWidth(state); }));

    /* Group 5: rotate */
    var g5 = mk('div', 'rodaPdfBtnGroup');
    g5.appendChild(toolbarBtn('fa-undo',   'Rotate left (CCW)',  function () { rotateCCW(state); }));
    g5.appendChild(toolbarBtn('fa-repeat', 'Rotate right (CW)',  function () { rotateCW(state); }));

    /* Group 6: view modes (right-aligned) */
    var g6 = mk('div', 'rodaPdfBtnGroup rodaPdfBtnGroupRight');
    g6.appendChild(toolbarBtn('fa-play-circle', 'Presentation mode',
      function () { togglePresentation(state); }));
    var fsBtn = toolbarBtn('fa-expand', 'Fullscreen', function () {
      if (document.fullscreenElement === state.viewerEl) {
        document.exitFullscreen().catch(function () {});
      } else {
        requestFullscreen(state);
      }
    });
    state.fullscreenBtnEl = fsBtn;
    g6.appendChild(fsBtn);

    function sep() { return mk('span', 'rodaPdfSep'); }
    toolbar.appendChild(g1);  toolbar.appendChild(sep());
    toolbar.appendChild(g2);  toolbar.appendChild(sep());
    toolbar.appendChild(g3);  toolbar.appendChild(sep());
    toolbar.appendChild(g4);  toolbar.appendChild(sep());
    toolbar.appendChild(g5);
    toolbar.appendChild(g6);

    /* ---- Search bar (hidden by default) ---- */
    var searchBar   = mk('div', 'rodaPdfSearchBar rodaPdfSearchBarHidden');
    var searchInput = mk('input', 'rodaPdfSearchInput');
    searchInput.type        = 'text';
    searchInput.placeholder = 'Search\u2026';
    searchInput.setAttribute('aria-label', 'Search document');

    var searchCount  = mk('span', 'rodaPdfSearchCount');
    var prevMatchBtn = mk('button', 'rodaPdfBtn');
    prevMatchBtn.type = 'button'; prevMatchBtn.title = 'Previous match';
    prevMatchBtn.appendChild(icon('fa-chevron-up'));
    prevMatchBtn.addEventListener('click', function () { prevSearchMatch(state); });

    var nextMatchBtn = mk('button', 'rodaPdfBtn');
    nextMatchBtn.type = 'button'; nextMatchBtn.title = 'Next match';
    nextMatchBtn.appendChild(icon('fa-chevron-down'));
    nextMatchBtn.addEventListener('click', function () { nextSearchMatch(state); });

    var closeSearchBtn = mk('button', 'rodaPdfBtn');
    closeSearchBtn.type = 'button'; closeSearchBtn.title = 'Close search';
    closeSearchBtn.appendChild(icon('fa-times'));
    closeSearchBtn.addEventListener('click', function () { closeSearch(state); });

    searchBar.appendChild(searchInput);
    searchBar.appendChild(prevMatchBtn);
    searchBar.appendChild(nextMatchBtn);
    searchBar.appendChild(searchCount);
    searchBar.appendChild(closeSearchBtn);

    searchInput.addEventListener('input', function () {
      clearTimeout(state.searchDebounceTimer);
      state.searchDebounceTimer = setTimeout(function () { performSearch(state); }, 350);
    });
    searchInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        clearTimeout(state.searchDebounceTimer);
        if (state.searchQuery) {
          e.shiftKey ? prevSearchMatch(state) : nextSearchMatch(state);
        } else {
          performSearch(state);
        }
      } else if (e.key === 'Escape') {
        closeSearch(state);
      }
    });

    /* ---- Content area: sidebar + pages ---- */
    var content        = mk('div', 'rodaPdfContent');
    var sidebar        = mk('div', 'rodaPdfSidebar');
    var sidebarContent = mk('div', 'rodaPdfSidebarContent');
    sidebar.appendChild(sidebarContent);
    var pagesEl = mk('div', 'rodaPdfPages');
    content.appendChild(sidebar);
    content.appendChild(pagesEl);

    viewer.appendChild(toolbar);
    viewer.appendChild(searchBar);
    viewer.appendChild(content);

    /* Store refs */
    state.toolbarEl        = toolbar;
    state.pagesEl          = pagesEl;
    state.sidebarEl        = sidebar;
    state.sidebarContentEl = sidebarContent;
    state.pageInputEl      = pageInput;
    state.pageCountEl      = pageCount;
    state.searchBarEl      = searchBar;
    state.searchInputEl    = searchInput;
    state.searchCountEl    = searchCount;
  }

  /* ------------------------------------------------------------------ */
  /* Page placeholders + lazy observer                                   */
  /* ------------------------------------------------------------------ */

  async function buildPageList(state) {
    /* Compute fit-width scale now that DOM is laid out */
    var availW = state.pagesEl.clientWidth || state.viewerEl.clientWidth || 800;
    var firstPage = await state.pdfDoc.getPage(1);
    var vp1 = firstPage.getViewport({ scale: 1, rotation: state.rotation });
    state.scale = Math.max(0.25, (availW - 20) / vp1.width);

    var vp = firstPage.getViewport({ scale: state.scale, rotation: state.rotation });
    var w  = Math.floor(vp.width);
    var h  = Math.floor(vp.height);

    state.pageEls = [];
    for (var i = 1; i <= state.totalPages; i++) {
      var pageEl = mk('div', 'rodaPdfPage');
      pageEl.dataset.pageNum = i;
      pageEl.style.width  = w + 'px';
      pageEl.style.height = h + 'px';
      state.pagesEl.appendChild(pageEl);
      state.pageEls.push(pageEl);
    }

    setupPageObserver(state);
  }

  function setupPageObserver(state) {
    if (state.pageObserver) {
      state.pageObserver.disconnect();
      state.pageObserver = null;
    }
    /* root:null = viewport; use viewerEl as root when fullscreen */
    var root = (document.fullscreenElement === state.viewerEl)
      ? state.viewerEl : null;

    state.pageObserver = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        var num = parseInt(entry.target.dataset.pageNum, 10);
        if (!state.renderedPages.has(num)) renderPage(state, num);
      });
    }, { root: root, rootMargin: '300px 0px', threshold: 0 });

    state.pageEls.forEach(function (el) { state.pageObserver.observe(el); });
  }

  /* ------------------------------------------------------------------ */
  /* Page rendering (canvas + text layer)                                */
  /* ------------------------------------------------------------------ */

  async function renderPage(state, num) {
    var pageEl = state.pageEls[num - 1];
    if (!pageEl || !state.pdfDoc) return;

    /* Cancel in-progress render for this slot */
    var prev = state.renderTasks.get(num);
    if (prev) { try { prev.cancel(); } catch (e) {} state.renderTasks.delete(num); }

    pageEl.innerHTML = '';
    state.renderedPages.delete(num);

    var lib, page;
    try {
      lib  = await getLib(state.baseUrl);
      page = await state.pdfDoc.getPage(num);
    } catch (e) { return; }

    var viewport = page.getViewport({ scale: state.scale, rotation: state.rotation });
    var w = Math.floor(viewport.width);
    var h = Math.floor(viewport.height);

    /* Correct placeholder dimensions (may differ from estimate if page sizes vary) */
    pageEl.style.width  = w + 'px';
    pageEl.style.height = h + 'px';

    /* High-DPI canvas */
    var dpr    = window.devicePixelRatio || 1;
    var canvas = mk('canvas', 'rodaPdfCanvas');
    canvas.width        = Math.floor(w * dpr);
    canvas.height       = Math.floor(h * dpr);
    canvas.style.width  = w + 'px';
    canvas.style.height = h + 'px';
    pageEl.appendChild(canvas);

    var ctx = canvas.getContext('2d');
    ctx.scale(dpr, dpr);

    var renderTask = page.render({ canvasContext: ctx, viewport: viewport });
    state.renderTasks.set(num, renderTask);

    try {
      await renderTask.promise;
    } catch (e) {
      if (e && e.name === 'RenderingCancelledException') return;
      return;
    }
    state.renderTasks.delete(num);

    /* Text layer */
    var textDiv = mk('div', 'rodaPdfTextLayer');
    textDiv.style.width  = w + 'px';
    textDiv.style.height = h + 'px';
    /* --total-scale-factor is consumed by the span transform formula in our CSS.
       Since left/top are already percent of the viewport, 1 is correct here. */
    textDiv.style.setProperty('--total-scale-factor', '1');
    pageEl.appendChild(textDiv);

    try {
      await buildTextLayer(lib, page, viewport, textDiv);
    } catch (e) { /* non-fatal — canvas still works */ }

    state.renderedPages.add(num);

    /* Apply active search highlights to the freshly rendered text layer */
    if (state.searchQuery) {
      var newMarks = applyHighlightsToLayer(state.searchQuery, textDiv, pageEl);
      newMarks.forEach(function (el) {
        insertMarkSorted(state.searchMarks, num, el);
      });
      updateSearchCount(state);
    }
  }

  async function buildTextLayer(lib, page, viewport, container) {
    /* pdf.js v5 uses a class-based TextLayer API */
    if (lib.TextLayer) {
      var tl = new lib.TextLayer({
        textContentSource: page.streamTextContent(),
        container:         container,
        viewport:          viewport,
      });
      await tl.render();
    } else if (lib.renderTextLayer) {
      /* fallback for older builds */
      var textContent = await page.getTextContent();
      var task = lib.renderTextLayer({
        textContentSource: textContent,
        container:         container,
        viewport:          viewport,
      });
      await task.promise;
    }
  }

  /* ------------------------------------------------------------------ */
  /* Re-render all pages (on scale / rotation change)                   */
  /* ------------------------------------------------------------------ */

  function rerenderAll(state) {
    if (!state.pdfDoc) return;
    var savedPage = state.currentPage;

    /* Cancel in-progress renders */
    state.renderTasks.forEach(function (t) { try { t.cancel(); } catch (e) {} });
    state.renderTasks.clear();
    state.renderedPages.clear();
    state.searchMarks = [];
    state.searchIndex = -1;

    /* Clear page content; update placeholder heights from page 1 estimate */
    state.pdfDoc.getPage(1).then(function (page) {
      var vp = page.getViewport({ scale: state.scale, rotation: state.rotation });
      var w  = Math.floor(vp.width);
      var h  = Math.floor(vp.height);
      state.pageEls.forEach(function (el) {
        el.innerHTML      = '';
        el.style.width    = w + 'px';
        el.style.height   = h + 'px';
      });
      setupPageObserver(state);
      /* Scroll back to the same page after layout settles */
      requestAnimationFrame(function () { scrollToPage(state, savedPage); });
    });
  }

  /* ------------------------------------------------------------------ */
  /* Navigation                                                           */
  /* ------------------------------------------------------------------ */

  function scrollToPage(state, n) {
    var pageEl = state.pageEls[n - 1];
    if (!pageEl) return;

    var toolbarH = state.toolbarEl ? state.toolbarEl.offsetHeight : 40;
    var searchH  = (state.searchBarEl &&
                    !state.searchBarEl.classList.contains('rodaPdfSearchBarHidden'))
                   ? (state.searchBarEl.offsetHeight || 36) : 0;
    var overhead = toolbarH + searchH + 8;

    if (document.fullscreenElement === state.viewerEl) {
      /* scroll inside the fullscreen element */
      state.viewerEl.scrollTo({ top: pageEl.offsetTop - overhead, behavior: 'smooth' });
    } else {
      var absTop = pageEl.getBoundingClientRect().top + window.scrollY - overhead;
      window.scrollTo({ top: absTop, behavior: 'smooth' });
    }
  }

  function goToPage(state, n) {
    if (!state.pdfDoc) return;
    n = Math.max(1, Math.min(n, state.totalPages));
    state.currentPage = n;
    updatePageDisplay(state);
    if (state.presentationMode) {
      renderPresentationPage(state, n);
    } else {
      scrollToPage(state, n);
    }
  }

  function prevPage(state) { goToPage(state, state.currentPage - 1); }
  function nextPage(state) { goToPage(state, state.currentPage + 1); }

  function updatePageDisplay(state) {
    if (state.pageInputEl) state.pageInputEl.value = state.currentPage;
    if (state.pageCountEl) state.pageCountEl.textContent = '\u2044 ' + state.totalPages;
    state.thumbEls.forEach(function (el, n) {
      el.classList.toggle('rodaPdfThumbActive', n === state.currentPage);
    });
  }

  /* ------------------------------------------------------------------ */
  /* Scroll tracking — update currentPage indicator as user scrolls     */
  /* ------------------------------------------------------------------ */

  function setupScrollTracking(state) {
    var ticking = false;
    state.scrollHandler = function () {
      if (!ticking) {
        requestAnimationFrame(function () { trackCurrentPage(state); ticking = false; });
        ticking = true;
      }
    };
    /* Also listen on the viewer element when in fullscreen */
    window.addEventListener('scroll', state.scrollHandler, { passive: true });
    state.viewerEl.addEventListener('scroll', state.scrollHandler, { passive: true });
  }

  function trackCurrentPage(state) {
    var toolbarH = state.toolbarEl ? state.toolbarEl.offsetHeight : 40;
    var best = 1, bestScore = -Infinity;
    state.pageEls.forEach(function (el, idx) {
      var rect = el.getBoundingClientRect();
      var vis = Math.min(rect.bottom, window.innerHeight) - Math.max(rect.top, toolbarH);
      if (vis > bestScore) { bestScore = vis; best = idx + 1; }
    });
    if (best !== state.currentPage) {
      state.currentPage = best;
      updatePageDisplay(state);
    }
  }

  /* ------------------------------------------------------------------ */
  /* Zoom                                                                 */
  /* ------------------------------------------------------------------ */

  function zoomIn(state) {
    state.scale = Math.min(state.scale * 1.25, 5.0);
    rerenderAll(state);
  }

  function zoomOut(state) {
    state.scale = Math.max(state.scale / 1.25, 0.25);
    rerenderAll(state);
  }

  function fitWidth(state) {
    if (!state.pdfDoc) return;
    var availW = state.pagesEl ? state.pagesEl.clientWidth : state.viewerEl.clientWidth;
    if (!availW) return;
    state.pdfDoc.getPage(1).then(function (page) {
      var vp1 = page.getViewport({ scale: 1, rotation: state.rotation });
      state.scale = Math.max(0.25, (availW - 20) / vp1.width);
      rerenderAll(state);
    });
  }

  /* ------------------------------------------------------------------ */
  /* Rotation                                                             */
  /* ------------------------------------------------------------------ */

  function rotateCW(state)  { state.rotation = (state.rotation + 90)  % 360; rerenderAll(state); }
  function rotateCCW(state) { state.rotation = (state.rotation + 270) % 360; rerenderAll(state); }

  /* ------------------------------------------------------------------ */
  /* Search                                                               */
  /* ------------------------------------------------------------------ */

  function toggleSearch(state) {
    if (!state.searchBarEl) return;
    if (state.searchBarEl.classList.contains('rodaPdfSearchBarHidden')) {
      state.searchBarEl.classList.remove('rodaPdfSearchBarHidden');
      if (state.searchInputEl) state.searchInputEl.focus();
    } else {
      closeSearch(state);
    }
  }

  function closeSearch(state) {
    clearHighlights(state);
    state.searchQuery = null;
    if (state.searchInputEl) state.searchInputEl.value = '';
    updateSearchCount(state);
    if (state.searchBarEl) state.searchBarEl.classList.add('rodaPdfSearchBarHidden');
  }

  function escapeRegex(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  function escapeHtml(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  function performSearch(state) {
    clearHighlights(state);
    var query = state.searchInputEl ? state.searchInputEl.value.trim() : '';
    state.searchQuery = query || null;
    state.searchMarks = [];
    state.searchIndex = -1;

    if (!query) { updateSearchCount(state); return; }

    /* Highlight all rendered text layers */
    state.pageEls.forEach(function (pageEl, idx) {
      var pn = idx + 1;
      var tl = pageEl.querySelector('.rodaPdfTextLayer');
      if (!tl) return;
      applyHighlightsToLayer(query, tl, pageEl).forEach(function (el) {
        state.searchMarks.push({ pageNum: pn, el: el });
      });
    });

    updateSearchCount(state);
    if (state.searchMarks.length > 0) {
      state.searchIndex = 0;
      focusSearchMatch(state);
    }
  }

  /* Returns the first text node inside el (depth-first) */
  function firstTextNode(el) {
    for (var c = el.firstChild; c; c = c.nextSibling) {
      if (c.nodeType === 3 && c.textContent.length > 0) return c;
      if (c.nodeType === 1) { var n = firstTextNode(c); if (n) return n; }
    }
    return null;
  }

  /*
   * Highlight all occurrences of query inside a rendered text layer.
   *
   * Instead of modifying span.innerHTML (which disturbs the scaleX transforms
   * that PDF.js sets on each span), we use the Range API to locate the exact
   * rendered rectangle of each match and overlay a positioned <div> on top of
   * the page element.  The page element is the positioning context
   * (position:relative) so the divs stay put as the user scrolls.
   *
   * Returns an array of highlight div elements created.
   */
  function applyHighlightsToLayer(query, textLayerEl, pageEl) {
    var marks    = [];
    var regex    = new RegExp(escapeRegex(query), 'gi');
    var pageRect = pageEl.getBoundingClientRect();
    var spans    = textLayerEl.querySelectorAll('span');

    spans.forEach(function (span) {
      var text = span.textContent;
      if (!text) return;
      regex.lastIndex = 0;
      if (!regex.test(text)) return;

      var textNode = firstTextNode(span);
      regex.lastIndex = 0;
      var m;
      while ((m = regex.exec(text)) !== null) {
        if (m[0].length === 0) { regex.lastIndex++; continue; }

        var rects = [];
        if (textNode && textNode.textContent === text) {
          /* Exact match: use Range to get sub-string pixel rects */
          try {
            var range = document.createRange();
            range.setStart(textNode, m.index);
            range.setEnd(textNode, m.index + m[0].length);
            rects = Array.from(range.getClientRects());
          } catch (e) { /* fall through to span-rect fallback */ }
        }
        if (!rects.length) {
          /* Fallback: highlight the entire span */
          rects = [span.getBoundingClientRect()];
        }

        rects.forEach(function (rect) {
          if (rect.width < 1 && rect.height < 1) return;
          var hl = mk('div', 'rodaPdfHighlight');
          hl.style.left   = (rect.left   - pageRect.left) + 'px';
          hl.style.top    = (rect.top    - pageRect.top)  + 'px';
          hl.style.width  = rect.width   + 'px';
          hl.style.height = rect.height  + 'px';
          pageEl.appendChild(hl);
          marks.push(hl);
        });
      }
    });
    return marks;
  }

  /* Insert a mark into state.searchMarks in page-number order */
  function insertMarkSorted(marks, pageNum, el) {
    var insertAt = marks.length;
    for (var i = marks.length - 1; i >= 0; i--) {
      if (marks[i].pageNum <= pageNum) { insertAt = i + 1; break; }
      if (i === 0) insertAt = 0;
    }
    marks.splice(insertAt, 0, { pageNum: pageNum, el: el });
  }

  function clearHighlights(state) {
    if (!state.viewerEl) return;
    state.viewerEl.querySelectorAll('div.rodaPdfHighlight').forEach(function (el) {
      el.remove();
    });
    state.searchMarks = [];
    state.searchIndex = -1;
  }

  function nextSearchMatch(state) {
    if (!state.searchQuery) { performSearch(state); return; }
    if (!state.searchMarks.length) return;
    state.searchIndex = (state.searchIndex + 1) % state.searchMarks.length;
    focusSearchMatch(state);
  }

  function prevSearchMatch(state) {
    if (!state.searchQuery) { performSearch(state); return; }
    if (!state.searchMarks.length) return;
    state.searchIndex = (state.searchIndex - 1 + state.searchMarks.length) % state.searchMarks.length;
    focusSearchMatch(state);
  }

  function focusSearchMatch(state) {
    if (state.searchIndex < 0 || state.searchIndex >= state.searchMarks.length) return;
    var match = state.searchMarks[state.searchIndex];

    /* Deselect all, select current */
    state.searchMarks.forEach(function (m) {
      m.el.classList.remove('rodaPdfHighlightSelected');
    });
    match.el.classList.add('rodaPdfHighlightSelected');

    /* Scroll so the selected highlight is visible */
    var rect     = match.el.getBoundingClientRect();
    var toolbarH = state.toolbarEl ? state.toolbarEl.offsetHeight : 40;
    var searchH  = (state.searchBarEl &&
                    !state.searchBarEl.classList.contains('rodaPdfSearchBarHidden'))
                   ? (state.searchBarEl.offsetHeight || 36) : 0;
    var overhead = toolbarH + searchH;
    if (rect.top < overhead + 20 || rect.bottom > window.innerHeight - 20) {
      /* Scroll the highlight element into view with header offset */
      var absTop = rect.top + (
        document.fullscreenElement === state.viewerEl
          ? state.viewerEl.scrollTop
          : window.scrollY
      ) - overhead - 40;
      if (document.fullscreenElement === state.viewerEl) {
        state.viewerEl.scrollTo({ top: absTop, behavior: 'smooth' });
      } else {
        window.scrollTo({ top: absTop, behavior: 'smooth' });
      }
    }
    updateSearchCount(state);
  }

  function updateSearchCount(state) {
    if (!state.searchCountEl) return;
    if (!state.searchQuery) {
      state.searchCountEl.textContent = '';
    } else if (!state.searchMarks.length) {
      state.searchCountEl.textContent = 'Not found';
    } else {
      var idx = state.searchIndex >= 0 ? state.searchIndex + 1 : '?';
      state.searchCountEl.textContent = idx + ' / ' + state.searchMarks.length;
    }
  }

  /* ------------------------------------------------------------------ */
  /* Sidebar                                                              */
  /* ------------------------------------------------------------------ */

  function toggleSidebar(state, mode) {
    if (state.sidebarMode === mode) { closeSidebar(state); }
    else                            { openSidebar(state, mode); }
  }

  function closeSidebar(state) {
    state.sidebarMode = null;
    state.sidebarEl.classList.remove('rodaPdfSidebarOpen');
    cancelThumbs(state);
    state.sidebarContentEl.innerHTML = '';
  }

  function openSidebar(state, mode) {
    cancelThumbs(state);
    state.sidebarContentEl.innerHTML = '';
    state.sidebarMode = mode;
    state.sidebarEl.classList.add('rodaPdfSidebarOpen');
    if (mode === 'thumbs')  buildThumbs(state);
    if (mode === 'outline') buildOutline(state);
  }

  function cancelThumbs(state) {
    state.thumbRenderTasks.forEach(function (t) { try { t.cancel(); } catch (e) {} });
    state.thumbRenderTasks.clear();
    state.thumbEls.clear();
    if (state.thumbObserver) { state.thumbObserver.disconnect(); state.thumbObserver = null; }
  }

  /* Thumbnails */

  function buildThumbs(state) {
    var container = mk('div', 'rodaPdfThumbs');
    state.sidebarContentEl.appendChild(container);

    for (var i = 1; i <= state.totalPages; i++) {
      (function (num) {
        var wrap               = mk('div', 'rodaPdfThumb');
        wrap.dataset.thumbPage = num;
        var lbl                = mk('span', 'rodaPdfThumbLabel');
        lbl.textContent        = num;
        wrap.appendChild(lbl);
        wrap.addEventListener('click', function () { goToPage(state, num); });
        container.appendChild(wrap);
        state.thumbEls.set(num, wrap);
      }(i));
    }

    state.thumbObserver = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting)
          renderThumb(state, parseInt(entry.target.dataset.thumbPage, 10));
      });
    }, { root: state.sidebarEl, rootMargin: '150px 0px', threshold: 0 });

    state.thumbEls.forEach(function (w) { state.thumbObserver.observe(w); });
    updatePageDisplay(state);
  }

  async function renderThumb(state, num) {
    var wrap = state.thumbEls.get(num);
    if (!wrap || wrap.querySelector('canvas')) return;
    try {
      var page     = await state.pdfDoc.getPage(num);
      var viewport = page.getViewport({ scale: 0.22, rotation: state.rotation });
      var canvas   = mk('canvas', 'rodaPdfThumbCanvas');
      canvas.width  = Math.ceil(viewport.width);
      canvas.height = Math.ceil(viewport.height);
      var t = page.render({ canvasContext: canvas.getContext('2d'), viewport: viewport });
      state.thumbRenderTasks.set(num, t);
      await t.promise;
      state.thumbRenderTasks.delete(num);
      wrap.insertBefore(canvas, wrap.querySelector('.rodaPdfThumbLabel'));
    } catch (err) {
      state.thumbRenderTasks.delete(num);
    }
  }

  /* Outline */

  async function buildOutline(state) {
    var outline = await state.pdfDoc.getOutline();
    if (!outline || !outline.length) {
      var empty = mk('p', 'rodaPdfOutlineEmpty');
      empty.textContent = 'No outline available for this document.';
      state.sidebarContentEl.appendChild(empty);
      return;
    }
    state.sidebarContentEl.appendChild(buildOutlineList(state, outline));
  }

  function buildOutlineList(state, items) {
    var ul = mk('ul', 'rodaPdfOutlineList');
    items.forEach(function (item) {
      var li = mk('li', 'rodaPdfOutlineItem');
      var a  = mk('a', 'rodaPdfOutlineLink');
      a.href = '#'; a.textContent = item.title || '(untitled)';
      if (item.dest) {
        a.addEventListener('click', async function (e) {
          e.preventDefault();
          try {
            var dest = item.dest;
            if (typeof dest === 'string') dest = await state.pdfDoc.getDestination(dest);
            if (Array.isArray(dest) && dest[0]) {
              var idx = await state.pdfDoc.getPageIndex(dest[0]);
              goToPage(state, idx + 1);
            }
          } catch (e2) {}
        });
      }
      li.appendChild(a);
      if (item.items && item.items.length) li.appendChild(buildOutlineList(state, item.items));
      ul.appendChild(li);
    });
    return ul;
  }

  /* ------------------------------------------------------------------ */
  /* Presentation mode (custom canvas overlay, one page at a time)       */
  /* ------------------------------------------------------------------ */

  function togglePresentation(state) {
    state.presentationMode ? exitPresentation(state) : enterPresentation(state);
  }

  function enterPresentation(state) {
    if (!state.pdfDoc) return;
    state.presentationMode = true;

    var overlay  = mk('div', 'rodaPdfPresOverlay');
    var canvas   = mk('canvas', 'rodaPdfPresCanvas');
    var counter  = mk('div', 'rodaPdfPresCounter');
    var closeBtn = mk('button', 'rodaPdfPresClose');
    closeBtn.type = 'button'; closeBtn.title = 'Exit presentation (Esc)';
    closeBtn.appendChild(icon('fa-times'));
    closeBtn.addEventListener('click', function () { exitPresentation(state); });

    var prevBtn = mk('button', 'rodaPdfPresNav rodaPdfPresPrev');
    prevBtn.type = 'button'; prevBtn.title = 'Previous page';
    prevBtn.appendChild(icon('fa-chevron-left'));
    prevBtn.addEventListener('click', function () { goToPage(state, state.currentPage - 1); });

    var nextBtn = mk('button', 'rodaPdfPresNav rodaPdfPresNext');
    nextBtn.type = 'button'; nextBtn.title = 'Next page';
    nextBtn.appendChild(icon('fa-chevron-right'));
    nextBtn.addEventListener('click', function () { goToPage(state, state.currentPage + 1); });

    overlay.appendChild(canvas);
    overlay.appendChild(counter);
    overlay.appendChild(closeBtn);
    overlay.appendChild(prevBtn);
    overlay.appendChild(nextBtn);
    state.viewerEl.appendChild(overlay);

    state.keyHandler = function (e) {
      switch (e.key) {
        case 'Escape':                          exitPresentation(state);                    break;
        case 'ArrowRight': case 'ArrowDown': case ' ':
          e.preventDefault(); goToPage(state, state.currentPage + 1);                      break;
        case 'ArrowLeft':  case 'ArrowUp':
          e.preventDefault(); goToPage(state, state.currentPage - 1);                      break;
      }
    };
    document.addEventListener('keydown', state.keyHandler);

    var fsReq = state.viewerEl.requestFullscreen || state.viewerEl.webkitRequestFullscreen;
    if (fsReq) fsReq.call(state.viewerEl).catch(function () {});

    renderPresentationPage(state, state.currentPage);
  }

  async function renderPresentationPage(state, n) {
    var overlay = state.viewerEl.querySelector('.rodaPdfPresOverlay');
    if (!overlay) return;
    var canvas  = overlay.querySelector('.rodaPdfPresCanvas');
    var counter = overlay.querySelector('.rodaPdfPresCounter');

    n = Math.max(1, Math.min(n, state.totalPages));
    state.currentPage = n;
    updatePageDisplay(state);
    if (counter) counter.textContent = n + ' \u2044 ' + state.totalPages;

    var page  = await state.pdfDoc.getPage(n);
    var vp1   = page.getViewport({ scale: 1.0, rotation: state.rotation });
    var availW = overlay.clientWidth  - 100;
    var availH = overlay.clientHeight -  60;
    var scale  = Math.min(availW / vp1.width, availH / vp1.height);
    var vp     = page.getViewport({ scale: scale, rotation: state.rotation });

    canvas.width  = Math.ceil(vp.width);
    canvas.height = Math.ceil(vp.height);
    await page.render({ canvasContext: canvas.getContext('2d'), viewport: vp }).promise;
  }

  function exitPresentation(state) {
    state.presentationMode = false;
    var overlay = state.viewerEl.querySelector('.rodaPdfPresOverlay');
    if (overlay) overlay.remove();
    if (state.keyHandler) {
      document.removeEventListener('keydown', state.keyHandler);
      state.keyHandler = null;
    }
    if (document.fullscreenElement) document.exitFullscreen().catch(function () {});
  }

  /* ------------------------------------------------------------------ */
  /* Fullscreen                                                           */
  /* ------------------------------------------------------------------ */

  function requestFullscreen(state) {
    var e   = state.viewerEl;
    var req = e.requestFullscreen || e.webkitRequestFullscreen;
    if (req) req.call(e).catch(function () {});
  }

  /* ------------------------------------------------------------------ */
  /* Initialisation                                                       */
  /* ------------------------------------------------------------------ */

  async function init(viewerEl, fileUrl, baseUrl) {
    if (viewers.has(viewerEl)) destroy(viewerEl);

    var state = newState(viewerEl, fileUrl, baseUrl);
    viewers.set(viewerEl, state);

    buildDOM(state);

    /* Fullscreen change handler */
    state.fsChangeHandler = function () {
      var inFs = document.fullscreenElement === state.viewerEl;
      /* Toggle fullscreen button icon/title */
      if (state.fullscreenBtnEl) {
        var iconEl = state.fullscreenBtnEl.querySelector('i');
        if (iconEl) iconEl.className = 'fa ' + (inFs ? 'fa-compress' : 'fa-expand');
        state.fullscreenBtnEl.title = inFs ? 'Exit fullscreen' : 'Fullscreen';
      }
      /* Class on viewer drives CSS overflow for element-level scroll */
      state.viewerEl.classList.toggle('rodaPdfViewerFs', inFs);
      /* Re-setup intersection observer (root changes) */
      if (state.pageObserver) setupPageObserver(state);
      /* Exit presentation if fullscreen closed via Esc */
      if (!inFs && state.presentationMode) exitPresentation(state);
    };
    document.addEventListener('fullscreenchange', state.fsChangeHandler);

    var loadingEl = viewerEl.querySelector('.rodaPdfLoading');

    try {
      var lib     = await getLib(baseUrl);
      var loadDoc = lib.getDocument({
        url:        fileUrl,
        cMapUrl:    baseUrl + 'webjars/pdfjs-dist/cmaps/',
        cMapPacked: true,
      });
      var pdfDoc = await loadDoc.promise;
      state.pdfDoc     = pdfDoc;
      state.totalPages = pdfDoc.numPages;

      if (loadingEl) loadingEl.remove();

      await buildPageList(state);
      setupScrollTracking(state);
      updatePageDisplay(state);

    } catch (err) {
      if (loadingEl) {
        loadingEl.className   = 'rodaPdfError';
        loadingEl.textContent = 'Could not load document. ' + ((err && err.message) || err);
      }
      console.error('[RodaPdfViewer] init error:', err);
    }
  }

  /* ------------------------------------------------------------------ */
  /* Cleanup                                                              */
  /* ------------------------------------------------------------------ */

  function destroy(viewerEl) {
    var state = viewers.get(viewerEl);
    if (!state) return;

    clearTimeout(state.searchDebounceTimer);
    cancelThumbs(state);

    if (state.pageObserver)    state.pageObserver.disconnect();
    if (state.keyHandler)      document.removeEventListener('keydown', state.keyHandler);
    if (state.fsChangeHandler) document.removeEventListener('fullscreenchange', state.fsChangeHandler);
    if (state.scrollHandler) {
      window.removeEventListener('scroll', state.scrollHandler);
      viewerEl.removeEventListener('scroll', state.scrollHandler);
    }

    state.renderTasks.forEach(function (t) { try { t.cancel(); } catch (e) {} });
    if (state.pdfDoc) state.pdfDoc.destroy();

    viewers.delete(viewerEl);
  }

  /* ------------------------------------------------------------------ */
  /* Public API                                                           */
  /* ------------------------------------------------------------------ */

  global.RodaPdfViewer = {
    init:    function (containerEl, fileUrl, baseUrl) { init(containerEl, fileUrl, baseUrl); },
    destroy: function (containerEl)                   { destroy(containerEl); },
  };

}(window));
