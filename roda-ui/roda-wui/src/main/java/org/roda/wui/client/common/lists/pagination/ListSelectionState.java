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
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.UIObject;
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

  public interface ProcessRelativeItem<T> {
    void process(T object);
  }

  private static <T extends IsIndexed> void openRelative(final ListSelectionState<T> state, final int relativeIndex,
    final AsyncCallback<ListSelectionState<T>> callback) {
    openRelative(state, relativeIndex, callback, new ProcessRelativeItem<T>() {

      @Override
      public void process(T object) {
        HistoryUtils.resolve(object);
      }
    });
  }

  private static <T extends IsIndexed> void openRelative(final ListSelectionState<T> state, final int relativeIndex,
    final AsyncCallback<ListSelectionState<T>> callback, final ProcessRelativeItem<T> processor) {
    final int newIndex = state.getIndex() + relativeIndex;
    if (newIndex >= 0) {
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
                openRelative(state, relativeIndex < 0 ? relativeIndex - 1 : relativeIndex + 1, callback, processor);
              } else {
                processor.process(first);
                callback.onSuccess(ListSelectionState.create(first, state.getFilter(), state.getJustActive(),
                  state.getFacets(), state.getSorter(), newIndex));
              }
            } else {
              callback.onFailure(new NotFoundException("No items were found"));
            }
          }
        });
    }
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

  public static <T extends IsIndexed> void jump(final Class<T> objectClass, int relativeIndex) {
    jump(objectClass, relativeIndex, new ProcessRelativeItem<T>() {

      @Override
      public void process(T object) {
        HistoryUtils.resolve(object);
      }
    });
  }

  public static <T extends IsIndexed> void jump(final Class<T> objectClass, final int relativeIndex,
    final ProcessRelativeItem<T> processor) {

    ListSelectionState<T> last = last(objectClass);
    if (last != null) {

      AsyncCallback<ListSelectionState<T>> callback = new AsyncCallback<ListSelectionState<T>>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            // TODO i18n
            if (relativeIndex > 0) {
              Toast.showInfo("Cannot jump to next", "Reached the end of the list");
            } else {
              Toast.showInfo("Cannot jump to previous", "Reached the beggining of the list");
            }

          } else {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }
        }

        @Override
        public void onSuccess(ListSelectionState<T> newState) {
          save(newState);
        }
      };

      openRelative(last, relativeIndex, callback, processor);
    }
  }

  public static <T extends IsIndexed> void previous(final Class<T> objectClass) {
    jump(objectClass, -1);
  }

  public static <T extends IsIndexed> void next(Class<T> objectClass) {
    jump(objectClass, +1);
  }

  public static <T extends IsIndexed> void previous(final Class<T> objectClass, ProcessRelativeItem<T> processor) {
    jump(objectClass, -1, processor);
  }

  public static <T extends IsIndexed> void next(Class<T> objectClass, ProcessRelativeItem<T> processor) {
    jump(objectClass, +1, processor);
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

  public static <T extends IsIndexed> void bindLayout(final Class<T> objectClass, final HasClickHandlers previousButton,
    final HasClickHandlers nextButton, final FocusPanel keyboardFocus, final boolean requireControlKeyModifier,
    final boolean requireShiftKeyModifier, final boolean requireAltKeyModifier, UIObject... extraUiObjectsToHide) {
    bindLayout(objectClass, previousButton, nextButton, keyboardFocus, requireControlKeyModifier,
      requireShiftKeyModifier, requireAltKeyModifier, new ProcessRelativeItem<T>() {

        @Override
        public void process(T object) {
          HistoryUtils.resolve(object);
        }
      }, extraUiObjectsToHide);
  }

  public static <T extends IsIndexed> void bindLayout(final Class<T> objectClass, final HasClickHandlers previousButton,
    final HasClickHandlers nextButton, final FocusPanel keyboardFocus, final boolean requireControlKeyModifier,
    final boolean requireShiftKeyModifier, final boolean requireAltKeyModifier, final ProcessRelativeItem<T> processor,
    final UIObject... extraUiObjectsToHide) {

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

    // TODO add HTML entities, icons or i18n
    if (previousButton instanceof UIObject && nextButton instanceof UIObject) {
      ((UIObject) previousButton).setTitle(b + "LEFT");
      ((UIObject) nextButton).setTitle(b + "RIGHT");
    }

    previousButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        previous(objectClass, processor);
      }
    });

    nextButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        next(objectClass, processor);
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
            next(objectClass, processor);
          } else if (ne.getKeyCode() == KeyCodes.KEY_LEFT) {
            ne.preventDefault();
            previous(objectClass, processor);
          }
        }
      }
    });

    updateLayout(objectClass, previousButton, nextButton, extraUiObjectsToHide);
  }

  public static <T extends IsIndexed> void updateLayout(final Class<T> objectClass,
    final HasClickHandlers previousButton, final HasClickHandlers nextButton, final UIObject... extraUiObjectsToHide) {
    hasPreviousNext(objectClass, new AsyncCallback<Pair<Boolean, Boolean>>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        if (previousButton instanceof UIObject && nextButton instanceof UIObject) {
          ((UIObject) previousButton).setVisible(false);
          ((UIObject) nextButton).setVisible(false);
        }
      }

      @Override
      public void onSuccess(Pair<Boolean, Boolean> result) {
        Boolean hasPrevious = result.getFirst();
        Boolean hasNext = result.getSecond();

        // visibility
        if (previousButton instanceof UIObject && nextButton instanceof UIObject) {
          ((UIObject) previousButton).setVisible(hasPrevious || hasNext);
          ((UIObject) nextButton).setVisible(hasPrevious || hasNext);

          for (UIObject uiObj : extraUiObjectsToHide) {
            uiObj.setVisible(hasPrevious || hasNext);
          }

          HtmlSnippetUtils.setCssClassDisabled((UIObject) previousButton, !hasPrevious);
          HtmlSnippetUtils.setCssClassDisabled((UIObject) nextButton, !hasNext);
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
