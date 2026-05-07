package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.wui.client.common.panels.GenericCollapsibleCardPanel;
import org.roda.wui.client.services.Services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobParametersPanel extends GenericCollapsibleCardPanel<IndexedJob> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final IndexedJob job;
  private final String i18nPluginName;
  private final Map<String, PluginInfo> pluginsInfo;
  private List<PluginParameter> pluginParameters;

  public JobParametersPanel(IndexedJob job, Map<String, PluginInfo> pluginsInfo, List<PluginParameter> parameters,
    String i18nPluginName) {
    super(true, false);
    this.job = job;
    this.i18nPluginName = i18nPluginName;
    this.pluginsInfo = pluginsInfo;
    this.pluginParameters = parameters;
    setData(job);
  }

  @Override
  protected void defineHeader(IndexedJob data) {
    // Use the HeaderBuilder to recreate the layout from your image
    buildHeader(messages.logEntryParameters()).build();
  }

  @Override
  protected void buildFields(IndexedJob data) {
    buildField(messages.jobPlugin()).withValue(i18nPluginName).build();
    for (PluginParameter parameter : pluginParameters) {
      if (PluginParameter.PluginParameterType.BOOLEAN.equals(parameter.getType())) {
        createBooleanLayout(parameter);
      } else if (PluginParameter.PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
        createPluginSipToAipLayout(parameter);
      } else if (PluginParameter.PluginParameterType.AIP_ID.equals(parameter.getType())) {
        createParentLayout(parameter);
      } else if (PluginParameter.PluginParameterType.CONVERSION_PROFILE.equals(parameter.getType())) {
        createConvertProfileLayout(parameter);
      } else if (PluginParameter.PluginParameterType.CONVERSION.equals(parameter.getType())) {
        createConversionLayout(parameter);
      } else {
        createStringLayout(parameter);
      }
    }
  }

  private void createConversionLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    Map<String, String> map = new HashMap<>();
    String[] keyValuePairs = value.split(";");
    for (String pair : keyValuePairs) {
      String[] parts = pair.split("=");
      if (parts.length == 2) {
        map.put(parts[0].trim(), parts[1].trim());
      }
    }

    String profile = job.getPluginParameters().get(RodaConstants.PLUGIN_PARAMS_CONVERSION_PROFILE);
    buildField("Conversion Profile").withValue(profile).build();

    String profileOptions = job.getPluginParameters().get("parameter.option." + profile);
    String content = profileOptions.substring(1, profileOptions.length() - 1);
    String[] profileOptionsArray = content.split(", ");

    for (String optionName : profileOptionsArray) {
      String optionNameCleaned = optionName.split("\\.")[1].replace("_", " ");
      String optionNameCapitalized = optionNameCleaned.substring(0, 1).toUpperCase() + optionNameCleaned.substring(1);
      String optionValue = job.getPluginParameters().get(optionName);

      buildField(optionNameCapitalized).withValue(optionValue).build();
    }

    if (map.get("type").equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION)) {
      String title = map.get("title");
      String description = map.get("description");

      buildField("Dissemination title").withValue(title).build();
      buildField("Dissemination description").withValue(description).build();
    } else {
      String representationType = map.get("value");
      buildField("Representation type").withValue(representationType).build();
    }
  }

  private void createConvertProfileLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    if (value == null) {
      buildField(parameter.getName()).withValue(parameter.getDefaultValue()).build();
    } else {
      String profileOptions = job.getPluginParameters().get("parameter.option." + value);
      String content = profileOptions.substring(1, profileOptions.length() - 1);
      String[] profileOptionsArray = content.split(", ");

      for (String profileOption : profileOptionsArray) {
        String profileOptionName = profileOption.split("\\.")[1];
        buildField(profileOptionName).withValue(job.getPluginParameters().get(profileOption)).build();
      }
    }
  }

  private void createStringLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().containsKey(parameter.getId())
            ? job.getPluginParameters().get(parameter.getId())
            : parameter.getDefaultValue();

    buildField(parameter.getName()).withValue(value).build();
  }

  private void createParentLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().containsKey(parameter.getId())
            ? job.getPluginParameters().get(parameter.getId())
            : parameter.getDefaultValue();

    GenericCollapsibleCardPanel<IndexedJob>.FieldBuilder fieldBuilder = buildField(parameter.getName());

    if (value != null && !value.isEmpty()) {
      Services services = new Services("Retrieve AIP", "get");
      services.aipResource(s -> s.findByUuid(value, LocaleInfo.getCurrentLocale().getLocaleName()))
              .whenComplete((indexedAIP, throwable) -> {
                if (throwable != null) {
                  if (throwable.getCause() instanceof NotFoundException) {
                    fieldBuilder.withValue(value + " (Not found)");
                  } else {
                    fieldBuilder.withValue(value + " (Error retrieving AIP)");
                  }
                } else {
                  fieldBuilder.withValue(indexedAIP.getTitle());
                }
              });
    } else {
      fieldBuilder.withValue("Catalogue");
    }

    fieldBuilder.build();
  }

  private void createPluginSipToAipLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());

    PluginInfo sipToAipPlugin = pluginsInfo.get(value);

    if (sipToAipPlugin != null) {
      value = messages.pluginLabelWithVersion(sipToAipPlugin.getName(), sipToAipPlugin.getVersion());
      buildField(parameter.getName()).withValue(value).build();
    } else {
      buildField(parameter.getName()).withValue(value).build();
    }
  }

  private void createBooleanLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().containsKey(parameter.getId())
      ? job.getPluginParameters().get(parameter.getId())
      : parameter.getDefaultValue();

    String cssClass = "label-default";
    if (value != null && !value.isEmpty()) {
      if ("true".equalsIgnoreCase(value)) {
        value = "Enabled";
        cssClass = "label-success";
      } else {
        value = "Disabled";
        cssClass = "label-default";
      }
    }


    buildField(parameter.getName()).withBadge(value, cssClass).build();
  }
}
