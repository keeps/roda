package pt.gov.dgarq.roda.servlet;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.User;

/**
 * The class managing the user's authentication state. For better performance,
 * the authentication state has been cached. It is the proxy to add and get the
 * user's attributes model into the store. It checks the authentication stage
 * from reading the attributes store. It also has a timer task to release the
 * memory spaces which were allocated to expired authentication state objects.
 * 
 * @author Rui Castro
 */
public class AuthenticationCache {

	static final private Logger logger = Logger
			.getLogger(AuthenticationCache.class);

	private static long TTL = 60000;// 1 min = 60*1000

	private static long DELAY = 43200000;// 12 hour = 12*60*60*1000

	private static long PERIOD = 86200000;// 24 hour = 24*60*60*1000

	private static Map<String, AuthenticationState> stateCacheMap = new HashMap<String, AuthenticationState>();

	protected Timer timer;

	/**
	 * Constructs a new {@link AuthenticationCache}.
	 */
	public AuthenticationCache() {
		// garbage collection: remove expired user
		timer = new Timer();
		timer.schedule(new ExpiredUsersCleaner(), DELAY, PERIOD);
	}

	/**
	 * Verifies if the {@link User} with the specified username and password is
	 * authenticated in the cache.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * 
	 * @return <code>true</code> if the {@link User} is authenticated,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAuthenticated(String username, String password) {

		AuthenticationState state = stateCacheMap.get(username);

		if (state == null)
			return false;// hasn't been authenticated
		else if (Calendar.getInstance().getTimeInMillis() > state.getTtl())
			return false;// expired
		else if (!state.getPassword().equals(password))
			return false;
		else
			return true;
	}

	/**
	 * Get a {@link User} from the store.
	 * 
	 * @param username
	 * @param password
	 * 
	 * @return the user
	 */
	public User getUser(String username, String password) {

		AuthenticationState state = stateCacheMap.get(username);

		if (state != null && state.getPassword().equals(password)) {
			return state.getUser();
		}
		return null;
	}

	/**
	 * Add a {@link User} into the authentication cache.
	 * 
	 * @param username
	 * @param password
	 * @param user
	 * 
	 */
	public void addUser(String username, String password, User user) {

		AuthenticationState model = new AuthenticationState(username, password,
				user, generateTTL());

		stateCacheMap.put(username, model);
	}

	/**
	 * Stops the timer that runs the cache cleaner.
	 */
	public void stopCleanerTimer() {
		this.timer.cancel();
		logger.info("Cleaner timer stopped");
	}

	/**
	 * Generate a TTL. The {@link User} will be valid only within the TTL
	 * period.
	 * 
	 * @return the TTL.
	 */
	private long generateTTL() {

		Calendar calendar = Calendar.getInstance();
		long longExpiration = TTL;
		long TTL = calendar.getTimeInMillis() + longExpiration;

		logger.debug("User will expire on: " + new Date(TTL));

		return TTL;
	}

	/**
	 * Execute a timer task to remove {@link User}s from the cache when they
	 * expire.
	 */
	class ExpiredUsersCleaner extends TimerTask {

		public void run() {

			logger.debug(getClass().getSimpleName() + " is running");

			Set<String> userNameSet = new HashSet<String>(stateCacheMap
					.keySet());

			if (userNameSet != null) {

				for (String username : userNameSet) {

					AuthenticationState model = stateCacheMap.get(username);

					if (Calendar.getInstance().getTimeInMillis() > model
							.getTtl()) {

						stateCacheMap.remove(username);

						logger.debug(username + " removed from cache");
					}
				}

			}
		}
	}

}
