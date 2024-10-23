package org.roda.core.plugins.base.maintenance.backfill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.XMLUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.index.IndexService;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.plugins.base.maintenance.backfill.beans.Add;
import org.roda.core.plugins.base.maintenance.backfill.beans.Delete;
import org.roda.core.plugins.base.maintenance.backfill.beans.DocType;
import org.roda.core.plugins.base.maintenance.backfill.beans.FieldType;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.XMLContentPayload;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateBackfillPluginUtils {
  private GenerateBackfillPluginUtils() {
    // do nothing
  }

  public static final String VALIDATE_AGAINST_NONE = "None";
  public static final String VALIDATE_AGAINST_INDEX = "Index";
  public static final String VALIDATE_AGAINST_STORAGE = "Storage";

  public static <I extends IsIndexed, M extends IsModelObject> DocType toDocBean(M object, Class<I> indexClass)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, NotSupportedException,
    GenericException {
    DocType docBean = new DocType();
    SolrInputDocument solrInputDocument = SolrCollectionRegistry.toSolrDocument(indexClass, object);

    for (SolrInputField field : solrInputDocument) {
      Object value = field.getValue();
      if (value instanceof String string && !string.isEmpty()) {
        FieldType fieldBean = new FieldType();
        fieldBean.setName(field.getName());
        fieldBean.setValue(string);
        docBean.getField().add(fieldBean);
      } else if (value instanceof List<?> multiValueField) {
        for (Object multiValue : multiValueField) {
          FieldType fieldBean = new FieldType();
          fieldBean.setName(field.getName());
          fieldBean.setValue(multiValue.toString());
          docBean.getField().add(fieldBean);
        }
      }
    }

    return docBean;
  }

  public static void writeAddBean(StorageService storage, String filename, Add addBean) throws RequestNotValidException,
    GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    StoragePath storagePath = DefaultStoragePath.parse(List.of(".", filename));
    String xml = XMLUtils.getXMLFromObject(addBean);
    ContentPayload payload = new XMLContentPayload(xml);
    storage.createBinary(storagePath, payload, false);
  }

  public static List<Add> readAddBeans(StorageService storage, String directoryPath)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException {
    StoragePath storagePath = DefaultStoragePath.parse(List.of(".", directoryPath));
    CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, false);
    List<Add> addBeans = new ArrayList<>();
    for (Resource resource : resources) {
      addBeans.add(readAddBean(resource));
    }
    return addBeans;
  }

  public static Add readAddBean(Resource addXMLResource)
    throws RequestNotValidException, GenericException, IOException {
    ContentPayload payload = ((DefaultBinary) addXMLResource).getContent();
    return XMLUtils.getObjectFromXML(payload.createInputStream(), Add.class);
  }

  public static void writeDeleteBean(StorageService storage, String filename, Delete deleteBean)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    StoragePath storagePath = DefaultStoragePath.parse(List.of(".", filename));
    String xml = XMLUtils.getXMLFromObject(deleteBean);
    ContentPayload payload = new XMLContentPayload(xml);
    storage.createBinary(storagePath, payload, false);
  }

  public static void createAndWriteDeletionBeans(StorageService storage, List<String> deletedIds) {

  }

  public static String getGeneratedBackfillPluginName(Class<? extends IsRODAObject> clazz) throws NotFoundException {
    if (clazz.equals(AIP.class)) {
      return GenerateAIPBackfillPlugin.class.getName();
    } else if (clazz.equals(RepresentationInformation.class)) {
      return GenerateRepresentationInformationBackfillPlugin.class.getName();
    } else if (clazz.equals(Risk.class)) {
      return GenerateRiskBackfillPlugin.class.getName();
    } else if (clazz.equals(RiskIncidence.class)) {
      return GenerateIncidenceBackfillPlugin.class.getName();
    } else if (clazz.equals(Job.class)) {
      return GenerateJobBackfillPlugin.class.getName();
    } else if (clazz.equals(Notification.class)) {
      return GenerateNotificationBackfillPlugin.class.getName();
    } else if (clazz.equals(TransferredResource.class)) {
      return GenerateTransferredResourceBackfillPlugin.class.getName();
    } else if (clazz.equals(RODAMember.class)) {
      return GenerateRODAMemberBackfillPlugin.class.getName();
    } else if (clazz.equals(LogEntry.class)) {
      return GenerateActionLogBackfillPlugin.class.getName();
    } else if (clazz.equals(IndexedPreservationAgent.class)) {
      return GeneratePreservationAgentBackfillPlugin.class.getName();
    } else if (clazz.equals(IndexedPreservationEvent.class)) {
      return GeneratePreservationRepositoryEventBackfillPlugin.class.getName();
    } else if (clazz.equals(DIP.class)) {
      return GenerateDIPBackfillPlugin.class.getName();
    } else if (clazz.equals(DisposalConfirmation.class)) {
      return GenerateDisposalConfirmationBackfillPlugin.class.getName();
    } else {
      throw new NotFoundException("Index class not found for class " + clazz.getName());
    }
  }

  public static Class<? extends IsIndexed> getIndexClass(Class<? extends IsRODAObject> clazz) throws NotFoundException {
    if (clazz.equals(AIP.class)) {
      return IndexedAIP.class;
    } else if (clazz.equals(RepresentationInformation.class)) {
      return RepresentationInformation.class;
    } else if (clazz.equals(Risk.class)) {
      return IndexedRisk.class;
    } else if (clazz.equals(RiskIncidence.class)) {
      return RiskIncidence.class;
    } else if (clazz.equals(Job.class)) {
      return Job.class;
    } else if (clazz.equals(Notification.class)) {
      return Notification.class;
    } else if (clazz.equals(TransferredResource.class)) {
      return TransferredResource.class;
    } else if (clazz.equals(RODAMember.class)) {
      return RODAMember.class;
    } else if (clazz.equals(LogEntry.class)) {
      return LogEntry.class;
    } else if (clazz.equals(IndexedPreservationAgent.class)) {
      return IndexedPreservationAgent.class;
    } else if (clazz.equals(IndexedPreservationEvent.class)) {
      return IndexedPreservationEvent.class;
    } else if (clazz.equals(DIP.class)) {
      return IndexedDIP.class;
    } else if (clazz.equals(DisposalConfirmation.class)) {
      return DisposalConfirmation.class;
    } else {
      throw new NotFoundException("Index class not found for class " + clazz.getName());
    }
  }

  public static <T extends IsRODAObject & IsModelObject, I extends IsIndexed> HashSet<String> generateBackfillForRODAObjects(
    StorageService storage, List<T> objects, int blockSize, Class<I> indexClass, Report report) {
    int totalBeans = 0;
    int count = 0;
    Add addType = new Add();
    HashSet<String> processedIds = new HashSet<>();

    for (T object : objects) {
      // TODO Handle exceptions
      try {
        processedIds.add(object.getId());
        if (count == blockSize) {
          count = 0;
          totalBeans += 1;
          GenerateBackfillPluginUtils.writeAddBean(storage, object.getClass().getName() + "_" + totalBeans + ".xml",
            addType);
        }
        addType.getDoc().add(GenerateBackfillPluginUtils.toDocBean(object, getIndexClass(indexClass)));
        count += 1;
      } catch (AuthorizationDeniedException e) {
        throw new RuntimeException(e);
      } catch (RequestNotValidException e) {
        throw new RuntimeException(e);
      } catch (NotFoundException e) {
        throw new RuntimeException(e);
      } catch (NotSupportedException e) {
        throw new RuntimeException(e);
      } catch (GenericException e) {
        throw new RuntimeException(e);
      } catch (AlreadyExistsException e) {
        throw new RuntimeException(e);
      }
    }
    // TODO Handle exceptions
    try {
      totalBeans += 1;
      GenerateBackfillPluginUtils.writeAddBean(storage,
        objects.getFirst().getClass().getSimpleName() + "_" + totalBeans + ".xml", addType);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      throw new RuntimeException(e);
    }
    return processedIds;
  }

  public static <I extends IsIndexed> List<String> checkIndexForDeletedObjects(IndexService index, Class<I> indexClass,
    List<String> objectIds) throws RequestNotValidException, GenericException, IOException {
    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter("id", objectIds));
    IterableIndexResult<I> indexResult = index.findAll(indexClass, filter, List.of("id"));
    List<String> existingIndexIdsList = new ArrayList<>();
    for (I indexedObject : indexResult) {
      existingIndexIdsList.add(indexedObject.getId());
    }
    indexResult.close();
    return objectIds.stream().filter(id -> !existingIndexIdsList.contains(id)).toList();
  }

  /*
   * public static <I extends IsIndexed> List<String>
   * checkIndexForModifiedObjects(IndexService index, Class<I> indexClass,
   * List<String> objectIds) throws RequestNotValidException, GenericException {
   * Filter filter = new Filter(); filter.add(new OneOfManyFilterParameter("id",
   * objectIds)); IndexResult<I> result = index.find(indexClass, filter,
   * Sorter.NONE, Sublist.NONE, List.of("id")); return
   * result.getResults().stream().map(I::getId).toList(); }
   */

  public static <I extends IsIndexed> List<String> checkIndexForAddedObjects(IndexService index, Class<I> indexClass,
    Set<String> objectIds) throws RequestNotValidException, GenericException, IOException {
    IterableIndexResult<I> indexResult = index.findAll(indexClass, Filter.ALL, List.of("id"));
    List<String> unprocessedIndexIds = new ArrayList<>();
    for (I indexObject : indexResult) {
      if (!objectIds.contains(indexObject.getId())) {
        unprocessedIndexIds.add(indexObject.getId());
      }
    }
    indexResult.close();
    return unprocessedIndexIds;
  }
}
