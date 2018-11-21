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
import org.apache.xmlbeans.XmlOptions;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.storage.Binary;

import gov.loc.premis.v3.AgentComplexType;
import gov.loc.premis.v3.ExtensionComplexType;

public class PreservationAgentCollection
  extends AbstractSolrCollection<IndexedPreservationAgent, PreservationMetadata> {

  @Override
  public Class<IndexedPreservationAgent> getIndexClass() {
    return IndexedPreservationAgent.class;
  }

  @Override
  public Class<PreservationMetadata> getModelClass() {
    return PreservationMetadata.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_PRESERVATION_AGENTS;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_PRESERVATION_AGENTS);
  }

  @Override
  public String getUniqueId(PreservationMetadata modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.PRESERVATION_AGENT_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_AGENT_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_AGENT_EXTENSION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_AGENT_NOTE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_AGENT_VERSION, Field.TYPE_STRING));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    // TODO check if the _txt versions are needed
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      new CopyField(RodaConstants.PRESERVATION_AGENT_NAME, RodaConstants.PRESERVATION_AGENT_NAME + "_txt"),
      new CopyField(RodaConstants.PRESERVATION_AGENT_TYPE, RodaConstants.PRESERVATION_AGENT_TYPE + "_txt"),
      new CopyField(RodaConstants.PRESERVATION_AGENT_VERSION, RodaConstants.PRESERVATION_AGENT_VERSION + "_txt"));
  }

  @Override
  public SolrInputDocument toSolrDocument(PreservationMetadata pm, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(pm, info);

    Binary binary = RodaCoreFactory.getModelService().retrievePreservationAgent(pm.getId());

    boolean validate = false;
    try {
      AgentComplexType agent = PremisV3Utils.binaryToAgent(binary.getContent(), validate);
      doc.addField(RodaConstants.PRESERVATION_AGENT_NAME, PremisV3Utils.toStringList(agent.getAgentNameArray()));
      doc.addField(RodaConstants.PRESERVATION_AGENT_TYPE, agent.getAgentType().getStringValue());

      String extensions = "";
      for (ExtensionComplexType e : agent.getAgentExtensionArray()) {
        extensions += e.xmlText(new XmlOptions().setSavePrettyPrint());
      }
      doc.addField(RodaConstants.PRESERVATION_AGENT_EXTENSION, extensions);
      doc.addField(RodaConstants.PRESERVATION_AGENT_NOTE, Arrays.asList(agent.getAgentNoteArray()));
      doc.addField(RodaConstants.PRESERVATION_AGENT_VERSION, agent.getAgentVersion());

    } catch (ValidationException e) {
      throw new GenericException(e);
    }

    return doc;
  }

  @Override
  public IndexedPreservationAgent fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn)
    throws GenericException {

    final IndexedPreservationAgent ipa = super.fromSolrDocument(doc, fieldsToReturn);

    final String id = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_UUID), null);
    final String name = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_NAME), null);
    final String type = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_TYPE), null);
    final String extension = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_EXTENSION), null);
    final String version = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_VERSION), null);
    final String note = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_AGENT_NOTE), null);
    final List<String> roles = SolrUtils.objectToListString(doc.get(RodaConstants.PRESERVATION_AGENT_ROLES));

    ipa.setId(id);
    ipa.setName(name);
    ipa.setType(type);
    ipa.setExtension(extension);
    ipa.setVersion(version);
    ipa.setNote(note);
    ipa.setRoles(roles);

    return ipa;

  }

}
