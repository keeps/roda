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
package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.formats.Format;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.ActionableObject;
import org.roda.wui.client.common.actions.ActionableWidgetBuilder;
import org.roda.wui.client.common.actions.FormatActions;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowFormat extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(FormatRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "format";
    }
  };

  private static ShowFormat instance = null;

  interface MyUiBinder extends UiBinder<Widget, ShowFormat> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FORMAT_ID,
    RodaConstants.FORMAT_NAME, RodaConstants.FORMAT_DEFINITION, RodaConstants.FORMAT_CATEGORY,
    RodaConstants.FORMAT_LATEST_VERSION, RodaConstants.FORMAT_DEVELOPER, RodaConstants.FORMAT_POPULARITY,
    RodaConstants.FORMAT_INITIAL_RELEASE, RodaConstants.FORMAT_IS_OPEN_FORMAT, RodaConstants.FORMAT_STANDARD,
    RodaConstants.FORMAT_WEBSITE, RodaConstants.FORMAT_PROVENANCE_INFORMATION, RodaConstants.FORMAT_EXTENSIONS,
    RodaConstants.FORMAT_MIMETYPES, RodaConstants.FORMAT_PRONOMS, RodaConstants.FORMAT_UTIS,
    RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS, RodaConstants.FORMAT_VERSIONS);

  private Format format;

  @UiField
  Label formatId;

  @UiField
  Label formatName;

  @UiField
  Label formatDefinitionKey, formatDefinitionValue;

  @UiField
  Label formatCategoryKey;

  @UiField
  FlowPanel formatCategoryValue;

  @UiField
  Label formatLatestVersionKey, formatLatestVersionValue;

  @UiField
  Label formatPopularityKey, formatPopularityValue;

  @UiField
  Label formatDeveloperKey, formatDeveloperValue;

  @UiField
  Label formatInitialReleaseKey, formatInitialReleaseValue;

  @UiField
  Label formatStandardKey, formatStandardValue;

  @UiField
  Label formatIsOpenFormat;

  @UiField
  Label formatWebsiteKey;

  @UiField
  FlowPanel formatWebsiteValue;

  @UiField
  Label formatProvenanceInformationKey, formatProvenanceInformationValue;

  @UiField
  Label formatExtensionsKey;

  @UiField
  FlowPanel formatExtensionsValue;

  @UiField
  Label formatMimetypesKey;

  @UiField
  FlowPanel formatMimetypesValue;

  @UiField
  Label formatPronomsKey;

  @UiField
  FlowPanel formatPronomsValue;

  @UiField
  Label formatUtisKey;

  @UiField
  FlowPanel formatUtisValue;

  @UiField
  Label formatVersionsKey;

  @UiField
  FlowPanel formatVersionsValue;

  @UiField
  Label formatAlternativesKey;

  @UiField
  FlowPanel formatAlternativesValue;

  @UiField
  SimplePanel actionsSidebar;
  private ActionableWidgetBuilder<Format> actionableWidgetBuilder = new ActionableWidgetBuilder<>(FormatActions.get())
    .withCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (result.equals(Actionable.ActionImpact.DESTROYED)) {
          HistoryUtils.newHistory(FormatRegister.RESOLVER);
        }
      }
    });

  /**
   * Create a new panel to view a format
   */
  public ShowFormat() {
    this.format = new Format();
    initWidget(uiBinder.createAndBindUi(this));
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(format)));
  }

  public ShowFormat(Format format) {
    initWidget(uiBinder.createAndBindUi(this));
    this.format = format;
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(format)));

    formatId.setText(format.getId());
    formatName.setText(format.getName());

    formatDefinitionValue.setText(format.getDefinition());
    formatDefinitionKey.setVisible(StringUtils.isNotBlank(format.getDefinition()));

    List<String> categoryList = format.getCategories();
    formatCategoryValue.setVisible(categoryList != null && !categoryList.isEmpty());
    formatCategoryKey.setVisible(categoryList != null && !categoryList.isEmpty());

    if (categoryList != null) {
      for (String category : categoryList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(category));
        formatCategoryValue.add(parPanel);
      }
    }

    formatLatestVersionValue.setText(format.getLatestVersion());
    formatLatestVersionKey.setVisible(StringUtils.isNotBlank(format.getLatestVersion()));

    formatPopularityValue.setText(format.getPopularity() != null ? Integer.toString(format.getPopularity()) : "");
    formatPopularityKey.setVisible(format.getPopularity() != null);
    formatPopularityValue.setVisible(format.getPopularity() != null);

    formatDeveloperValue.setText(format.getDeveloper());
    formatDeveloperKey.setVisible(StringUtils.isNotBlank(format.getDeveloper()));

    formatInitialReleaseValue.setText(format.getInitialRelease() != null
      ? DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(format.getInitialRelease()) : "");
    formatInitialReleaseKey.setVisible(format.getInitialRelease() != null);
    formatInitialReleaseValue.setVisible(format.getInitialRelease() != null);

    formatIsOpenFormat
      .setText(format.isOpenFormat() ? messages.formatIsOpenFormat() : messages.formatIsNotOpenFormat());

    formatStandardValue.setText(format.getStandard());
    formatStandardKey.setVisible(StringUtils.isNotBlank(format.getStandard()));

    List<String> websiteList = format.getWebsites();
    formatWebsiteValue.setVisible(websiteList != null && !websiteList.isEmpty());
    formatWebsiteKey.setVisible(websiteList != null && !websiteList.isEmpty());

    if (websiteList != null) {
      for (String website : websiteList) {
        if (isValidUrl(website)) {
          Anchor anchor = new Anchor(website, false, website, "_blank");
          HTML br = new HTML();
          formatWebsiteValue.add(anchor);
          formatWebsiteValue.add(br);
        } else {
          HTML parPanel = new HTML();
          parPanel.setHTML(messages.formatListItems(website));
          formatCategoryValue.add(parPanel);
        }
      }
    }

    formatProvenanceInformationValue.setText(format.getProvenanceInformation());
    formatProvenanceInformationKey.setVisible(StringUtils.isNotBlank(format.getProvenanceInformation()));

    List<String> extensionsList = format.getExtensions();
    formatExtensionsValue.setVisible(extensionsList != null && !extensionsList.isEmpty());
    formatExtensionsKey.setVisible(extensionsList != null && !extensionsList.isEmpty());

    if (extensionsList != null) {
      for (String extension : extensionsList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(extension));
        formatExtensionsValue.add(parPanel);
      }
    }

    List<String> mimetypesList = format.getMimetypes();
    formatMimetypesValue.setVisible(mimetypesList != null && !mimetypesList.isEmpty());
    formatMimetypesKey.setVisible(mimetypesList != null && !mimetypesList.isEmpty());

    if (mimetypesList != null) {
      for (String mimetype : mimetypesList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(mimetype));
        formatMimetypesValue.add(parPanel);
      }
    }

    List<String> pronomsList = format.getPronoms();
    formatPronomsValue.setVisible(pronomsList != null && !pronomsList.isEmpty());
    formatPronomsKey.setVisible(pronomsList != null && !pronomsList.isEmpty());

    if (pronomsList != null) {
      for (String pronom : pronomsList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(pronom));
        formatPronomsValue.add(parPanel);
      }
    }

    List<String> utisList = format.getUtis();
    formatUtisValue.setVisible(utisList != null && !utisList.isEmpty());
    formatUtisKey.setVisible(utisList != null && !utisList.isEmpty());

    if (utisList != null) {
      for (String uti : utisList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(uti));
        formatUtisValue.add(parPanel);
      }
    }

    List<String> alternatives = format.getAlternativeDesignations();
    formatAlternativesValue.setVisible(alternatives != null && !alternatives.isEmpty());
    formatAlternativesKey.setVisible(alternatives != null && !alternatives.isEmpty());

    if (alternatives != null) {
      for (String alt : alternatives) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(alt));
        formatAlternativesValue.add(parPanel);
      }
    }

    List<String> versions = format.getVersions();
    formatVersionsValue.setVisible(versions != null && !versions.isEmpty());
    formatVersionsKey.setVisible(versions != null && !versions.isEmpty());

    if (versions != null) {
      for (String version : versions) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.formatListItems(version));
        formatVersionsValue.add(parPanel);
      }
    }
  }

  public static ShowFormat getInstance() {
    if (instance == null) {
      instance = new ShowFormat();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  // Java method
  public native boolean isValidUrl(String url) /*-{
		var pattern = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		return pattern.test(url);
  }-*/;

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      String formatId = historyTokens.get(0);
      BrowserService.Util.getInstance().retrieve(Format.class.getName(), formatId, fieldsToReturn,
        new AsyncCallback<Format>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(Format result) {
            ShowFormat formatPanel = new ShowFormat(result);
            callback.onSuccess(formatPanel);
          }
        });
    } else {
      HistoryUtils.newHistory(FormatRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }
}
