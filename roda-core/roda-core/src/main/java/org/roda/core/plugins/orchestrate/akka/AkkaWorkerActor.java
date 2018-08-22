/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.List;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AkkaWorkerActor extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaWorkerActor.class);

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  public AkkaWorkerActor() {
    super();
    this.storage = getStorage();
    this.model = getModel();
    this.index = getIndex();
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    super.setup(msg);
    if (msg instanceof Messages.PluginExecuteIsReady) {
      handlePluginExecuteIsReady(msg);
    } else if (msg instanceof Messages.PluginAfterAllExecuteIsReady) {
      handlePluginAfterAllExecuteIsReady(msg);
    } else {
      LOGGER.error("Received a message that it doesn't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
  }

  private void handlePluginExecuteIsReady(Object msg) {
    Messages.PluginExecuteIsReady message = (Messages.PluginExecuteIsReady) msg;
    List<LiteOptionalWithCause> objectsToBeProcessed = message.getList();
    message.logProcessingStarted();
    Plugin<IsRODAObject> messagePlugin = message.getPlugin();
    try {
      messagePlugin.execute(index, model, storage, objectsToBeProcessed);
      getSender().tell(new Messages.PluginExecuteIsDone(messagePlugin, false), getSelf());
    } catch (Throwable e) {
      // 20170120 hsilva: it is required to catch Throwable as there are some
      // linking errors that only will happen during the execution (e.g.
      // java.lang.NoSuchMethodError)
      LOGGER.error("Error executing plugin.execute()", e);
      getSender().tell(new Messages.PluginExecuteIsDone(messagePlugin, true, getErrorMessage(e)), getSelf());
    }
    message.logProcessingEnded();
  }

  private String getErrorMessage(Throwable e) {
    StringBuilder ret = new StringBuilder();
    ret.append("An exception has occurred. Exception '").append(e.getClass().getName()).append("' with message '")
      .append(e.getMessage()).append("'");
    if (e.getCause() != null) {
      ret.append(" [inner exception '").append(e.getCause().getClass().getName()).append("' with message '")
        .append(e.getCause().getMessage()).append("']");
    }
    return ret.toString();
  }

  private void handlePluginAfterAllExecuteIsReady(Object msg) {
    Messages.PluginAfterAllExecuteIsReady message = (Messages.PluginAfterAllExecuteIsReady) msg;
    message.logProcessingStarted();
    Plugin<?> plugin = message.getPlugin();
    try {
      plugin.afterAllExecute(index, model, storage);
      getSender().tell(new Messages.PluginAfterAllExecuteIsDone(plugin, false), getSelf());
    } catch (Throwable e) {
      // 20170120 hsilva: it is required to catch Throwable as there are some
      // linking errors that only will happen during the execution (e.g.
      // java.lang.NoSuchMethodError)
      LOGGER.error("Error executing plugin.afterAllExecute()", e);
      getSender().tell(new Messages.PluginAfterAllExecuteIsDone(plugin, true), getSelf());
    }
    message.logProcessingEnded();
  }

}
