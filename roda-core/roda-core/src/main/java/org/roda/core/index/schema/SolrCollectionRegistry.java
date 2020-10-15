/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.collections.AIPCollection;
import org.roda.core.index.schema.collections.DIPCollection;
import org.roda.core.index.schema.collections.DIPFileCollection;
import org.roda.core.index.schema.collections.DisposalConfirmationCollection;
import org.roda.core.index.schema.collections.FileCollection;
import org.roda.core.index.schema.collections.JobCollection;
import org.roda.core.index.schema.collections.JobReportCollection;
import org.roda.core.index.schema.collections.LogEntryCollection;
import org.roda.core.index.schema.collections.MemberCollection;
import org.roda.core.index.schema.collections.NotificationCollection;
import org.roda.core.index.schema.collections.PreservationAgentCollection;
import org.roda.core.index.schema.collections.PreservationEventCollection;
import org.roda.core.index.schema.collections.RepresentationCollection;
import org.roda.core.index.schema.collections.RepresentationInformationCollection;
import org.roda.core.index.schema.collections.RiskCollection;
import org.roda.core.index.schema.collections.RiskIncidenceCollection;
import org.roda.core.index.schema.collections.TransferredResourceCollection;

public final class SolrCollectionRegistry {

  private SolrCollectionRegistry() {

  }

  private static final Map<Class<? extends IsIndexed>, SolrCollection<? extends IsIndexed, ? extends IsModelObject>> REGISTRY = new HashMap<>();
  private static final Map<Class<? extends IsModelObject>, Class<? extends IsIndexed>> MODEL_TO_INDEX = new HashMap<>();

  static {
    register(new LogEntryCollection());

    register(new AIPCollection());
    register(new RepresentationCollection());
    register(new FileCollection());

    register(new DIPCollection());
    register(new DIPFileCollection());

    register(new JobCollection());
    register(new JobReportCollection());

    register(new TransferredResourceCollection());

    register(new MemberCollection());
    register(new NotificationCollection());

    register(new PreservationAgentCollection());
    register(new PreservationEventCollection());

    register(new RiskCollection());
    register(new RiskIncidenceCollection());

    register(new RepresentationInformationCollection());

    register(new DisposalConfirmationCollection());
  }

  public static <T extends IsIndexed, M extends IsModelObject> void register(SolrCollection<T, M> collection) {
    REGISTRY.put(collection.getIndexClass(), collection);
    MODEL_TO_INDEX.put(collection.getModelClass(), collection.getIndexClass());
  }

  public static <T extends IsIndexed> Class<T> giveRespectiveIndexClass(Class<? extends IsRODAObject> inputClass) {
    Class<? extends IsIndexed> indexClass = MODEL_TO_INDEX.get(inputClass);
    if (indexClass == null) {
      return (Class<T>) inputClass;
    } else {
      return (Class<T>) indexClass;
    }
  }

  public static Collection<SolrCollection<? extends IsIndexed, ? extends IsModelObject>> registry() {
    return Collections.unmodifiableCollection(REGISTRY.values());
  }

  public static List<String> registryIndexNames() {
    return registry().stream().map(col -> col.getIndexName()).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public static <I extends IsIndexed, M extends IsModelObject> SolrCollection<I, M> get(Class<I> indexClass) {
    SolrCollection<I, M> ret = (SolrCollection<I, M>) REGISTRY.get(indexClass);

    // if a model class is injected it will still try to find the collection
    // so legacy behavior is maintained
    if (ret == null) {
      for (SolrCollection<? extends IsIndexed, ? extends IsModelObject> col : REGISTRY.values()) {
        if (col.getModelClass().equals(indexClass)) {
          ret = (SolrCollection<I, M>) col;
          break;
        }
      }
    }

    return ret;
  }

  public static <I extends IsIndexed> I fromSolrDocument(Class<I> indexClass, SolrDocument doc,
    List<String> fieldsToReturn) throws GenericException, NotSupportedException {
    SolrCollection<I, IsModelObject> solrCollection = get(indexClass);
    if (solrCollection != null) {
      return solrCollection.fromSolrDocument(doc, fieldsToReturn);
    } else {
      throw new NotSupportedException(
        "Could not find Solr collection relative to '" + indexClass.getName() + "' in registry.");
    }
  }

  public static <I extends IsIndexed> I fromSolrDocument(Class<I> indexClass, SolrDocument doc)
    throws GenericException, NotSupportedException {
    return fromSolrDocument(indexClass, doc, Collections.emptyList());
  }

  public static <I extends IsIndexed, M extends IsModelObject> SolrInputDocument toSolrDocument(Class<I> indexClass,
    M object, IndexingAdditionalInfo utils) throws GenericException, NotSupportedException, RequestNotValidException,
    NotFoundException, AuthorizationDeniedException {
    SolrCollection<I, M> solrCollection = get(indexClass);
    if (solrCollection != null) {
      return solrCollection.toSolrDocument(object, utils);
    } else {
      throw new NotSupportedException(
        "Could not find Solr collection relative to '" + indexClass.getName() + "' in registry.");
    }
  }

  public static <I extends IsIndexed, M extends IsModelObject> SolrInputDocument toSolrDocument(Class<I> indexClass,
    M object) throws GenericException, NotSupportedException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    return toSolrDocument(indexClass, object, IndexingAdditionalInfo.empty());
  }

  public static <I extends IsIndexed> String getIndexName(Class<I> indexClass) throws NotSupportedException {
    SolrCollection<I, IsModelObject> solrCollection = get(indexClass);
    if (solrCollection != null) {
      return solrCollection.getIndexName();
    } else {
      throw new NotSupportedException(
        "Could not find Solr collection relative to '" + indexClass.getName() + "' in registry.");
    }
  }

  public static <I extends IsIndexed> List<String> getCommitIndexNames(Class<I> indexClass)
    throws NotSupportedException {
    SolrCollection<I, IsModelObject> solrCollection = get(indexClass);
    if (solrCollection != null) {
      return solrCollection.getCommitIndexNames();
    } else {
      throw new NotSupportedException(
        "Could not find Solr collection relative to '" + indexClass.getName() + "' in registry.");
    }
  }
}
