package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.Reports;
import pt.gov.dgarq.roda.core.stubs.Scheduler;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 * 
 */
public class SchedulerListTasksTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RODAClient rodaClient = null;

		try {

			if (args.length == 4) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL),new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length >= 7) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err.println(SchedulerListTasksTest.class.getSimpleName()
						+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			Scheduler schedulerService = rodaClient.getSchedulerService();
			Reports reportsService = rodaClient.getReportsService();

			System.out.println("\n********************************");
			System.out.println("Get number of tasks");
			System.out.println("********************************");

			int taskCount = schedulerService.getTaskCount(null);
			System.out.println("Scheduler has " + taskCount + " tasks");

			System.out.println("\n********************************");
			System.out.println("Get tasks");
			System.out.println("********************************");

			Task[] tasks = schedulerService.getTasks(null);
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get number of task instances");
			System.out.println("********************************");

			int taskInstanceCount = schedulerService.getTaskInstanceCount(null);
			System.out.println("Scheduler has " + taskInstanceCount
					+ " task instances");

			System.out.println("\n********************************");
			System.out.println("Get task instances (maximum of 20 entries)");
			System.out.println("********************************");

			TaskInstance[] taskInstances = schedulerService
					.getTaskInstances(new ContentAdapter(null, null,
							new Sublist(0, 20)));
			if (taskInstances != null) {

				System.out.println("Scheduler task instances:");
				List<TaskInstance> taskInstanceList = Arrays
						.asList(taskInstances);
				for (Iterator<TaskInstance> iterator = taskInstanceList
						.iterator(); iterator.hasNext();) {

					TaskInstance taskInstance = (TaskInstance) iterator.next();

					System.out.println(taskInstance);

					// if (taskInstance.getReportID() != null) {
					// Report report = reportsService.getReport(taskInstance
					// .getReportID());
					// System.out.println("Task report: " + report);
					// }
				}

			} else {
				System.out.println("Scheduler has NO task instances");
			}

			System.out.println("\n********************************");
			System.out
					.println("Get number of task instances with name LIKE '%virus%'");
			System.out.println("********************************");

			Filter ingestLikeFilter = new Filter(new LikeFilterParameter(
					"name", "%virus%"));

			taskInstanceCount = schedulerService
					.getTaskInstanceCount(ingestLikeFilter);
			System.out.println("Scheduler has " + taskInstanceCount
					+ " task instances");

			System.out.println("\n********************************");
			System.out.println("Get task instances with name LIKE '%virus%'");
			System.out.println("********************************");

			taskInstances = schedulerService
					.getTaskInstances(new ContentAdapter(ingestLikeFilter,
							null, new Sublist(0, 20)));

			if (taskInstances != null) {

				System.out.println("Scheduler task instances:");
				List<TaskInstance> taskInstanceList = Arrays
						.asList(taskInstances);
				for (Iterator<TaskInstance> iterator = taskInstanceList
						.iterator(); iterator.hasNext();) {

					TaskInstance taskInstance = (TaskInstance) iterator.next();

					System.out.println(taskInstance);

					// if (taskInstance.getReportID() != null) {
					// Report report = reportsService.getReport(taskInstance
					// .getReportID());
					// System.out.println("Task report: " + report);
					// }
				}

			} else {
				System.out.println("Scheduler has NO task instances");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
