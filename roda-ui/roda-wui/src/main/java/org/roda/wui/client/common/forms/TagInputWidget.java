package org.roda.wui.client.common.forms;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.TextBox;
import org.roda.wui.client.common.utils.Tagify;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class TagInputWidget extends TextBox {

  private Tagify tagifyInstance;
  private List<String> pendingTags; // Stores tags if model is loaded before DOM attachment

  public TagInputWidget() {
    super();
    this.addStyleName("form-textbox");

    this.addAttachHandler(event -> {
      if (event.isAttached() && tagifyInstance == null) {
        // Initialize the JS library once the element is natively in the DOM
        tagifyInstance = new Tagify(this.getElement());

        // If the form set the model before attachment, apply it now
        if (pendingTags != null && !pendingTags.isEmpty()) {
          tagifyInstance.addTags(pendingTags.toArray(new String[0]));
          pendingTags = null; // Clear queue
        }
      }
    });
  }

  /**
   * Reads Tagify's hidden JSON value and converts it back to a List<String>
   */
  public List<String> getTags() {
    List<String> tags = new ArrayList<>();
    String jsonText = this.getText();

    if (jsonText != null && !jsonText.isEmpty()) {
      try {
        JSONArray jsonArray = JSONParser.parseStrict(jsonText).isArray();
        if (jsonArray != null) {
          for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.get(i).isObject();
            if (obj != null && obj.containsKey("value")) {
              tags.add(obj.get("value").isString().stringValue());
            }
          }
        }
      } catch (Exception e) {
        // Safe to ignore: occurs briefly if field is cleared or mid-typing
      }
    }
    return tags;
  }

  /**
   * Sets the tags visually. Handles pre-DOM and post-DOM attachment states.
   */
  public void setTags(List<String> tags) {
    if (tagifyInstance != null) {
      tagifyInstance.removeAllTags();
      if (tags != null && !tags.isEmpty()) {
        tagifyInstance.addTags(tags.toArray(new String[0]));
      }
    } else {
      // Store them to apply when the attach handler fires
      this.pendingTags = tags;
    }
  }
}
