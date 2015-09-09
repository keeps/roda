/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class ControlledVocabularyEditor implements SourcesChangeEvents {

  private static Map<String, String[]> controlledVocabularyCache = new HashMap<String, String[]>();

  private static Map<String, String> defaultValueCache = new HashMap<String, String>();

  public static void getControlledVocabulary(final String field, final AsyncCallback<String[]> callback) {
    if (controlledVocabularyCache.containsKey(field)) {
      callback.onSuccess(controlledVocabularyCache.get(field));
    } else {
      EditorService.Util.getInstance().getControlledVocabulary(field, new AsyncCallback<String[]>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(String[] values) {
          controlledVocabularyCache.put(field, values);
          callback.onSuccess(values);
        }

      });
    }
  }

  public static void getDefaultValue(final String field, final AsyncCallback<String> callback) {
    if (defaultValueCache.containsKey(field)) {
      callback.onSuccess(defaultValueCache.get(field));
    } else {
      EditorService.Util.getInstance().getDefaultValue(field, new AsyncCallback<String>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(String value) {
          defaultValueCache.put(field, value);
          callback.onSuccess(value);
        }

      });
    }
  }

  private static String UNDEFINED_VALUE = "UNDEFINED";

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final String field;

  private final ListBox layout;

  private boolean initialized;

  private List<Command> initListeners;

  public ControlledVocabularyEditor(final String field) {
    this.field = field;
    layout = new ListBox();
    initialized = false;
    initListeners = new Vector<Command>();
    getControlledVocabulary(field, new AsyncCallback<String[]>() {

      public void onFailure(Throwable caught) {
        logger.error("Error getting controlled vocabulary of " + field);
      }

      public void onSuccess(String[] values) {
        for (int i = 0; i < values.length; i++) {
          if (values[i] != null) {
            layout.addItem(values[i]);
          } else {
            // TODO externalize string
            layout.addItem("--", UNDEFINED_VALUE);
          }

        }
        getDefaultValue(field, new AsyncCallback<String>() {

          public void onFailure(Throwable caught) {
            logger.error("Error getting of " + field, caught);
          }

          public void onSuccess(String value) {
            initialized = true;
            setSelected(value);
            onLoad();

          }

        });

      }

    });
  }

  private void onLoad() {
    for (Command c : initListeners) {
      c.execute();
    }
    initListeners.clear();
  }

  private void ensureLoaded(Command command) {
    if (initialized) {
      command.execute();
    } else {
      initListeners.add(command);
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public String getSelected() {
    String value = layout.getValue(layout.getSelectedIndex());
    return value.equals(UNDEFINED_VALUE) ? null : value;
  }

  public void setSelected(String value) {
    final String valueString = value == null ? UNDEFINED_VALUE : value;
    ensureLoaded(new Command() {

      public void execute() {
        int i = 0;
        while (i < layout.getItemCount() && !layout.getValue(i).equals(valueString)) {
          i++;
        }
        if (i < layout.getItemCount()) {
          layout.setSelectedIndex(i);
        } else {
          logger.error(valueString + " not defined in " + field + " controlled vocabulary");
        }
      }

    });

  }

  public void addChangeListener(ChangeListener listener) {
    layout.addChangeListener(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    layout.removeChangeListener(listener);
  }

}
