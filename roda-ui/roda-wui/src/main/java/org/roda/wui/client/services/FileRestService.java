package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.roda.core.data.v2.ip.IndexedFile;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 files")
@RequestMapping(path = "../api/v2/files")
public interface FileRestService extends RODAEntityRestService<IndexedFile> {
}
