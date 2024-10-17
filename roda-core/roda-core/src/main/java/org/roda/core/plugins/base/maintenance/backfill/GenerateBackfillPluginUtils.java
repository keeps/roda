package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.XMLUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.plugins.base.maintenance.backfill.beans.Add;
import org.roda.core.plugins.base.maintenance.backfill.beans.DocType;
import org.roda.core.plugins.base.maintenance.backfill.beans.FieldType;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.XMLContentPayload;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateBackfillPluginUtils {
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
}
