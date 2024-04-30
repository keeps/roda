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
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.BlockJoinParentFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
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
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.base.maintenance.AddRepresentationInformationFilterPlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.StorageService;
import org.roda.wui.api.controllers.BrowserHelper;
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
    RepresentationInformationCustomForm form, User user, boolean commit)
    throws GenericException, AuthorizationDeniedException, NotFoundException {
    if (form != null) {
      ri.setExtras(getRepresentationInformationExtra(form, ri.getFamily()));
    }
    return RodaCoreFactory.getModelService().createRepresentationInformation(ri, user.getName(), commit);
  }

  public RepresentationInformation updateRepresentationInformation(RepresentationInformation ri,
    RepresentationInformationCustomForm form, User user, boolean commit)
    throws NotFoundException, AuthorizationDeniedException, GenericException {
    if (form != null) {
      ri.setExtras(getRepresentationInformationExtra(form, ri.getFamily()));
    }
    return RodaCoreFactory.getModelService().updateRepresentationInformation(ri, user.getName(), commit);
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

  public RepresentationInformationFamily retrieveRepresentationInformationFamily(String familyType, String localeString)
    throws NotFoundException {
    Locale locale = ServerTools.parseLocale(localeString);
    List<SupportedMetadataTypeBundle> supportedMetadataTypeBundles = BrowserHelper
      .retrieveExtraSupportedMetadata(RodaCoreFactory.getRodaConfigurationAsList("ui.ri.family"), locale);

    SupportedMetadataTypeBundle searchResult = supportedMetadataTypeBundles.stream()
      .filter(p -> p.getType().equals(familyType)).findFirst().orElseThrow(() -> new NotFoundException(
        "The family type provided didn't had any match with the configurable representation families"));

    RepresentationInformationFamily representationInformationFamily = new RepresentationInformationFamily();
    representationInformationFamily.setFamilyValues(searchResult.getValues());

    return representationInformationFamily;
  }

  public RepresentationInformationFamily retrieveRepresentationInformationFamily(String representationInformationId,
    String familyType, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Locale locale = ServerTools.parseLocale(localeString);
    List<SupportedMetadataTypeBundle> supportedMetadataTypeBundles = BrowserHelper
      .retrieveExtraSupportedMetadata(RodaCoreFactory.getRodaConfigurationAsList("ui.ri.family"), locale);

    RepresentationInformation ri = RodaCoreFactory.getModelService()
      .retrieveRepresentationInformation(representationInformationId);

    SupportedMetadataTypeBundle searchResult = supportedMetadataTypeBundles.stream()
      .filter(p -> p.getType().equals(familyType)).findFirst().orElseThrow(() -> new NotFoundException(
        "The family type provided didn't had any match with the configurable representation families"));

    Set<MetadataValue> familyValues = new TreeSet<>(parseMetadataValues(searchResult.getValues(), ri.getExtras()));

    RepresentationInformationFamily representationInformationFamily = new RepresentationInformationFamily();
    representationInformationFamily.setFamilyValues(familyValues);

    return representationInformationFamily;
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

    BlockJoinParentFilterParameter parentFilter = new BlockJoinParentFilterParameter(
      new SimpleFilterParameter("content_type", "ri"),
      new SimpleFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_LINK, fetched.getId()));

    Filter filter = new Filter(parentFilter);

    Map<String, String> inverses = getInverses();

    fetched.getRelations().forEach(relation -> relation
      .setRelationTypeI18n(retrieveRelationTypeTranslation(relation.getRelationType(), localeString, false)));

    List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
      RodaConstants.REPRESENTATION_INFORMATION_NAME);

    FindRequest findRequest = FindRequest.getBuilder(RepresentationInformation.class.getName(), filter, true)
      .withFieldsToReturn(fieldsToReturn).withChildren(true).build();
    IndexResult<RepresentationInformation> indexResult = indexService.find(RepresentationInformation.class, findRequest,
      requestContext);

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

  public StreamResponse downloadRepresentationInformation(String id)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    final ConsumesOutputStream stream;

    StorageService storage = RodaCoreFactory.getStorageService();
    Binary riBinary = storage.getBinary(ModelUtils.getRepresentationInformationStoragePath(id));
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
