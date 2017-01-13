package org.roda.wui.client.common.lists.pagination;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class ListSelectionState<T extends IsIndexed> {

  private final T selected;
  private final Filter filter;
  private final Boolean justActive;
  private final Facets facets;
  private final Sorter sorter;
  private Integer index;

  private static Map<Class<? extends IsIndexed>, ListSelectionState<?>> CLIPBOARD = new HashMap<>();

  private ListSelectionState(T selected, Filter filter, Boolean justActive, Facets facets, Sorter sorter,
    Integer index) {
    super();
    this.selected = selected;
    this.filter = filter;
    this.justActive = justActive;
    this.facets = facets;
    this.sorter = sorter;
    this.index = index;
  }

  public static <T extends IsIndexed> ListSelectionState<T> create(T selected, Filter filter, Boolean justActive,
    Facets facets, Sorter sorter, Integer index) {
    return new ListSelectionState<>(selected, filter, justActive, facets, sorter, index);
  }

  private static <T extends IsIndexed> void openRelative(final ListSelectionState<T> state, final int relativeIndex,
    final AsyncCallback<ListSelectionState<T>> callback) {
    final int newIndex = state.getIndex() + relativeIndex;
    BrowserService.Util.getInstance().find(state.getSelected().getClass().getName(), state.getFilter(),
      state.getSorter(), new Sublist(newIndex, 1), state.getFacets(), LocaleInfo.getCurrentLocale().getLocaleName(),
      state.getJustActive(), new AsyncCallback<IndexResult<T>>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(IndexResult<T> result) {
          if (!result.getResults().isEmpty()) {
            T first = result.getResults().get(0);

            // if we are jumping to the same file, try the next one
            if (first.getUUID().equals(state.getSelected().getUUID())) {
              openRelative(state, relativeIndex < 0 ? relativeIndex - 1 : relativeIndex + 1, callback);
            } else {
              HistoryUtils.resolve(first);
              callback.onSuccess(ListSelectionState.create(first, state.getFilter(), state.getJustActive(),
                state.getFacets(), state.getSorter(), newIndex));
            }
          } else {
            callback.onFailure(new NotFoundException("No items were found"));
          }
        }
      });
  }

  public static <T extends IsIndexed> void save(final ListSelectionState<T> state) {
    CLIPBOARD.put(state.getSelected().getClass(), state);
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> ListSelectionState<T> last(Class<T> objectClass) {
    return (ListSelectionState<T>) CLIPBOARD.get(objectClass);
  }

  public static <T extends IsIndexed> boolean hasLast(Class<T> objectClass) {
    return CLIPBOARD.containsKey(objectClass);
  }

  public static <T extends IsIndexed> void previous(final Class<T> objectClass) {
    ListSelectionState<T> last = last(objectClass);
    if (last != null) {
      openRelative(last, -1, new AsyncCallback<ListSelectionState<T>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(ListSelectionState<T> newState) {
          save(newState);
        }
      });
    }
  }

  public static <T extends IsIndexed> void next(Class<T> objectClass) {
    ListSelectionState<T> last = last(objectClass);
    if (last != null) {
      openRelative(last, +1, new AsyncCallback<ListSelectionState<T>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(ListSelectionState<T> newState) {
          save(newState);
        }
      });
    }
  }

  public static <T extends IsIndexed> void hasPreviousNext(final Class<T> objectClass,
    final AsyncCallback<Pair<Boolean, Boolean>> callback) {
    final ListSelectionState<T> last = last(objectClass);
    if (last != null) {
      BrowserService.Util.getInstance().count(objectClass.getName(), last.getFilter(), new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Long totalCount) {
          Integer lastIndex = last.getIndex();
          Boolean hasPrevious = lastIndex > 0;
          Boolean hasNext = lastIndex < totalCount - 1;
          callback.onSuccess(Pair.create(hasPrevious, hasNext));
        }
      });
    } else {
      callback.onSuccess(Pair.create(Boolean.FALSE, Boolean.FALSE));
    }
  }

  public static <T extends IsIndexed> void bindBrowseOpener(final AsyncTableCell<T, ?> list) {
    list.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        ListSelectionState<T> selectionState = list.getListSelectionState();

        if (selectionState != null) {
          save(selectionState);
          HistoryUtils.resolve(selectionState.getSelected());
        }
      }
    });
  }

  public static <T extends IsIndexed> void bindLayout(final Class<T> objectClass, final FocusPanel previousButton,
    final FocusPanel nextButton, final FocusPanel keyboardFocus, final boolean requireControlKeyModifier,
    final boolean requireShiftKeyModifier, final boolean requireAltKeyModifier) {

    StringBuilder b = new StringBuilder();

    if (requireControlKeyModifier) {
      b.append("CTRL + ");
    }

    if (requireShiftKeyModifier) {
      b.append("SHIFT + ");
    }

    if (requireAltKeyModifier) {
      b.append("ALT + ");
    }

    // TODO add HTML entities or icons
    previousButton.setTitle(b + "LEFT");
    nextButton.setTitle(b + "RIGHT");

    hasPreviousNext(objectClass, new AsyncCallback<Pair<Boolean, Boolean>>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        previousButton.setVisible(false);
        nextButton.setVisible(false);
      }

      @Override
      public void onSuccess(Pair<Boolean, Boolean> result) {
        Boolean hasPrevious = result.getFirst();
        Boolean hasNext = result.getSecond();

        // visibility
        if (!hasPrevious && !hasNext) {
          previousButton.setVisible(false);
          nextButton.setVisible(false);
        } else {
          HtmlSnippetUtils.setCssClassDisabled(previousButton, !hasPrevious);
          HtmlSnippetUtils.setCssClassDisabled(nextButton, !hasNext);
        }

        // actions
        if (hasPrevious) {
          previousButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
              previous(objectClass);
            }
          });
        }

        if (hasNext) {
          nextButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
              next(objectClass);
            }
          });

          keyboardFocus.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
              boolean controlModifier = !requireControlKeyModifier || event.isControlKeyDown();
              boolean shiftModifier = !requireShiftKeyModifier || event.isShiftKeyDown();
              boolean altModifier = !requireAltKeyModifier || event.isAltKeyDown();

              if (controlModifier && shiftModifier && altModifier) {
                NativeEvent ne = event.getNativeEvent();
                if (ne.getKeyCode() == KeyCodes.KEY_RIGHT) {
                  ne.preventDefault();
                  next(objectClass);
                } else if (ne.getKeyCode() == KeyCodes.KEY_LEFT) {
                  ne.preventDefault();
                  previous(objectClass);
                }
              }
            }
          });
        }

      }
    });
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public T getSelected() {
    return selected;
  }

  public Filter getFilter() {
    return filter;
  }

  public Boolean getJustActive() {
    return justActive;
  }

  public Facets getFacets() {
    return facets;
  }

  public Sorter getSorter() {
    return sorter;
  }

}
