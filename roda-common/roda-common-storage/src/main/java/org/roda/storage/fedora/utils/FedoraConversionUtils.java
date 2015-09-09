package org.roda.storage.fedora.utils;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.roda.storage.Binary;
import org.roda.storage.Container;
import org.roda.storage.ContentPayload;
import org.roda.storage.DefaultBinary;
import org.roda.storage.DefaultContainer;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Directory;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StoragePath;
import org.roda.storage.fedora.FedoraContentPayload;
import org.roda.storage.fedora.FedoraStorageService;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * Fedora related conversions utility class
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public final class FedoraConversionUtils {

  /**
   * Private empty constructor
   * */
  private FedoraConversionUtils() {

  }

  /**
   * Converts a {@code ContentPayload} into a {@code FedoraContent} object
   * 
   * @param payload
   *          the payload to be converted
   * @param mimetype
   *          the mimetype of the payload
   * */
  public static FedoraContent contentPayloadToFedoraContent(ContentPayload payload, String mimetype)
    throws StorageServiceException {
    try {
      return new FedoraContent().setContent(payload.createInputStream()).setContentType(mimetype);
    } catch (IOException e) {
      throw new StorageServiceException("Error while converting content payload into fedora content",
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  /**
   * Converts a {@code ContentPayload} into a {@code FedoraContent} object using
   * the default mimetype "application/octet-stream"
   * 
   * @param payload
   *          the payload to be converted
   * */
  public static FedoraContent contentPayloadToFedoraContent(ContentPayload payload) throws StorageServiceException {
    return contentPayloadToFedoraContent(payload, "application/octet-stream");
  }

  private static StoragePath getStoragePath(FedoraDatastream ds) throws StorageServiceException {
    try {
      return DefaultStoragePath.parse(ds.getPath().substring(0, ds.getPath().lastIndexOf("/")));
    } catch (FedoraException e) {
      throw new StorageServiceException("Error while getting the storage path from a particular Fedora datastream",
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  private static StoragePath getStoragePath(FedoraObject obj) throws StorageServiceException {
    try {
      return DefaultStoragePath.parse(obj.getPath());
    } catch (FedoraException e) {
      throw new StorageServiceException("Error while getting the storage path from a particular Fedora object",
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  /**
   * Converts a {@code FedoraDatastream} into a {@code Binary}
   * 
   * @param datastream
   *          the Fedora datastream to be converted
   * */
  public static Binary fedoraDatastreamToBinary(FedoraDatastream datastream) throws StorageServiceException {
    try {
      Map<String, Set<String>> properties = tripleIteratorToMap(datastream.getProperties());
      ContentPayload cp = new FedoraContentPayload(datastream);
      long sizeInBytes = datastream.getContentSize();
      URI contentDigest = datastream.getContentDigest();
      return new DefaultBinary(getStoragePath(datastream), properties, cp, sizeInBytes, false,
        extractContentDigest(contentDigest));
    } catch (FedoraException e) {
      throw new StorageServiceException("Error while converting a Fedora datastream into a Binary",
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  private static Map<String, String> extractContentDigest(URI contentDigest) {
    Map<String, String> ret = new HashMap<String, String>();
    if (contentDigest != null) {
      final String[] values = contentDigest.getSchemeSpecificPart().split(":");
      if (values.length == 2) {
        ret.put(values[0], values[1]);
      }
    }
    return ret;
  }

  /**
   * Converts a {@code FedoraObject} into a {@code Directory}
   * 
   * @param object
   *          the Fedora object to be converted
   * */
  public static Directory fedoraObjectToDirectory(String fedoraRepositoryURL, FedoraObject object)
    throws StorageServiceException {
    try {
      Map<String, Set<String>> metadata = tripleIteratorToMap(fedoraRepositoryURL, object.getProperties(),
        NodeFactory.createURI(object.getPath()));
      Directory d = new DefaultDirectory(getStoragePath(object), metadata);
      return d;
    } catch (FedoraException e) {
      throw new StorageServiceException("Error while converting a Fedora object into a Directory",
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }

  }

  /**
   * Converts a {@code Triple} iterator into a {@code Map}
   * 
   * @param iterator
   *          triple iterator to be converted
   * */
  public static Map<String, Set<String>> tripleIteratorToMap(Iterator<Triple> iterator) {
    Map<String, Set<String>> map = new HashMap<String, Set<String>>();
    while (iterator.hasNext()) {

      Triple t = iterator.next();
      if (t.getMatchObject().isLiteral()
        && t.getMatchPredicate().getNameSpace().equalsIgnoreCase(FedoraStorageService.RODA_NAMESPACE)) {
        if (map.containsKey(t.getMatchPredicate().getLocalName())) {
          Set<String> oldList = map.get(t.getMatchPredicate().getLocalName());
          oldList.add(t.getMatchObject().getLiteral().getValue().toString());
          map.put(t.getMatchPredicate().getLocalName(), oldList);
        } else {
          Set<String> values = new HashSet<String>();
          values.add(t.getMatchObject().getLiteral().getValue().toString());
          map.put(t.getMatchPredicate().getLocalName(), values);
        }
      }
    }
    return map;
  }

  /**
   * Converts a {@code Triple} iterator into a {@code Map} filtering by subject
   * 
   * @param fedoraRepositoryURL
   *          fedora repository url (will be used to create an URI for matching
   *          the triple subject)
   * @param iterator
   *          triple iterator to be converted
   * @param subject
   *          subject by which the triples should be filtered
   * */
  public static Map<String, Set<String>> tripleIteratorToMap(String fedoraRepositoryURL, Iterator<Triple> iterator,
    Node subject) {
    Map<String, Set<String>> map = new HashMap<String, Set<String>>();
    while (iterator.hasNext()) {

      Triple t = iterator.next();

      if (t.getMatchSubject().hasURI(fedoraRepositoryURL + subject.getURI()) && t.getMatchObject().isLiteral()
        && t.getMatchPredicate().getNameSpace().equalsIgnoreCase(FedoraStorageService.RODA_NAMESPACE)) {
        if (map.containsKey(t.getMatchPredicate().getLocalName())) {
          Set<String> oldList = map.get(t.getMatchPredicate().getLocalName());
          oldList.add(t.getMatchObject().getLiteral().getValue().toString());
          map.put(t.getMatchPredicate().getLocalName(), oldList);
        } else {
          Set<String> values = new HashSet<String>();
          values.add(t.getMatchObject().getLiteral().getValue().toString());
          map.put(t.getMatchPredicate().getLocalName(), values);
        }
      }
    }
    return map;
  }

  /**
   * Converts a {@code FedoraObject} into a {@code Container}
   * 
   * @param object
   *          Fedora object to be converted
   * */
  public static Container fedoraObjectToContainer(FedoraObject object) throws StorageServiceException {
    try {
      return new DefaultContainer(getStoragePath(object), tripleIteratorToMap(object.getProperties()));
    } catch (FedoraException e) {

      throw new StorageServiceException("Error while converting a Fedora object into a Container",
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }
}
