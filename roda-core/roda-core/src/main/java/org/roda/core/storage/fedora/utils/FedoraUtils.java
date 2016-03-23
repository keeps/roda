/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora.utils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.DefaultBinaryVersion;
import org.roda.core.storage.fedora.FedoraStorageService;

/**
 * Fedora related utility class
 * 
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class FedoraUtils {

  /**
   * Private empty constructor
   */
  private FedoraUtils() {

  }

  /**
   * Creates a {@code String} version of a {@code StoragePath} for Fedora
   */
  public static String createFedoraPath(StoragePath storagePath) {
    // XXX white spaces must be URL Encoded
    return storagePath.asString().replaceAll(" ", "%20");
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
   * Method for creating a SPARQL update query using the prefix "roda:". Query
   * is built by doing the concatenation of RODA prefix/namespace declaration,
   * <code>DELETE DATA</code> instruction using <code>metadataToDelete</code>
   * data (if not null) and <code>INSERT DATA</code> instruction using
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
}
