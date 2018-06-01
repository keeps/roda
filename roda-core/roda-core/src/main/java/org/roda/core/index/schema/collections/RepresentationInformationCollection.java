package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepresentationInformationCollection
  extends AbstractSolrCollection<RepresentationInformation, RepresentationInformation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationInformationCollection.class);

  @Override
  public Class<RepresentationInformation> getIndexClass() {
    return RepresentationInformation.class;
  }

  @Override
  public Class<RepresentationInformation> getModelClass() {
    return RepresentationInformation.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_REPRESENTATION_INFORMATION;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_REPRESENTATION_INFORMATION);
  }

  @Override
  public String getUniqueId(RepresentationInformation modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_NAME, Field.TYPE_TEXT).setRequired(true)
      .setMultiValued(false));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_FAMILY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_TAGS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_EXTRAS, Field.TYPE_TEXT).setMultiValued(false));

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS, Field.TYPE_STRING).setIndexed(false));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS_WITH_RI, Field.TYPE_STRING).setStored(false)
      .setMultiValued(true));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, Field.TYPE_STRING).setMultiValued(true));

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY, Field.TYPE_STRING));

    fields.add(SolrCollection.getSortFieldOf(RodaConstants.REPRESENTATION_INFORMATION_NAME));

    fields.add(SolrCollection.getSearchField());

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getSortCopyFieldOf(RodaConstants.REPRESENTATION_INFORMATION_NAME));
  }

  @Override
  public SolrInputDocument toSolrDocument(RepresentationInformation ri, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(ri, preCalculatedFields, accumulators, flags);

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_NAME, ri.getName());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION, ri.getDescription());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_FAMILY, ri.getFamily());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_TAGS, ri.getTags());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_EXTRAS, ri.getExtras());

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT, ri.getSupport().toString());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS, JsonUtils.getJsonFromObject(ri.getRelations()));

    if (ri.getRelations() != null) {
      doc.addField(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS_WITH_RI,
        ri.getRelations().stream().filter(r -> RelationObjectType.REPRESENTATION_INFORMATION.equals(r.getObjectType()))
          .map(r -> r.getLink()).collect(Collectors.toList()));
    }

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, ri.getFilters());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON, SolrUtils.formatDate(ri.getCreatedOn()));
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY, ri.getCreatedBy());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, SolrUtils.formatDate(ri.getUpdatedOn()));
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY, ri.getUpdatedBy());

    return doc;
  }

  @Override
  public RepresentationInformation fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn)
    throws GenericException {

    final RepresentationInformation ri = super.fromSolrDocument(doc, fieldsToReturn);

    ri.setName(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_NAME), null));
    ri.setDescription(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION), null));
    ri.setFamily(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_FAMILY), null));
    ri.setTags(SolrUtils.objectToListString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_TAGS)));
    ri.setExtras(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_EXTRAS), null));

    ri.setSupport(SolrUtils.objectToEnum(doc.get(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT),
      RepresentationInformationSupport.class, RepresentationInformationSupport.KNOWN));
    ri.setRelations(SolrUtils.objectToListRelation(doc.get(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS)));

    ri.setFilters(SolrUtils.objectToListString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_FILTERS)));

    ri.setCreatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON)));
    ri.setCreatedBy(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY), ""));
    ri.setUpdatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON)));
    ri.setUpdatedBy(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY), ""));

    return ri;

  }

}
