package org.roda.wui.api.v2.services;

import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.v2.log.ClientLogCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class ClientLoggerService {

  public void log(ClientLogCreateRequest clientLogCreateRequest, HttpServletRequest request) {
    String info = "[" + request.getRemoteAddr() + "] ";

    switch (clientLogCreateRequest.getLevel()) {
      case DEBUG -> debug(clientLogCreateRequest.getClassName(), clientLogCreateRequest.getMessage(),
        clientLogCreateRequest.getThrowable(), info);
      case ERROR -> error(clientLogCreateRequest.getClassName(), clientLogCreateRequest.getMessage(),
        clientLogCreateRequest.getThrowable(), info);
      case FATAL -> fatal(clientLogCreateRequest.getClassName(), clientLogCreateRequest.getMessage(),
        clientLogCreateRequest.getThrowable(), info);
      case INFO -> info(clientLogCreateRequest.getClassName(), clientLogCreateRequest.getMessage(),
        clientLogCreateRequest.getThrowable(), info);
      case TRACE -> trace(clientLogCreateRequest.getClassName(), clientLogCreateRequest.getMessage(),
        clientLogCreateRequest.getThrowable(), info);
      case WARN -> warn(clientLogCreateRequest.getClassName(), clientLogCreateRequest.getMessage(),
        clientLogCreateRequest.getThrowable(), info);
      default -> throw new IllegalStateException("Unexpected value: " + clientLogCreateRequest.getLevel());
    }
  }

  public void debug(String className, String message, Throwable throwable, String info) {
    Logger logger = LoggerFactory.getLogger(className);
    logger.debug("{}{}", info, message, throwable);
  }

  public void info(String className, String message, Throwable throwable, String info) {
    Logger logger = LoggerFactory.getLogger(className);
    logger.info("{}{}", info, message, throwable);
  }

  public void trace(String className, String message, Throwable throwable, String info) {
    Logger logger = LoggerFactory.getLogger(className);
    logger.trace("{}{}", info, message, throwable);
  }

  public void warn(String className, String message, Throwable throwable, String info) {
    Logger logger = LoggerFactory.getLogger(className);
    logger.warn("{}{}", info, message, throwable);
  }

  public void error(String className, String message, Throwable throwable, String info) {
    Logger logger = LoggerFactory.getLogger(className);
    logger.error("{}{}", info, message, throwable);
  }

  public void fatal(String className, String message, Throwable throwable, String info) {
    Logger logger = LoggerFactory.getLogger(className);
    logger.error("{}{}", info, message, throwable);
  }
}
