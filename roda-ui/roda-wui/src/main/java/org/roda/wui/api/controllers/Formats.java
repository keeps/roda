/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Formats extends RodaCoreService {

  private static final String FORMATS_COMPONENT = "Formats";

  private Formats() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Format createFormat(RodaUser user, Format format)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    RodaCoreFactory.getModelService().createFormat(format, false);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, FORMATS_COMPONENT, "createFormat", null, duration, "format", format);

    return format;
  }

  public static void deleteFormat(RodaUser user, String formatId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, new Object(){}.getClass().getEnclosingMethod());

    // delegate
    RodaCoreFactory.getModelService().deleteFormat(formatId, false);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, FORMATS_COMPONENT, "deleteFormat", null, duration, "formatId", formatId);
  }

  public static List<Format> retrieveFormats(IndexResult<Format> listFormatsIndexResult) {
    // TODO: this method should also checkRoles? If so, a RodaUser is needed.
    List<Format> formats = new ArrayList<Format>();
    for (Format format : listFormatsIndexResult.getResults()) {
      formats.add(format);
    }
    return formats;
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
