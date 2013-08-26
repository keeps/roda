//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2011 Mirko Nasato and contributors
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter.process;

import java.io.IOException;

public interface ProcessManager {

    public static final long PID_NOT_FOUND = -2;
    public static final long PID_UNKNOWN = -1;

    void kill(Process process, long pid) throws IOException;

    /**
     * @param query
     * @return the pid if found, {@link #PID_NOT_FOUND} if not,
     *   or {@link #PID_UNKNOWN} if this implementation is unable to find out
     * @throws IOException
     */
    long findPid(ProcessQuery query) throws IOException;

}
