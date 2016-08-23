/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

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
    this.storage = super.getStorage();
    this.model = super.getModel();
    this.index = super.getIndex();
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
    message.logProcessingStarted();
    Plugin<?> plugin = message.getPlugin();
    try {
      plugin.execute(index, model, storage, message.getList());
      getSender().tell(new Messages.PluginExecuteIsDone(plugin, false), getSelf());
    } catch (Exception e) {
      LOGGER.error("Error executing plugin.execute()", e);
      getSender().tell(new Messages.PluginExecuteIsDone(plugin, true), getSelf());
    }
    message.logProcessingEnded();
  }

  private void handlePluginAfterAllExecuteIsReady(Object msg) {
    Messages.PluginAfterAllExecuteIsReady message = (Messages.PluginAfterAllExecuteIsReady) msg;
    message.logProcessingStarted();
    Plugin<?> plugin = message.getPlugin();
    try {
      plugin.afterAllExecute(index, model, storage);
      getSender().tell(new Messages.PluginAfterAllExecuteIsDone(plugin, false), getSelf());
    } catch (Exception e) {
      LOGGER.error("Error executing plugin.afterAllExecute()", e);
      getSender().tell(new Messages.PluginAfterAllExecuteIsDone(plugin, true), getSelf());
    }
    message.logProcessingEnded();
  }

}
