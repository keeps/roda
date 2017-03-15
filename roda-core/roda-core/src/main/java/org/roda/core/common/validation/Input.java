/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.validation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.ls.LSInput;

public class Input implements LSInput {

  private BufferedInputStream inputStream;
  private String publicId;
  private String systemId;

  public Input(String publicId, String sysId, InputStream input) {
    this.publicId = publicId;
    this.systemId = sysId;
    this.inputStream = new BufferedInputStream(input);
  }

  @Override
  public String getPublicId() {
    return publicId;
  }

  @Override
  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  @Override
  public String getBaseURI() {
    return null;
  }

  @Override
  public InputStream getByteStream() {
    return null;
  }

  @Override
  public boolean getCertifiedText() {
    return false;
  }

  @Override
  public Reader getCharacterStream() {
    return null;
  }

  @Override
  public String getEncoding() {
    return null;
  }

  @Override
  public String getStringData() {
    synchronized (inputStream) {
      if (inputStream != null) {
        try {
          byte[] input = new byte[inputStream.available()];
          int read = inputStream.read(input);
          return read > 0 ? new String(input) : "";
        } catch (IOException e) {
          return null;
        } finally {
          IOUtils.closeQuietly(inputStream);
        }
      } else {
        return null;
      }
    }
  }

  @Override
  public void setBaseURI(String baseURI) {
    // do nothing
  }

  @Override
  public void setByteStream(InputStream byteStream) {
    // do nothing
  }

  @Override
  public void setCertifiedText(boolean certifiedText) {
    // do nothing
  }

  @Override
  public void setCharacterStream(Reader characterStream) {
    // do nothing
  }

  @Override
  public void setEncoding(String encoding) {
    // do nothing
  }

  @Override
  public void setStringData(String stringData) {
    // do nothing
  }

  @Override
  public String getSystemId() {
    return systemId;
  }

  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public BufferedInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(BufferedInputStream inputStream) {
    this.inputStream = inputStream;
  }

}
