/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tika.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumUtils {
  private static WebDriver driver;
  private static String url;
  private static String driverPath; // "/path/to/chromedriver"
  private static Map<String, String> locations = new HashMap<>();
  private static final String TABLE_CLASS = "cellTableOddRow";

  private SeleniumUtils() {
    // do nothing
  }

  /**
   * 
   * @param args:
   *          the first argument is the RODA base url and the second argument is
   *          the driver path
   * @throws InterruptedException
   * @throws IOException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    if (args.length != 2) {
      System.err.println("Number of arguments not correct since it is only needed two arguments. "
        + "The first argument is the RODA base url and the second argument is the driver path");
      commandHelp();
      System.exit(0);
    }

    url = args[0];
    driverPath = args[1];

    ChromeDriverService service = new ChromeDriverService.Builder().usingDriverExecutable(new File(driverPath))
      .usingAnyFreePort().build();
    service.start();

    driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
    driver.get(url);

    // welcome page
    saveHTML();

    savePublicPages();
    saveLoginPages();
    saveHelpPages();
    savePlanningPages();
    saveAdminPages();
    saveIngestPages();
    saveSearchPages();
    saveBrowsePages();

    driver.quit();
    service.stop();

    for (Entry<String, String> entry : locations.entrySet()) {
      String location = entry.getKey();
      String html = getHTMLSource(location);
      Pattern expression = Pattern.compile("<div id=\"webaxscore\".*?<span>(.*?)</span>");
      Matcher matcher = expression.matcher(html);
      if (matcher.find()) {
        System.out.println(location + " | " + locations.get(location) + " | " + matcher.group(1));
      }
    }
  }

  private static void saveBrowsePages() {
    try {
      // browse page
      WebElement searchButton = driver.findElement(By.className("browse_menu_item"));
      searchButton.click();
      saveHTML();

      // show browse page
      List<WebElement> aipList = driver.findElements(By.className(TABLE_CLASS));
      aipList.get(0).click();
      saveHTML();

      // permissions page
      List<WebElement> permissionPage = driver.findElements(By.className("btn-edit"));
      permissionPage.get(1).click();
      saveHTML();
      goBack();

      // create metadata page
      WebElement createMetadataPage = driver.findElement(By.className("fa-plus-circle"));
      createMetadataPage.click();
      saveHTML();
      goBack();

      // edit metadata page
      WebElement editMetadataPage = driver.findElement(By.className("fa-edit"));
      editMetadataPage.click();
      saveHTML();
      goBack();

      // history metadata page
      WebElement historyMetadataPage = driver.findElement(By.className("fa-history"));
      historyMetadataPage.click();
      saveHTML();
      goBack();

      // show browse representation page
      List<WebElement> representationList = driver.findElements(By.className(TABLE_CLASS));
      representationList.get(0).click();
      saveHTML();

      // show browse file page
      List<WebElement> fileList = driver.findElements(By.className(TABLE_CLASS));
      fileList.get(0).click();
      saveHTML();
      goBack();
      goBack();

      goBack();
    } catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
      System.err.println("Error running Selenium on browse pages");
    }
  }

  private static void saveSearchPages() {
    try {
      // search page
      WebElement searchButton = driver.findElement(By.className("search_menu_item"));
      searchButton.click();
      saveHTML();

      // advanced search page
      WebElement loadButton = driver.findElement(By.className("fa-angle-down"));
      loadButton.click();
      saveHTML();
    } catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
      System.err.println("Error running Selenium on search pages");
    }
  }

  private static void saveIngestPages() {
    try {
      // pre-ingest page
      WebElement ingestButton = driver.findElement(By.className("ingest_menu_item"));
      ingestButton.click();
      Thread.sleep(1000);
      WebElement preIngestPage = driver.findElement(By.className("ingest_pre_item"));
      preIngestPage.click();
      saveHTML();

      // transfer page
      ingestButton = driver.findElement(By.className("ingest_menu_item"));
      ingestButton.click();
      Thread.sleep(1000);
      WebElement transferPage = driver.findElement(By.className("ingest_transfer_item"));
      transferPage.click();
      saveHTML();

      // load page
      WebElement loadButton = driver.findElement(By.className("btn-upload"));
      loadButton.click();
      saveHTML();
      goBack();

      // ingest state page
      ingestButton = driver.findElement(By.className("ingest_menu_item"));
      ingestButton.click();
      Thread.sleep(1000);
      WebElement statePage = driver.findElement(By.className("ingest_list_item"));
      statePage.click();
      saveHTML();

      // apprasal page
      ingestButton = driver.findElement(By.className("ingest_menu_item"));
      ingestButton.click();
      Thread.sleep(1000);
      WebElement apprasalPage = driver.findElement(By.className("ingest_appraisal_item"));
      apprasalPage.click();
      saveHTML();
    } catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
      System.err.println("Error running Selenium on ingest pages");
    }
  }

  private static void saveAdminPages() {
    try {
      // administration process
      WebElement adminButton = driver.findElement(By.className("administration_menu_item"));
      adminButton.click();
      Thread.sleep(1000);
      WebElement processPage = driver.findElement(By.className("administration_actions_item"));
      processPage.click();
      saveHTML();

      // new process page
      WebElement newProcess = driver.findElement(By.className("btn-plus"));
      newProcess.click();
      saveHTML();

      goBack();

      // administration users
      adminButton = driver.findElement(By.className("administration_menu_item"));
      adminButton.click();
      Thread.sleep(1000);
      WebElement usersPage = driver.findElement(By.className("administration_user_item"));
      usersPage.click();
      saveHTML();

      // new user page
      List<WebElement> newUser = driver.findElements(By.className("btn-plus"));
      newUser.get(1).click();
      saveHTML();
      goBack();

      // new group page
      newUser = driver.findElements(By.className("btn-plus"));
      newUser.get(2).click();
      saveHTML();
      goBack();

      // administration activity
      adminButton = driver.findElement(By.className("administration_menu_item"));
      adminButton.click();
      Thread.sleep(1000);
      WebElement activityPage = driver.findElement(By.className("administration_log_item"));
      activityPage.click();
      saveHTML();

      // administration notifications
      adminButton = driver.findElement(By.className("administration_menu_item"));
      adminButton.click();
      Thread.sleep(1000);
      WebElement notificationsPage = driver.findElement(By.className("administration_notifications_item"));
      notificationsPage.click();
      saveHTML();

      // administration statistics
      adminButton = driver.findElement(By.className("administration_menu_item"));
      adminButton.click();
      Thread.sleep(1000);
      WebElement statisticsPage = driver.findElement(By.className("administration_statistics_item"));
      statisticsPage.click();
      saveHTML();
    } catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
      System.err.println("Error running Selenium on admin pages");
    }
  }

  private static void savePlanningPages() {
    try {
      // risk register page
      WebElement directoryButton = driver.findElement(By.className("planning_menu_item"));
      directoryButton.click();
      Thread.sleep(1000);
      WebElement riskPage = driver.findElement(By.className("planning_risk_item"));
      riskPage.click();
      saveHTML();

      // show risk page
      List<WebElement> tableRisks = driver.findElements(By.className(TABLE_CLASS));
      if (!tableRisks.isEmpty()) {
        tableRisks.get(0).click();
        saveHTML();

        // edit risk page
        WebElement editRisk = driver.findElement(By.className("btn-edit"));
        editRisk.click();
        saveHTML();
        goBack();

        goBack();
      }

      // new risk page
      List<WebElement> newRisk = driver.findElements(By.className("btn-plus"));
      newRisk.get(1).click();
      saveHTML();
      goBack();

      // format register page
      directoryButton = driver.findElement(By.className("planning_menu_item"));
      directoryButton.click();
      Thread.sleep(1000);
      WebElement formatPage = driver.findElement(By.className("planning_format_item"));
      formatPage.click();
      saveHTML();

      // show format page
      List<WebElement> tableFormats = driver.findElements(By.className(TABLE_CLASS));
      if (!tableFormats.isEmpty()) {
        tableFormats.get(0).click();
        saveHTML();

        // edit format page
        WebElement editFormat = driver.findElement(By.className("btn-edit"));
        editFormat.click();
        saveHTML();
        goBack();

        goBack();
      }

      // new format page
      List<WebElement> newFormat = driver.findElements(By.className("btn-plus"));
      newFormat.get(1).click();
      saveHTML();
      goBack();

      // event register page
      directoryButton = driver.findElement(By.className("planning_menu_item"));
      directoryButton.click();
      Thread.sleep(1000);
      WebElement eventPage = driver.findElement(By.className("planning_event_item"));
      eventPage.click();
      saveHTML();

      // show format page
      List<WebElement> tableEvents = driver.findElements(By.className(TABLE_CLASS));
      if (!tableEvents.isEmpty()) {
        tableEvents.get(0).click();
        saveHTML();
        goBack();
      }

      // agent register page
      directoryButton = driver.findElement(By.className("planning_menu_item"));
      directoryButton.click();
      Thread.sleep(1000);
      WebElement agentPage = driver.findElement(By.className("planning_agent_item"));
      agentPage.click();
      saveHTML();

      // show format page
      List<WebElement> tableAgents = driver.findElements(By.className(TABLE_CLASS));
      if (!tableAgents.isEmpty()) {
        tableAgents.get(0).click();
        saveHTML();
        goBack();
      }
    } catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
      System.err.println("Error running Selenium on planning pages");
    }
  }

  private static void saveHelpPages() {
    try {
      // help page
      WebElement helpMenu = driver.findElement(By.className("help_menu_item"));
      helpMenu.click();
      saveHTML();
    } catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
      System.err.println("Error running Selenium on help pages");
    }
  }

  private static void saveLoginPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // login page
    WebElement loginMenu = driver.findElement(By.className("user_menu_item"));
    loginMenu.click();
    saveHTML();

    // forget password page
    List<WebElement> loginLinks = driver.findElements(By.className("login-link"));
    loginLinks.get(0).click();
    saveHTML();
    goBack();

    // register page
    loginLinks = driver.findElements(By.className("login-link"));
    loginLinks.get(1).click();
    saveHTML();
    goBack();

    // welcome page after login
    WebElement loginName = driver.findElement(By.className("gwt-TextBox"));
    loginName.sendKeys("admin");
    WebElement loginPassword = driver.findElement(By.className("gwt-PasswordTextBox"));
    loginPassword.sendKeys("roda");
    WebElement loginButton = driver.findElement(By.className("login-button"));
    loginButton.click();
    saveHTML();
  }

  private static void savePublicPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // publications page
    driver.get(url + "#theme/Publications.html");
    saveHTML();
    goBack();
  }

  private static void saveHTML() throws FileNotFoundException, InterruptedException, UnsupportedEncodingException {
    saveHTML(10000);
  }

  private static void saveHTML(int sleepTime)
    throws FileNotFoundException, InterruptedException, UnsupportedEncodingException {
    Thread.sleep(sleepTime);

    try {
      WebElement cookieButton = driver.findElement(By.className("cc_btn"));
      cookieButton.click();
    } catch (NoSuchElementException e) {
      // do nothing
    }

    sendPostRequest(driver.getPageSource());
  }

  private static void goBack() throws InterruptedException {
    driver.navigate().back();
    Thread.sleep(1000);
  }

  private static void sendPostRequest(String source) throws UnsupportedEncodingException {
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addTextBody("input", source);

    HttpPost httpPost = new HttpPost("http://www.acessibilidade.gov.pt/accessmonitor/");
    httpPost.setEntity(builder.build());

    try {
      CloseableHttpResponse response = httpClient.execute(httpPost);
      System.err.println(response);

      for (Header h : response.getAllHeaders()) {
        if ("location".equalsIgnoreCase(h.getName())) {
          locations.put(h.getValue(), driver.getCurrentUrl());
        }
      }
    } catch (IOException e) {
      System.err.println("Error sending POST request!");
    }
  }

  private static String getHTMLSource(String link) throws IOException {
    URL url = new URL(link);
    URLConnection connection = url.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }

    IOUtils.closeQuietly(in);
    return content.toString();
  }

  private static void commandHelp() {
    System.out.println("program <roda_url> <path_to_chrome_driver>");
  }
}
