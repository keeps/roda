/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.storage.Binary;
import org.roda.core.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;

public class JHOVEPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(JHOVEPluginUtils.class);

  private JHOVEPluginUtils() {
    // do nothing
  }

  public static Path inspect(File targetFile) throws Exception {
    if (targetFile == null || !targetFile.isFile() || !targetFile.exists()) {
      LOGGER.warn("target file '{}' cannot be found.", targetFile);
      throw new FileNotFoundException("target file '" + targetFile + "' cannot be found.");
    }

    Calendar calendar = Calendar.getInstance();

    App app = new App(JHOVEPluginUtils.class.getSimpleName(), "1.0",
      new int[] {calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)},
      "Format Identification Utility", "");
    JhoveBase jhoveBase = new JhoveBase();

    // FIXME why do we have to create a copy of jhove config file? can this be
    // optimized?
    File configFile = File.createTempFile("jhove", "conf");
    FileOutputStream fos = new FileOutputStream(configFile);
    String jhoveConfigPath = RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "jhove", "config");
    IOUtils.copy(FileUtility.getConfigurationFile(RodaCoreFactory.getConfigPath(), jhoveConfigPath), fos);
    fos.close();

    jhoveBase.init(configFile.getAbsolutePath(), null);

    File outputFile = File.createTempFile("jhove", "output");
    LOGGER.debug("JHOVE output file {}", outputFile);

    Module module = jhoveBase.getModule(null);
    OutputHandler aboutHandler = jhoveBase.getHandler(null);
    OutputHandler xmlHandler = jhoveBase.getHandler("XML");

    LOGGER.debug("Calling JHOVE dispatch(...) on file {}", targetFile);

    jhoveBase.dispatch(app, module, aboutHandler, xmlHandler, outputFile.getAbsolutePath(),
      new String[] {targetFile.getAbsolutePath()});

    configFile.delete();
    return outputFile.toPath();
  }

  public static Path runJhove(Binary binary) throws Exception {
    // FIXME temp file that doesn't get deleted afterwards
    java.io.File f = File.createTempFile("temp", ".temp");
    try (FileOutputStream fos = new FileOutputStream(f)) {
      InputStream inputStream = binary.getContent().createInputStream();
      IOUtils.copy(inputStream, fos);
      IOUtils.closeQuietly(inputStream);
    }
    return inspect(f);
  }

}
