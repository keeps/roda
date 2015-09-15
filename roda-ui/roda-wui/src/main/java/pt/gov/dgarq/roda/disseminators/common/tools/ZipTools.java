package pt.gov.dgarq.roda.disseminators.common.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.disseminators.common.RepresentationHelper;

/**
 * Tools to handle Zips
 * 
 * @author Luis Faria
 * 
 */
public class ZipTools {

  /**
   * Zip a list of files into an output stream
   * 
   * @param files
   * @param out
   * @throws IOException
   */
  public static void zip(List<ZipEntryInfo> files, OutputStream out) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(out);

    for (ZipEntryInfo file : files) {
      ZipEntry entry = new ZipEntry(file.getName());
      zos.putNextEntry(entry);
      sendToZip(file.getInputStream(), zos);
      zos.closeEntry();
    }

    zos.close();
    out.close();
  }

  /**
   * Put a representation in a zip and send it
   * 
   * @param request
   * @param rep
   *          the representation to send
   * @param out
   *          the output stream where to dump the zip
   * @throws IOException
   * @throws LoginException
   * @throws RODAClientException
   */
  public static void sendZippedRepresentation(HttpServletRequest request, RepresentationObject rep, OutputStream out)
    throws IOException, LoginException, RODAClientException {
    RepresentationHelper representationHelper = new RepresentationHelper();
    ZipOutputStream zos = new ZipOutputStream(out);
    Set<String> fileNames = new HashSet<String>();

    addEntryToZip(zos, representationHelper.getRootMethod(request, rep), rep.getRootFile().getOriginalName());

    for (int i = 0; i < rep.getPartFiles().length; i++) {
      String originalName = rep.getPartFiles()[i].getOriginalName();
      if (fileNames.contains(originalName)) {
        String newFileName;
        int appendIndex = 1;
        do {
          newFileName = createFileName(originalName, appendIndex++);

        } while (fileNames.contains(newFileName));

        originalName = newFileName;
      }

      fileNames.add(originalName);

      addEntryToZip(zos, representationHelper.getPartFileMethod(request, rep, i), originalName);
    }
    zos.close();
    out.close();
  }

  private static String createFileName(String original, int append) {
    String ret;
    int dotIndex = original.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < original.length() - 1) {
      String name = original.substring(0, dotIndex);
      String extention = original.substring(dotIndex);
      ret = name + "_" + append + extention;
    } else {
      ret = original + "_" + append;
    }
    return ret;
  }

  private static void addEntryToZip(ZipOutputStream zos, GetMethod method, String originalName) throws IOException {
    ZipEntry entry = new ZipEntry(originalName);
    zos.putNextEntry(entry);
    int status = method.getStatusCode();
    if (status == HttpStatus.SC_OK) {
      sendToZip(method.getResponseBodyAsStream(), zos);
    } else {
      throw new IOException("Error getting stream, HTTP Code: " + status);
    }
    zos.closeEntry();
  }

  private static void sendToZip(InputStream in, ZipOutputStream zos) throws IOException {
    byte[] buffer = new byte[4096];
    int retval;

    do {
      retval = in.read(buffer, 0, 4096);
      if (retval != -1) {
        zos.write(buffer, 0, retval);
      }
    } while (retval != -1);

    in.close();

  }
}
