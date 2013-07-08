package pt.gov.dgarq.roda.sipcreator.installer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.launch4j.ant.Launch4jTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;

import pt.gov.dgarq.roda.core.ClassificationPlanHelper;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.servlet.ExtendedUserPrincipal;
import pt.gov.dgarq.roda.servlet.LdapAuthenticationFilter;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;
import pt.gov.dgarq.roda.util.TempDir;

import com.izforge.izpack.compiler.CompilerConfig;

/**
 * 
 * @author Luis Faria
 * 
 */
public class GetRodaIn extends HttpServlet implements Servlet {

	private static final long serialVersionUID = -2969960103558601287L;

	private static Logger logger = Logger.getLogger(GetRodaIn.class);

	private static final String RODA_IN_INSTALLER_FILE = "roda-in-installer.jar";
	private static final String RODA_IN_WIN_INSTALLER_FILE = "roda-in-installer.exe";

	private static final String OPERATIVE_SYSTEM_PARAMETER = "os";

	private final File tmpDir;
	private final Properties properties;
	private Properties version = null;

	/**
	 * Create a GetRodaIn servlet
	 */
	public GetRodaIn() {
		tmpDir = new File(System.getProperty("java.io.tmpdir"));
		properties = new Properties();
		try {
			properties
					.load(getConfigurationFile("roda-in-installer.properties"));
		} catch (IOException e) {
			logger.fatal("Could not load config", e);
		}
	}

	/**
	 * Get configuration file
	 * 
	 * @param relativePath
	 *            the file path relative to the config folder (e.g.
	 *            "roda-wui.properties)"
	 * @return properties file input stream
	 */
	public static InputStream getConfigurationFile(String relativePath) {
		InputStream ret;
		String roda_home = getRodaHome();

		File staticConfig = new File(roda_home, "config" + File.separator
				+ relativePath);

		if (staticConfig.exists()) {
			try {
				ret = new FileInputStream(staticConfig);
			} catch (FileNotFoundException e) {
				ret = GetRodaIn.class.getResourceAsStream("/config/"
						+ relativePath);
			}
		} else {
			ret = GetRodaIn.class
					.getResourceAsStream("/config/" + relativePath);
		}
		return ret;
	}

	private static String getRodaHome() {
		String roda_home;
		if (System.getProperty("roda.home") != null) {
			roda_home = System.getProperty("roda.home");
		} else if (System.getenv("RODA_HOME") != null) {
			roda_home = System.getenv("RODA_HOME");
		} else {
			roda_home = null;
		}
		return roda_home;
	}

	protected Properties getVersion() {
		if (version == null) {
			version = new Properties();
			try {
				version.load(new FileInputStream(new File(getServletContext()
						.getRealPath("roda-in-version.properties"))));
				logger.debug("Got properties with version "
						+ version.getProperty("roda.in.version"));

			} catch (IOException e) {
				logger.error("Could not load RODA-in version properties file",
						e);
			}
		}
		return version;
	}

	protected URL getRodaServicesUrl() {
		URL ret = null;
		try {
			ret = new URL(properties.getProperty("roda.services.url"));
		} catch (MalformedURLException e) {
			logger.error("Error parsing RODA Core Services URL", e);
		}
		return ret;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Target operative system
	 */
	public enum OperativeSystem {
		/**
		 * Windows operative system
		 */
		WINDOWS,
		/**
		 * Mac OS operative system
		 */
		MACOS,
		/**
		 * Linux based operative system, like Ubuntu
		 */
		LINUX

	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		OperativeSystem os = parseOperativeSystem(request
				.getParameter(OPERATIVE_SYSTEM_PARAMETER));
		String pathInfo = request.getPathInfo();
		String user = null;
		if (pathInfo != null && pathInfo.length() > 1) {
			user = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
		}

		if (user != null) {
			try {
				sendInstaller(os, user, request, response);
			} catch (Exception e) {
				logger.error("Error sending installer", e);
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						e.getMessage());
			}
		} else {
			sendInstaller(os, request, response);
		}

	}

	private void sendInstaller(OperativeSystem os, String user,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (os == OperativeSystem.WINDOWS) {
			File installer = File.createTempFile("installer_", ".exe", tmpDir);
			createWindowsInstaller(user, installer, request);
			response.setContentType("application/x-msdownload");
			response.setHeader("Content-Disposition", "filename="
					+ RODA_IN_WIN_INSTALLER_FILE);
			try {
				FileHelper.copy(new FileInputStream(installer),
						response.getOutputStream());
			} catch (IOException e) {
				logger.error("Error sending installer", e);
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						e.getMessage());
			}
			installer.delete();

		} else {
			File installer = File.createTempFile("installer_", ".jar", tmpDir);
			logger.debug("Creating generic installer into "
					+ installer.getAbsolutePath());
			createGenericInstaller(user, installer, request);
			logger.debug("Sending generic installer");
			response.setContentType("application/java-archive");
			response.setHeader("Content-Disposition", "filename="
					+ RODA_IN_INSTALLER_FILE);
			try {
				FileHelper.copy(new FileInputStream(installer),
						response.getOutputStream());
			} catch (IOException e) {
				logger.error("Error sending installer", e);
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						e.getMessage());
			}
			installer.delete();
		}
	}

	/**
	 * Gets the {@link User} that requested this service.
	 * 
	 * @return the {@link User} that requested this service or <code>null</code>
	 *         if it doesn't exist.
	 */
	private User getClientUser(HttpServletRequest request) {

		User user = null;

		if (request instanceof RodaServletRequestWrapper) {
			RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) request;
			user = rodaRequestWrapper.getLdapUserPrincipal();
		} else {
			user = new User(request.getUserPrincipal().getName());
		}

		return user;
	}

	/**
	 * Gets the password used to authenticate the current request.
	 * 
	 * This method
	 * 
	 * @return the password use to authenticate this current request or
	 *         <code>null</code> if it doesn't exist.
	 */
	protected String getClientUserPassword(HttpServletRequest request) {

		String password = null;

		if (request instanceof RodaServletRequestWrapper) {

			RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) request;
			ExtendedUserPrincipal userPrincipal = (ExtendedUserPrincipal) rodaRequestWrapper
					.getLdapUserPrincipal();
			password = userPrincipal.getPassword();

		} else {

			String[] usernamePassword = LdapAuthenticationFilter
					.parseUsernamePassword(request);
			password = usernamePassword[1];

		}

		return password;
	}

	private void createClassificationPlan(String username, File workDir,
			HttpServletRequest request) throws BrowserException,
			LoginException, RODAClientException, NoSuchRODAObjectException,
			InvalidDescriptionObjectException, MetadataException,
			EadCMetadataException, IOException {
		ClassificationPlanHelper helper = new ClassificationPlanHelper(workDir,
				tmpDir);

		User user = getClientUser(request);
		String password = getClientUserPassword(request);
		helper.update(new RODAClient(getRodaServicesUrl(), user.getName(),
				password));
	}

	private void createGenericInstaller(File output, HttpServletRequest request)
			throws Exception {
		String baseDir = getServletContext().getRealPath("WEB-INF/resources");
		System.setProperty("RODA_IN_HOME", baseDir);
		File installTemplateFile = new File(baseDir, "generic-install.xml");
		File installXML = File.createTempFile("roda_install", ".xml", tmpDir);
		BufferedWriter installWriter = new BufferedWriter(new FileWriter(
				installXML));

		String installTemplate = readFileAsString(installTemplateFile);
		installWriter.write(String.format(installTemplate, getVersion()
				.getProperty("roda.in.version"),
				getVersion().getProperty("roda.in.build.code"), getVersion()
						.getProperty("roda.in.build.date"), getRodaHome()));
		installWriter.close();

		logger.debug("Creating installer compiler");
		CompilerConfig izPack = new CompilerConfig(
				installXML.getAbsolutePath(), baseDir, "standard",
				output.getAbsolutePath());

		logger.debug("Executing installer compiler");
		izPack.executeCompiler();
		logger.debug("Cleaning installer resources");
	}

	private void createWindowsInstaller(File output, HttpServletRequest request)
			throws Exception {
		File genericInstaller = File.createTempFile("installer_", ".jar",
				tmpDir);
		logger.debug("Creating generic installer");
		createGenericInstaller(genericInstaller, request);

		logger.debug("Wrapping generic installer in a windows executable");
		Launch4jTask launch4jTask = new Launch4jTask();
		launch4jTask.setConfigFile(new File(getServletContext().getRealPath(
				"WEB-INF/launch4j.xml")));
		launch4jTask.setOutfile(output);
		launch4jTask.setJar(genericInstaller);
		launch4jTask.setBindir(new File(getRodaHome(), "in/launch4j"));
		Project project = new Project();
		project.setBasedir(getServletContext().getRealPath("WEB-INF/"));
		launch4jTask.setProject(project);
		launch4jTask.execute();

	}

	private void createGenericInstaller(String user, File output,
			HttpServletRequest request) throws Exception {
		String baseDir = getServletContext().getRealPath("WEB-INF/resources");
		System.setProperty("RODA_IN_HOME", baseDir);
		File installTemplateFile = new File(baseDir, "user-install.xml");
		File installXML = File.createTempFile("roda_install", ".xml", tmpDir);
		BufferedWriter installWriter = new BufferedWriter(new FileWriter(
				installXML));

		logger.debug("Fetching classification plan");
		File classificationPlan = TempDir.createUniqueTemporaryDirectory(
				"roda_cp_", tmpDir);
		logger.debug("Getting user " + user + " classification plan");
		createClassificationPlan(user, classificationPlan, request);

		String installTemplate = readFileAsString(installTemplateFile);
		installWriter.write(String.format(installTemplate, classificationPlan
				.getAbsolutePath(),
				getVersion().getProperty("roda.in.version"), getVersion()
						.getProperty("roda.in.build.code"), getVersion()
						.getProperty("roda.in.build.date"), getRodaHome()));
		installWriter.close();

		logger.debug("Creating installer compiler");
		CompilerConfig izPack = new CompilerConfig(
				installXML.getAbsolutePath(), baseDir, "standard",
				output.getAbsolutePath());

		logger.debug("Executing installer compiler");
		izPack.executeCompiler();
		logger.debug("Cleaning installer resources");
		FileUtils.deleteDirectory(classificationPlan);
	}

	private void createWindowsInstaller(String user, File output,
			HttpServletRequest request) throws Exception {
		File genericInstaller = File.createTempFile("installer_", ".jar",
				tmpDir);
		logger.debug("Creating generic installer");
		createGenericInstaller(user, genericInstaller, request);

		logger.debug("Wrapping generic installer in a windows executable");
		Launch4jTask launch4jTask = new Launch4jTask();
		launch4jTask.setConfigFile(new File(getServletContext().getRealPath(
				"WEB-INF/launch4j.xml")));
		launch4jTask.setOutfile(output);
		launch4jTask.setJar(genericInstaller);
		launch4jTask.setBindir(new File(System.getenv("RODA_HOME"),
				"in/launch4j"));
		Project project = new Project();
		project.setBasedir(getServletContext().getRealPath("WEB-INF/"));
		launch4jTask.setProject(project);
		launch4jTask.execute();

	}

	/**
	 * @param filePath
	 *            the name of the file to open. Not sure if it can accept URLs
	 *            or just filenames. Path handling could be better, and buffer
	 *            sizes are hardcoded
	 */
	private static String readFileAsString(File file)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

	private void sendInstaller(OperativeSystem os, HttpServletRequest request,
			HttpServletResponse response) {

		if (os == OperativeSystem.WINDOWS) {
			File installer = new File(getServletContext().getRealPath(
					RODA_IN_WIN_INSTALLER_FILE));
			try {
				if (!installer.exists()) {
					createWindowsInstaller(installer, request);
				}

				response.setContentType("application/dos-exe");
				response.setHeader("Content-Disposition", "filename="
						+ RODA_IN_WIN_INSTALLER_FILE);

				FileHelper.copy(new FileInputStream(installer),
						response.getOutputStream());
			} catch (IOException e) {
				logger.info("Error sending installer", e);
			} catch (Exception e) {
				logger.error("Error creating installer", e);
			}
		} else {
			File installer = new File(getServletContext().getRealPath(
					RODA_IN_INSTALLER_FILE));
			try {
				if (!installer.exists()) {
					createGenericInstaller(installer, request);
				}

				response.setContentType("application/java-archive");
				response.setHeader("Content-Disposition", "filename="
						+ RODA_IN_INSTALLER_FILE);

				FileHelper.copy(new FileInputStream(installer),
						response.getOutputStream());
			} catch (IOException e) {
				logger.info("Error sending installer", e);
			} catch (Exception e) {
				logger.error("Error creating installer", e);
			}

		}
	}

	/**
	 * 
	 * @param os
	 * @return the operative system, or null if none specified
	 */
	private OperativeSystem parseOperativeSystem(String os) {
		OperativeSystem ret = null;
		if (os == null) {
			ret = null;
		} else if (os.equals("windows")) {
			ret = OperativeSystem.WINDOWS;
		} else if (os.equals("macos")) {
			ret = OperativeSystem.MACOS;
		} else if (os.equals("linux")) {
			ret = OperativeSystem.LINUX;
		}
		return ret;
	}

}