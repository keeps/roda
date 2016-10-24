/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora.utils;

import java.io.IOException;
import java.io.InputStream;
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
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultBinaryVersion;
import org.roda.core.storage.DefaultContainer;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.Directory;
import org.roda.core.storage.fedora.FedoraContentPayload;
import org.roda.core.storage.fedora.FedoraStorageService;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * Fedora related conversions utility class
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class FedoraConversionUtils {

  /**
   * Private empty constructor
   */
  private FedoraConversionUtils() {

  }

  /**
   * Converts a {@code ContentPayload} into a {@code FedoraContent} object
   * 
   * @param payload
   *          the payload to be converted
   * @param mimetype
   *          the mimetype of the payload
   * @throws GenericException
   */
  public static FedoraContent contentPayloadToFedoraContent(ContentPayload payload, String mimetype)
    throws GenericException {
    InputStream inputStream = null;
    try {
      inputStream = payload.createInputStream();
      return new FedoraContent().setContent(inputStream).setContentType(mimetype);
    } catch (IOException e) {
      throw new GenericException("Error while converting content payload into fedora content", e);
    }
  }

  /**
   * Converts a {@code ContentPayload} into a {@code FedoraContent} object using
   * the default mimetype "application/octet-stream"
   * 
   * @param payload
   *          the payload to be converted
   */
  public static FedoraContent contentPayloadToFedoraContent(ContentPayload payload) throws GenericException {
    return contentPayloadToFedoraContent(payload, "application/octet-stream");
  }

  private static StoragePath getStoragePath(FedoraDatastream ds) throws GenericException, RequestNotValidException {
    try {
      return FedoraUtils.fedoraPathToStoragePath(ds.getPath());
    } catch (FedoraException e) {
      throw new GenericException("Error while getting the storage path from a particular Fedora datastream", e);
    }
  }

  private static StoragePath getStoragePath(FedoraObject obj) throws GenericException, RequestNotValidException {
    try {
      return FedoraUtils.fedoraPathToStoragePath(obj.getPath());
    } catch (FedoraException e) {
      throw new GenericException("Error while getting the storage path from a particular Fedora object", e);
    }
  }

  /**
   * Converts a {@code FedoraDatastream} into a {@code Binary}
   * 
   * @param datastream
   *          the Fedora datastream to be converted
   * @throws RequestNotValidException
   */
  public static Binary fedoraDatastreamToBinary(FedoraDatastream datastream)
    throws GenericException, RequestNotValidException {
    try {
      ContentPayload cp = new FedoraContentPayload(datastream);
      // TODO version properties doesn't contain size...
      long sizeInBytes = 0;
      try {
        sizeInBytes = datastream.getContentSize();
      } catch (NullPointerException npe) {

      }
      URI contentDigest = datastream.getContentDigest();

      return new DefaultBinary(getStoragePath(datastream), cp, sizeInBytes, false, extractContentDigest(contentDigest));
    } catch (FedoraException e) {
      throw new GenericException("Error while converting a Fedora datastream into a Binary", e);
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
   * @throws RequestNotValidException
   */
  public static Directory fedoraObjectToDirectory(String fedoraRepositoryURL, FedoraObject object)
    throws GenericException, RequestNotValidException {
    return new DefaultDirectory(getStoragePath(object));
  }

  /**
   * Converts a {@code Triple} iterator into a {@code Map}
   * 
   * @param iterator
   *          triple iterator to be converted
   */
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
   */
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
   * @throws RequestNotValidException
   */
  public static Container fedoraObjectToContainer(FedoraObject object)
    throws GenericException, RequestNotValidException {
    return new DefaultContainer(getStoragePath(object));
  }

  /**
   * Converts a {@code FedoraDatastream} into a {@code BinaryVersion}
   * 
   * @param datastream
   *          Fedora data stream to be converted
   * @param version
   *          The version label
   * @param id
   * @throws GenericException,
   *           RequestNotValidException, FedoraException
   */
  public static BinaryVersion convertDataStreamToBinaryVersion(FedoraDatastream datastream, String id,
    Map<String, String> properties) throws GenericException, RequestNotValidException, FedoraException {
    Binary binary = FedoraConversionUtils.fedoraDatastreamToBinary(datastream);
    return new DefaultBinaryVersion(binary, id, datastream.getCreatedDate(), properties);
  }
}
