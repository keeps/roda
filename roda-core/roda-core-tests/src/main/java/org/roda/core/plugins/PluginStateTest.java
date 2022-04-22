package org.roda.core.plugins;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Miguel Guimarãese <mguimaraes@keep.pt>
 */

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class PluginStateTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginStateTest.class);

  @Test
  public void testPluginStateWhenAllPluginsAreMandatory() {
    PluginState initialState = PluginState.RUNNING;
    PluginState afterPlugin1 = ReportUtils.calculatePluginState(initialState, PluginState.SKIPPED, true);
    PluginState afterPlugin2 = ReportUtils.calculatePluginState(afterPlugin1, PluginState.SKIPPED, true);
    PluginState afterPlugin3 = ReportUtils.calculatePluginState(afterPlugin2, PluginState.SKIPPED, true);
    PluginState finalState = ReportUtils.calculatePluginState(afterPlugin3, PluginState.SUCCESS, true);
    Assert.assertEquals(PluginState.SUCCESS, finalState);
  }

  @Test
  public void testPluginStateWhenAllPluginsAreMandatoryAndSkip() {
    PluginState initialState = PluginState.RUNNING;
    PluginState afterPlugin1 = ReportUtils.calculatePluginState(initialState, PluginState.SKIPPED, true);
    PluginState afterPlugin2 = ReportUtils.calculatePluginState(afterPlugin1, PluginState.SKIPPED, true);
    PluginState finalState = ReportUtils.calculatePluginState(afterPlugin2, PluginState.SKIPPED, true);
    Assert.assertEquals(PluginState.SKIPPED, finalState);
  }

  @Test
  public void testPluginStateWhenAllPluginsAreMandatoryAndOneFails() {
    PluginState initialState = PluginState.RUNNING;
    PluginState afterPlugin1 = ReportUtils.calculatePluginState(initialState, PluginState.SKIPPED, true);
    PluginState afterPlugin2 = ReportUtils.calculatePluginState(afterPlugin1, PluginState.FAILURE, true);
    PluginState finalState = ReportUtils.calculatePluginState(afterPlugin2, PluginState.SUCCESS, true);
    Assert.assertEquals(PluginState.FAILURE, finalState);
  }

  @Test
  public void testPluginStateWhenFailsAnOptionalPlugin() {
    PluginState initialState = PluginState.RUNNING;
    PluginState afterPlugin1 = ReportUtils.calculatePluginState(initialState, PluginState.FAILURE, false);
    PluginState afterPlugin2 = ReportUtils.calculatePluginState(afterPlugin1, PluginState.SKIPPED, true);
    PluginState finalState = ReportUtils.calculatePluginState(afterPlugin2, PluginState.SUCCESS, true);
    Assert.assertEquals(PluginState.PARTIAL_SUCCESS, finalState);
  }

  @Test
  public void testPluginStateWhenFailsAnOptionalPluginStartingWithASuccess() {
    PluginState initialState = PluginState.RUNNING;
    PluginState afterPlugin1 = ReportUtils.calculatePluginState(initialState, PluginState.SUCCESS, true);
    PluginState afterPlugin2 = ReportUtils.calculatePluginState(afterPlugin1, PluginState.FAILURE, false);
    PluginState finalState = ReportUtils.calculatePluginState(afterPlugin2, PluginState.SKIPPED, true);
    Assert.assertEquals(PluginState.PARTIAL_SUCCESS, finalState);
  }

}
