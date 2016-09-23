package org.roda.core.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
  private static int pageCounter = 1;
  private static String url = "http://192.168.2.164:8888/";
  private static List<String> locations = new ArrayList<String>();

  public static void main(String[] args) throws InterruptedException, IOException {
    ChromeDriverService service = new ChromeDriverService.Builder()
      .usingDriverExecutable(new File("/home/nvieira/rodapages/chromedriver")).usingAnyFreePort().build();
    service.start();

    driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
    driver.get(url);

    // welcome page
    saveHTML();

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

    driver.quit();
    service.stop();

    System.err.println("LOCATIONS: " + locations);
  }

  private static void saveHTML() throws FileNotFoundException, InterruptedException, UnsupportedEncodingException {
    Thread.sleep(4000);
    File file = new File("/home/nvieira/rodapages/tests/test_" + pageCounter + ".html");
    PrintWriter pw = new PrintWriter(file);
    pw.print(driver.getPageSource());
    IOUtils.closeQuietly(pw);
    pageCounter++;
    // TODO sendPostRequest(file);
  }

  private static void goBack() throws InterruptedException {
    driver.navigate().back();
    Thread.sleep(1000);
  }

  private static void sendPostRequest(File htmlFile) throws UnsupportedEncodingException {
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addBinaryBody("up_file", htmlFile);
    builder.addTextBody("acm", "3657e55ad40b449");
    builder.addTextBody("wcag20", "WCAG 2.0");
    builder.addTextBody("upload", "Verifique");

    HttpPost httpPost = new HttpPost("http://www.acessibilidade.gov.pt/accessmonitor/");
    httpPost.addHeader("content-type", "multipart/form-data");
    httpPost.addHeader("referer", "http://www.acessibilidade.gov.pt/accessmonitor/");
    httpPost.addHeader("origin", "http://www.acessibilidade.gov.pt");
    httpPost.setEntity(builder.build());

    try {
      CloseableHttpResponse response = httpClient.execute(httpPost);
      System.err.println(response);

      for (Header h : response.getAllHeaders()) {
        if ("location".equalsIgnoreCase(h.getName())) {
          locations.add(h.getValue());
        }
      }
    } catch (IOException e) {
      System.err.println("Error sending POST request!");
    }
  }
}
