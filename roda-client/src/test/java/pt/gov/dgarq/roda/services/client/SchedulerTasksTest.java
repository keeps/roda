package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.stubs.Scheduler;

/**
 * @author Rui Castro
 * 
 */
public class SchedulerTasksTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RODAClient rodaClient = null;

		try {

			if (args.length == 1) {

				// http://localhost:8180/
				String hostUrl = args[0];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length >= 3) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err.println(SchedulerTasksTest.class.getSimpleName()
						+ " protocol://hostname:port/ [username password]");
				System.exit(1);
			}

			Scheduler schedulerService = rodaClient.getSchedulerService();

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
			System.out.println("Pause all tasks");
			System.out.println("********************************");

			if (tasks != null) {

				List<Task> taskList = Arrays.asList(tasks);

				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();

					task = schedulerService.pauseTask(task.getName());

					System.out.println(task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Resume all tasks");
			System.out.println("********************************");

			if (tasks != null) {

				List<Task> taskList = Arrays.asList(tasks);

				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();

					task = schedulerService.resumeTask(task.getName());

					System.out.println(task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("SchedulerContentAdapterEngine tests");
			System.out.println("********************************");

			Filter filter = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("paused", "true"),
					new SimpleFilterParameter("name", "Statistics") });

			System.out.println("\n********************************");
			System.out.println("Get number of tasks");
			System.out.println("********************************");

			System.out.println("Using " + filter);

			taskCount = schedulerService.getTaskCount(filter);
			System.out.println("Scheduler has " + taskCount + " tasks");

			System.out.println("\n********************************");
			System.out.println("Get tasks");
			System.out.println("********************************");

			System.out.println("Using " + filter);

			tasks = schedulerService.getTasks(null);
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
			System.out.println("Get tasks sorted by name ASCENDING");
			System.out.println("********************************");

			Sorter sorter = new Sorter(new SortParameter[] { new SortParameter(
					"name", false) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getName() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by name DESCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter("name",
					true) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getName() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by description ASCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"description", false) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getDescription() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by description DESCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"description", true) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getDescription() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by startDate ASCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"startDate", false) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getStartDate() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by startDate DESCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"startDate", true) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getStartDate() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by repeatCount ASCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"repeatCount", false) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getRepeatCount() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by repeatCount DESCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"repeatCount", true) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getRepeatCount() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by repeatInterval ASCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"repeatInterval", false) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getRepeatInterval() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out.println("Get tasks sorted by repeatInterval DESCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] { new SortParameter(
					"repeatInterval", true) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getRepeatInterval() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

			System.out.println("\n********************************");
			System.out
					.println("Get tasks sorted by repeatInterval DESCENDING name ASCENDING");
			System.out.println("********************************");

			sorter = new Sorter(new SortParameter[] {
					new SortParameter("repeatInterval", true),
					new SortParameter("name", false) });

			tasks = schedulerService.getTasks(new ContentAdapter(null, sorter,
					null));
			if (tasks != null) {
				System.out.println("Scheduler tasks:");
				List<Task> taskList = Arrays.asList(tasks);
				for (Iterator<Task> iterator = taskList.iterator(); iterator
						.hasNext();) {
					Task task = iterator.next();
					System.out.println(task.getRepeatInterval() + " - " + task);
				}
			} else {
				System.out.println("Scheduler has NO tasks");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
