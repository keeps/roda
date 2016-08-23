/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Faria
 * 
 * @deprecated 20160824 hsilva: not seeing any method using it, so it will be
 *             removed soon
 */
public class Batch {

  private final List<Runnable> queue;
  private boolean running;

  /**
   * Create a new batch
   */
  public Batch() {
    queue = new ArrayList<Runnable>();
    running = false;
  }

  /**
   * Start working.
   */
  public void start() {
    if (!running) {
      running = true;
      new Thread() {
        public void run() {
          work();
        }
      }.start();

    }
  }

  /**
   * Stop working. Waits until current running job ends.
   */
  public synchronized void stop() {
    running = false;
    notifyAll();
  }

  /**
   * Add work to queue
   * 
   * @param runnable
   */
  public synchronized void add(Runnable runnable) {
    queue.add(runnable);
    notifyAll();
  }

  /**
   * Wait until runnable was executed
   * 
   * @param runnable
   */
  public synchronized void wait(Runnable runnable) {
    while (queue.contains(runnable)) {
      try {
        wait();
      } catch (InterruptedException e) {
        // do nothing
      }
    }
  }

  private void work() {
    while (running) {
      synchronized (this) {
        while (queue.size() == 0) {
          try {
            wait();
          } catch (InterruptedException e) {
            // do nothing
          }
        }
      }
      if (running) {
        Runnable job = queue.remove(0);
        job.run();
        synchronized (this) {
          notifyAll();
        }
      } else {
        System.out.println("Batch.work() Skipped working because not running");
      }

    }
  }

  /**
   * Test main
   * 
   * @param args
   */
  public static void main(String... args) {
    final Batch batch = new Batch();
    batch.start();
    System.out.println("Starting batch");
    List<Runnable> processes = new ArrayList<Runnable>();
    for (int i = 0; i < 5; i++) {
      final int index = i;
      Runnable runable = new Runnable() {

        public void run() {
          System.out.println(String.format("Start batch %1$d", index));
          try {
            Thread.sleep((index + 1) * 1000);
          } catch (InterruptedException e) {
            // do nothing
          }
          System.out.println(String.format("End batch %1$d", index));
        }

      };
      processes.add(runable);
    }

    System.out.println("Adding processes");
    for (Runnable process : processes) {
      batch.add(process);
    }

    System.out.println("Waiting for processes to finish");
    for (Runnable process : processes) {
      batch.wait(process);
    }

    System.out.println("Adding processes in threads and wainting on each thread");
    int i = 0;
    for (final Runnable process : processes) {
      final int index = i;
      Thread t = new Thread() {
        public void run() {
          batch.add(process);
          batch.wait(process);
          System.out.println("Ended thread " + index);
        }
      };
      System.out.println("Starting thread " + index);
      t.start();
      i++;

    }

  }

}
