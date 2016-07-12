/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.Report;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

@Test(groups = {"all", "travis"})
public class JsonUtilsTest {

  @Test
  public void testList() throws GenericException {
    List<String> list = Arrays.asList("a", "b", "c");
    String jsonString = JsonUtils.getJsonFromObject(list);

    List<String> list2 = JsonUtils.getListFromJson(jsonString, String.class);

    for (String string : list2) {
      Assert.assertNotNull(string);
    }
  }

  @Test
  public void testReportList() throws GenericException {
    List<Report> list = Arrays.asList(new Report(), new Report());

    String jsonString = JsonUtils.getJsonFromObject(list);

    List<Report> list2 = JsonUtils.getListFromJson(jsonString, Report.class);

    for (Report report : list2) {
      AssertJUnit.assertNotNull(report);
    }
  }
}
