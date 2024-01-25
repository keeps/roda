/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkController {

  /**
   * ZkNode where named configs are stored.
   *
   * From:
   * https://github.com/apache/solr/blob/30bf94db62fd92354a9c9437eacf884f8fc862d9/solr/core/src/java/org/apache/solr/cloud/ZkConfigSetService.java#L54
   */
  public static final String CONFIGS_ZKNODE = "/configs";
  /**
   * From:
   * https://github.com/apache/solr/blob/30bf94db62fd92354a9c9437eacf884f8fc862d9/solr/core/src/java/org/apache/solr/core/ConfigSetService.java#L47-L49
   */
  public static final String UPLOAD_FILENAME_EXCLUDE_REGEX = "^\\..*$";
  public static final Pattern UPLOAD_FILENAME_EXCLUDE_PATTERN = Pattern.compile(UPLOAD_FILENAME_EXCLUDE_REGEX);
  private static final Logger LOGGER = LoggerFactory.getLogger(ZkController.class);

  private ZkController() {

  }

  /**
   * Validates if the chroot exists in zk (or if it is successfully created).
   * Optionally, if create is set to true this method will create the path in case
   * it doesn't exist
   *
   * @return true if the path exists or is created false if the path doesn't exist
   *         and 'create' = false
   */
  public static boolean checkChrootPath(String zkHost, boolean create) throws KeeperException, InterruptedException {
    if (!containsChroot(zkHost)) {
      return true;
    }
    LOGGER.trace("zkHost includes chroot");
    String chrootPath = zkHost.substring(zkHost.indexOf("/"));

    try (SolrZkClient tmpClient = new SolrZkClient.Builder().withUrl(zkHost.substring(0, zkHost.indexOf("/")))
      .withTimeout(60, TimeUnit.SECONDS).withConnTimeOut(30, TimeUnit.SECONDS).build()) {
      boolean exists = tmpClient.exists(chrootPath, true);
      if (!exists && create) {
        LOGGER.info("creating chroot {}", chrootPath);
        tmpClient.makePath(chrootPath, false, true);
        exists = true;
      }
      return exists;
    }
  }

  /**
   * Validates if zkHost contains a chroot. See
   * http://zookeeper.apache.org/doc/r3.2.2/zookeeperProgrammers.html#ch_zkSessions
   */
  public static boolean containsChroot(String zkHost) {
    return zkHost.contains("/");
  }

  public static void uploadConfig(SolrZkClient zkClient, String configName, Path dir) throws IOException {
    zkClient.uploadToZK(dir, CONFIGS_ZKNODE + "/" + configName, UPLOAD_FILENAME_EXCLUDE_PATTERN);
  }

}
