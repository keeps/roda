/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.bundle.v2;

import org.roda.core.data.v2.IsRODAObject;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PackageState implements Serializable {
    private static final long serialVersionUID = 3209917472339221135L;
    private Class<? extends IsRODAObject> className;
    private int count = 0;
    private String filePath;

    public PackageState() {
    }

    public Class<? extends IsRODAObject> getClassName() {
        return className;
    }

    public void setClassName(Class<? extends IsRODAObject> className) {
        this.className = className;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
