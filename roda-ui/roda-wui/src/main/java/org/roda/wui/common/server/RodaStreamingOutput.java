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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.roda.core.common.ConsumesOutputStream;

public class RodaStreamingOutput implements StreamingOutput {
  private final ConsumesOutputStream outputHandler;

  public RodaStreamingOutput(ConsumesOutputStream outputHandler) {
    super();
    this.outputHandler = outputHandler;
  }

  @Override
  public void write(OutputStream output) throws IOException, WebApplicationException {
    outputHandler.consumeOutputStream(output);

  }

}