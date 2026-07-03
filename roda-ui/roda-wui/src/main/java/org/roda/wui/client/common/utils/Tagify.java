package org.roda.wui.client.common.utils;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Tagify")
public class Tagify {

    // Constructor binds the JS library to a native DOM element
    public Tagify(Element inputElement) {

    }

    // Map any methods you need to call from Java
  public native void addTags(String[] tags);

  public native void removeAllTags();
}
