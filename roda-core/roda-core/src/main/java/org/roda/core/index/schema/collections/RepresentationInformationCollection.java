/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;

public class RepresentationInformationCollection
  extends AbstractSolrCollection<RepresentationInformation, RepresentationInformation> {

  public static final String CONTENT_TYPE = "content_type";

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
    return List.of(RodaConstants.INDEX_REPRESENTATION_INFORMATION);
  }

  @Override
  public String getUniqueId(RepresentationInformation modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_NAME, Field.TYPE_TEXT).setRequired(false)
      .setMultiValued(false));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_FAMILY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_TAGS, Field.TYPE_STRING).setMultiValued(true));

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS_WITH_RI, Field.TYPE_STRING).setStored(false)
      .setMultiValued(true));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, Field.TYPE_STRING).setMultiValued(true));

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY, Field.TYPE_STRING));

    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_TITLE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_LINK, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_RELATION_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_INFORMATION_OBJECT_TYPE, Field.TYPE_STRING));
    fields.add(new Field(CONTENT_TYPE, Field.TYPE_STRING));

    fields.add(SolrCollection.getSortFieldOf(RodaConstants.REPRESENTATION_INFORMATION_NAME));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getSortCopyFieldOf(RodaConstants.REPRESENTATION_INFORMATION_NAME));
  }

  @Override
  public SolrInputDocument toSolrDocument(ModelService model, RepresentationInformation ri, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(model, ri, info);

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_NAME, ri.getName());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_DESCRIPTION, ri.getDescription());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_FAMILY, ri.getFamily());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_TAGS, ri.getTags());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT, ri.getSupport().toString());

    if (ri.getRelations() != null) {
      doc.addField(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS_WITH_RI,
        ri.getRelations().stream().filter(r -> RelationObjectType.REPRESENTATION_INFORMATION.equals(r.getObjectType()))
          .map(RepresentationInformationRelation::getLink).toList());
    }

    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, ri.getFilters());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON, SolrUtils.formatDate(ri.getCreatedOn()));
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY, ri.getCreatedBy());
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON, SolrUtils.formatDate(ri.getUpdatedOn()));
    doc.addField(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY, ri.getUpdatedBy());

    // Added to distinguish parent documents from nested documents
    doc.addField(CONTENT_TYPE, "ri");

    List<SolrInputDocument> children = new ArrayList<>();
    int i = 0;
    for (RepresentationInformationRelation relation : ri.getRelations()) {
      SolrInputDocument representationInformationRelation = new SolrInputDocument();
      representationInformationRelation.addField(RodaConstants.INDEX_UUID, ri.getUUID() + "!" + ++i);
      representationInformationRelation.addField(RodaConstants.REPRESENTATION_INFORMATION_TITLE, relation.getTitle());
      representationInformationRelation.addField(RodaConstants.REPRESENTATION_INFORMATION_LINK, relation.getLink());
      representationInformationRelation.addField(RodaConstants.REPRESENTATION_INFORMATION_RELATION_TYPE,
        relation.getRelationType());
      representationInformationRelation.addField(RodaConstants.REPRESENTATION_INFORMATION_OBJECT_TYPE,
        relation.getObjectType().toString());
      representationInformationRelation.addField(CONTENT_TYPE, "relation");

      children.add(representationInformationRelation);
    }

    doc.setField(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS, children);

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

    ri.setRelations(
      SolrUtils.objectToRepresentationInformationRelation(doc.get(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS)));

    ri.setFilters(SolrUtils.objectToListString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_FILTERS)));

    ri.setCreatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CREATED_ON)));
    ri.setCreatedBy(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_CREATED_BY), ""));
    ri.setUpdatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_ON)));
    ri.setUpdatedBy(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_INFORMATION_UPDATED_BY), ""));

    return ri;

  }
}
