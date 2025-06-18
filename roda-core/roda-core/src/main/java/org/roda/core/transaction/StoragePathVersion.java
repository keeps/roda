package org.roda.core.transaction;

import org.roda.core.data.v2.ip.StoragePath;

public record StoragePathVersion(StoragePath storagePath, String version) {
}
