package org.roda.core.index.schema;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.CopyFieldsResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.DynamicFieldsResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.FieldsResponse;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrBootstrapUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrBootstrapUtils.class);

  private static Map<String, Field> getFields(SolrClient client, String collectionName) throws GenericException {

    SchemaRequest.Fields fields = new SchemaRequest.Fields();
    FieldsResponse response;
    try {
      response = fields.process(client, collectionName);
      return response.getFields().stream().map(f -> new Field(f))
        .collect(Collectors.toMap(Field::getName, Function.identity()));
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not get schema fields", e);
    }
  }

  private static Map<String, DynamicField> getDynamicFields(SolrClient client, String collectionName)
    throws GenericException {

    SchemaRequest.DynamicFields fields = new SchemaRequest.DynamicFields();
    DynamicFieldsResponse response;
    try {
      response = fields.process(client, collectionName);
      return response.getDynamicFields().stream().map(f -> new DynamicField(f))
        .collect(Collectors.toMap(DynamicField::getName, Function.identity()));
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not get schema dynamic fields", e);
    }
  }

  private static Set<CopyField> getCopyFields(SolrClient client, String collectionName) throws GenericException {

    SchemaRequest.CopyFields fields = new SchemaRequest.CopyFields();
    CopyFieldsResponse response;
    try {
      response = fields.process(client, collectionName);
      return response.getCopyFields().stream().map(f -> new CopyField(f)).collect(Collectors.toSet());
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not get schema dynamic fields", e);
    }
  }

  private static <T extends IsIndexed, M extends IsModelObject> void bootstrapCollection(SolrClient client,
    SolrCollection<T, M> collection) throws GenericException {

    // check if fields already exist, only create if they do not
    Map<String, Field> fields = getFields(client, collection.getIndexName());
    Map<String, DynamicField> dynamicFields = getDynamicFields(client, collection.getIndexName());
    Set<CopyField> copyFields = getCopyFields(client, collection.getIndexName());

    SchemaBuilder b = new SchemaBuilder();
    collection.getFields().forEach(f -> {
      if (!fields.containsKey(f.getName())) {
        b.addField(f);
      } else if (!fields.get(f.getName()).isEquivalentTo(f)) {
        // TODO this check doesn't work well because attribute omissions are set
        // to default values, should only compare attributes that are not
        // Optional.empty()
        LOGGER.warn("Field {} of collection {} should be updated. Existing: {}. Required: {}", f.getName(),
          collection.getIndexName(), fields.get(f.getName()), f);
      }
    });

    collection.getCopyFields().forEach(cf -> {
      if (!copyFields.contains(cf)) {
        b.addCopyField(cf);
      }
    });

    collection.getDynamicFields().forEach(df -> {
      if (!dynamicFields.containsKey(df.getName())) {
        b.addDynamicField(df);
      } else if (!dynamicFields.get(df.getName()).isEquivalentTo(df)) {
        LOGGER.warn("Dynamic field {} of collection {} should be updated. Existing: {}. Required: {}", df.getName(),
          collection.getIndexName(), dynamicFields.get(df.getName()), df);
      }
    });

    // XXX find fields that could be removed/pruned?

    if (!b.isEmpty()) {
      b.build(client, collection.getIndexName());
    } else {
      LOGGER.info("Collection {} is up to date", collection.getIndexName());
    }
  }

  public static void bootstrapSchemas(SolrClient client) throws GenericException {
    LOGGER.info("Bootstrapping schemas");

    for (SolrCollection<? extends IsIndexed, ? extends IsModelObject> collection : SolrCollectionRegistry.registry()) {
      bootstrapCollection(client, collection);
    }
  }

}
