package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Date;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;

/**
 * @author Rui Castro
 * 
 */
public class UserBrowserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// http://localhost:8180/
		String hostUrl = args[0];
		String username = args[1];
		String password = args[2];

		try {

			RODAClient rodaClient = new RODAClient(new URL(hostUrl), username,
					password);

			UserBrowser userBrowser = rodaClient.getUserBrowserService();

			/*
			 * Count users
			 */
			System.out.println("\n***********************************");
			System.out.println("Count users ==> ");
			System.out.println("***********************************");

			int userCount = userBrowser.getUserCount(null);

			System.out.println(userCount + " users.");

			System.out.println("\n***********************************");
			System.out.println("Count users ==> ");

			Filter userCountfilter = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("active", "true"),
					new OneOfManyFilterParameter("fullName", new String[] {
							"Rui Castro", "Tobias", }) });

			System.out.println("Using " + userCountfilter);
			System.out.println("***********************************");

			userCount = userBrowser.getUserCount(userCountfilter);

			System.out.println(userCount + " users in repository.");

			/*
			 * Get users
			 */
			System.out.println("\n***********************************");
			System.out.println("Get all users ==> ");
			System.out.println("***********************************");

			Date start = new Date();
			User[] users = userBrowser.getUsers(null);
			Date stop = new Date();
			double duration = ((double) (stop.getTime() - start.getTime())) / 1000;

			System.out.println(users.length + " users");
			System.out.println("duration: " + duration + " sec");

			System.out.println("\n***********************************");
			System.out.println("Get all usernames ==> ");
			System.out.println("***********************************");

			start = new Date();
			String[] usernames = userBrowser.getUserNames(null);
			stop = new Date();
			duration = ((double) (stop.getTime() - start.getTime())) / 1000;

			System.out.println(usernames.length + " user names");
			System.out.println("duration: " + duration + " sec");

			System.out.println("\n***********************************");
			System.out.println("Get first 3 active users ==> ");

			Filter usersFilter = new Filter(
					new FilterParameter[] { new SimpleFilterParameter("active",
							"true") });
			ContentAdapter usersContentAdapter = new ContentAdapter(
					usersFilter, null, new Sublist(0, 3));

			System.out.println("Using " + usersContentAdapter);
			System.out.println("***********************************");

			users = userBrowser.getUsers(usersContentAdapter);
			for (int i = 0; users != null && i < users.length; i++) {
				System.out.println(users[i]);
			}

			System.out.println("\n***********************************");
			System.out.println("Count users with name starting with 'a' ==> ");

			usersFilter = new Filter(
					new FilterParameter[] { new RegexFilterParameter("name",
							"^a.*") });

			System.out.println("Using " + usersFilter);
			System.out.println("***********************************");

			userCount = userBrowser.getUserCount(usersFilter);
			System.out.println(userCount + " users.");

			System.out.println("\n***********************************");
			System.out.println("Get users with name starting with 'a' ==> ");

			usersContentAdapter = new ContentAdapter(usersFilter, null, null);

			System.out.println("Using " + usersContentAdapter);
			System.out.println("***********************************");

			users = userBrowser.getUsers(usersContentAdapter);
			for (int i = 0; users != null && i < users.length; i++) {
				System.out.println(users[i]);
			}

			System.out.println("\n***********************************");
			System.out.println("Get first 3 users ==> ");

			usersContentAdapter = new ContentAdapter(null, null, new Sublist(0,
					3));

			System.out.println("Using " + usersContentAdapter);
			System.out.println("***********************************");

			users = userBrowser.getUsers(usersContentAdapter);
			for (int i = 0; users != null && i < users.length; i++) {
				System.out.println(users[i]);
			}

			System.out.println("\n***********************************");
			System.out
					.println("Get all users ordered by businessCategory DESC, fullName ASC ==> ");

			Sorter sorter = new Sorter(new SortParameter[] {
					new SortParameter("businessCategory", true),
					new SortParameter("fullName", false) });

			usersContentAdapter = new ContentAdapter(null, sorter, null);

			System.out.println("Using " + usersContentAdapter);
			System.out.println("***********************************");

			users = userBrowser.getUsers(usersContentAdapter);
			for (int i = 0; users != null && i < users.length; i++) {
				// System.out.println(users[i]);
			}

			System.out.println("\n***********************************");
			System.out.println("Get all users ==> ");
			System.out.println("***********************************");

			User[] allUsers = userBrowser.getUsers(null);
			for (int i = 0; allUsers != null && i < allUsers.length; i++) {
				// System.out.println(allUsers[i]);
			}

			System.out.println("\n***********************************");
			System.out.println("Get users in group administrators ==> ");
			System.out.println("***********************************");

			User[] usersInGroupAdmin = userBrowser
					.getUsersInGroup("administrators");
			for (int i = 0; usersInGroupAdmin != null
					&& i < usersInGroupAdmin.length; i++) {
				System.out.println(usersInGroupAdmin[i]);
			}

			/*
			 * Count groups
			 */
			System.out
					.println("\n********************************************");
			System.out.println("Count groups (administrators, guests)");

			Filter groupCountfilter = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("active", "true"),
					new OneOfManyFilterParameter("name", new String[] {
							"administrators", "guests", }) });

			System.out.println("Using " + groupCountfilter);
			System.out.println("********************************************");

			int groupCount = userBrowser.getGroupCount(groupCountfilter);

			System.out.println(groupCount + " groups.");

			/*
			 * Get groups
			 */
			System.out.println("\n***********************************");
			System.out.println("Get groups (administrators, guests) ");

			Filter groupfilter = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("active", "true"),
					new OneOfManyFilterParameter("name", new String[] {
							"administrators", "guests", }) });

			ContentAdapter groupsContentAdapter = new ContentAdapter(
					groupfilter, null, null);

			System.out.println("Using " + groupsContentAdapter);
			System.out.println("***********************************");

			Group[] groups = userBrowser.getGroups(groupsContentAdapter);
			for (int i = 0; groups != null && i < groups.length; i++) {
				System.out.println(groups[i]);
			}

			/*
			 * get all roles
			 */
			System.out.println("\n***********************************");
			System.out.println("Get all roles ==> ");
			System.out.println("***********************************");

			String[] roles = userBrowser.getRoles();
			for (int i = 0; i < roles.length; i++) {
				System.out.println(roles[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
