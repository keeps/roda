package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.Plugins;
import pt.gov.dgarq.roda.core.stubs.Scheduler;

/**
 * @author Rui Castro
 * 
 */
public class SchedulerAddTaskTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RODAClient rodaClient = null;

		try {

			String taskName = null;

			if (args.length == 2) {

				// http://localhost:8180/
				String hostUrl = args[0];
				taskName = args[1];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length >= 4) {

				// http://localhost:8180/ user pass taskName
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];

				taskName = args[3];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err
						.println(SchedulerAddTaskTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] taskName");
				System.exit(1);
			}

			Scheduler schedulerService = rodaClient.getSchedulerService();
			Plugins pluginsService = rodaClient.getPluginsService();

			System.out.println("\n********************************");
			System.out.println("Get plugins");
			System.out.println("********************************");

			PluginInfo[] pluginsInfo = pluginsService.getPluginsInfo();
			if (pluginsInfo == null) {

				System.out.println("NO plugins");

			} else {

				System.out.println("Plugins:");
				for (int i = 0; i < pluginsInfo.length; i++) {
					PluginInfo pluginInfo = pluginsInfo[i];
					System.out.println(pluginInfo);
				}

				PluginInfo pluginInfo = pluginsInfo[0];

				System.out.println("Using plugin " + pluginInfo);

				System.out.println("\n********************************");
				System.out.println("Add a new task");
				System.out.println("********************************");

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.SECOND, 15);

				PluginParameter[] parameters = new PluginParameter[] {
						new PluginParameter("username", "string", "taskuser", true,
								false, null),
						new PluginParameter("password", "password", "xxx", true,
								false, null) };
				pluginInfo.setParameters(parameters);

				Task task = new Task("testTask", "This is a test task",
						rodaClient.getUsername(), calendar.getTime(), 0, 0,
						pluginInfo);

				Task addedTask = schedulerService.addTask(task);

				System.out.println("Task added " + addedTask);

				System.out.println("\n********************************");
				System.out.println("Get number of tasks");
				System.out.println("********************************");

				int taskCount = schedulerService.getTaskCount(null);
				System.out.println("Scheduler has " + taskCount + " tasks");

				System.out.println("\n********************************");
				System.out.println("Get tasks");
				System.out.println("********************************");

				Task[] tasks = schedulerService.getTasks(new ContentAdapter(
						null, null, new Sublist(0, 20)));
				if (tasks != null) {
					System.out.println("Scheduler tasks:");
					List taskList = Arrays.asList(tasks);
					for (Iterator iterator = taskList.iterator(); iterator
							.hasNext();) {
						task = (Task) iterator.next();
						System.out.println(task);
					}
				} else {
					System.out.println("Scheduler has NO tasks");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
