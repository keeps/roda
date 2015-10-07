package org.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.StatisticsConstants;

/**
 * Picker for time segmentation
 * 
 * @author Luis Faria
 * 
 */
public class SegmentationPicker extends Composite implements SourcesChangeEvents {
  private static final String RADIO_GROUP = "SEGMENTATION";

  // layout
  private StatisticsConstants constants = (StatisticsConstants) GWT.create(StatisticsConstants.class);

  private final HorizontalPanel layout;
  private final List<RadioButton> options;
  private final List<ChangeListener> listeners;

  // data
  private Segmentation segmentation;

  /**
   * Create a new segmentation picker
   */
  public SegmentationPicker() {
    this(Segmentation.values()[0]);
  }

  /**
   * Create a new segmentation picker
   * 
   * @param segmentation
   *          the selected value
   */
  public SegmentationPicker(Segmentation segmentation) {
    this.layout = new HorizontalPanel();
    this.options = new ArrayList<RadioButton>();
    this.listeners = new ArrayList<ChangeListener>();

    for (final Segmentation s : Segmentation.values()) {
      String segmentationLabel;
      try {
        segmentationLabel = constants.getString("segmentation_" + s.toString());
      } catch (MissingResourceException e) {
        segmentationLabel = s.toString();
      }
      RadioButton radio = new RadioButton(RADIO_GROUP, segmentationLabel);
      options.add(radio);
      layout.add(radio);

      radio.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          setSegmentation(s);
          onChange();
        }

      });
    }

    setSegmentation(segmentation);

    initWidget(layout);

  }

  /**
   * Get current selected segmentation
   * 
   * @return the selected segmentation
   */
  public Segmentation getSegmentation() {
    return segmentation;
  }

  /**
   * Set selected segmentation
   * 
   * @param segmentation
   */
  public void setSegmentation(Segmentation segmentation) {
    if (this.segmentation == null || !this.segmentation.equals(segmentation)) {
      int index = Arrays.asList(Segmentation.values()).indexOf(segmentation);
      RadioButton radio = options.get(index);
      radio.setChecked(true);

      this.segmentation = segmentation;

      onChange();
    }

  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  protected void onChange() {
    for (ChangeListener listener : listeners) {
      listener.onChange(this);
    }
  }
}
