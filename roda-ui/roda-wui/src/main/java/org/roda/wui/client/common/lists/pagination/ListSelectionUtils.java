package org.roda.wui.client.common.lists.pagination;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class ListSelectionUtils {

  private static final String STORAGE_PREFIX = "ListSelectionState.Clipboard.";

  private static Map<String, ListSelectionState<?>> CLIPBOARD = new HashMap<>();
  private static final Storage storage = Storage.getLocalStorageIfSupported();

  static {
    loadClipboardOnStorage();
  }

  private static void loadClipboardOnStorage() {
    if (storage != null) {
      StorageMap storageMap = new StorageMap(storage);
      for (Entry<String, String> entry : storageMap.entrySet()) {
        if (entry.getKey().startsWith(STORAGE_PREFIX)) {
          String className = entry.getKey().substring(STORAGE_PREFIX.length());
          try {
            ListSelectionState<IndexedAIP> state = ListSelectionStateMappers.getObject(className, entry.getValue());
            CLIPBOARD.put(className, state);
          } catch (JsonDeserializationException e) {
            GWT.log("Could not load selection state of class: " + className, e);
          }

        }
      }
    }
  }

  private static <T extends IsIndexed> void saveOnStorage(String className, ListSelectionState<T> state) {
    if (storage != null) {
      storage.setItem(STORAGE_PREFIX + className, ListSelectionStateMappers.getJson(className, state));
    }
  }

  private ListSelectionUtils() {
    super();
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
                callback.onSuccess(ListSelectionUtils.create(first, state.getFilter(), state.getJustActive(),
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
    String className = state.getSelected().getClass().getName();
    CLIPBOARD.put(className, state);
    saveOnStorage(className, state);
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> ListSelectionState<T> last(Class<T> objectClass) {
    return (ListSelectionState<T>) CLIPBOARD.get(objectClass.getName());
  }

  public static <T extends IsIndexed> boolean hasLast(Class<T> objectClass) {
    return CLIPBOARD.containsKey(objectClass.getName());
  }

  public static <T extends IsIndexed> void jump(final T object, int relativeIndex) {
    jump(object, relativeIndex, new ProcessRelativeItem<T>() {

      @Override
      public void process(T object) {
        HistoryUtils.resolve(object);
      }
    });
  }

  public static <T extends IsIndexed> void jump(final T object, final int relativeIndex,
    final ProcessRelativeItem<T> processor) {

    @SuppressWarnings("unchecked")
    ListSelectionState<T> last = last((Class<T>) object.getClass());
    if (last != null) {

      if (last.getSelected().getUUID().equals(object.getUUID())) {

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
      } else {
        GWT.log("Trying to jump with an incoherent state");
      }
    }
  }

  public static <T extends IsIndexed> void previous(final T object) {
    jump(object, -1);
  }

  public static <T extends IsIndexed> void next(T object) {
    jump(object, +1);
  }

  public static <T extends IsIndexed> void previous(final T object, ProcessRelativeItem<T> processor) {
    jump(object, -1, processor);
  }

  public static <T extends IsIndexed> void next(T object, ProcessRelativeItem<T> processor) {
    jump(object, +1, processor);
  }

  public static <T extends IsIndexed> void hasPreviousOrNext(final T object,
    final AsyncCallback<Pair<Boolean, Boolean>> callback) {
    @SuppressWarnings("unchecked")
    Class<T> objectClass = (Class<T>) object.getClass();
    final ListSelectionState<T> last = last(objectClass);
    if (last != null) {
      if (last.getSelected().getUUID().equals(object.getUUID())) {

        BrowserService.Util.getInstance().count(objectClass.getName(), last.getFilter(), last.getJustActive(),
          new AsyncCallback<Long>() {

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

  public static <T extends IsIndexed> void bindLayout(final T object, final HasClickHandlers previousButton,
    final HasClickHandlers nextButton, final FocusPanel keyboardFocus, final boolean requireControlKeyModifier,
    final boolean requireShiftKeyModifier, final boolean requireAltKeyModifier, UIObject... extraUiObjectsToHide) {
    bindLayout(object, previousButton, nextButton, keyboardFocus, requireControlKeyModifier, requireShiftKeyModifier,
      requireAltKeyModifier, new ProcessRelativeItem<T>() {

        @Override
        public void process(T object) {
          HistoryUtils.resolve(object);
        }
      }, extraUiObjectsToHide);
  }

  public static <T extends IsIndexed> void bindLayout(final T object, final HasClickHandlers previousButton,
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
        previous(object, processor);
      }
    });

    nextButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        next(object, processor);
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
            next(object, processor);
          } else if (ne.getKeyCode() == KeyCodes.KEY_LEFT) {
            ne.preventDefault();
            previous(object, processor);
          }
        }
      }
    });

    updateLayout(object, previousButton, nextButton, extraUiObjectsToHide);
  }

  public static <T extends IsIndexed> void updateLayout(final T object, final HasClickHandlers previousButton,
    final HasClickHandlers nextButton, final UIObject... extraUiObjectsToHide) {
    hasPreviousOrNext(object, new AsyncCallback<Pair<Boolean, Boolean>>() {

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

          if (previousButton instanceof FocusWidget && nextButton instanceof FocusWidget) {
            ((FocusWidget) previousButton).setEnabled(hasPrevious);
            ((FocusWidget) nextButton).setEnabled(hasNext);
          } else {
            HtmlSnippetUtils.setCssClassDisabled((UIObject) previousButton, !hasPrevious);
            HtmlSnippetUtils.setCssClassDisabled((UIObject) nextButton, !hasNext);
          }
        }
      }
    });
  }

}
