/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.advanced.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.AdvancedSearchMessages;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import pt.gov.dgarq.roda.wui.dissemination.search.advanced.client.DescriptiveFieldChooser.FieldOption;
import pt.gov.dgarq.roda.wui.dissemination.search.advanced.client.KeywordPicker.KeywordParameter;

/**
 * @author Luis Faria
 * 
 */
public abstract class KeywordItem extends AccessibleFocusPanel {

  private static AdvancedSearchMessages messages = (AdvancedSearchMessages) GWT.create(AdvancedSearchMessages.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  // private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

  private final FlowPanel layout;

  private final KeywordParameter keywordParameter;

  private final CheckBox checkbox;

  private final Image removeButton;

  /**
   * Create new keyword item
   * 
   * @param keywordParameter
   */
  public KeywordItem(KeywordParameter keywordParameter) {
    this.keywordParameter = keywordParameter;
    layout = new FlowPanel();
    checkbox = new CheckBox(createDescription(), true);
    checkbox.setChecked(true);
    checkbox.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        if (checkbox.isChecked()) {
          KeywordItem.this.removeStyleDependentName("unchecked");
        } else {
          KeywordItem.this.addStyleDependentName("unchecked");
        }
      }

    });
    removeButton = commonImageBundle.minusLight().createImage();
    removeButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        onKeywordRemove();
      }

    });

    setWidget(layout);
    layout.add(checkbox);
    layout.add(removeButton);

    // layout.setCellVerticalAlignment(checkbox, HasAlignment.ALIGN_TOP);
    setStylePrimaryName("wui-keywordItem");
    checkbox.addStyleName("description");
    removeButton.addStyleName("remove");
  }

  private String createDescription() {
    String ret;
    String[] keywords;
    switch (keywordParameter.constraint) {
      case DefaultSearchParameter.MATCH_ALL_WORDS:
        keywords = keywordParameter.keyword.split(" ");
        if (keywordParameter.hasAllFields) {
          ret = messages.allWordsInAllFields(keywordList(keywords, true));
        } else {
          ret = messages.allWordsIn(keywordList(keywords, true), fieldList(keywordParameter.fields));
        }
        break;
      case DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD:
        keywords = keywordParameter.keyword.split(" ");
        if (keywordParameter.hasAllFields) {
          ret = messages.atLeastOneWordInAllFields(keywordList(keywords, false));
        } else {
          ret = messages.atLeastOneWordIn(keywordList(keywords, false), fieldList(keywordParameter.fields));
        }
        break;
      case DefaultSearchParameter.MATCH_EXACT_PHRASE:
        if (keywordParameter.hasAllFields) {
          ret = messages.exactPhraseInAllFields("<strong>\"" + keywordParameter.keyword + "\"</strong>");
        } else {
          ret = messages.exactPhraseIn("<strong>\"" + keywordParameter.keyword + "\"</strong>",
            fieldList(keywordParameter.fields));
        }
        break;

      default: /* DONT_MATCH_WORDS */
        keywords = keywordParameter.keyword.split(" ");
        if (keywordParameter.hasAllFields) {
          ret = messages.withoutWordsInAllFields(keywordList(keywords, true));
        } else {
          ret = messages.withoutWordsIn(keywordList(keywords, true), fieldList(keywordParameter.fields));
        }
        break;
    }

    return ret;
  }

  private String keywordList(String[] list, boolean exclusive) {
    String ret = "";

    for (int i = 0; i < list.length; i++) {
      if (i == 0) {
        ret = "<strong>\"" + list[i] + "\"</strong>";
      } else {
        if (i < list.length - 1) {
          ret = messages.listSeparatorComma(ret, "<strong>\"" + list[i] + "\"</strong>");
        } else {
          if (exclusive) {
            ret = messages.listSeparatorAnd(ret, "<strong>\"" + list[i] + "\"</strong>");
          } else {
            ret = messages.listSeparatorOr(ret, "<strong>\"" + list[i] + "\"</strong>");
          }
        }
      }
    }

    return ret;
  }

  private String fieldList(List<FieldOption> fieldList) {
    String ret = "";

    for (int i = 0; i < fieldList.size(); i++) {
      if (i == 0) {
        ret = fieldList.get(i).getDescription();
      } else {
        if (i < fieldList.size() - 1) {
          ret = messages.listSeparatorComma(ret, fieldList.get(i).getDescription());
        } else {
          ret = messages.listSeparatorOr(ret, fieldList.get(i).getDescription());
        }
      }
    }

    return ret;
  }

  protected abstract void onKeywordRemove();

  /**
   * Get keyword parameter
   * 
   * @return the keyword parameter
   */
  public KeywordParameter getKeywordParameter() {
    return keywordParameter;
  }

  /**
   * Is current keyword item checked
   * 
   * @return true if checked
   */
  public boolean isChecked() {
    return checkbox.isChecked();
  }

}
