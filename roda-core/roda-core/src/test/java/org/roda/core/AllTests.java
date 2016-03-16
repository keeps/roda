/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.roda.core.common.JsonUtilsTest;
import org.roda.core.common.monitor.MonitorIndexTest;
import org.roda.core.index.IndexServiceTest;
import org.roda.core.index.PermissionsTest;
import org.roda.core.index.SolrUtilsTest;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.plugins.EARKSIPPluginsTest;
import org.roda.core.plugins.InternalConvertPluginsTestForTravis;
import org.roda.core.plugins.InternalPluginsTest;
import org.roda.core.storage.fedora.FedoraStorageServiceTest;
import org.roda.core.storage.fs.FileStorageServiceTest;

@RunWith(Suite.class)
@SuiteClasses({JsonUtilsTest.class, IndexServiceTest.class, ModelServiceTest.class, FileStorageServiceTest.class,
  FedoraStorageServiceTest.class, SolrUtilsTest.class, InternalPluginsTest.class, InternalConvertPluginsTestForTravis.class,
  EARKSIPPluginsTest.class, MonitorIndexTest.class, PermissionsTest.class})
public class AllTests {

}
