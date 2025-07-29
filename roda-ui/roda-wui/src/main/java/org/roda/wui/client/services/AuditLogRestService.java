/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import org.roda.core.data.v2.log.LogEntry;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "Audit logs")
@RequestMapping(path = "../api/v2/audit-logs")
public interface AuditLogRestService extends RODAEntityRestService<LogEntry> {

}
