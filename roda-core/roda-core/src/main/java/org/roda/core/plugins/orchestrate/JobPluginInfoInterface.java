/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.plugins.Plugin;

public interface JobPluginInfoInterface {

  <T extends IsRODAObject> JobPluginInfo processJobPluginInformation(Plugin<T> plugin, JobInfo jobInfo);
}
