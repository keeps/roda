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

import akka.actor.UntypedActor;

public class AkkaWorkerActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaWorkerActor.class);

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  public AkkaWorkerActor(StorageService storage, ModelService model, IndexService index) {
    this.storage = storage;
    this.model = model;
    this.index = index;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Messages.PluginToBeExecuted) {
      Messages.PluginToBeExecuted message = (Messages.PluginToBeExecuted) msg;
      Plugin<?> plugin = message.getPlugin();
      try {
        plugin.execute(index, model, storage, message.getList());
        getSender().tell(new Messages.PluginExecuteIsDone(plugin, false), getSelf());
      } catch (Exception e) {
        LOGGER.error("Error executing plugin.execute()", e);
        getSender().tell(new Messages.PluginExecuteIsDone(plugin, true), getSelf());
      }
    } else {
      LOGGER.error("Received a message that it doesn't know how to process (" + msg.getClass().getName() + ")...");
      unhandled(msg);
    }
  }

}
