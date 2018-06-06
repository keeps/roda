/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.fedora.FedoraStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fedora related utility class
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class FedoraUtils {
  private static final String FEDORA_PATH_DELIMITER = "/";
  private static final Logger LOGGER = LoggerFactory.getLogger(FedoraUtils.class);

  /**
   * Private empty constructor
   */
  private FedoraUtils() {
    // do nothing
  }

  /**
   * Creates a {@code String} version of a {@code StoragePath} for Fedora
   */
  public static String storagePathToFedoraPath(StoragePath storagePath) {
    return storagePath.asList().stream().map(t -> doubleURLEncode(t))
      .collect(Collectors.joining(FEDORA_PATH_DELIMITER));
  }

  public static StoragePath fedoraPathToStoragePath(String fedoraPath) throws RequestNotValidException {
    List<String> path = Arrays.asList(fedoraPath.split("/")).stream().map(t -> doubleURLDecode(t))
      .collect(Collectors.toList());
    return DefaultStoragePath.parse(path);
  }

  public static String doubleURLEncode(String origin) {
    String encoded = origin;
    String encoding = "UTF-8";
    try {
      encoded = URLEncoder.encode(encoded, encoding);
      encoded = URLEncoder.encode(encoded, encoding);
    } catch (UnsupportedEncodingException uee) {
      LOGGER.error(uee.getMessage(), uee);
    }
    return encoded;
  }

  public static String doubleURLDecode(String origin) {
    String decoded = origin;
    String encoding = "UTF-8";
    try {
      decoded = URLDecoder.decode(decoded, encoding);
      decoded = URLDecoder.decode(decoded, encoding);
    } catch (UnsupportedEncodingException uee) {
      LOGGER.error(uee.getMessage(), uee);
    }
    return decoded;
  }

  /**
   * Method for creating a SPARQL update query using the prefix "roda:"
   * 
   * @param action
   *          action to be performed
   * @param subject
   *          subject to which the action is going to be applied
   * @param predicate
   *          predicate without the prefix (added automatically)
   * @param literal
   *          literal to be used
   */
  public static String createSparqlUpdateQuery(String action, String subject, String predicate, String literal) {
    return "PREFIX " + FedoraStorageService.RODA_PREFIX + ": <" + FedoraStorageService.RODA_NAMESPACE + "> " + action
      + " { " + subject + " " + FedoraStorageService.RODA_PREFIX + ":" + predicate + " '" + literal + "' . }";
  }

  /**
   * Method for creating a SPARQL update query using the prefix "roda:". Query is
   * built by doing the concatenation of RODA prefix/namespace declaration,
   * <code>DELETE DATA</code> instruction using <code>metadataToDelete</code> data
   * (if not null) and <code>INSERT DATA</code> instruction using
   * <code>metadataToAdd</code> data (if not null).
   * 
   * @param metadataToAdd
   *          the metadata to be added
   * @param metadataToDelete
   *          the metadata to be deleted.
   */
  public static String createSparqlUpdateQuery(Map<String, Set<String>> metadataToAdd,
    Map<String, Set<String>> metadataToDelete) {

    boolean hasMetadataToAdd = metadataToAdd != null && !metadataToAdd.isEmpty();
    boolean hasMetadataToDelete = metadataToDelete != null && !metadataToDelete.isEmpty();

    if (!hasMetadataToAdd && !hasMetadataToDelete) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    // PREFIX
    sb.append("PREFIX ").append(FedoraStorageService.RODA_PREFIX).append(": <")
      .append(FedoraStorageService.RODA_NAMESPACE).append(">\n");

    // DELETE
    if (hasMetadataToDelete) {
      sb.append("DELETE { <> ");
      addAllTriples(metadataToDelete, sb);
      sb.append("}\n");
    }

    // INSERT
    if (hasMetadataToAdd) {
      sb.append("INSERT { <> ");
      addAllTriples(metadataToAdd, sb);
      sb.append("}\n");
      sb.append(" WHERE {}");
    }

    return sb.toString();
  }

  private static void addAllTriples(Map<String, Set<String>> metadata, StringBuilder sb) {

    Iterator<Entry<String, Set<String>>> entriesIt = metadata.entrySet().iterator();

    // list of all predicate-object entries separated by a ';'
    while (entriesIt.hasNext()) {
      Entry<String, Set<String>> entry = entriesIt.next();

      // predicate
      sb.append(FedoraStorageService.RODA_PREFIX).append(":").append(entry.getKey());

      // list of literals separated by ','
      Iterator<String> literalsIt = entry.getValue().iterator();
      while (literalsIt.hasNext()) {
        String literal = literalsIt.next();
        sb.append(" '").append(literal).append("'");

        if (literalsIt.hasNext()) {
          sb.append(" , ");
        }
      }

      if (entriesIt.hasNext()) {
        sb.append(" ; ");
      }
    }
  }

  public static StoragePath doubleURLDecode(StoragePath storagePath) {
    try {
      List<String> encodedPath = new ArrayList<>();
      encodedPath.add(doubleURLDecode(storagePath.getContainerName()));
      if (!storagePath.isFromAContainer()) {
        for (String s : storagePath.getDirectoryPath()) {
          encodedPath.add(doubleURLDecode(s));
        }
        encodedPath.add(doubleURLDecode(storagePath.getName()));
      }
      return DefaultStoragePath.parse(encodedPath);
    } catch (RequestNotValidException rnve) {
      return storagePath;
    }
  }
}
