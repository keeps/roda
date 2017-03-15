/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.pipe;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.dispatch.Mapper;
import akka.dispatch.Recover;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class Frontend extends UntypedActor {

  private ActorRef masterProxy = getContext().actorOf(ClusterSingletonProxy.props("/user/master",
    ClusterSingletonProxySettings.create(getContext().system()).withRole("backend")), "masterProxy");

  @Override
  public void onReceive(Object message) {

    Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    Future<Object> f = ask(masterProxy, message, timeout);

    final ExecutionContext ec = getContext().system().dispatcher();

    Future<Object> res = f.map(new Mapper<Object, Object>() {
      @Override
      public Object apply(Object msg) {
        if (msg instanceof Master.Ack)
          return Ok.getInstance();
        else
          return super.apply(msg);
      }
    }, ec).recover(new Recover<Object>() {
      @Override
      public Object recover(Throwable failure) throws Throwable {
        return NotOk.getInstance();
      }
    }, ec);

    pipe(res, ec).to(getSender());
  }

  public static final class Ok implements Serializable {
    private static final long serialVersionUID = -3294129455437847409L;

    private Ok() {
    }

    private static final Ok instance = new Ok();

    public static Ok getInstance() {
      return instance;
    }

    @Override
    public String toString() {
      return "Ok";
    }
  }

  public static final class NotOk implements Serializable {
    private static final long serialVersionUID = 3982864218335249125L;

    private NotOk() {
    }

    private static final NotOk instance = new NotOk();

    public static NotOk getInstance() {
      return instance;
    }

    @Override
    public String toString() {
      return "NotOk";
    }
  }
}
