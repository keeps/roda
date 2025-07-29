/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.ParentWhichFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCustomForm;
import org.roda.core.data.v2.ri.RepresentationInformationFamily;
import org.roda.core.data.v2.ri.RepresentationInformationFilterRequest;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.core.data.v2.ri.RepresentationInformationRelationOptions;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.maintenance.AddRepresentationInformationFilterPlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
public class RepresentationInformationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationInformationService.class);

  private IndexService indexService;

  @Autowired
  public void setIndexService(IndexService service) {
    this.indexService = service;
  }

  public Job deleteRepresentationInformationByJob(SelectedItems<RepresentationInformation> selectedItems, User user)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return CommonServicesUtils.createAndExecuteInternalJob("Delete representation information", selectedItems,
      DeleteRODAObjectPlugin.class, user, Collections.emptyMap(),
      "Could not execute delete transferred resources action");
  }

  public Job updateRepresentationInformationListWithFilter(RepresentationInformationFilterRequest request, User user)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER, request.getFilterToAdd());
    return CommonServicesUtils.createAndExecuteInternalJob("Update representation information with filter",
      request.getSelectedItems(), AddRepresentationInformationFilterPlugin.class, user, pluginParameters,
      "Could not update representation information with filter " + request.getFilterToAdd());
  }

  public boolean validateRepresentationInformation(RepresentationInformation representationInformation) {
    return !StringUtils.isBlank(representationInformation.getName());
  }

  public RepresentationInformation createRepresentationInformation(RepresentationInformation ri,
    RepresentationInformationCustomForm form, RequestContext requestContext, boolean commit)
    throws GenericException, AuthorizationDeniedException, NotFoundException {
    if (form != null) {
      ri.setExtras(getRepresentationInformationExtra(form, ri.getFamily()));
    }
    return requestContext.getModelService().createRepresentationInformation(ri, requestContext.getUser().getName(),
      commit);
  }

  public RepresentationInformation updateRepresentationInformation(RepresentationInformation ri,
    RepresentationInformationCustomForm form, RequestContext requestContext, boolean commit)
    throws NotFoundException, AuthorizationDeniedException, GenericException {
    if (form != null) {
      ri.setExtras(getRepresentationInformationExtra(form, ri.getFamily()));
    }
    return requestContext.getModelService().updateRepresentationInformation(ri, requestContext.getUser().getName(),
      commit);
  }

  private String getRepresentationInformationExtra(RepresentationInformationCustomForm form, String family)
    throws NotFoundException {
    Handlebars handlebars = new Handlebars();
    Map<String, String> data = new HashMap<>();
    handlebars.registerHelper("field", (o, options) -> options.fn());

    try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.METADATA_REPRESENTATION_INFORMATION_TEMPLATE_FOLDER + "/" + family + ".xml.hbs")) {
      String rawTemplate = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
      Template tmpl = handlebars.compileInline(rawTemplate);

      Set<MetadataValue> values = form.getValues();
      if (values != null) {
        values.forEach(metadataValue -> {
          String val = metadataValue.get("value");
          if (val != null) {
            val = val.replaceAll("\\s", "");
            if (!val.isEmpty()) {
              data.put(metadataValue.get("name"), metadataValue.get("value"));
            }
          }
        });
      }

      return tmpl.apply(data);
    } catch (IOException e) {
      LOGGER.error("Error getting template from stream", e);
      throw new NotFoundException("Unable to find the template for the representation information family: " + family);
    }
  }

  public RepresentationInformationFamily retrieveRepresentationInformationFamilyConfigurations(String familyType,
    String localeString) throws NotFoundException, GenericException {
    Locale locale = ServerTools.parseLocale(localeString);
    List<SupportedMetadataTypeBundle> supportedMetadataTypeBundles = retrieveSupportedMetadataTypes(
      RodaCoreFactory.getRodaConfigurationAsList("ui.ri.family"), locale);

    SupportedMetadataTypeBundle searchResult = supportedMetadataTypeBundles.stream()
      .filter(p -> p.getType().equals(familyType)).findFirst().orElseThrow(() -> new NotFoundException(
        "The family type provided didn't had any match with the configurable representation families"));

    RepresentationInformationFamily representationInformationFamily = new RepresentationInformationFamily();
    representationInformationFamily.setFamilyValues(searchResult.getValues());

    return representationInformationFamily;
  }

  public RepresentationInformationFamily retrieveRepresentationInformationFamily(ModelService model,
    String representationInformationId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Locale locale = ServerTools.parseLocale(localeString);
    List<SupportedMetadataTypeBundle> supportedMetadataTypeBundles = retrieveSupportedMetadataTypes(
      RodaCoreFactory.getRodaConfigurationAsList("ui.ri.family"), locale);

    RepresentationInformation ri = model.retrieveRepresentationInformation(representationInformationId);

    SupportedMetadataTypeBundle searchResult = supportedMetadataTypeBundles.stream()
      .filter(p -> p.getType().equals(ri.getFamily())).findFirst().orElseThrow(() -> new NotFoundException(
        "The family type provided didn't had any match with the configurable representation families"));

    Set<MetadataValue> familyValues = new TreeSet<>(parseMetadataValues(searchResult.getValues(), ri.getExtras()));

    RepresentationInformationFamily representationInformationFamily = new RepresentationInformationFamily();
    representationInformationFamily.setFamilyValues(familyValues);

    return representationInformationFamily;
  }

  private List<SupportedMetadataTypeBundle> retrieveSupportedMetadataTypes(List<String> types, Locale locale)
    throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    List<SupportedMetadataTypeBundle> supportedMetadata = new ArrayList<>();
    if (types != null) {
      for (String id : types) {
        String type = id;
        String version = null;
        if (id.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
          version = id.substring(id.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1);
          type = id.substring(0, id.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
        }
        String key = RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + type;
        if (version != null) {
          key += RodaConstants.METADATA_VERSION_SEPARATOR + version.toLowerCase();
        }
        String label = messages.getTranslation(key, type);

        String template = null;
        Set<MetadataValue> values = new HashSet<>();
        try (InputStream templateStream = RodaCoreFactory
          .getConfigurationFileAsStream(RodaConstants.METADATA_REPRESENTATION_INFORMATION_TEMPLATE_FOLDER + "/"
            + ((version != null) ? type + RodaConstants.METADATA_VERSION_SEPARATOR + version : type)
            + RodaConstants.METADATA_TEMPLATE_EXTENSION)) {

          if (templateStream != null) {
            template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
            values = ServerTools.transform(template);
            for (MetadataValue mv : values) {
              String generator = mv.get("auto-generate");
              if (generator != null && !generator.isEmpty()) {
                mv.set("value", "");
              }
              String labels = mv.get("label");
              String labelI18N = mv.get("labeli18n");
              if (labels != null && labelI18N != null) {
                Map<String, String> labelsMaps = JsonUtils.getMapFromJson(labels);
                try {
                  labelsMaps.put(locale.toString(), RodaCoreFactory.getI18NMessages(locale).getTranslation(labelI18N));
                } catch (MissingResourceException e) {
                  LOGGER.debug("Missing resource: {}", labelI18N);
                }
                labels = JsonUtils.getJsonFromObject(labelsMaps);
                mv.set("label", labels);
              }

              CommonServicesUtils.getMetadataValueLabels(mv, locale, messages);
              CommonServicesUtils.getMetadataValueI18nPrefix(mv, locale, messages);
            }
          }
        } catch (IOException e) {
          LOGGER.error("Error getting the template from the stream", e);
        }

        supportedMetadata.add(new SupportedMetadataTypeBundle(id, type, version, label, template, values));
      }
    }

    return supportedMetadata;
  }

  private Set<MetadataValue> parseMetadataValues(Set<MetadataValue> values, String xml) {
    Set<MetadataValue> result = new HashSet<>();

    if (values != null) {
      for (MetadataValue metadataValue : values) {
        String xpathRaw = metadataValue.get("xpath");
        if (xpathRaw != null && !xpathRaw.isEmpty()) {
          String[] xpaths = xpathRaw.split("##%##");
          String value = "";

          if (StringUtils.isNotBlank(xml)) {
            List<String> allValues = new ArrayList<>();
            for (String xpath : xpaths) {
              allValues.addAll(ServerTools.applyXpath(xml, xpath));
            }
            // if any of the values is different, concatenate all values in
            // a string, otherwise return the value
            boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.getFirst().trim()));
            if (allEqual && !allValues.isEmpty()) {
              value = allValues.getFirst();
            } else {
              value = String.join(" / ", allValues);
            }
          }

          metadataValue.set("value", value.trim());
          result.add(metadataValue);
        } else {
          result.add(metadataValue);
        }
      }
    }

    return result;
  }

  private Map<String, String> getInverses() {
    Map<String, String> map = new HashMap<>();
    for (RelationObjectType relationType : RelationObjectType.values()) {
      List<String> configs = RodaCoreFactory.getRodaConfigurationAsList("ui.ri.relation",
        relationType.toString().toLowerCase());

      for (String config : configs) {
        String fieldName = RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_FIELDS);
        String inverse = RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_INVERSE,
          RodaConstants.SEARCH_FIELD_FIELDS);

        if (StringUtils.isNotBlank(inverse)) {
          map.put(fieldName, inverse);
        }
      }
    }

    return map;
  }

  public RepresentationInformation enrichRepresentationInformationRelations(RepresentationInformation fetched,
    String localeString, RequestContext requestContext) {

    ParentWhichFilterParameter parentFilter = new ParentWhichFilterParameter(
      new SimpleFilterParameter("content_type", "ri"),
      new SimpleFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_LINK, fetched.getId()));

    Filter filter = new Filter(parentFilter);

    Map<String, String> inverses = getInverses();

    fetched.getRelations().forEach(relation -> relation
      .setRelationTypeI18n(retrieveRelationTypeTranslation(relation.getRelationType(), localeString, false)));

    List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
      RodaConstants.REPRESENTATION_INFORMATION_NAME);

    FindRequest findRequest = FindRequest.getBuilder(filter, true).withFieldsToReturn(fieldsToReturn).withChildren(true)
      .build();
    IndexResult<RepresentationInformation> indexResult = indexService.find(RepresentationInformation.class,
      findRequest);

    indexResult.getResults()
      .forEach(ri -> ri.getRelations().stream().filter(p -> p.getLink().equals(fetched.getId())).forEach(riRelation -> {
        RepresentationInformationRelation relation = new RepresentationInformationRelation();
        relation.setObjectType(riRelation.getObjectType());
        relation.setRelationType(inverses.get(riRelation.getRelationType()));
        relation.setRelationTypeI18n(retrieveRelationTypeTranslation(riRelation.getRelationType(), localeString, true));
        relation.setLink(ri.getId());
        relation.setTitle(ri.getName());

        fetched.getRelations().add(relation);
      }));

    return fetched;
  }

  public String retrieveRelationTypeTranslation(String relationType, String localeString, boolean isInverse) {
    Locale locale = ServerTools.parseLocale(localeString);
    Messages i18NMessages = RodaCoreFactory.getI18NMessages(locale);
    if (isInverse) {
      return i18NMessages.getTranslation(
        RodaCoreFactory.getRodaConfigurationAsString("ui.search.fields.RepresentationInformation.relation",
          relationType.toLowerCase(), RodaConstants.SEARCH_FIELD_INVERSE, RodaConstants.SEARCH_FIELD_I18N));
    }

    return i18NMessages.getTranslation(
      RodaCoreFactory.getRodaConfigurationAsString("ui.search.fields.RepresentationInformation.relation",
        relationType.toLowerCase(), RodaConstants.SEARCH_FIELD_I18N));
  }

  public StreamResponse downloadRepresentationInformation(ModelService model, String id)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    final ConsumesOutputStream stream;

    Optional<LiteRODAObject> liteRI = LiteRODAObjectFactory.get(RepresentationInformation.class, id);
    if (liteRI.isEmpty()) {
      throw new RequestNotValidException("Could not get representation information lite with id: " + id);
    }
    Binary riBinary = model.getBinary(liteRI.get());
    stream = new BinaryConsumesOutputStream(riBinary);
    return new StreamResponse(stream);
  }

  public RepresentationInformationRelationOptions retrieveRelationTypeTranslations(String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    Messages i18NMessages = RodaCoreFactory.getI18NMessages(locale);

    RepresentationInformationRelationOptions options = new RepresentationInformationRelationOptions();

    for (RelationObjectType relationType : RelationObjectType.values()) {
      List<String> configs = RodaCoreFactory.getRodaConfigurationAsList("ui.ri.relation",
        relationType.toString().toLowerCase());
      Map<String, String> translations = new HashMap<>();

      for (String config : configs) {
        String fieldName = RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_FIELDS);
        String translation = i18NMessages
          .getTranslation(RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_I18N));
        translations.put(fieldName, translation);
      }

      options.getRelationsTranslations().put(relationType.toString(), translations);
    }

    return options;
  }
}
