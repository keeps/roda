/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.roda.core.data.v2.ip.DIPFile;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Tag(name = "DIP files")
@RequestMapping(path = "../api/v2/dip-files")
public interface DIPFileRestService extends RODAEntityRestService<DIPFile> {
}
