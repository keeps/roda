/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.browse;

import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.Locale;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.ApplicationAuth;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import it.burning.cron.CronExpressionDescriptor;

/**
 * Browser Service Implementation
 *
 * @author Luis Faria
 *
 */
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {

  static final String FONDLIST_PAGESIZE = "10";
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  /**
   * Create a new BrowserService Implementation instance
   *
   */
  public BrowserServiceImpl() {
    // do nothing
  }

  @Override
  protected void doUnexpectedFailure(Throwable e) {
    LOGGER.error("Unexpected failure", e);
    super.doUnexpectedFailure(e);
  }

  public String getCrontabValue(String locale) {
    String syncSchedule = RodaCoreFactory.getRodaConfigurationAsString("core.synchronization.scheduleInfo");
    String description = null;
    if (StringUtils.isNotBlank(syncSchedule)) {
      CronExpressionDescriptor.setDefaultLocale(locale.split("_")[0]);
      description = CronExpressionDescriptor.getDescription(syncSchedule);
    }
    return description;
  }
}
