package org.roda.core.transaction;

import java.util.Arrays;
import java.util.Objects;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt> Handles rollback operations for RODA
 *         model objects based on transaction logs.
 */
public class TransactionModelRollbackHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionModelRollbackHandler.class);

  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    T get() throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;
  }

  @FunctionalInterface
  public interface ThrowingRunnable {
    void run() throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;
  }

  @FunctionalInterface
  public interface ThrowingConsumer<T> {
    void accept(T t) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;
  }

  private static <T extends IsRODAObject> void process(T rodaObject, TransactionalModelOperationLog modelOperation,
    ThrowingSupplier<T> retrieveFromStorage, ThrowingRunnable notifyDeleted, ThrowingConsumer<T> notifyUpdated)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    if (modelOperation.getOperationType() == OperationType.CREATE) {
      LOGGER.info("Rollback {} creation for ID: {}", rodaObject.getClass().getSimpleName(), rodaObject.getId());
      notifyDeleted.run();
    } else if (modelOperation.getOperationType() != OperationType.READ) {
      LOGGER.info("Rollback {} update/delete for ID: {}", rodaObject.getClass().getSimpleName(), rodaObject.getId());
      T retrieved = retrieveFromStorage.get();
      notifyUpdated.accept(retrieved);
    }
  }

  public static <T extends IsRODAObject> void processObject(T rodaObject, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    if (Objects.requireNonNull(rodaObject) instanceof AIP aip) {
      processAIP(aip, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof Representation representation) {
      processRepresentation(representation, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof File file) {
      processFile(file, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof DIP dip) {
      processDIP(dip, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof DIPFile dipFile) {
      processDIPFile(dipFile, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof DescriptiveMetadata dm) {
      processDescriptiveMetadata(dm, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof PreservationMetadata pm) {
      processPreservationMetadata(pm, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof DisposalConfirmation disposalConfirmation) {
      processDisposalConfirmation(disposalConfirmation, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof Notification notification) {
      processNotification(notification, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof RepresentationInformation representationInformation) {
      processRepresentationInformation(representationInformation, modelOperation, mainModelService,
        stagingModelService);
    } else if (rodaObject instanceof Risk risk) {
      processRisk(risk, modelOperation, mainModelService, stagingModelService);
    } else if (rodaObject instanceof RiskIncidence riskIncidence) {
      processRiskIncidence(riskIncidence, modelOperation, mainModelService, stagingModelService);
    } else {
      LOGGER.warn("The entity class {} is not supported for rollback yet. (ID:{})",
        rodaObject.getClass().getSimpleName(), rodaObject.getId());
    }
  }

  private static <T extends IsRODAObject> void processAIP(AIP aip, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveAIP(aip.getId());
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService.notifyAipUpdated((AIP) retrieved);
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyAipDeleted(aip.getId());
    process(aip, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static <T extends IsRODAObject> void processRepresentation(Representation representation,
    TransactionalModelOperationLog modelOperation, ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveRepresentation(representation.getAipId(),
      representation.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyRepresentationDeleted(representation.getAipId(),
      representation.getId());
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyRepresentationUpdated((Representation) retrieved);
    process(representation, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processFile(File file, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyFileDeleted(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId());
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService.notifyFileUpdated((File) retrieved);
    process(file, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processDIP(DIP dip, TransactionalModelOperationLog modelOperation, ModelService mainModelService,
    ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveDIP(dip.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyDIPDeleted(dip.getId(), true);
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService.notifyDIPUpdated((DIP) retrieved,
      true);
    process(dip, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processDIPFile(DIPFile dipfile, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveDIPFile(dipfile.getDipId(),
      dipfile.getPath(), dipfile.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyDIPFileDeleted(dipfile.getDipId(),
      dipfile.getPath(), dipfile.getId());
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyDIPFileUpdated((DIPFile) retrieved);
    process(dipfile, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processDescriptiveMetadata(DescriptiveMetadata dm, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveDescriptiveMetadata(dm.getAipId(),
      dm.getRepresentationId(), dm.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyDescriptiveMetadataDeleted(dm.getAipId(),
      dm.getRepresentationId(), dm.getId());
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyDescriptiveMetadataUpdated((DescriptiveMetadata) retrieved);
    process(dm, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processPreservationMetadata(PreservationMetadata pm,
    TransactionalModelOperationLog modelOperation, ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrievePreservationMetadata(pm.getId(),
      pm.getType());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyPreservationMetadataDeleted(pm);
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyPreservationMetadataUpdated((PreservationMetadata) retrieved);
    process(pm, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processDisposalConfirmation(DisposalConfirmation disposalConfirmation,
    TransactionalModelOperationLog modelOperation, ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService
      .retrieveDisposalConfirmation(disposalConfirmation.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService
      .notifyDisposalConfirmationDeleted(disposalConfirmation.getId(), true);
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyDisposalConfirmationCreatedOrUpdated((DisposalConfirmation) retrieved);
    process(disposalConfirmation, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processNotification(Notification notification, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveNotification(notification.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyNotificationDeleted(notification.getId());
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyNotificationCreatedOrUpdated((Notification) retrieved);
    process(notification, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processRepresentationInformation(RepresentationInformation representationInformation,
    TransactionalModelOperationLog modelOperation, ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService
      .retrieveRepresentationInformation(representationInformation.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService
      .notifyRepresentationInformationDeleted(representationInformation.getId(), true);
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyRepresentationInformationCreatedOrUpdated((RepresentationInformation) retrieved, true);
    process(representationInformation, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processRisk(Risk risk, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    // TODO: is this the only option for retrieve the incidences count?
    IndexedRisk indexedRisk = RodaCoreFactory.getIndexService().retrieve(IndexedRisk.class, risk.getUUID(),
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCES_COUNT));
    final int incidences = indexedRisk.getIncidencesCount();

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveRisk(risk.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyRiskDeleted(risk.getId(), true);
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyRiskCreatedOrUpdated((Risk) retrieved, incidences, true);
    process(risk, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

  private static void processRiskIncidence(RiskIncidence riskIncidence, TransactionalModelOperationLog modelOperation,
    ModelService mainModelService, ModelService stagingModelService)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    ThrowingSupplier<IsRODAObject> supplier = () -> mainModelService.retrieveRiskIncidence(riskIncidence.getId());
    ThrowingRunnable notifyDeleted = () -> stagingModelService.notifyRiskIncidenceDeleted(riskIncidence.getId(), true);
    ThrowingConsumer<IsRODAObject> notifyUpdated = retrieved -> mainModelService
      .notifyRiskIncidenceCreatedOrUpdated((RiskIncidence) retrieved, true);
    process(riskIncidence, modelOperation, supplier, notifyDeleted, notifyUpdated);
  }

}
