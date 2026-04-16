/**
 * RODA PDF Viewer
 *
 * Uses the PDF.js high-level components (PDFViewer + PDFFindController) from
 * web/pdf_viewer.mjs — exactly the same approach as the official demo at
 * https://mozilla.github.io/pdf.js/web/viewer.html.
 *
 * pdf_viewer.css is loaded once from the webjar so that the text layer CSS
 * custom properties (--total-scale-factor, --font-height, etc.) work correctly.
 *
 * All styles for our toolbar / search bar / sidebar live in main.gss under
 * .rodaPdfViewer.  The page/textLayer/highlight CSS comes from pdf_viewer.css.
 */
(function (global) {
  'use strict';

  /* Map<HTMLElement, ViewerState> */
  var viewers = new Map();

  /* Cached ES-module handles */
  var _pdfjs       = null;   /* build/pdf.mjs          */
  var _pdfjsViewer = null;   /* web/pdf_viewer.mjs     */
  var _cssLoaded   = false;

  /* ------------------------------------------------------------------ */
  /* Library loading                                                      */
  /* ------------------------------------------------------------------ */

  async function getLibs(baseUrl) {
    if (_pdfjs && _pdfjsViewer) return { pdfjs: _pdfjs, pdfjsViewer: _pdfjsViewer };

    /* Load pdf_viewer.css once — needed for text-layer CSS custom props */
    if (!_cssLoaded) {
      _cssLoaded = true;
      if (!document.querySelector('link[data-roda-pdfjs-css]')) {
        var link = document.createElement('link');
        link.rel  = 'stylesheet';
        link.href = baseUrl + 'webjars/pdfjs-dist/web/pdf_viewer.css';
        link.setAttribute('data-roda-pdfjs-css', '');
        document.head.appendChild(link);
      }
    }

    _pdfjs       = await import(baseUrl + 'webjars/pdfjs-dist/build/pdf.mjs');
    _pdfjsViewer = await import(baseUrl + 'webjars/pdfjs-dist/web/pdf_viewer.mjs');

    _pdfjs.GlobalWorkerOptions.workerSrc =
      baseUrl + 'webjars/pdfjs-dist/build/pdf.worker.mjs';

    return { pdfjs: _pdfjs, pdfjsViewer: _pdfjsViewer };
  }

  /* ------------------------------------------------------------------ */
  /* State factory                                                        */
  /* ------------------------------------------------------------------ */

  function newState(viewerEl, fileUrl, baseUrl) {
    return {
      viewerEl:            viewerEl,
      fileUrl:             fileUrl,
      baseUrl:             baseUrl,
      /* PDF.js components */
      pdfDoc:              null,
      pdfViewer:           null,
      findController:      null,
      eventBus:            null,
      linkService:         null,
      /* page info */
      currentPage:         1,
      totalPages:          0,
      /* search */
      searchQuery:         null,
      searchMatchCurrent:  0,
      searchMatchTotal:    0,
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
      scrollContainerEl:   null,
      sidebarEl:           null,
      sidebarContentEl:    null,
      pageInputEl:         null,
      pageCountEl:         null,
      searchBarEl:         null,
      searchInputEl:       null,
      searchCountEl:       null,
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

    /* Loading indicator */
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

    /* Group 3: search */
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
        e.shiftKey ? prevSearchMatch(state) : nextSearchMatch(state);
      } else if (e.key === 'Escape') {
        closeSearch(state);
      }
    });

    /* ---- Content area: sidebar + scroll container ---- */
    var content        = mk('div', 'rodaPdfContent');
    var sidebar        = mk('div', 'rodaPdfSidebar');
    var sidebarContent = mk('div', 'rodaPdfSidebarContent');
    sidebar.appendChild(sidebarContent);

    /*
     * PDFViewer requires its container to be position:absolute.
     * We wrap it in a position:relative element that takes up the
     * remaining space, and make the actual scroll container absolute inside it.
     */
    var scrollWrapper    = mk('div', 'rodaPdfScrollWrapper');
    var scrollContainer  = mk('div', 'rodaPdfScrollContainer');
    var pdfViewerDiv     = mk('div', 'pdfViewer');
    scrollContainer.appendChild(pdfViewerDiv);
    scrollWrapper.appendChild(scrollContainer);

    content.appendChild(sidebar);
    content.appendChild(scrollWrapper);

    viewer.appendChild(toolbar);
    viewer.appendChild(searchBar);
    viewer.appendChild(content);

    /* Store refs */
    state.toolbarEl        = toolbar;
    state.scrollContainerEl = scrollContainer;
    state.sidebarEl        = sidebar;
    state.sidebarContentEl = sidebarContent;
    state.pageInputEl      = pageInput;
    state.pageCountEl      = pageCount;
    state.searchBarEl      = searchBar;
    state.searchInputEl    = searchInput;
    state.searchCountEl    = searchCount;
  }

  /* ------------------------------------------------------------------ */
  /* Navigation                                                           */
  /* ------------------------------------------------------------------ */

  function goToPage(state, n) {
    if (!state.pdfViewer) return;
    n = Math.max(1, Math.min(n, state.totalPages));
    state.pdfViewer.currentPageNumber = n;
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
  /* Zoom                                                                 */
  /* ------------------------------------------------------------------ */

  function zoomIn(state) {
    if (!state.pdfViewer) return;
    state.pdfViewer.currentScale = Math.min(state.pdfViewer.currentScale * 1.25, 5.0);
  }

  function zoomOut(state) {
    if (!state.pdfViewer) return;
    state.pdfViewer.currentScale = Math.max(state.pdfViewer.currentScale / 1.25, 0.25);
  }

  function fitWidth(state) {
    if (!state.pdfViewer) return;
    state.pdfViewer.currentScaleValue = 'page-width';
  }

  /* ------------------------------------------------------------------ */
  /* Rotation                                                             */
  /* ------------------------------------------------------------------ */

  function rotateCW(state) {
    if (!state.pdfViewer) return;
    state.pdfViewer.pagesRotation = (state.pdfViewer.pagesRotation + 90) % 360;
  }

  function rotateCCW(state) {
    if (!state.pdfViewer) return;
    state.pdfViewer.pagesRotation = (state.pdfViewer.pagesRotation + 270) % 360;
  }

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
    state.searchQuery = null;
    if (state.searchInputEl) state.searchInputEl.value = '';
    if (state.searchBarEl) state.searchBarEl.classList.add('rodaPdfSearchBarHidden');
    updateSearchCount(state, null);
    /* Tell PDFFindController to clear highlights */
    if (state.eventBus) {
      state.eventBus.dispatch('findbarclose', { source: null });
    }
  }

  function performSearch(state) {
    var query = state.searchInputEl ? state.searchInputEl.value.trim() : '';
    state.searchQuery = query || null;

    if (!state.eventBus) return;

    if (!query) {
      updateSearchCount(state, null);
      state.eventBus.dispatch('findbarclose', { source: null });
      return;
    }

    state.eventBus.dispatch('find', {
      source:          null,
      type:            '',       /* new search */
      query:           query,
      highlightAll:    true,
      caseSensitive:   false,
      entireWord:      false,
      findPrevious:    false,
      matchDiacritics: true,
    });
  }

  function nextSearchMatch(state) {
    if (!state.searchQuery) { performSearch(state); return; }
    if (!state.eventBus) return;
    state.eventBus.dispatch('find', {
      source:          null,
      type:            'again',
      query:           state.searchQuery,
      highlightAll:    true,
      caseSensitive:   false,
      entireWord:      false,
      findPrevious:    false,
      matchDiacritics: true,
    });
  }

  function prevSearchMatch(state) {
    if (!state.searchQuery) { performSearch(state); return; }
    if (!state.eventBus) return;
    state.eventBus.dispatch('find', {
      source:          null,
      type:            'again',
      query:           state.searchQuery,
      highlightAll:    true,
      caseSensitive:   false,
      entireWord:      false,
      findPrevious:    true,
      matchDiacritics: true,
    });
  }

  function updateSearchCount(state, matchesCount) {
    if (!state.searchCountEl) return;
    if (!matchesCount || !state.searchQuery) {
      state.searchCountEl.textContent = '';
    } else if (matchesCount.total === 0) {
      state.searchCountEl.textContent = 'Not found';
    } else {
      state.searchCountEl.textContent = matchesCount.current + ' / ' + matchesCount.total;
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
    if (!state.pdfDoc) return;
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
    if (!wrap || wrap.querySelector('canvas') || !state.pdfDoc) return;
    try {
      var page     = await state.pdfDoc.getPage(num);
      var viewport = page.getViewport({ scale: 0.22, rotation: 0 });
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
    if (!state.pdfDoc) return;
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

    var loadingEl = viewerEl.querySelector('.rodaPdfLoading');

    /* Fullscreen change handler */
    state.fsChangeHandler = function () {
      var inFs = document.fullscreenElement === state.viewerEl;
      if (state.fullscreenBtnEl) {
        var iconEl = state.fullscreenBtnEl.querySelector('i');
        if (iconEl) iconEl.className = 'fa ' + (inFs ? 'fa-compress' : 'fa-expand');
        state.fullscreenBtnEl.title = inFs ? 'Exit fullscreen' : 'Fullscreen';
      }
      state.viewerEl.classList.toggle('rodaPdfViewerFs', inFs);
    };
    document.addEventListener('fullscreenchange', state.fsChangeHandler);

    try {
      var libs = await getLibs(baseUrl);
      var pdfjs = libs.pdfjs;
      var pdfjsViewer = libs.pdfjsViewer;

      /* Create PDF.js high-level components */
      var eventBus     = new pdfjsViewer.EventBus();
      var linkService  = new pdfjsViewer.PDFLinkService({ eventBus: eventBus });
      var findController = new pdfjsViewer.PDFFindController({
        linkService:                 linkService,
        eventBus:                    eventBus,
        updateMatchesCountOnProgress: false,
      });

      var pdfViewer = new pdfjsViewer.PDFViewer({
        container:      state.scrollContainerEl,
        eventBus:       eventBus,
        linkService:    linkService,
        findController: findController,
      });

      linkService.setViewer(pdfViewer);

      state.eventBus       = eventBus;
      state.linkService    = linkService;
      state.findController = findController;
      state.pdfViewer      = pdfViewer;

      /* EventBus listeners */
      eventBus._on('pagechanging', function (evt) {
        state.currentPage = evt.pageNumber;
        updatePageDisplay(state);
      });

      eventBus._on('scalechanging', function (evt) {
        /* keep scale in sync if needed in future */
      });

      eventBus._on('pagesloaded', function (evt) {
        /* Initial fit-to-width */
        pdfViewer.currentScaleValue = 'page-width';
        state.totalPages = evt.pagesCount;
        state.currentPage = pdfViewer.currentPageNumber || 1;
        updatePageDisplay(state);
      });

      eventBus._on('updatefindmatchescount', function (evt) {
        updateSearchCount(state, evt.matchesCount);
      });

      eventBus._on('updatefindcontrolstate', function (evt) {
        /* FindState: 0=Found, 1=NotFound, 2=Wrapped, 3=Pending */
        if (evt.state === 1 /* NotFound */) {
          updateSearchCount(state, { current: 0, total: 0 });
        } else if (evt.matchesCount) {
          updateSearchCount(state, evt.matchesCount);
        }
      });

      /* Load the PDF document */
      var loadDoc = pdfjs.getDocument({
        url:        fileUrl,
        cMapUrl:    baseUrl + 'webjars/pdfjs-dist/cmaps/',
        cMapPacked: true,
      });

      var pdfDoc = await loadDoc.promise;
      state.pdfDoc     = pdfDoc;
      state.totalPages = pdfDoc.numPages;

      if (loadingEl) loadingEl.remove();

      pdfViewer.setDocument(pdfDoc);
      linkService.setDocument(pdfDoc);
      findController.setDocument(pdfDoc);

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

    if (state.fsChangeHandler) document.removeEventListener('fullscreenchange', state.fsChangeHandler);
    if (state.keyHandler)      document.removeEventListener('keydown', state.keyHandler);

    if (state.pdfViewer) {
      try { state.pdfViewer.setDocument(null); } catch (e) {}
    }
    if (state.pdfDoc) {
      try { state.pdfDoc.destroy(); } catch (e) {}
    }

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
