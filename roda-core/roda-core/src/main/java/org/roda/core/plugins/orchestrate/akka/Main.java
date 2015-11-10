package org.roda.core.plugins.orchestrate.akka;

import java.util.concurrent.TimeUnit;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorIdentity;
import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.persistence.journal.leveldb.SharedLeveldbJournal;
import akka.persistence.journal.leveldb.SharedLeveldbStore;
import akka.util.Timeout;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

// FIXME to be deleted
public class Main {
  public static void main(String[] args) throws InterruptedException {
    if (args.length == 0) {
      startBackend(2551, "backend");
      Thread.sleep(5000);
      startBackend(2552, "backend");
      startWorker(0);
      Thread.sleep(5000);
      startFrontend(0);
    }
    else {
      int port = Integer.parseInt(args[0]);
      if (2000 <= port && port <= 2999)
        startBackend(port, "backend");
      else if (3000 <= port && port <= 3999)
        startFrontend(port);
      else
        startWorker(port);
    }
  }

  private static FiniteDuration workTimeout = Duration.create(10, "seconds");

  public static void startBackend(int port, String role) {
    Config conf = ConfigFactory.parseString("akka.cluster.roles=[" + role + "]").
//        withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=[\"akka.tcp://ClusterSystem@192.168.2.56:2551\",\"akka.tcp://ClusterSystem@192.168.2.56:2552\"]")).
        withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)).
        withFallback(ConfigFactory.load());
    
    ActorSystem system = ActorSystem.create("ClusterSystem", conf);

    startupSharedJournal(system, (port == 2551),
        ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"));

    system.actorOf(
        ClusterSingletonManager.props(
            Master.props(workTimeout),
            PoisonPill.getInstance(),
            ClusterSingletonManagerSettings.create(system).withRole(role)
        ),
        "master");
  }

  public static void startWorker(int port) {
    Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load("worker"));

    ActorSystem system = ActorSystem.create("WorkerSystem", conf);

    ActorRef clusterClient = system.actorOf(
        ClusterClient.props(ClusterClientSettings.create(system)),
        "clusterClient");
    system.actorOf(Worker.props(clusterClient, Props.create(WorkExecutor.class)), "worker");
  }

  public static void startFrontend(int port) {
    Config conf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load());

    ActorSystem system = ActorSystem.create("ClusterSystem", conf);
    ActorRef frontend = system.actorOf(Props.create(Frontend.class), "frontend");
    system.actorOf(Props.create(WorkProducer.class, frontend), "producer");
    system.actorOf(Props.create(WorkResultConsumer.class), "consumer");
  }


  public static void startupSharedJournal(final ActorSystem system, boolean startStore, final ActorPath path) {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore) {
      system.actorOf(Props.create(SharedLeveldbStore.class), "store");
    }
    // register the shared journal

    Timeout timeout = new Timeout(15, TimeUnit.SECONDS );

    ActorSelection actorSelection = system.actorSelection(path);
    Future<Object> f = Patterns.ask(actorSelection, new Identify(null), timeout);

    f.onSuccess(new OnSuccess<Object>() {

      @Override
      public void onSuccess(Object arg0) throws Throwable {
        if (arg0 instanceof ActorIdentity && ((ActorIdentity) arg0).getRef() != null) {
          SharedLeveldbJournal.setStore(((ActorIdentity) arg0).getRef(), system);
        } else {
          system.log().error("Lookup of shared journal at {} timed out", path);
          System.exit(-1);
        }

      }}, system.dispatcher());

    f.onFailure(new OnFailure() {
      public void onFailure(Throwable ex) throws Throwable {
        system.log().error(ex, "Lookup of shared journal at {} timed out", path);
      }}, system.dispatcher());
  }
}
