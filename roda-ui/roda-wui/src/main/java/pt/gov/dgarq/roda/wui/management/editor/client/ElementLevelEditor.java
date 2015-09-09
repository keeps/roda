/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.dissemination.client.Dissemination;

/**
 * @author Luis Faria
 * 
 */
public class ElementLevelEditor implements MetadataElementEditor {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final ListBox layout;

  private final List<Command> waitingForInitialization;

  private boolean initialized;

  public ElementLevelEditor(final String pid) {
    layout = new ListBox();
    initialized = false;
    waitingForInitialization = new Vector<Command>();

    if (pid == null) {
      init(DescriptionLevelUtils.REPRESENTATION_DESCRIPTION_LEVELS
        .toArray(new DescriptionLevel[DescriptionLevelUtils.REPRESENTATION_DESCRIPTION_LEVELS.size()]));
    } else {
      EditorService.Util.getInstance().getPossibleLevels(pid, new AsyncCallback<DescriptionLevel[]>() {

        public void onFailure(Throwable caught) {
          logger.error("Error getting possible levels for " + pid, caught);
        }

        public void onSuccess(DescriptionLevel[] possibleLevels) {
          init(possibleLevels);
        }

      });
    }

    layout.addStyleName("wui-editor-level");
  }

  private void init(DescriptionLevel[] possibleLevels) {

    for (int i = 0; i < possibleLevels.length; i++) {
      layout.addItem(Dissemination.getInstance().getElementLevelTranslation(possibleLevels[i]),
        possibleLevels[i].getLevel());
    }
    initialized = true;
    for (Command command : waitingForInitialization) {
      command.execute();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor
   * #getValue()
   */
  public EadCValue getValue() {
    return new DescriptionLevel(layout.getValue(layout.getSelectedIndex()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor
   * #getWidget()
   */
  public Widget getWidget() {
    return layout;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor
   * #isEmpty()
   */
  public boolean isEmpty() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor
   * #setValue(pt.gov.dgarq.roda.core.data.eadc.EadCValue)
   */
  public void setValue(EadCValue value) {
    if (value != null && value instanceof DescriptionLevel) {
      final DescriptionLevel level = (DescriptionLevel) value;
      if (initialized) {
        setValue(level);
      } else {
        waitingForInitialization.add(new Command() {

          public void execute() {
            setValue(level);
          }

        });
      }
    }
  }

  private void setValue(DescriptionLevel level) {
    boolean foundit = false;
    for (int i = 0; i < layout.getItemCount() && !foundit; i++) {
      if (level.getLevel().equals(layout.getValue(i))) {
        layout.setSelectedIndex(i);
        foundit = true;
      }
    }
    if (!foundit) {
      logger.error("Tryed to set level to a illegal value: '" + level + "'");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.google.gwt.user.client.ui.SourcesChangeEvents#addChangeListener(com
   * .google.gwt.user.client.ui.ChangeListener)
   */
  public void addChangeListener(ChangeListener listener) {
    layout.addChangeListener(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.user.client.ui.SourcesChangeEvents#removeChangeListener
   * (com.google.gwt.user.client.ui.ChangeListener)
   */
  public void removeChangeListener(ChangeListener listener) {
    layout.removeChangeListener(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor
   * #isValid()
   */
  public boolean isValid() {
    return true;
  }
}
