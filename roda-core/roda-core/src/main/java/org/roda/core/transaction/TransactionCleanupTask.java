package org.roda.core.transaction;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.roda.core.config.ConfigurationManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * Scheduled task responsible for cleaning up committed transactions in the RODA
 * system and backing up their logs to CSV files.
 *
 * The task fetches all committed transactions, appends their data to backup CSV
 * files, and then deletes the transactions from the system. After processing,
 * the CSV files are compressed and stored.
 *
 * This class is annotated with Spring's {@code @Component} to be managed as a
 * Spring Bean and uses the {@code @Scheduled} annotation to run periodically
 * according to a configured interval.
 * 
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class TransactionCleanupTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCleanupTask.class);

  private final RODATransactionManager transactionManager;
  private final TransactionLogService transactionLogService;
  private final ConfigurationManager configurationManager;

  private Path transactionLogFile;
  private Path transactionModelOperationLogFile;
  private Path transactionStoragePathOperationLogFile;
  private boolean headerWritten = false;

  public TransactionCleanupTask(RODATransactionManager transactionManager,
    TransactionLogService transactionLogService) {
    this.transactionManager = transactionManager;
    this.transactionLogService = transactionLogService;
    this.configurationManager = ConfigurationManager.getInstance();

    LOGGER.info("TransactionCleanupTask created");
  }

  /**
   * Scheduled method that performs cleanup of committed transactions.
   *
   * If both the transaction manager and configuration manager are initialized,
   * this method retrieves all committed transactions, logs their data to CSV
   * files, deletes the transactions from the system, and compresses the resulting
   * files.
   */
  @Scheduled(fixedDelayString = "${transactions.cleanup.interval.millis}")
  public void cleanCommittedTransactions() {
    if (transactionManager.isInitialized() && configurationManager.isInstantiated()) {
      List<TransactionLog> committedTransactions = transactionLogService.getCommittedTransactions();

      if (committedTransactions.isEmpty()) {
        return;
      }

      try {
        initBackupsFile();

        for (TransactionLog transactionLog : committedTransactions) {
          UUID transactionId = transactionLog.getId();
          try {
            appendToCsv(transactionLog);
            // Clean up the transaction
            transactionManager.cleanCommittedTransactions(transactionId);
            LOGGER.info("Cleaned up committed transaction {}", transactionId);
          } catch (RODATransactionException | NotFoundException | GenericException e) {
            LOGGER.error("Error cleaning transaction {}: {}", transactionId, e.getMessage(), e);
          }
        }

        compressBackupsFile();
      } catch (IOException e) {
        LOGGER.error("Error backing up transactions: {}", e.getMessage(), e);
      }
    }
  }

  private void initBackupsFile() throws IOException {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    Path backupPath = configurationManager.getStagingStoragePath()
      .resolve(RodaConstants.CORE_STAGING_TRANSACTIONS_LOG_BACKUP_FOLDER).resolve(timestamp);
    Files.createDirectories(backupPath);

    transactionLogFile = backupPath.resolve("transaction_log.csv");
    transactionModelOperationLogFile = backupPath.resolve("transaction_model_operation_log.csv");
    transactionStoragePathOperationLogFile = backupPath.resolve("transaction_storage_path_operation_log.csv");

    if (!Files.exists(transactionLogFile)) {
      Files.createFile(transactionLogFile);
    }

    if (!Files.exists(transactionModelOperationLogFile)) {
      Files.createFile(transactionModelOperationLogFile);
    }

    if (!Files.exists(transactionStoragePathOperationLogFile)) {
      Files.createFile(transactionStoragePathOperationLogFile);
    }

    headerWritten = false;
  }

  private void appendToCsv(TransactionLog transactionLog) throws IOException, RODATransactionException {
    writeToCsv(transactionLogFile, List.of(transactionLog), TransactionLog.class, !headerWritten);

    List<TransactionalModelOperationLog> modelOperations = transactionLogService
      .getModelOperations(transactionLog.getId());
    writeToCsv(transactionModelOperationLogFile, modelOperations, TransactionalModelOperationLog.class, !headerWritten);

    List<TransactionalStoragePathOperationLog> storagePathOperationLogs = transactionLogService
      .getStoragePathsOperations(transactionLog.getId());
    writeToCsv(transactionStoragePathOperationLogFile, storagePathOperationLogs,
      TransactionalStoragePathOperationLog.class, !headerWritten);

    headerWritten = true;
  }

  /**
   * Writes a list of data objects to a CSV file.
   * 
   * This method serializes the fields of each object in {@code dataList} into a
   * CSV row. The field names are used as column headers, unless a {@code @Column}
   * annotation is present and defines a specific name. Static fields and fields
   * annotated with {@code @OneToMany} are ignored.
   * 
   * For fields annotated with {@code @ManyToOne}, the method tries to extract the
   * {@code id} field from the associated object and include it instead of the
   * full object.
   *
   * @param filePath
   *          the path to the CSV file to write to
   * @param dataList
   *          the list of data objects to write
   * @param clazz
   *          the class of the objects contained in the list
   * @param writeHeader
   *          whether the CSV header row should be written
   * @param <T>
   *          the type of the data objects to write
   * @throws IOException
   *           if an error occurs during file access or writing
   */
  public <T> void writeToCsv(Path filePath, List<T> dataList, Class<T> clazz, boolean writeHeader) throws IOException {
    if (dataList.isEmpty())
      return;

    List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
      .filter(field -> !Modifier.isStatic(field.getModifiers()))
      .filter(field -> !field.isAnnotationPresent(OneToMany.class)).toList();

    for (Field field : fields) {
      field.setAccessible(true);
    }

    List<String> headers = fields.stream().map(field -> {
      Column column = field.getAnnotation(Column.class);
      return (column != null && !column.name().isEmpty()) ? column.name() : field.getName();
    }).collect(Collectors.toList());

    try (
      BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.APPEND);
      CSVPrinter csvPrinter = writeHeader
        ? new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))
        : new CSVPrinter(writer, CSVFormat.DEFAULT)) {

      for (T item : dataList) {
        List<String> row = new ArrayList<>();
        for (Field field : fields) {
          Object value = field.get(item);
          if (value != null && field.isAnnotationPresent(ManyToOne.class)) {
            try {
              Field idField = value.getClass().getDeclaredField("id");
              idField.setAccessible(true);
              Object idValue = idField.get(value);
              row.add(idValue != null ? idValue.toString() : "");
            } catch (NoSuchFieldException | IllegalAccessException e) {
              row.add("");
            }
          } else {
            row.add(value != null ? value.toString() : "");
          }
        }
        csvPrinter.printRecord(row);
      }

      csvPrinter.flush();
    } catch (IllegalAccessException e) {
      throw new IOException("Error accessing fields of class " + clazz.getName(), e);
    }
  }

  private void compressBackupsFile() throws IOException {
    compressBackupFile(transactionLogFile);
    compressBackupFile(transactionModelOperationLogFile);
    compressBackupFile(transactionStoragePathOperationLogFile);
  }

  private void compressBackupFile(Path file) throws IOException {
    Path zipFile = Paths.get(file.toString().replace(".csv", ".zip"));

    try (FileOutputStream fos = new FileOutputStream(zipFile.toFile());
      ZipOutputStream zos = new ZipOutputStream(fos);
      FileInputStream fis = new FileInputStream(file.toFile())) {

      ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
      zos.putNextEntry(zipEntry);

      byte[] buffer = new byte[1024];
      int length;
      while ((length = fis.read(buffer)) >= 0) {
        zos.write(buffer, 0, length);
      }

      zos.closeEntry();
    }

    Files.delete(file);

    LOGGER.info("Backup file compressed to {}", zipFile);
  }

}
