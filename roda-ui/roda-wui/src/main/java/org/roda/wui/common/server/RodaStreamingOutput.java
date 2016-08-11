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