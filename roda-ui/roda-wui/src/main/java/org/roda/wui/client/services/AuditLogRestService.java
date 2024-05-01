package org.roda.wui.client.services;

import org.roda.core.data.v2.log.LogEntry;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "v2 audit logs")
@RequestMapping(path = "../api/v2/audit-logs")
public interface AuditLogRestService extends RODAEntityRestService<LogEntry> {

}
