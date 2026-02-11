package org.roda.core.storage;

import java.io.IOException;
import java.io.OutputStream;

public interface SeekableContentPayload extends ContentPayload {
    /**
     * Writes a specific range of the content to the output stream.
     * * @param out The output stream to write to
     * @param offset The start byte position (inclusive)
     * @param length The number of bytes to write
     */
    void writeTo(OutputStream out, long offset, long length) throws IOException;
}