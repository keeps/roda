/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import java.io.IOException;
import java.io.OutputStream;

import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.StreamResponse;

import jakarta.ws.rs.core.StreamingOutput;

public class RodaStreamingOutput implements StreamingOutput {
  private final ConsumesOutputStream outputHandler;

  public RodaStreamingOutput(final ConsumesOutputStream outputHandler) {
    this.outputHandler = outputHandler;
  }

  @Override
  public void write(final OutputStream output) throws IOException {
    outputHandler.consumeOutputStream(output);
  }

  public StreamResponse toStreamResponse() {
    return new StreamResponse(outputHandler);
  }

}