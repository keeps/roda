package pt.gov.dgarq.roda.services.client;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.stubs.Plugins;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class PluginsTest {
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
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length >= 6) {

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

			Plugins pluginsService = rodaClient.getPluginsService();

			System.out.println("\n********************************");
			System.out.println("Get plugins info");
			System.out.println("********************************");

			PluginInfo[] pluginsInfo = pluginsService.getPluginsInfo();
			if (pluginsInfo != null) {

				if (pluginsInfo != null) {

					System.out.println(pluginsInfo.length
							+ " plugins installed");

					for (PluginInfo pluginInfo : pluginsInfo) {
						System.out.println(pluginInfo);
						System.out.println("ID:\t" + pluginInfo.getId());
						System.out.println("Version:\t"
								+ pluginInfo.getVersion());
						System.out.println("Name:\t" + pluginInfo.getName());
						System.out.println("Description:\t"
								+ pluginInfo.getDescription());
						System.out.println("Parameters:\n");

						for (PluginParameter pluginParameter : pluginInfo
								.getParameters()) {
							System.out.println("\tname:\t"
									+ pluginParameter.getName());
							System.out.println("\tdescription:\t"
									+ pluginParameter.getDescription());
							System.out.println("\ttype:\t"
									+ pluginParameter.getType());
							System.out.println("\tvalue:\t"
									+ pluginParameter.getValue());
							System.out.println("\tpossible values:\t"
									+ Arrays.toString(pluginParameter
											.getPossibleValues()));
							System.out
									.println("--------------------------------------");
						}
					}

				} else {
					System.out.println("No plugins installed");
				}

			} else {
				System.out.println("No Plugins installed");
			}

		} catch (Exception e) {

			if (e instanceof InvocationTargetException) {
				((InvocationTargetException) e).getCause().printStackTrace();
			}

			e.printStackTrace();
		}

	}
}
