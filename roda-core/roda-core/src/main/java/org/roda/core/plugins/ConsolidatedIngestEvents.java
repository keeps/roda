/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps, per ingest job and per AIP, the preservation events that the ingest
 * steps would have written, so that the ingest workflow writes them as a single
 * event. Nothing is kept in memory: the events are appended, one JSON per line,
 * to one file per AIP in the job working directory
 * ({@code <working directory>/<jobId>/ingest-events/<aipId>.jsonl}), which is
 * removed when the AIP event is written, when the ingest ends and, in any case,
 * by the orchestrator when the job reaches a final state.
 *
 * Events are only captured while that folder exists, i.e. while the ingest
 * workflow has the job open, so a step plugin run on its own always writes its
 * own event.
 */
public final class ConsolidatedIngestEvents {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsolidatedIngestEvents.class);

  private static final String FOLDER = "ingest-events";
  private static final String FILE_EXTENSION = ".jsonl";

  private ConsolidatedIngestEvents() {
    // do nothing
  }

  private static Path getFolder(String jobId) {
    return RodaCoreFactory.getWorkingDirectory().resolve(jobId).resolve(FOLDER);
  }

  private static Path getFile(String jobId, String aipId) {
    return getFolder(jobId).resolve(URLEncoder.encode(aipId, StandardCharsets.UTF_8) + FILE_EXTENSION);
  }

  /**
   * Starts capturing the events of the job. If the folder cannot be created,
   * nothing is captured and the events are written as always.
   */
  public static void open(String jobId) {
    if (jobId != null) {
      try {
        Files.createDirectories(getFolder(jobId));
      } catch (IOException e) {
        LOGGER.error("Could not create the folder to keep the ingest events of job '{}', they will be written "
          + "one per step", jobId, e);
      }
    }
  }

  public static boolean isOpen(String jobId) {
    return jobId != null && Files.isDirectory(getFolder(jobId));
  }

  /**
   * @return false if the event could not be kept (job not open or I/O error),
   *         in which case it must be written as always
   */
  public static boolean add(String jobId, String aipId, StepEvent event) {
    if (!isOpen(jobId)) {
      return false;
    }
    String json = JsonUtils.getJsonFromObject(event);
    if (json == null) {
      return false;
    }
    try {
      Files.writeString(getFile(jobId, aipId), json + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
      return true;
    } catch (IOException e) {
      LOGGER.error("Could not keep the ingest event of AIP '{}' of job '{}'", aipId, jobId, e);
      return false;
    }
  }

  public static boolean hasEvents(String jobId, String aipId) {
    return jobId != null && Files.exists(getFile(jobId, aipId));
  }

  /**
   * Reads and deletes the events kept for an AIP (empty if there are none).
   * Lines that cannot be read are skipped and logged.
   */
  public static List<StepEvent> remove(String jobId, String aipId) {
    if (jobId == null) {
      return Collections.emptyList();
    }
    Path file = getFile(jobId, aipId);
    if (!Files.exists(file)) {
      return Collections.emptyList();
    }

    List<StepEvent> events = new ArrayList<>();
    try {
      for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
        if (!line.isBlank()) {
          try {
            events.add(JsonUtils.getObjectFromJson(line, StepEvent.class));
          } catch (GenericException e) {
            LOGGER.error("Could not read an ingest event of AIP '{}' of job '{}'", aipId, jobId, e);
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Could not read the ingest events of AIP '{}' of job '{}'", aipId, jobId, e);
    } finally {
      FSUtils.deletePathQuietly(file);
    }
    return events;
  }

  /**
   * Stops capturing events for the job, deleting the events not written yet.
   */
  public static void close(String jobId) {
    if (jobId == null) {
      return;
    }
    Path folder = getFolder(jobId);
    if (Files.isDirectory(folder)) {
      try (var files = Files.list(folder)) {
        long notWritten = files.count();
        if (notWritten > 0) {
          LOGGER.warn("Discarding the ingest events of job '{}' kept for {} AIP(s) that were never written", jobId,
            notWritten);
        }
      } catch (IOException e) {
        LOGGER.debug("Could not list the ingest events folder of job '{}'", jobId, e);
      }
      FSUtils.deletePathQuietly(folder);
    }
  }

  /**
   * An event as a step would have written it, with every value resolved when
   * it was captured (the plugin may change its event type and messages
   * afterwards).
   */
  public record StepEvent(String pluginName, String pluginVersion, String pluginAgentId, String eventType,
    String eventDescription, Date eventDate, PluginState outcome, String outcomeDetailNote,
    String outcomeDetailExtension, List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes,
    List<LinkingIdentifier> agents) {
  }
}
