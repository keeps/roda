/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.CopyFieldsResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.DynamicFieldsResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse.FieldsResponse;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SolrBootstrapUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrBootstrapUtils.class);
  private static final String TEXT_TO_VECTOR_MODEL_STORE_PATH = "/schema/text-to-vector-model-store";
  private static final String LANGCHAIN4J_OPENAI_MODEL_CLASS = "dev.langchain4j.model.openai.OpenAiEmbeddingModel";

  private static Map<String, Field> getFields(SolrClient client, String collectionName) throws GenericException {

    SchemaRequest.Fields fields = new SchemaRequest.Fields();
    FieldsResponse response;
    try {
      response = fields.process(client, collectionName);
      return response.getFields().stream().map(Field::new)
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

    boolean hasVectorField = collection.getFields().stream()
      .anyMatch(f -> Field.TYPE_KNN_VECTOR.equals(f.getType()));
    if (hasVectorField) {
      registerTextToVectorModel(client, collection.getIndexName());
    }
  }

  /**
   * Registers (or re-registers) the embedding model used for query-time
   * text-to-vector search, so that {@code {!knn_text_to_vector model=...}}
   * queries can resolve it. This only wires up query-time vectorization -
   * index-time vectors are written by an external enrichment service, not by
   * RODA. Best-effort: semantic search is an optional feature, so failures
   * here are logged but must not prevent RODA from starting.
   */
  private static void registerTextToVectorModel(SolrClient client, String collectionName) {
    boolean enabled = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_INDEX_EMBEDDING_ENABLED));
    if (!enabled) {
      return;
    }

    String solrModel = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_INDEX_EMBEDDING_SOLR_MODEL);
    String baseUrl = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_INDEX_EMBEDDING_BASE_URL);
    String modelName = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_INDEX_EMBEDDING_MODEL_NAME);
    String apiKey = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_INDEX_EMBEDDING_API_KEY);

    if (StringUtils.isBlank(solrModel) || StringUtils.isBlank(baseUrl) || StringUtils.isBlank(modelName)) {
      LOGGER.warn(
        "Semantic search is enabled (core.index.embedding.enabled=true) but base_url/model_name/solr_model "
          + "are not fully configured; skipping text-to-vector model registration for collection {}",
        collectionName);
      return;
    }

    Map<String, Object> params = new LinkedHashMap<>();
    params.put("baseUrl", baseUrl);
    params.put("modelName", modelName);
    // LangChain4j's OpenAI client rejects a null/absent apiKey even against
    // auth-free servers, so always send a value.
    params.put("apiKey", StringUtils.isNotBlank(apiKey) ? apiKey : "not-needed");

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("class", LANGCHAIN4J_OPENAI_MODEL_CLASS);
    payload.put("name", solrModel);
    payload.put("params", params);

    try {
      byte[] body = new ObjectMapper().writeValueAsBytes(payload);
      GenericSolrRequest request = new GenericSolrRequest(SolrRequest.METHOD.PUT, TEXT_TO_VECTOR_MODEL_STORE_PATH)
        .withContent(body, "application/json");
      request.setRequiresCollection(true);
      request.process(client, collectionName);
      LOGGER.info("Registered text-to-vector model '{}' for collection {}", solrModel, collectionName);
    } catch (SolrServerException | IOException | RuntimeException e) {
      LOGGER.warn(
        "Could not register text-to-vector model '{}' for collection {} - semantic search queries against "
          + "this collection will fail with an unknown model error until this is resolved (requires the "
          + "language-models Solr module to be enabled, see docker-compose SOLR_MODULES)",
        solrModel, collectionName, e);
    }
  }

  public static void bootstrapSchemas(SolrClient client) throws GenericException {
    LOGGER.info("Bootstrapping schemas");
    for (SolrCollection<? extends IsIndexed, ? extends IsModelObject> collection : SolrCollectionRegistry.registry()) {
      bootstrapCollection(client, collection);
    }

    LOGGER.info("Finishing bootstrapping schemas");
  }

}
