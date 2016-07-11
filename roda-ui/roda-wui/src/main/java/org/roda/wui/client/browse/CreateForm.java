/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;

/**
 * Created by adrapereira on 13-06-2016.
 */
public class CreateForm {

  public static void create(FlowPanel panel, SupportedMetadataTypeBundle bundle) {
    for (MetadataValue mv : bundle.getValues()) {
      if (mv.get("hidden") != null && mv.get("hidden").equals("true"))
        continue;

      FlowPanel layout = new FlowPanel();
      layout.addStyleName("plugin-options-parameter");
      String controlType = mv.get("type");
      if (controlType == null) {
        addTextField(panel, layout, mv);
      } else {
        switch (controlType) {
          case "text":
            addTextField(panel, layout, mv);
            break;
          case "textarea":
          case "big-text":
          case "text-area":
            addTextArea(panel, layout, mv);
            break;
          case "list":
            addList(panel, layout, mv);
            break;
          case "date":
            addDatePicker(panel, layout, mv);
            break;
          default:
            addTextField(panel, layout, mv);
            break;
        }
      }
    }
  }

  private static String getFieldLabel(MetadataValue mv) {
    String result = mv.getId();
    String rawLabel = mv.get("label");
    if (rawLabel != null && rawLabel.length() > 0) {
      String loc = LocaleInfo.getCurrentLocale().getLocaleName();
      try {
        JSONObject jsonObject = JSONParser.parseLenient(rawLabel).isObject();
        JSONValue jsonValue = jsonObject.get(loc);
        if (jsonValue != null) {
          JSONString jsonString = jsonObject.get(loc).isString();

          if (jsonString != null) {
            result = jsonString.stringValue();
          }
        } else {
          // label for the desired language doesn't exist
          // do nothing
        }
      } catch (JSONException e) {
        // The JSON was malformed
        // do nothing
      }
    }
    return result;
  }

  private static void addTextField(FlowPanel panel, final FlowPanel layout, final MetadataValue mv) {
    // Top label
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final TextBox mvText = new TextBox();
    mvText.addStyleName("form-textbox");
    if (mv.get("value") != null) {
      mvText.setText(mv.get("value"));
    }
    mvText.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        mv.set("value", mvText.getValue());
      }
    });

    layout.add(mvLabel);
    layout.add(mvText);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    panel.add(layout);
  }

  private static void addTextArea(FlowPanel panel, final FlowPanel layout, final MetadataValue mv) {
    // Top label
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final TextArea mvText = new TextArea();
    mvText.addStyleName("form-textbox metadata-form-text-area");
    if (mv.get("value") != null) {
      mvText.setText(mv.get("value"));
    }
    mvText.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        mv.set("value", mvText.getValue());
      }
    });

    layout.add(mvLabel);
    layout.add(mvText);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    panel.add(layout);
  }

  private static void addList(FlowPanel panel, final FlowPanel layout, final MetadataValue mv) {
    // Top Label
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final ListBox mvList = new ListBox();
    mvList.addStyleName("form-textbox");

    String list = mv.get("list");
    if (list != null) {
      JSONArray jsonArray = JSONParser.parseLenient(list).isArray();
      for (int i = 0; i < jsonArray.size(); i++) {
        String value = jsonArray.get(i).isString().stringValue();
        mvList.addItem(value);

        if (value.equals(mv.get("value"))) {
          mvList.setSelectedIndex(i);
        }
      }
    }

    mvList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        mv.set("value", mvList.getSelectedValue());
      }
    });

    if (mv.get("value") == null || mv.get("value").isEmpty()) {
      mvList.setSelectedIndex(0);
      mv.set("value", mvList.getSelectedValue());
    }

    layout.add(mvLabel);
    layout.add(mvList);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    panel.add(layout);
  }

  private static void addDatePicker(FlowPanel panel, final FlowPanel layout, final MetadataValue mv) {
    // Top label
    final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final DateBox mvDate = new DateBox();
    mvDate.getDatePicker().setYearAndMonthDropdownVisible(true);
    mvDate.getDatePicker().setYearArrowsVisible(true);
    mvDate.addStyleName("form-textbox");
    mvDate.setFormat(new DateBox.DefaultFormat() {
      @Override
      public String format(DateBox dateBox, Date date) {
        if (date == null)
          return null;
        return dateTimeFormat.format(date);
      }
    });
    if (mv.get("value") != null && mv.get("value").length() > 0) {
      Date date = dateTimeFormat.parse(mv.get("value").trim());
      mvDate.setValue(date);
    }
    mvDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
      @Override
      public void onValueChange(ValueChangeEvent<Date> valueChangeEvent) {
        String newValue = dateTimeFormat.format(mvDate.getValue());
        mv.set("value", newValue);
      }
    });

    layout.add(mvLabel);
    layout.add(mvDate);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    panel.add(layout);
  }
}
