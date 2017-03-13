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

  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public String getBaseURI() {
    return null;
  }

  public InputStream getByteStream() {
    return null;
  }

  public boolean getCertifiedText() {
    return false;
  }

  public Reader getCharacterStream() {
    return null;
  }

  public String getEncoding() {
    return null;
  }

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

  public void setBaseURI(String baseURI) {
  }

  public void setByteStream(InputStream byteStream) {
  }

  public void setCertifiedText(boolean certifiedText) {
  }

  public void setCharacterStream(Reader characterStream) {
  }

  public void setEncoding(String encoding) {
  }

  public void setStringData(String stringData) {
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public BufferedInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(BufferedInputStream inputStream) {
    this.inputStream = inputStream;
  }

  public Input(String publicId, String sysId, InputStream input) {
    this.publicId = publicId;
    this.systemId = sysId;
    this.inputStream = new BufferedInputStream(input);
  }
}
