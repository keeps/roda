package pt.gov.dgarq.roda.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.reloading.ReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RodaCorePropertiesReloadStrategy implements ReloadingStrategy {

  /** Constant for the jar URL protocol. */
  private static final String JAR_PROTOCOL = "jar";

  /** Constant for the default refresh delay. */
  private static final int DEFAULT_REFRESH_DELAY = 5000;

  /** Stores a reference to the configuration to be monitored. */
  protected FileConfiguration configuration;

  /** The last time the configuration file was modified. */
  protected long lastModified;

  /** The last time the file was checked for changes. */
  protected long lastChecked;

  /** The minimum delay in milliseconds between checks. */
  protected long refreshDelay = DEFAULT_REFRESH_DELAY;

  /** A flag whether a reload is required. */
  private boolean reloading;

  /** The Log to use for diagnostic messages */
  private Log logger = LogFactory.getLog(FileChangedReloadingStrategy.class);

  public void setConfiguration(FileConfiguration configuration) {
    this.configuration = configuration;
  }

  public void init() {
    updateLastModified();
  }

  public boolean reloadingRequired() {
    if (!reloading) {
      long now = System.currentTimeMillis();

      if (now > lastChecked + refreshDelay) {
        lastChecked = now;
        if (hasChanged()) {
          if (logger.isDebugEnabled()) {
            logger.debug("File change detected: " + getName());
          }
          reloading = true;
        }
      }
    }

    return reloading;
  }

  public void reloadingPerformed() {
    RodaCoreFactory.reloadRodaConfigurationsAfterFileChange();
    updateLastModified();
  }

  /**
   * Return the minimal time in milliseconds between two reloadings.
   *
   * @return the refresh delay (in milliseconds)
   */
  public long getRefreshDelay() {
    return refreshDelay;
  }

  /**
   * Set the minimal time between two reloadings.
   *
   * @param refreshDelay
   *          refresh delay in milliseconds
   */
  public void setRefreshDelay(long refreshDelay) {
    this.refreshDelay = refreshDelay;
  }

  /**
   * Update the last modified time.
   */
  protected void updateLastModified() {
    File file = getFile();
    if (file != null) {
      lastModified = file.lastModified();
    }
    reloading = false;
  }

  /**
   * Check if the configuration has changed since the last time it was loaded.
   *
   * @return a flag whether the configuration has changed
   */
  protected boolean hasChanged() {
    File file = getFile();
    if (file == null || !file.exists()) {
      if (logger.isWarnEnabled() && lastModified != 0) {
        logger.warn("File was deleted: " + getName(file));
        lastModified = 0;
      }
      return false;
    }

    return file.lastModified() > lastModified;
  }

  /**
   * Returns the file that is monitored by this strategy. Note that the return
   * value can be <b>null </b> under some circumstances.
   *
   * @return the monitored file
   */
  protected File getFile() {
    return (configuration.getURL() != null) ? fileFromURL(configuration.getURL()) : configuration.getFile();
  }

  /**
   * Helper method for transforming a URL into a file object. This method
   * handles file: and jar: URLs.
   *
   * @param url
   *          the URL to be converted
   * @return the resulting file or <b>null </b>
   */
  private File fileFromURL(URL url) {
    if (JAR_PROTOCOL.equals(url.getProtocol())) {
      String path = url.getPath();
      try {
        return ConfigurationUtils.fileFromURL(new URL(path.substring(0, path.indexOf('!'))));
      } catch (MalformedURLException mex) {
        return null;
      }
    } else {
      return ConfigurationUtils.fileFromURL(url);
    }
  }

  private String getName() {
    return getName(getFile());
  }

  private String getName(File file) {
    String name = configuration.getURL().toString();
    if (name == null) {
      if (file != null) {
        name = file.getAbsolutePath();
      } else {
        name = "base: " + configuration.getBasePath() + "file: " + configuration.getFileName();
      }
    }
    return name;
  }

}
