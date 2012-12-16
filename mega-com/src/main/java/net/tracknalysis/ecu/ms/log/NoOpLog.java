/**
 * Copyright 2012 David Valeri.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.ecu.ms.Megasquirt;

/**
 * An implementation that performs no action other than logging to an SLF4J {@link Logger} at
 * the debug level.
 *
 * @author David Valeri
 */
public class NoOpLog implements Log {
    
    private static final Logger LOG = LoggerFactory.getLogger(NoOpLog.class);
    
    boolean logging = false;
    private volatile long startTime;

    @Override
    public synchronized void start() throws IOException {
        logging = true;
        startTime = System.currentTimeMillis();
        
        LOG.debug("Started logging.");
    }

    @Override
    public synchronized void stop() throws IOException {
        logging = false;
        
        LOG.debug("Stopped logging.");
    }

    @Override
    public synchronized void mark(String message) throws IOException {
        if (logging) {
            LOG.debug("Mark: {}", message);
        }
    }

    @Override
    public void mark() throws IOException {
        mark("No comment.");
    }

    @Override
    public boolean isMarkSupported() {
        return true;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public synchronized void write(Megasquirt ms) throws IOException {
        if (logging) {
            LOG.debug("Log Megasquirt '{}'.", ms);
        }
    }

    @Override
    public synchronized boolean isLogging() {
        return logging;
    }
}
