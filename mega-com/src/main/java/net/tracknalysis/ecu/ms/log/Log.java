/**
 * Copyright 2011, 2012 David Smith.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.ecu.ms.log;

import java.io.IOException;

import net.tracknalysis.ecu.ms.Megasquirt;


/**
 * Adaptation of the original code from MSLogger representing the interface for a
 * mechanism to log data from a {@link Megasquirt}.
 *
 * @author David Smith
 * @author David Valeri
 */
public interface Log {
    
    void start() throws IOException;
    
    void stop() throws IOException;

    /**
     * Place a mark indicator in the log with the given message if currently logging.
     *
     * @param message the message to accompany the mark indicator
     */
    void mark(String message) throws IOException;
    
    /**
     * Place a mark indicator in the log if currently logging.
     */
    void mark() throws IOException;
    
    /**
     * Returns true if the log supports marks.
     */
    boolean isMarkSupported();
    
    /**
     * Get the time at which the log started in milliseconds since January 1, 1970 00:00:00 UTC.
     */
    long getStartTime();

    /**
     * Write to the log if currently logging.
     *
     * @param ms the instance to pull log info from
     *
     * @throws IOException if there is an error writing the log output
     */
    void write(Megasquirt ms) throws IOException;
    
    boolean isLogging();
}
