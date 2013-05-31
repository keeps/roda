package pt.gov.dgarq.roda.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 */
public class ClassLoaderUtility {

	// Log object
	private static Logger logger = Logger.getLogger(ClassLoaderUtility.class);

	// Parameters
	private static final Class[] parameters = new Class[] { URL.class };

	public static ClassLoader classLoader = ClassLoaderUtility.class
			.getClassLoader();

	/**
	 * Add file to CLASSPATH
	 * 
	 * @param s
	 *            File name
	 * @throws IOException
	 *             IOException
	 */
	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}

	/**
	 * Add file to CLASSPATH
	 * 
	 * @param f
	 *            File object
	 * @throws IOException
	 *             IOException
	 */
	public static void addFile(File f) throws IOException {
		addURL(f.toURL());
	}

	/**
	 * Add URL to CLASSPATH
	 * 
	 * @param url
	 *            {@link URL}
	 * @throws IOException
	 *             IOException
	 */
	public static void addURL(URL url) throws IOException {

		URLClassLoader sysLoader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		URL urls[] = sysLoader.getURLs();
		for (int i = 0; i < urls.length; i++) {

			if (StringUtils
					.equalsIgnoreCase(urls[i].toString(), url.toString())) {
				logger.debug("URL " + url + " is already in the CLASSPATH");
				return;
			}
		}

		Class sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysLoader, new Object[] { url });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException(
					"Error, could not add URL to system classloader");
		}
	}

	/**
	 * Creates an instance of an object of the given class name.
	 * 
	 * @param className
	 *            the name of the class to create an instance.
	 * @return the instance created.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object createObject(String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		if (StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("className cannot be null");
		}

		logger.trace("Loading class " + className + " with ClassLoader "
				+ classLoader.getClass().getSimpleName());

		Class clazz = classLoader.loadClass(className);

		return clazz.newInstance();
	}

	/**
	 * Adds the given {@link URL}s to the classpath and creates an instance of
	 * the given class.
	 * 
	 * @param urls
	 *            a list of {@link URL}s to be added to the classpath.
	 * @param className
	 *            the name of the class for which to create an instance.
	 * @return the instance created.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object createObject(URL[] urls, String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		if (StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("className cannot be null");
		}

		URLClassLoader clazzLoader = new URLClassLoader(urls, classLoader);

		logger.trace("Loading class " + className + " with ClassLoader "
				+ classLoader.getClass().getSimpleName());

		Class clazz = clazzLoader.loadClass(className);
		return clazz.newInstance();
	}

	/**
	 * Adds the given file path to the classpath and creates an instance of the
	 * given class name.
	 * 
	 * @param filePath
	 *            the path of the file to add to the classpath.
	 * @param className
	 *            the name of the class for which to create an instance.
	 * @return the instance created.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object createObject(String filePath, String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {

		if (StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("className cannot be null");
		}

		ClassLoaderUtility.addFile(filePath);
		filePath = "jar:file://" + filePath + "!/";
		URL url = new File(filePath).toURL();

		URLClassLoader clazzLoader = new URLClassLoader(new URL[] { url },
				classLoader);

		logger.trace("Loading class " + className + " with ClassLoader "
				+ classLoader.getClass().getSimpleName());

		Class clazz = clazzLoader.loadClass(className);

		return clazz.newInstance();
	}

}
