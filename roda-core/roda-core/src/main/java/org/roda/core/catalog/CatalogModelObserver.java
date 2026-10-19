/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.catalog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the catalog up to date for AIP changes made directly on the main model,
 * i.e. outside a RODA transaction (e.g. in {@code beforeAllExecute} /
 * {@code afterAllExecute}). Changes made inside a transaction are written to the
 * catalog at commit instead, so this observer is skipped when transaction
 * notifications are replayed.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public final class CatalogModelObserver implements InvocationHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogModelObserver.class);

  private final CatalogService catalog;
  private final ModelService model;

  private CatalogModelObserver(CatalogService catalog, ModelService model) {
    this.catalog = catalog;
    this.model = model;
  }

  public static ModelObserver create(CatalogService catalog, ModelService model) {
    return (ModelObserver) Proxy.newProxyInstance(ModelObserver.class.getClassLoader(),
      new Class<?>[] {ModelObserver.class}, new CatalogModelObserver(catalog, model));
  }

  public static boolean isCatalogObserver(ModelObserver observer) {
    return Proxy.isProxyClass(observer.getClass())
      && Proxy.getInvocationHandler(observer) instanceof CatalogModelObserver;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    if (method.getDeclaringClass() == Object.class) {
      return switch (method.getName()) {
        case "equals" -> proxy == args[0];
        case "hashCode" -> System.identityHashCode(proxy);
        default -> "CatalogModelObserver";
      };
    }

    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>((ModelObserver) proxy);
    try {
      if (args != null && args.length > 0 && args[0] instanceof AIP aip) {
        catalog.synchronize(List.of(aip.getId()), model);
      } else if ("aipDeleted".equals(method.getName())) {
        catalog.delete((String) args[0]);
      }
    } catch (RuntimeException e) {
      LOGGER.error("Error updating the catalog on {}", method.getName(), e);
      ret.add(e);
    }
    return ret;
  }
}
