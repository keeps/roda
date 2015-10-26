/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.roda.index.IndexServiceTest;
import org.roda.index.SolrUtilsTest;
import org.roda.model.ModelServiceTest;
import org.roda.storage.fedora.FedoraStorageServiceTest;
import org.roda.storage.fs.FileStorageServiceTest;

@RunWith(Suite.class)
@SuiteClasses({IndexServiceTest.class, ModelServiceTest.class, FileStorageServiceTest.class,
  FedoraStorageServiceTest.class, SolrUtilsTest.class})
public class AllTests {

}
