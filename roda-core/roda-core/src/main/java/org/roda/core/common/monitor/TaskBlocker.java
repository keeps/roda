package org.roda.core.common.monitor;

public interface TaskBlocker {

  void acquire();

  void release();
}
