package org.roda.wui.client.common.forms;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface GenericDataPanel<T> {

  boolean isValid();

  T getValue();
}
