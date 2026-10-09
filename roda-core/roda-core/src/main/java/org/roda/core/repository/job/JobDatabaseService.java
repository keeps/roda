/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.db.jobs.Job;
import org.roda.core.data.v2.db.jobs.JobAttachment;
import org.roda.core.data.v2.db.jobs.JobPluginParameter;
import org.roda.core.data.v2.db.jobs.JobReport;
import org.roda.core.data.v2.db.jobs.JobReportSourceOriginalId;
import org.roda.core.data.v2.db.jobs.JobReportStep;
import org.roda.core.data.v2.db.jobs.JobSourceObject;
import org.roda.core.data.v2.db.jobs.JobSourceSelection;
import org.roda.core.data.v2.db.jobs.JobStats;
import org.roda.core.data.v2.db.jobs.JobUser;
import org.roda.core.data.v2.db.jobs.PluginDescriptor;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.Report;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

/**
 * Database layer for running jobs and their reports (the {@code db.jobs}
 * entities). Takes and returns the model objects
 * ({@link org.roda.core.data.v2.jobs.Job}, {@link Report}), so that the model
 * service keeps a single representation; each method runs in one database
 * transaction.
 *
 * Writes touch only what changed: a job update rewrites its row and counters
 * but syncs its child rows only when they differ from what was last saved, and
 * a report update keeps the stored steps the report still starts with,
 * replacing only the steps after them (normally a single step is appended, or
 * the last one replaced).
 *
 * @author RODA Development Team
 */
@Service
public class JobDatabaseService {
  private static final Pattern UUID_PATTERN = Pattern
    .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  // rows/ids per bulk statement (keeps bind parameters well under the driver's limit)
  private static final int BULK_CHUNK = 1000;
  // reports per page when reading many reports (flush, listings), and per transaction when deleting them
  private static final int REPORT_PAGE_SIZE = 500;
  private static final int REPORTS_PER_DELETE = 1000;
  // bounds of the per-job report caches
  private static final int MAX_CACHED_REPORTS_PER_JOB = 10_000;
  private static final int MAX_CACHED_JOBS = 1_000;

  @PersistenceContext
  private EntityManager em;

  private final JobRepository jobRepository;
  private final JobStatsRepository jobStatsRepository;
  private final JobPluginParameterRepository jobPluginParameterRepository;
  private final JobSourceObjectRepository jobSourceObjectRepository;
  private final JobUserRepository jobUserRepository;
  private final JobAttachmentRepository jobAttachmentRepository;
  private final JobReportRepository jobReportRepository;
  private final JobReportStepRepository jobReportStepRepository;
  private final JobReportSourceOriginalIdRepository jobReportSourceOriginalIdRepository;
  private final PluginDescriptorRepository pluginDescriptorRepository;
  private final TransactionTemplate newTransaction;
  private final TransactionTemplate inTransaction;
  private final TransactionTemplate readOnlyTransaction;

  // plugins are shared reference data: cached once committed
  private final Map<String, Integer> pluginIds = new ConcurrentHashMap<>();
  private final Map<Integer, PluginDescriptor> pluginsById = new ConcurrentHashMap<>();
  // fingerprint of each job's child rows as last committed, to skip syncing them when unchanged
  private final Map<String, Integer> jobChildrenFingerprints = new ConcurrentHashMap<>();
  // committed values that rarely change, so saves and reads don't query them again (entries go when the
  // job is deleted): a job's instance id ("" for none), and per job its reports' pks by business id and
  // original source ids (kept per job, so a deleted job's entries go at once without listing them)
  private final Map<String, String> jobInstanceIds = new ConcurrentHashMap<>();
  private final Map<String, ReportCache> reportCaches = new ConcurrentHashMap<>();
  // committed child rows of each running job (plugin parameters, LIST source objects, users, attachments):
  // written with the job and rarely changed, but read on every retrieveJob, which the orchestrator calls a lot
  private final Map<String, JobEntityMapper.JobChildren> jobChildren = new ConcurrentHashMap<>();

  public JobDatabaseService(JobRepository jobRepository, JobStatsRepository jobStatsRepository,
    JobPluginParameterRepository jobPluginParameterRepository, JobSourceObjectRepository jobSourceObjectRepository,
    JobUserRepository jobUserRepository, JobAttachmentRepository jobAttachmentRepository,
    JobReportRepository jobReportRepository, JobReportStepRepository jobReportStepRepository,
    JobReportSourceOriginalIdRepository jobReportSourceOriginalIdRepository,
    PluginDescriptorRepository pluginDescriptorRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.jobStatsRepository = jobStatsRepository;
    this.jobPluginParameterRepository = jobPluginParameterRepository;
    this.jobSourceObjectRepository = jobSourceObjectRepository;
    this.jobUserRepository = jobUserRepository;
    this.jobAttachmentRepository = jobAttachmentRepository;
    this.jobReportRepository = jobReportRepository;
    this.jobReportStepRepository = jobReportStepRepository;
    this.jobReportSourceOriginalIdRepository = jobReportSourceOriginalIdRepository;
    this.pluginDescriptorRepository = pluginDescriptorRepository;
    this.newTransaction = new TransactionTemplate(transactionManager);
    this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.inTransaction = new TransactionTemplate(transactionManager);
    this.readOnlyTransaction = new TransactionTemplate(transactionManager);
    this.readOnlyTransaction.setReadOnly(true);
  }

  /**
   * Committed values of one job's reports: pk by business id, and original
   * source ids by pk. Capped: beyond {@link #MAX_CACHED_REPORTS_PER_JOB} reports,
   * the rest are simply looked up in the database.
   */
  private static final class ReportCache {
    private final Map<String, Long> pks = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> originalIds = new ConcurrentHashMap<>();

    void putPk(String reportId, Long pk) {
      if (pks.size() < MAX_CACHED_REPORTS_PER_JOB || pks.containsKey(reportId)) {
        pks.put(reportId, pk);
      }
    }

    void putOriginalIds(Long pk, List<String> ids) {
      if (originalIds.size() < MAX_CACHED_REPORTS_PER_JOB || originalIds.containsKey(pk)) {
        originalIds.put(pk, ids);
      }
    }
  }

  /**
   * @return the job's report cache, created if needed (all caches are dropped
   *         if too many jobs accumulate, e.g. jobs deleted by another node)
   */
  private ReportCache reportCache(String jobId) {
    if (reportCaches.size() >= MAX_CACHED_JOBS && !reportCaches.containsKey(jobId)) {
      reportCaches.clear();
    }
    return reportCaches.computeIfAbsent(jobId, k -> new ReportCache());
  }

  /**
   * @return the job id a report's business id starts with
   *         ({@code jobId-sourceObjectId-outcomeObjectId}), or {@code null}
   */
  private static String jobIdOfReport(String reportId) {
    if (reportId != null && reportId.length() > 37 && reportId.charAt(36) == '-') {
      String jobId = reportId.substring(0, 36);
      return isStorableJobId(jobId) ? jobId : null;
    }
    return null;
  }

  // ================================================================ jobs

  /**
   * Job ids are stored as {@code uuid}: a job with any other id (e.g. a legacy
   * job in storage) is never kept in the database.
   */
  public static boolean isStorableJobId(String jobId) {
    return jobId != null && UUID_PATTERN.matcher(jobId).matches();
  }

  @Transactional(readOnly = true)
  public boolean jobExists(String jobId) {
    return isStorableJobId(jobId) && jobRepository.existsById(jobId);
  }

  @Transactional(readOnly = true)
  public Optional<org.roda.core.data.v2.jobs.Job> findJob(String jobId) throws GenericException {
    if (!isStorableJobId(jobId)) {
      return Optional.empty();
    }
    Optional<Job> job = jobRepository.findById(jobId);
    return job.isPresent() ? Optional.of(toModel(job.get())) : Optional.empty();
  }

  /**
   * @return the running jobs (those not yet flushed to storage, which are listed
   *         from storage), with their counters and child rows read in bulk
   */
  @Transactional(readOnly = true)
  public List<org.roda.core.data.v2.jobs.Job> findAllJobs() throws GenericException {
    List<Object[]> rows = em.createQuery("select j, s from Job j left join JobStats s on s.jobId = j.id "
      + "where j.flushedAt is null order by j.startDate", Object[].class).getResultList();
    List<Job> jobs = rows.stream().map(row -> (Job) row[0]).toList();
    loadChildren(jobs.stream().filter(job -> !jobChildren.containsKey(job.getId())).toList());

    List<org.roda.core.data.v2.jobs.Job> ret = new ArrayList<>(rows.size());
    for (Object[] row : rows) {
      Job job = (Job) row[0];
      ret.add(JobEntityMapper.toModel(job, (JobStats) row[1], children(job)));
    }
    return ret;
  }

  /**
   * Reads the child rows of many jobs with one query per table, and caches them.
   */
  private void loadChildren(List<Job> jobs) {
    for (List<Job> chunk : chunks(jobs, BULK_CHUNK)) {
      List<String> ids = chunk.stream().map(Job::getId).toList();
      Map<String, List<JobPluginParameter>> parameters = new HashMap<>();
      em.createQuery("select p from JobPluginParameter p where p.jobId in :ids order by p.id", JobPluginParameter.class)
        .setParameter("ids", ids).getResultList()
        .forEach(p -> parameters.computeIfAbsent(p.getJobId(), k -> new ArrayList<>()).add(p));
      Map<String, List<JobUser>> users = new HashMap<>();
      em.createQuery("select u from JobUser u where u.jobId in :ids order by u.id", JobUser.class)
        .setParameter("ids", ids).getResultList()
        .forEach(u -> users.computeIfAbsent(u.getJobId(), k -> new ArrayList<>()).add(u));
      Map<String, List<JobAttachment>> attachments = new HashMap<>();
      em.createQuery("select a from JobAttachment a where a.jobId in :ids order by a.id", JobAttachment.class)
        .setParameter("ids", ids).getResultList()
        .forEach(a -> attachments.computeIfAbsent(a.getJobId(), k -> new ArrayList<>()).add(a));
      Map<String, List<String>> sourceObjects = new HashMap<>();
      List<String> listJobs = chunk.stream().filter(j -> j.getSourceSelection() == JobSourceSelection.LIST)
        .map(Job::getId).toList();
      if (!listJobs.isEmpty()) {
        em.createQuery("select o.jobId, o.objectId from JobSourceObject o where o.jobId in :ids order by o.id",
          Object[].class).setParameter("ids", listJobs).getResultList()
          .forEach(o -> sourceObjects.computeIfAbsent((String) o[0], k -> new ArrayList<>()).add((String) o[1]));
      }
      for (String id : ids) {
        jobChildren.putIfAbsent(id,
          JobEntityMapper.JobChildren.fromRows(parameters.getOrDefault(id, List.of()),
            sourceObjects.getOrDefault(id, List.of()), users.getOrDefault(id, List.of()),
            attachments.getOrDefault(id, List.of())));
      }
    }
  }

  private org.roda.core.data.v2.jobs.Job toModel(Job job) throws GenericException {
    return JobEntityMapper.toModel(job, jobStatsRepository.findById(job.getId()).orElse(null), children(job));
  }

  /**
   * @return the job's child row values, from memory when known (they are cached
   *         when the job is saved), otherwise read once from the database
   */
  private JobEntityMapper.JobChildren children(Job job) {
    String id = job.getId();
    JobEntityMapper.JobChildren children = jobChildren.get(id);
    if (children == null) {
      List<String> sourceObjectIds = job.getSourceSelection() == JobSourceSelection.LIST
        ? em.createQuery("select o.objectId from JobSourceObject o where o.jobId = :job order by o.id", String.class)
          .setParameter("job", id).getResultList()
        : List.of();
      children = JobEntityMapper.JobChildren.fromRows(jobPluginParameterRepository.findByJobIdOrderById(id),
        sourceObjectIds, jobUserRepository.findByJobIdOrderById(id), jobAttachmentRepository.findByJobIdOrderById(id));
      // only if still unknown: a save committed meanwhile holds newer values
      JobEntityMapper.JobChildren known = jobChildren.putIfAbsent(id, children);
      if (known != null) {
        children = known;
      }
    }
    return children;
  }

  /**
   * Creates or updates the job, its counters and its child rows.
   */
  @Transactional
  public void saveJob(org.roda.core.data.v2.jobs.Job model) {
    String id = model.getId();
    if (!isStorableJobId(id)) {
      throw new IllegalArgumentException("Job id is not a UUID, so the job cannot be kept in the database: " + id);
    }
    Job job = jobRepository.findById(id).orElse(null);
    boolean isNew = job == null;
    if (isNew) {
      job = new Job();
    }
    JobEntityMapper.toEntity(model, job);

    JobStats stats = isNew ? null : jobStatsRepository.findById(id).orElse(null);
    boolean newStats = stats == null;
    if (newStats) {
      stats = new JobStats(id);
    }
    JobEntityMapper.toEntity(model.getJobStats(), stats);

    if (isNew) {
      em.persist(job);
    }
    if (newStats) {
      em.persist(stats);
    }
    // child rows reference the job row, so it must exist before they are inserted
    em.flush();

    String instanceId = model.getInstanceId() == null ? "" : model.getInstanceId();
    afterCommit(() -> jobInstanceIds.put(id, instanceId));

    int fingerprint = JobEntityMapper.childrenFingerprint(model);
    Integer committed = jobChildrenFingerprints.get(id);
    if (isNew || committed == null || committed != fingerprint || !jobChildren.containsKey(id)) {
      syncJobChildren(model, isNew);
      JobEntityMapper.JobChildren children = JobEntityMapper.JobChildren.of(model);
      afterCommit(() -> {
        jobChildrenFingerprints.put(id, fingerprint);
        jobChildren.put(id, children);
      });
    }
  }

  private void syncJobChildren(org.roda.core.data.v2.jobs.Job model, boolean isNew) {
    String id = model.getId();

    // plugin parameters, by name
    Map<String, String> parameters = model.getPluginParameters() == null ? Map.of() : model.getPluginParameters();
    Map<String, JobPluginParameter> storedParameters = new HashMap<>();
    if (!isNew) {
      for (JobPluginParameter parameter : jobPluginParameterRepository.findByJobIdOrderById(id)) {
        storedParameters.put(parameter.getName(), parameter);
      }
    }
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      JobPluginParameter stored = storedParameters.remove(entry.getKey());
      if (stored == null) {
        JobPluginParameter parameter = new JobPluginParameter();
        parameter.setJobId(id);
        parameter.setName(entry.getKey());
        parameter.setValue(entry.getValue());
        em.persist(parameter);
      } else if (!Objects.equals(stored.getValue(), entry.getValue())) {
        stored.setValue(entry.getValue());
      }
    }
    storedParameters.values().forEach(em::remove);

    // users, by (username, role)
    Map<String, JobUserDetails> users = new LinkedHashMap<>();
    if (model.getJobUsersDetails() != null) {
      for (JobUserDetails user : model.getJobUsersDetails()) {
        if (user.getUsername() != null) {
          users.putIfAbsent(userKey(user.getUsername(), user.getRole()), user);
        }
      }
    }
    Map<String, JobUser> storedUsers = new HashMap<>();
    if (!isNew) {
      for (JobUser user : jobUserRepository.findByJobIdOrderById(id)) {
        storedUsers.put(userKey(user.getUsername(), user.getRole()), user);
      }
    }
    for (Map.Entry<String, JobUserDetails> entry : users.entrySet()) {
      JobUserDetails details = entry.getValue();
      JobUser stored = storedUsers.remove(entry.getKey());
      if (stored == null) {
        stored = new JobUser();
        stored.setJobId(id);
        stored.setUsername(details.getUsername());
        stored.setRole(details.getRole() == null ? "" : details.getRole());
        em.persist(stored);
      }
      stored.setFullName(details.getFullname());
      stored.setEmail(details.getEmail());
    }
    storedUsers.values().forEach(em::remove);

    // attachments, by file name
    Set<String> attachments = model.getAttachmentsList() == null ? Set.of()
      : new HashSet<>(model.getAttachmentsList());
    Set<String> storedAttachmentNames = new HashSet<>();
    if (!isNew) {
      for (JobAttachment attachment : jobAttachmentRepository.findByJobIdOrderById(id)) {
        if (attachments.contains(attachment.getFileName())) {
          storedAttachmentNames.add(attachment.getFileName());
        } else {
          em.remove(attachment);
        }
      }
    }
    for (String fileName : attachments) {
      if (fileName != null && !storedAttachmentNames.contains(fileName)) {
        JobAttachment attachment = new JobAttachment();
        attachment.setJobId(id);
        attachment.setFileName(fileName);
        em.persist(attachment);
      }
    }

    // LIST source objects, by object id (can be many: inserted in bulk)
    List<String> sourceObjectIds = JobEntityMapper.sourceObjectIds(model);
    Set<String> toInsert = new LinkedHashSet<>(sourceObjectIds);
    if (!isNew) {
      Set<String> wanted = new HashSet<>(sourceObjectIds);
      List<Long> toDelete = new ArrayList<>();
      for (JobSourceObject stored : jobSourceObjectRepository.findByJobIdOrderById(id)) {
        if (wanted.contains(stored.getObjectId())) {
          toInsert.remove(stored.getObjectId());
        } else {
          toDelete.add(stored.getId());
        }
      }
      if (!toDelete.isEmpty()) {
        em.flush();
        for (List<Long> chunk : chunks(toDelete, BULK_CHUNK)) {
          em.createNativeQuery("DELETE FROM job_source_objects WHERE id IN (:ids)").setParameter("ids", chunk)
            .executeUpdate();
        }
      }
    }
    insertSourceObjects(id, new ArrayList<>(toInsert));
  }

  private void insertSourceObjects(String jobId, List<String> objectIds) {
    for (List<String> chunk : chunks(objectIds, BULK_CHUNK)) {
      StringBuilder sql = new StringBuilder("INSERT INTO job_source_objects (job_id, object_id) VALUES ");
      for (int i = 0; i < chunk.size(); i++) {
        sql.append(i == 0 ? "" : ", ").append("(:job, :o").append(i).append(')');
      }
      Query query = em.createNativeQuery(sql.toString()).setParameter("job", jobId);
      for (int i = 0; i < chunk.size(); i++) {
        query.setParameter("o" + i, chunk.get(i));
      }
      query.executeUpdate();
    }
  }

  /**
   * Overwrites the counters of a job in the database with a single UPDATE.
   *
   * @return whether the job's counters row exists (and was updated)
   */
  @Transactional
  public boolean updateJobStats(String jobId, org.roda.core.data.v2.jobs.JobStats model) {
    if (!isStorableJobId(jobId)) {
      return false;
    }
    JobStats stats = new JobStats(jobId);
    JobEntityMapper.toEntity(model, stats);
    return em.createQuery("update JobStats s set s.completionPercentage = :completion, "
      + "s.sourceObjectsCount = :count, s.beingProcessed = :being, s.waitingToBeProcessed = :waiting, "
      + "s.processedSuccess = :success, s.processedPartialSuccess = :partial, s.processedFailure = :failure, "
      + "s.processedSkipped = :skipped, s.manualIntervention = :manual where s.jobId = :job")
      .setParameter("completion", stats.getCompletionPercentage()).setParameter("count", stats.getSourceObjectsCount())
      .setParameter("being", stats.getBeingProcessed()).setParameter("waiting", stats.getWaitingToBeProcessed())
      .setParameter("success", stats.getProcessedSuccess()).setParameter("partial", stats.getProcessedPartialSuccess())
      .setParameter("failure", stats.getProcessedFailure()).setParameter("skipped", stats.getProcessedSkipped())
      .setParameter("manual", stats.getManualIntervention()).setParameter("job", jobId).executeUpdate() > 0;
  }

  /**
   * Deletes the jobs with all their rows (reports, steps, counters, children).
   * Reports go in bounded transactions of {@link #REPORTS_PER_DELETE}, so a
   * large job never becomes one huge transaction; a deletion interrupted midway
   * is completed by deleting again.
   *
   * @return the number of jobs deleted
   */
  public int deleteJobs(Collection<String> jobIds) {
    List<String> ids = jobIds.stream().filter(JobDatabaseService::isStorableJobId).toList();
    if (ids.isEmpty()) {
      return 0;
    }
    int deleted = 0;
    for (List<String> chunk : chunks(ids, BULK_CHUNK)) {
      Integer reports;
      do {
        reports = inTransaction.execute(status -> deleteReportsOf(chunk));
      } while (reports != null && reports == REPORTS_PER_DELETE);
      Integer jobs = inTransaction.execute(status -> deleteJobRows(chunk));
      deleted += jobs == null ? 0 : jobs;
    }
    ids.forEach(this::forgetJob);
    return deleted;
  }

  /**
   * Deletes up to {@link #REPORTS_PER_DELETE} reports of the jobs, bottom-up
   * with one set-based statement per table (the foreign keys would cascade row
   * by row).
   *
   * @return the number of reports deleted
   */
  private int deleteReportsOf(List<String> jobIds) {
    List<Long> pks = em.createQuery("select r.pk from JobReport r where r.jobId in :jobs", Long.class)
      .setParameter("jobs", jobIds).setMaxResults(REPORTS_PER_DELETE).getResultList();
    if (!pks.isEmpty()) {
      for (String table : List.of("job_report_steps", "job_report_source_original_ids")) {
        em.createNativeQuery("DELETE FROM " + table + " WHERE report_pk IN (:pks)").setParameter("pks", pks)
          .executeUpdate();
      }
      em.createNativeQuery("DELETE FROM job_reports WHERE pk IN (:pks)").setParameter("pks", pks).executeUpdate();
    }
    return pks.size();
  }

  /**
   * Deletes the jobs' rows other than reports (which must be gone already).
   *
   * @return the number of jobs deleted
   */
  private int deleteJobRows(List<String> jobIds) {
    for (String table : List.of("job_plugin_parameters", "job_source_objects", "job_users", "job_attachments",
      "job_stats")) {
      em.createNativeQuery("DELETE FROM " + table + " WHERE job_id IN (:ids)").setParameter("ids", jobIds)
        .executeUpdate();
    }
    return em.createNativeQuery("DELETE FROM jobs WHERE id IN (:ids)").setParameter("ids", jobIds).executeUpdate();
  }

  private void forgetJob(String jobId) {
    jobChildrenFingerprints.remove(jobId);
    jobChildren.remove(jobId);
    jobInstanceIds.remove(jobId);
    reportCaches.remove(jobId);
  }

  /**
   * Deletes up to {@code batchSize} jobs already flushed to storage.
   *
   * @return the number of jobs deleted
   */
  public int deleteFlushedJobs(int batchSize) {
    List<String> ids = readOnlyTransaction.execute(status -> em
      .createQuery("select j.id from Job j where j.flushedAt is not null order by j.flushedAt", String.class)
      .setMaxResults(batchSize).getResultList());
    return ids == null ? 0 : deleteJobs(ids);
  }

  // ============================================================= reports

  @Transactional(readOnly = true)
  public Optional<Report> findReport(String reportId) {
    JobReport entity = findReportEntity(reportId);
    if (entity == null) {
      return Optional.empty();
    }
    return Optional.of(JobEntityMapper.toModel(entity, originalIds(entity),
      jobReportStepRepository.findByReportPkOrderBySeq(entity.getPk()), instanceIdOf(entity.getJobId()),
      this::plugin));
  }

  /**
   * @return the job's reports, with their steps, in creation order
   */
  @Transactional(readOnly = true)
  public List<Report> findReports(String jobId) {
    return loadReports(jobId, null);
  }

  /**
   * @return the job's reports written in the given RODA transaction, with their
   *         steps, in creation order (the reports of a block, which the
   *         transaction manager inspects when the block ends)
   */
  @Transactional(readOnly = true)
  public List<Report> findReportsByTransaction(String jobId, String transactionId) {
    return transactionId == null ? List.of() : loadReports(jobId, transactionId);
  }

  /**
   * One query per table for all the job's reports, or only those of one
   * transaction (no IN lists: a job can have many reports).
   */
  private List<Report> loadReports(String jobId, String transactionId) {
    if (!isStorableJobId(jobId)) {
      return List.of();
    }
    String inTransaction = transactionId == null ? "" : " and r.transactionId = :transaction";
    TypedQuery<JobReport> reportsQuery = em.createQuery(
      "select r from JobReport r where r.jobId = :job" + inTransaction + " order by r.pk", JobReport.class);
    TypedQuery<JobReportStep> stepsQuery = em.createQuery("select s from JobReportStep s, JobReport r "
      + "where s.reportPk = r.pk and r.jobId = :job" + inTransaction + " order by s.reportPk, s.seq",
      JobReportStep.class);
    TypedQuery<JobReportSourceOriginalId> originalIdsQuery = em.createQuery("select o from "
      + "JobReportSourceOriginalId o, JobReport r where o.reportPk = r.pk and r.jobId = :job" + inTransaction
      + " order by o.id", JobReportSourceOriginalId.class);
    for (TypedQuery<?> query : List.of(reportsQuery, stepsQuery, originalIdsQuery)) {
      query.setParameter("job", jobId);
      if (transactionId != null) {
        query.setParameter("transaction", transactionId);
      }
    }

    List<JobReport> reports = reportsQuery.getResultList();
    if (reports.isEmpty()) {
      return List.of();
    }
    Map<String, String> instanceIds = new HashMap<>();
    instanceIds.put(jobId, instanceIdOf(jobId));
    return toModels(reports, stepsQuery.getResultList(), originalIdsQuery.getResultList(), instanceIds);
  }

  /**
   * One page of a job's reports, in creation order, for reading a job's reports
   * without holding them all (e.g. when flushing it to storage).
   *
   * @param lastPk
   *          where the next page starts ({@code afterPk} of the next call)
   */
  public record ReportPage(List<Report> reports, long lastPk) {
  }

  /**
   * @return the job's reports created after {@code afterPk}, at most
   *         {@code limit}
   */
  @Transactional(readOnly = true)
  public ReportPage findReportsPage(String jobId, long afterPk, int limit) {
    if (!isStorableJobId(jobId)) {
      return new ReportPage(List.of(), afterPk);
    }
    List<JobReport> reports = em
      .createQuery("select r from JobReport r where r.jobId = :job and r.pk > :after order by r.pk", JobReport.class)
      .setParameter("job", jobId).setParameter("after", afterPk).setMaxResults(limit).getResultList();
    if (reports.isEmpty()) {
      return new ReportPage(List.of(), afterPk);
    }
    return new ReportPage(toModels(reports), reports.get(reports.size() - 1).getPk());
  }

  /**
   * @return the reports of all running jobs (those not yet flushed to storage,
   *         whose reports are listed from storage), read lazily page by page
   */
  public CloseableIterable<OptionalWithCause<Report>> iterateRunningJobReports() {
    return new PagedIterable<>(after -> readOnlyTransaction.execute(status -> {
      List<JobReport> reports = em.createQuery("select r from JobReport r, Job j where r.jobId = j.id "
        + "and j.flushedAt is null and r.pk > :after order by r.pk", JobReport.class).setParameter("after", after)
        .setMaxResults(REPORT_PAGE_SIZE).getResultList();
      List<OptionalWithCause<Report>> items = toModels(reports).stream().map(OptionalWithCause::of).toList();
      return new PagedIterable.Page<>(items, reports.isEmpty() ? after : reports.get(reports.size() - 1).getPk(),
        reports.size() < REPORT_PAGE_SIZE);
    }));
  }

  /**
   * @return the (job id, report id) of the reports of all running jobs, read
   *         lazily page by page (for listings that only need references)
   */
  public CloseableIterable<List<String>> iterateRunningJobReportIds() {
    return new PagedIterable<>(after -> readOnlyTransaction.execute(status -> {
      List<Object[]> rows = em.createQuery("select r.pk, r.jobId, r.id from JobReport r, Job j "
        + "where r.jobId = j.id and j.flushedAt is null and r.pk > :after order by r.pk", Object[].class)
        .setParameter("after", after).setMaxResults(REPORT_PAGE_SIZE).getResultList();
      List<List<String>> items = rows.stream().map(row -> List.of((String) row[1], (String) row[2])).toList();
      return new PagedIterable.Page<>(items, rows.isEmpty() ? after : (Long) rows.get(rows.size() - 1)[0],
        rows.size() < REPORT_PAGE_SIZE);
    }));
  }

  /**
   * Builds the models of a page of reports, reading their steps and original ids
   * with one query each.
   */
  private List<Report> toModels(List<JobReport> reports) {
    if (reports.isEmpty()) {
      return List.of();
    }
    List<Long> pks = reports.stream().map(JobReport::getPk).toList();
    List<JobReportStep> steps = em
      .createQuery("select s from JobReportStep s where s.reportPk in :pks order by s.reportPk, s.seq",
        JobReportStep.class)
      .setParameter("pks", pks).getResultList();
    List<JobReportSourceOriginalId> originalIds = em.createQuery(
      "select o from JobReportSourceOriginalId o where o.reportPk in :pks order by o.id", JobReportSourceOriginalId.class)
      .setParameter("pks", pks).getResultList();
    Map<String, String> instanceIds = new HashMap<>();
    reports.forEach(r -> instanceIds.computeIfAbsent(r.getJobId(), this::instanceIdOf));
    return toModels(reports, steps, originalIds, instanceIds);
  }

  private List<Report> toModels(List<JobReport> reports, List<JobReportStep> steps,
    List<JobReportSourceOriginalId> originalIds, Map<String, String> instanceIds) {
    Map<Long, List<JobReportStep>> stepsByReport = new HashMap<>();
    for (JobReportStep step : steps) {
      stepsByReport.computeIfAbsent(step.getReportPk(), k -> new ArrayList<>()).add(step);
    }
    Map<Long, List<String>> originalIdsByReport = new HashMap<>();
    for (JobReportSourceOriginalId originalId : originalIds) {
      originalIdsByReport.computeIfAbsent(originalId.getReportPk(), k -> new ArrayList<>())
        .add(originalId.getOriginalId());
    }
    List<Report> ret = new ArrayList<>(reports.size());
    for (JobReport report : reports) {
      ret.add(JobEntityMapper.toModel(report, originalIdsByReport.getOrDefault(report.getPk(), List.of()),
        stepsByReport.getOrDefault(report.getPk(), List.of()), instanceIds.get(report.getJobId()), this::plugin));
    }
    return ret;
  }

  /**
   * Result of {@link #saveReport(Report, String)}.
   *
   * @param stored
   *          whether the report was saved (its job is a running job in the
   *          database); if not, nothing was written
   * @param previousReplaced
   *          whether a report stored under the previous id was found (and now
   *          holds the report under its new id)
   */
  public record ReportSave(boolean stored, boolean previousReplaced) {
    static final ReportSave NOT_STORED = new ReportSave(false, false);
  }

  /**
   * Creates or updates a report, its original source ids and its steps, if its
   * job is a running job in the database.
   *
   * @param previousReportId
   *          the id the report had before its outcome (and therefore its id)
   *          changed, or {@code null}
   */
  @Transactional
  public ReportSave saveReport(Report model, String previousReportId) {
    String jobId = model.getJobId();
    if (!isStorableJobId(jobId) || !(jobInstanceIds.containsKey(jobId) || jobRepository.existsById(jobId))) {
      return ReportSave.NOT_STORED;
    }

    JobReport report = null;
    boolean previousFound = false;
    if (previousReportId != null) {
      report = findReportEntity(previousReportId);
      previousFound = report != null;
    }
    if (report == null) {
      report = findReportEntity(model.getId());
    } else {
      // the id changed: normally nothing is stored under the new id yet
      JobReport current = findReportEntity(model.getId());
      if (current != null && !current.getPk().equals(report.getPk())) {
        // both ids exist: keep the row under the new id, drop the old one
        deleteReport(report);
        report = current;
      }
    }

    boolean isNew = report == null;
    if (isNew) {
      report = new JobReport();
    }
    JobEntityMapper.toEntity(model, report,
      pluginId(model.getPlugin(), model.getPluginVersion(), model.getPluginName()));
    if (isNew) {
      em.persist(report);
    }
    Long pk = report.getPk();
    String reportId = model.getId();
    afterCommit(() -> {
      ReportCache cache = reportCache(jobId);
      if (previousReportId != null) {
        cache.pks.remove(previousReportId);
      }
      cache.putPk(reportId, pk);
    });

    syncOriginalIds(jobId, pk, model.getSourceObjectOriginalIds(), isNew);
    syncSteps(pk, model.getReports(), isNew);
    return new ReportSave(true, previousFound);
  }

  private void syncOriginalIds(String jobId, Long reportPk, List<String> originalIds, boolean isNew) {
    List<String> wanted = originalIds == null ? List.of()
      : originalIds.stream().filter(Objects::nonNull).distinct().toList();
    ReportCache cache = reportCaches.get(jobId);
    if (!isNew && cache != null && wanted.equals(cache.originalIds.get(reportPk))) {
      return;
    }
    List<JobReportSourceOriginalId> stored = isNew ? List.of()
      : jobReportSourceOriginalIdRepository.findByReportPkInOrderById(List.of(reportPk));
    afterCommit(() -> reportCache(jobId).putOriginalIds(reportPk, wanted));
    if (stored.stream().map(JobReportSourceOriginalId::getOriginalId).toList().equals(wanted)) {
      return;
    }
    stored.forEach(em::remove);
    em.flush();
    for (String originalId : wanted) {
      JobReportSourceOriginalId row = new JobReportSourceOriginalId();
      row.setReportPk(reportPk);
      row.setOriginalId(originalId);
      em.persist(row);
    }
  }

  /**
   * Keeps the stored steps the report still starts with and replaces the rest:
   * appending a step inserts one row, replacing the last step deletes one row
   * and inserts one, and the report row itself is never rewritten.
   *
   * The stored steps are compared through a narrow projection (their details as
   * an MD5 digest, not the text), and the replaced ones are deleted with one
   * statement on the primary key {@code (report_pk, seq)}.
   */
  private void syncSteps(Long reportPk, List<Report> steps, boolean isNew) {
    List<JobReportStep> incoming = new ArrayList<>();
    if (steps != null) {
      for (Report step : steps) {
        incoming.add(JobEntityMapper.toStepEntity(step, reportPk,
          pluginId(step.getPlugin(), step.getPluginVersion(), step.getPluginName())));
      }
    }
    List<Object[]> stored = isNew ? List.of()
      : em.createQuery("select s.seq, s.pluginId, s.pluginState, s.pluginIsMandatory, s.htmlPluginDetails, "
        + "s.outcomeObjectState, s.dateCreated, s.dateUpdated, function('md5', coalesce(s.pluginDetails, '')) "
        + "from JobReportStep s where s.reportPk = :pk order by s.seq", Object[].class)
        .setParameter("pk", reportPk).getResultList();

    int common = 0;
    while (common < stored.size() && common < incoming.size()
      && JobEntityMapper.sameStep(stored.get(common), incoming.get(common))) {
      common++;
    }
    if (common < stored.size()) {
      em.createNativeQuery("DELETE FROM job_report_steps WHERE report_pk = :pk AND seq >= :seq")
        .setParameter("pk", reportPk).setParameter("seq", stored.get(common)[0]).executeUpdate();
    }
    for (JobReportStep step : incoming.subList(common, incoming.size())) {
      em.persist(step);
    }
  }

  private void deleteReport(JobReport report) {
    em.createNativeQuery("DELETE FROM job_report_steps WHERE report_pk = :pk").setParameter("pk", report.getPk())
      .executeUpdate();
    em.createNativeQuery("DELETE FROM job_report_source_original_ids WHERE report_pk = :pk")
      .setParameter("pk", report.getPk()).executeUpdate();
    em.remove(report);
    em.flush();
    Long pk = report.getPk();
    String reportId = report.getId();
    String jobId = report.getJobId();
    afterCommit(() -> {
      ReportCache cache = reportCaches.get(jobId);
      if (cache != null) {
        cache.originalIds.remove(pk);
        if (reportId != null) {
          cache.pks.remove(reportId, pk);
        }
      }
    });
  }

  /**
   * @return the stored report with this business id, found by primary key when
   *         its pk is known, or {@code null}
   */
  private JobReport findReportEntity(String reportId) {
    String jobId = jobIdOfReport(reportId);
    ReportCache cache = jobId == null ? null : reportCaches.get(jobId);
    Long pk = cache == null ? null : cache.pks.get(reportId);
    if (pk != null) {
      JobReport report = em.find(JobReport.class, pk);
      if (report != null && reportId.equals(report.getId())) {
        return report;
      }
      cache.pks.remove(reportId);
    }
    JobReport report = jobReportRepository.findByReportId(reportId).orElse(null);
    if (report != null) {
      reportCache(report.getJobId()).putPk(reportId, report.getPk());
    }
    return report;
  }

  private List<String> originalIds(JobReport report) {
    ReportCache cache = reportCache(report.getJobId());
    List<String> ids = cache.originalIds.get(report.getPk());
    if (ids == null) {
      ids = jobReportSourceOriginalIdRepository.findByReportPkInOrderById(List.of(report.getPk())).stream()
        .map(JobReportSourceOriginalId::getOriginalId).toList();
      cache.putOriginalIds(report.getPk(), ids);
    }
    return ids;
  }

  // ============================================================= plugins

  /**
   * @return the id of the plugin with this class and version, registering it
   *         (in its own transaction) the first time it is seen; {@code null}
   *         when there is no plugin class
   */
  private Integer pluginId(String className, String version, String name) {
    if (className == null || className.isEmpty()) {
      return null;
    }
    String normalizedVersion = version == null ? "" : version;
    String key = className + '\u0000' + normalizedVersion;
    Integer id = pluginIds.get(key);
    if (id == null) {
      PluginDescriptor plugin = newTransaction.execute(status -> {
        em.createNativeQuery("INSERT INTO plugins (class_name, version, name) VALUES (:className, :version, :name) "
          + "ON CONFLICT (class_name, version) DO NOTHING").setParameter("className", className)
          .setParameter("version", normalizedVersion)
          .setParameter("name", name == null || name.isEmpty() ? className : name).executeUpdate();
        return pluginDescriptorRepository.findByClassNameAndVersion(className, normalizedVersion).orElseThrow();
      });
      id = plugin.getId();
      pluginIds.put(key, id);
      pluginsById.put(id, plugin);
    }
    return id;
  }

  private PluginDescriptor plugin(int id) {
    return pluginsById.computeIfAbsent(id, k -> pluginDescriptorRepository.findById(k).orElseThrow());
  }

  // ============================================================= helpers

  private String instanceIdOf(String jobId) {
    String instanceId = jobInstanceIds.get(jobId);
    if (instanceId == null) {
      Optional<Job> job = jobRepository.findById(jobId);
      if (job.isEmpty()) {
        return null;
      }
      instanceId = job.get().getInstanceId() == null ? "" : job.get().getInstanceId();
      jobInstanceIds.put(jobId, instanceId);
    }
    return instanceId.isEmpty() ? null : instanceId;
  }

  private static String userKey(String username, String role) {
    return username + '\u0000' + (role == null ? "" : role);
  }

  private static <T> List<List<T>> chunks(List<T> list, int size) {
    List<List<T>> ret = new ArrayList<>();
    for (int i = 0; i < list.size(); i += size) {
      ret.add(list.subList(i, Math.min(list.size(), i + size)));
    }
    return ret;
  }

  private static void afterCommit(Runnable action) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          action.run();
        }
      });
    } else {
      action.run();
    }
  }
}
