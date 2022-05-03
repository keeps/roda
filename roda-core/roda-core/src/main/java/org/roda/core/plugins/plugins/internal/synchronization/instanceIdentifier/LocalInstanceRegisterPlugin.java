package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.multiple.DefaultMultipleStepPlugin;
import org.roda.core.plugins.plugins.multiple.Step;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class LocalInstanceRegisterPlugin extends DefaultMultipleStepPlugin<IsRODAObject> {
  Logger LOGGER = LoggerFactory.getLogger(LocalInstanceRegisterPlugin.class);
  private Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private static List<Step> steps = new ArrayList<>();

  static {
    steps.add(new Step(InstanceIdentifierAIPPlugin.class.getName(), AIP.class, "", true, true));
    steps.add(new Step(InstanceIdentifierDIPPlugin.class.getName(), DIP.class, "", true, true));
    steps.add(new Step(InstanceIdentifierRepresentationInformationPlugin.class.getName(),
      RepresentationInformation.class, "", true, true));
    steps.add(new Step(InstanceIdentifierNotificationPlugin.class.getName(), Notification.class, "", true, true));
    steps.add(new Step(InstanceIdentifierRiskPlugin.class.getName(), Risk.class, "", true, true));
    steps.add(new Step(InstanceIdentifierRiskIncidencePlugin.class.getName(), RiskIncidence.class, "", true, true));
    steps.add(new Step(InstanceIdentifierJobPlugin.class.getName(), Job.class, "", true, true));
    steps.add(
      new Step(InstanceIdentifierRepositoryEventPlugin.class.getName(), PreservationMetadata.class, "", true, true));
    steps.add(new Step(RegisterPlugin.class.getName(), null, "", true, true));
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "RODA Object instance identifier";
  }

  @Override
  public String getDescription() {
    return "Add the instance identifier on the data that exists on the storage as also on the index. "
      + "If an object already has an instance identifier it will be updated by the new one. "
      + "This task aims to help the synchronization between a RODA central instance and the RODA local instance, "
      + "since when an local object is accessed in RODA Central it should have the instance identifier in order to "
      + "inform from which source is it from.";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Updated the instance identifier";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The instance identifier was updated successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Could not update the instance identifier";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<IsRODAObject> cloneMe() {
    return new LocalInstanceRegisterPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<Class<IsRODAObject>> getObjectClasses() {
    return Collections.emptyList();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void setTotalSteps() {
    this.totalSteps = steps.size();
  }

  @Override
  public List<Step> getPluginSteps() {
    return steps;
  }

  @Override
  public PluginParameter getPluginParameter(String pluginParameterId) {
    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      return new PluginParameter();
    }
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    setTotalSteps();
    super.setParameterValues(parameters);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    for (Step step : steps) {
      try {
        int count = 0;
        if (!RegisterPlugin.class.getName().equals(step.getPluginName())) {
          count = countObjects(index,
            (Class<? extends AbstractPlugin<? extends IsRODAObject>>) Class.forName(step.getPluginName()));
        } else {
          count++;
        }
        step.setSourceObjectsCount(Optional.of(count));

      } catch (ClassNotFoundException | GenericException e) {
        LOGGER.error("Could define the number of sourceObjectsCount.", e);
        throw new PluginException("Could define the number of sourceObjectsCount. ", e);
      }

    }
    return new Report();
  }

  private int countObjects(IndexService index, Class<? extends AbstractPlugin<? extends IsRODAObject>> pluginClass)
    throws GenericException {
    int count = 0;
    try {
      Class<? extends IsIndexed> indexedClass = getIndexedClassFromPluginClass(pluginClass);
      Filter filter = new Filter();
      if (IndexedPreservationEvent.class.isAssignableFrom(indexedClass)) {
        filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID));
      }
      count = index.count(indexedClass, filter).intValue();
      // Sum one job because this plugin creates a Job.
      if (indexedClass.getName().equals(Job.class.getName())) {
        count++;
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Could not define the number of sourceObjectsCount.", e);
      throw new GenericException("Could not define the number of sourceObjectsCount.", e);
    }
    return count;
  }

  private Class<? extends IsIndexed> getIndexedClassFromPluginClass(
    Class<? extends AbstractPlugin<? extends IsRODAObject>> pluginClass) {
    if (InstanceIdentifierAIPPlugin.class.isAssignableFrom(pluginClass)) {
      return IndexedAIP.class;
    } else if (InstanceIdentifierDIPPlugin.class.isAssignableFrom(pluginClass)) {
      return IndexedDIP.class;
    } else if (InstanceIdentifierRepresentationInformationPlugin.class.isAssignableFrom(pluginClass)) {
      return RepresentationInformation.class;
    } else if (InstanceIdentifierNotificationPlugin.class.isAssignableFrom(pluginClass)) {
      return Notification.class;
    } else if (InstanceIdentifierRiskPlugin.class.isAssignableFrom(pluginClass)) {
      return IndexedRisk.class;
    } else if (InstanceIdentifierRiskIncidencePlugin.class.isAssignableFrom(pluginClass)) {
      return RiskIncidence.class;
    } else if (InstanceIdentifierJobPlugin.class.isAssignableFrom(pluginClass)) {
      return Job.class;
    } else if (InstanceIdentifierRepositoryEventPlugin.class.isAssignableFrom(pluginClass)) {
      return IndexedPreservationEvent.class;
    } else {
      return null;
    }
  }
}
