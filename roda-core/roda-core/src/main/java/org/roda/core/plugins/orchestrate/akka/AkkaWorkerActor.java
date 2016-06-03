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
  private final Logger logger = LoggerFactory.getLogger(getClass());

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
        getSender().tell("", getSelf());
        // getSender().tell(returnMessage, getSelf());
      } catch (Throwable e) {
        logger.error("Error executing action!", e);
        getSender().tell("", getSelf());
        // getSender().tell(returnMessage, getSelf());
        throw e;
      }
    } else {
      unhandled(msg);
    }
  }

}
