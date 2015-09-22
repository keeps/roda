package org.roda.action.orchestrate.embed.actors;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.embed.AkkaEmbeddedActionOrchestrator.ActionMessage;
import org.roda.index.IndexService;
import org.roda.model.ModelService;
import org.roda.storage.StorageService;

import akka.actor.UntypedActor;

public class WorkerActor extends UntypedActor {
  private final Logger logger = Logger.getLogger(getClass());

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  public WorkerActor(StorageService storage, ModelService model, IndexService index) {
    this.storage = storage;
    this.model = model;
    this.index = index;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof ActionMessage) {
      ActionMessage message = (ActionMessage) msg;
      message.getAction().init();
      try {
        message.getAction().execute(index, model, storage, message.getList());
      } catch (Exception e) {
        logger.error("Error executing action!", e);
      }
      message.getAction().shutdown();
      getSender().tell("Done!", getSelf());
    }
  }

}
