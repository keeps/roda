package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatCollection extends AbstractSolrCollection<Format, Format> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormatCollection.class);

  @Override
  public Class<Format> getIndexClass() {
    return Format.class;
  }

  @Override
  public Class<Format> getModelClass() {
    return Format.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_FORMAT;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_FORMAT);
  }

  @Override
  public String getUniqueId(Format modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.FORMAT_NAME, Field.TYPE_TEXT).setRequired(true).setMultiValued(false));
    fields.add(new Field(RodaConstants.FORMAT_DEFINITION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.FORMAT_CATEGORY, Field.TYPE_TEXT).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_LATEST_VERSION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FORMAT_POPULARITY, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.FORMAT_DEVELOPER, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.FORMAT_INITIAL_RELEASE, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.FORMAT_STANDARD, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FORMAT_IS_OPEN_FORMAT, Field.TYPE_BOOLEAN));
    fields.add(new Field(RodaConstants.FORMAT_WEBSITE, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_PROVENANCE_INFORMATION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FORMAT_EXTENSIONS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_MIMETYPES, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_PRONOMS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_UTIS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FORMAT_VERSIONS, Field.TYPE_STRING).setMultiValued(true));

    fields.add(SolrCollection.getSortFieldOf(RodaConstants.FORMAT_NAME));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getSortCopyFieldOf(RodaConstants.FORMAT_NAME));
  }

  @Override
  public SolrInputDocument toSolrDocument(Format format, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(format, preCalculatedFields, accumulators, flags);

    doc.addField(RodaConstants.FORMAT_NAME, format.getName());
    doc.addField(RodaConstants.FORMAT_DEFINITION, format.getDefinition());
    doc.addField(RodaConstants.FORMAT_CATEGORY, format.getCategories());
    doc.addField(RodaConstants.FORMAT_LATEST_VERSION, format.getLatestVersion());
    if (format.getPopularity() != null) {
      doc.addField(RodaConstants.FORMAT_POPULARITY, format.getPopularity());
    }
    doc.addField(RodaConstants.FORMAT_DEVELOPER, format.getDeveloper());
    doc.addField(RodaConstants.FORMAT_INITIAL_RELEASE, SolrUtils.formatDate(format.getInitialRelease()));
    doc.addField(RodaConstants.FORMAT_STANDARD, format.getStandard());
    doc.addField(RodaConstants.FORMAT_IS_OPEN_FORMAT, format.isOpenFormat());
    doc.addField(RodaConstants.FORMAT_WEBSITE, format.getWebsites());
    doc.addField(RodaConstants.FORMAT_PROVENANCE_INFORMATION, format.getProvenanceInformation());
    doc.addField(RodaConstants.FORMAT_EXTENSIONS, format.getExtensions());
    doc.addField(RodaConstants.FORMAT_MIMETYPES, format.getMimetypes());
    doc.addField(RodaConstants.FORMAT_PRONOMS, format.getPronoms());
    doc.addField(RodaConstants.FORMAT_UTIS, format.getUtis());
    doc.addField(RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS, format.getAlternativeDesignations());
    doc.addField(RodaConstants.FORMAT_VERSIONS, format.getVersions());

    return doc;
  }

  @Override
  public Format fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final Format format = super.fromSolrDocument(doc, fieldsToReturn);

    format.setName(SolrUtils.objectToString(doc.get(RodaConstants.FORMAT_NAME), null));
    format.setDefinition(SolrUtils.objectToString(doc.get(RodaConstants.FORMAT_DEFINITION), null));
    format.setCategories(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_CATEGORY)));
    format.setLatestVersion(SolrUtils.objectToString(doc.get(RodaConstants.FORMAT_LATEST_VERSION), null));
    format.setPopularity(SolrUtils.objectToInteger(doc.get(RodaConstants.FORMAT_POPULARITY), null));
    format.setDeveloper(SolrUtils.objectToString(doc.get(RodaConstants.FORMAT_DEVELOPER), null));
    format.setInitialRelease(SolrUtils.objectToDate(doc.get(RodaConstants.FORMAT_INITIAL_RELEASE)));
    format.setStandard(SolrUtils.objectToString(doc.get(RodaConstants.FORMAT_STANDARD), null));
    format.setOpenFormat(SolrUtils.objectToBoolean(doc.get(RodaConstants.FORMAT_IS_OPEN_FORMAT), Boolean.FALSE));
    format.setWebsites(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_WEBSITE)));
    format
      .setProvenanceInformation(SolrUtils.objectToString(doc.get(RodaConstants.FORMAT_PROVENANCE_INFORMATION), null));
    format.setExtensions(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_EXTENSIONS)));
    format.setMimetypes(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_MIMETYPES)));
    format.setPronoms(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_PRONOMS)));
    format.setUtis(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_UTIS)));
    format
      .setAlternativeDesignations(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS)));
    format.setVersions(SolrUtils.objectToListString(doc.get(RodaConstants.FORMAT_VERSIONS)));

    return format;

  }

}
