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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumUtils {
  private static WebDriver driver;
  private static String url;
  private static String driverPath; // "/path/to/chromedriver"
  private static Map<String, String> locations = new HashMap<String, String>();

  public static void main(String[] args) throws InterruptedException, IOException {
    if (args.length != 2) {
      System.err.println("Number of arguments not correct");
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
    saveHelpPages();
    savePlanningPages();
    saveAdminPages();
    saveIngestPages();
    saveSearchPages();
    saveBrowsePages();

    driver.quit();
    service.stop();

    for (String location : locations.keySet()) {
      String html = getHTMLSource(location);
      Pattern expression = Pattern.compile("<div id=\"webaxscore\".*?<span>(.*?)</span>");
      Matcher matcher = expression.matcher(html);
      if (matcher.find()) {
        System.out.println(location + " | " + locations.get(location) + " | " + matcher.group(1));
      }
    }
  }

  private static void saveBrowsePages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // browse page
    WebElement searchButton = driver.findElement(By.id("gwt-uid-2"));
    searchButton.click();
    saveHTML();

    // show browse page
    List<WebElement> aipList = driver.findElements(
      By.className("org-roda-wui-common-client-widgets-MyCellTableResources-TableStyle-cellTableEvenRow"));
    aipList.get(0).click();
    saveHTML();

    // permissions page
    List<WebElement> permissionPage = driver.findElements(By.className("btn-edit"));
    permissionPage.get(1).click();
    saveHTML();
    goBack();

    // events page
    List<WebElement> eventsPage = driver.findElements(By.className("btn-clock"));
    eventsPage.get(0).click();
    saveHTML();
    goBack();

    // create metadata page
    List<WebElement> createMetadataPage = driver.findElements(By.className("btn-plus"));
    createMetadataPage.get(2).click();
    saveHTML();
    goBack();
    goBack();

    // edit metadata page
    WebElement editMetadataPage = driver.findElement(By.className("fa-edit"));
    editMetadataPage.click();
    saveHTML();
    goBack();

    // new process page
    List<WebElement> newProcessPage = driver.findElements(By.className("btn-play"));
    newProcessPage.get(1).click();
    saveHTML();

    // risk association plugin new process page
    WebElement riskAssociationButton = driver
      .findElement(By.xpath("//option[@value='org.roda.core.plugins.plugins.risks.RiskAssociationPlugin']"));
    riskAssociationButton.click();
    saveHTML();

    goBack();
  }

  private static void saveSearchPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // search page
    WebElement searchButton = driver.findElement(By.id("gwt-uid-3"));
    searchButton.click();
    saveHTML();

    // advanced search page
    WebElement loadButton = driver.findElement(By.className("fa-angle-down"));
    loadButton.click();
    saveHTML();
  }

  private static void saveIngestPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // pre-ingest page
    WebElement ingestButton = driver.findElement(By.id("gwt-uid-21"));
    ingestButton.click();
    Thread.sleep(1000);
    WebElement preIngestPage = driver.findElement(By.id("gwt-uid-4"));
    preIngestPage.click();
    saveHTML();

    // transfer page
    ingestButton = driver.findElement(By.id("gwt-uid-21"));
    ingestButton.click();
    Thread.sleep(1000);
    WebElement transferPage = driver.findElement(By.id("gwt-uid-5"));
    transferPage.click();
    saveHTML();

    // load page
    WebElement loadButton = driver.findElement(By.className("btn-upload"));
    loadButton.click();
    saveHTML();
    goBack();

    // ingest state page
    ingestButton = driver.findElement(By.id("gwt-uid-21"));
    ingestButton.click();
    Thread.sleep(1000);
    WebElement statePage = driver.findElement(By.id("gwt-uid-6"));
    statePage.click();
    saveHTML();

    // apprasal page
    ingestButton = driver.findElement(By.id("gwt-uid-21"));
    ingestButton.click();
    Thread.sleep(1000);
    WebElement apprasalPage = driver.findElement(By.id("gwt-uid-7"));
    apprasalPage.click();
    saveHTML();
  }

  private static void saveAdminPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // administration process
    WebElement adminButton = driver.findElement(By.id("gwt-uid-22"));
    adminButton.click();
    Thread.sleep(1000);
    WebElement processPage = driver.findElement(By.id("gwt-uid-8"));
    processPage.click();
    saveHTML();

    // new process page
    WebElement newProcess = driver.findElement(By.className("btn-plus"));
    newProcess.click();
    saveHTML();

    // risk association plugin new process page
    WebElement riskAssociationButton = driver
      .findElement(By.xpath("//option[@value='org.roda.core.plugins.plugins.risks.RiskAssociationPlugin']"));
    riskAssociationButton.click();
    saveHTML();

    goBack();

    // administration users
    adminButton = driver.findElement(By.id("gwt-uid-22"));
    adminButton.click();
    Thread.sleep(1000);
    WebElement usersPage = driver.findElement(By.id("gwt-uid-9"));
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
    adminButton = driver.findElement(By.id("gwt-uid-22"));
    adminButton.click();
    Thread.sleep(1000);
    WebElement activityPage = driver.findElement(By.id("gwt-uid-10"));
    activityPage.click();
    saveHTML();

    // administration statistics
    adminButton = driver.findElement(By.id("gwt-uid-22"));
    adminButton.click();
    Thread.sleep(1000);
    WebElement statisticsPage = driver.findElement(By.id("gwt-uid-12"));
    statisticsPage.click();
    saveHTML();
  }

  private static void savePlanningPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // risk register page
    WebElement directoryButton = driver.findElement(By.id("gwt-uid-23"));
    directoryButton.click();
    Thread.sleep(1000);
    WebElement riskPage = driver.findElement(By.id("gwt-uid-13"));
    riskPage.click();
    saveHTML();

    // show risk page
    List<WebElement> tableRisks = driver.findElements(
      By.className("org-roda-wui-common-client-widgets-MyCellTableResources-TableStyle-cellTableEvenRow"));
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
    directoryButton = driver.findElement(By.id("gwt-uid-23"));
    directoryButton.click();
    Thread.sleep(1000);
    WebElement formatPage = driver.findElement(By.id("gwt-uid-14"));
    formatPage.click();
    saveHTML();

    // show format page
    List<WebElement> tableFormats = driver.findElements(
      By.className("org-roda-wui-common-client-widgets-MyCellTableResources-TableStyle-cellTableEvenRow"));
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
  }

  private static void saveHelpPages() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // help page
    WebElement helpMenu = driver.findElement(By.id("gwt-uid-15"));
    helpMenu.click();
    saveHTML();

    // installing descriptive metadata formats help page
    driver.get(url + "#theme/InstallingNewDescriptiveMetadataFormats.html");
    saveHTML();
    goBack();

    // statistics help page
    driver.get(url + "#theme/HelpStatistics.html");
    saveHTML();
    goBack();

    // advanced search help page
    driver.get(url + "#theme/AdvancedSearch.html");
    saveHTML();
    goBack();
  }

  private static void savePublicPages()
    throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
    // publications page
    driver.get(url + "#theme/Publications.html");
    saveHTML();
    goBack();

    // community help page
    driver.get(url + "#theme/Community_help.html");
    saveHTML();
    goBack();

    // license page
    driver.get(url + "#theme/License.html");
    saveHTML();
    goBack();

    // what is roda page
    driver.get(url + "#theme/What_is_RODA.html");
    saveHTML();
    goBack();

    // login page
    WebElement loginMenu = driver.findElement(By.id("gwt-uid-24"));
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

  private static void saveHTML() throws FileNotFoundException, InterruptedException, UnsupportedEncodingException {
    saveHTML(10000);
  }

  private static void saveHTML(int sleepTime)
    throws FileNotFoundException, InterruptedException, UnsupportedEncodingException {
    Thread.sleep(sleepTime);
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
          locations.put(h.getValue(), driver.getTitle());
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
