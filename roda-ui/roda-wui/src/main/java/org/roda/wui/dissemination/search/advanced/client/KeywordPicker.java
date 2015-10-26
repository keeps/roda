/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.dissemination.search.advanced.client;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.SearchParameter;
import org.roda.core.data.search.DefaultSearchParameter;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.dissemination.search.advanced.client.DescriptiveFieldChooser.FieldOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.AdvancedSearchConstants;

/**
 * @author Luis Faria
 * 
 */
public abstract class KeywordPicker extends SimplePanel {

  private static AdvancedSearchConstants constants = (AdvancedSearchConstants) GWT
    .create(AdvancedSearchConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  /**
   * Keyword parameter for search
   * 
   */
  public class KeywordParameter {

    /**
     * is all fields option selected
     */
    public boolean hasAllFields;

    /**
     * Option fields
     */
    public List<FieldOption> fields;

    /**
     * Keyword to search
     */
    public String keyword;

    /**
     * Constraint type
     */
    public int constraint;

    /**
     * Create a new keyword parameter
     * 
     * @param keyword
     *          the keyword to search for
     * @param constraint
     *          the constraint type
     */
    public KeywordParameter(String keyword, int constraint) {
      this.hasAllFields = true;
      this.fields = fieldChooser.getAllFields();
      this.keyword = keyword;
      this.constraint = constraint;
    }

    /**
     * Create a new keyword parameter
     * 
     * @param fields
     *          the option fields to allow
     * @param keyword
     *          the keyword to search for
     * @param constraint
     *          the constraint type
     */
    public KeywordParameter(List<FieldOption> fields, String keyword, int constraint) {
      this.hasAllFields = false;
      this.fields = fields;
      this.keyword = keyword;
      this.constraint = constraint;
    }

    /**
     * Get search parameter
     * 
     * @return the search parameter
     */
    public SearchParameter getSearchParameter() {
      SearchParameter searchParameter;
      if (hasAllFields) {
        searchParameter = new DefaultSearchParameter(fieldChooser.getAllFieldNames(), keyword, constraint);
      } else {
        List<String> fieldNames = new ArrayList<String>();
        for (FieldOption fieldOption : fields) {
          fieldNames.add(fieldOption.getFieldName());
        }
        searchParameter = new DefaultSearchParameter((String[]) fieldNames.toArray(new String[] {}), keyword,
          constraint);
      }
      return searchParameter;
    }

  }

  private final DockPanel layout;

  private final HorizontalPanel centralLayout;

  private final VerticalPanel keywordsLayout;

  private final TextBox keywordBox;

  private final HorizontalPanel constraintsLayout;

  private final RadioButton allWordsConstraint;

  private final RadioButton exactPhraseModifier;

  private final RadioButton atLeastOneWordConstraint;

  private final RadioButton withoutWordsModifier;

  private final VerticalPanel fieldsOptionLayout;

  private final RadioButton allFieldsOption;

  private final RadioButton chooseFieldsOption;

  private final DescriptiveFieldChooser fieldChooser;

  private final Image addButton;

  /**
   * Create a new keyword picker
   */
  public KeywordPicker() {
    layout = new DockPanel();
    keywordsLayout = new VerticalPanel();
    keywordBox = new TextBox();

    constraintsLayout = new HorizontalPanel();
    allWordsConstraint = new RadioButton("search-modifiers", constants.allWords());
    exactPhraseModifier = new RadioButton("search-modifiers", constants.exactPhrase());
    atLeastOneWordConstraint = new RadioButton("search-modifiers", constants.atLeastOneWord());
    withoutWordsModifier = new RadioButton("search-modifiers", constants.withoutWords());

    constraintsLayout.add(allWordsConstraint);
    constraintsLayout.add(exactPhraseModifier);
    constraintsLayout.add(atLeastOneWordConstraint);
    constraintsLayout.add(withoutWordsModifier);

    keywordsLayout.add(keywordBox);
    keywordsLayout.add(constraintsLayout);

    fieldsOptionLayout = new VerticalPanel();
    allFieldsOption = new RadioButton("fields-option", constants.allFields());
    chooseFieldsOption = new RadioButton("fields-option", constants.chooseFields());

    fieldsOptionLayout.add(allFieldsOption);
    fieldsOptionLayout.add(chooseFieldsOption);

    fieldChooser = new DescriptiveFieldChooser();
    fieldChooser.setVisible(false);

    addButton = commonImageBundle.plusLight().createImage();

    addButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        onKeywordAdd();
      }

    });

    keywordBox.addKeyboardListener(new KeyboardListener() {

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        if (keyCode == KEY_ENTER) {
          onKeywordAdd();
        }
      }

    });

    centralLayout = new HorizontalPanel();
    centralLayout.add(fieldsOptionLayout);
    centralLayout.add(keywordsLayout);

    setWidget(layout);
    layout.add(centralLayout, DockPanel.CENTER);
    layout.add(addButton, DockPanel.EAST);
    layout.add(fieldChooser, DockPanel.SOUTH);

    allWordsConstraint.setChecked(true);
    allFieldsOption.setChecked(true);

    ClickListener showFieldChooser = new ClickListener() {

      public void onClick(Widget sender) {
        fieldChooser.setVisible(chooseFieldsOption.isChecked());
      }

    };

    chooseFieldsOption.addClickListener(showFieldChooser);
    allFieldsOption.addClickListener(showFieldChooser);

    constraintsLayout.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    this.addStyleName("wui-keywordPicker");
    layout.addStyleName("wui-keywordPicker-layout");
    centralLayout.addStyleName("wui-keywordPicker-layout-central");
    keywordBox.addStyleName("keywords");
    keywordsLayout.addStyleName("keywords-layout");
    constraintsLayout.addStyleName("modifiers");
    allWordsConstraint.addStyleName("modifier");
    atLeastOneWordConstraint.addStyleName("modifier");
    exactPhraseModifier.addStyleName("modifier");
    withoutWordsModifier.addStyleName("modifier");
    fieldsOptionLayout.addStyleName("fieldsOption");
    addButton.addStyleName("add");

  }

  /**
   * Action done when add button clicked
   * 
   * @return return true if keyword added, false otherwise
   */
  public boolean onKeywordAdd() {
    boolean success;
    if (keywordBox.getText().length() == 0) {
      Window.alert(constants.addingKeywordButItsEmptyAlert());
      success = false;
    } else if (chooseFieldsOption.isChecked() && fieldChooser.getSelected().size() == 0) {
      Window.alert(constants.addingKeywordButNoFieldsAlert());
      success = false;
    } else {
      onKeywordAdd(getKeywordParameter());
      success = true;
    }
    return success;
  }

  protected int getConstraint() {
    int constraint;
    if (allWordsConstraint.isChecked()) {
      constraint = DefaultSearchParameter.MATCH_ALL_WORDS;
    } else if (atLeastOneWordConstraint.isChecked()) {
      constraint = DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD;
    } else if (exactPhraseModifier.isChecked()) {
      constraint = DefaultSearchParameter.MATCH_EXACT_PHRASE;
    } else {
      constraint = DefaultSearchParameter.MATCH_WITHOUT_WORDS;
    }
    return constraint;
  }

  protected abstract void onKeywordAdd(KeywordParameter searchParameter);

  /**
   * Get the search parameter
   * 
   * @return the search parameter
   */
  public DefaultSearchParameter getSearchParameter() {
    String[] fields;
    if (allFieldsOption.isChecked()) {
      fields = fieldChooser.getAllFieldNames();
    } else {
      fields = fieldChooser.getSelectedFieldNames();
    }

    return new DefaultSearchParameter(fields, keywordBox.getText(), getConstraint());
  }

  /**
   * Get keyword parameter
   * 
   * @return the keyword parameter
   */
  public KeywordParameter getKeywordParameter() {
    KeywordParameter keywordParameter;
    if (allFieldsOption.isChecked()) {
      keywordParameter = new KeywordParameter(keywordBox.getText(), getConstraint());
    } else {
      keywordParameter = new KeywordParameter(fieldChooser.getSelected(), keywordBox.getText(), getConstraint());
    }

    return keywordParameter;
  }

  public void clear() {
    keywordBox.setText("");
    allFieldsOption.setChecked(true);
    fieldChooser.setVisible(false);
    allWordsConstraint.setChecked(true);
  }

}
