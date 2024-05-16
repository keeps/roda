package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 dip")
@RequestMapping(path = "../api/v2/dip")
public interface DIPRestService extends RODAEntityRestService<IndexedDIP> {
}
