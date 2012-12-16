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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.tracknalysis.ecu.ms.Megasquirt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of a composite log.
 *
 * @author David Valeri
 */
public class DefaultCompositeLog implements CompositeLog {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCompositeLog.class);
    
    private List<Log> logs = new ArrayList<Log>(2);
    private volatile long startTime = 0;
    
    public DefaultCompositeLog(Log... logs) {
        if (logs != null) {
            this.logs.addAll(Arrays.asList(logs));
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Ensures that {@link Log#start()} is called on all wrapped log instances, even if there are
     * {@code IOException} failures in one of more of the wrapped logs.  This method will throw
     * an IOException if any wrapped instance throws an IOException.  If any wrapped instance
     * throws an unchecked exception, this method will throw the exception immediately.
     */
    @Override
    public synchronized void start() throws IOException {
        startTime = System.currentTimeMillis();
        IOException exception = null;
        
        for (Log log : logs) {
            try {
                log.start();
            } catch (IOException e) {
                exception = e;
                LOG.warn("Error starting log instance " + log + ".", e);
            }
        }
        
        if (exception != null) {
            IOException toThrow = new IOException(
                    "One or more IOExceptions encountered while starting "
                            + "wrapped logs.  Rethrowing last exception encountered.");
            toThrow.initCause(exception);
            
            throw toThrow;
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Ensures that {@link Log#stop()} is called on all wrapped log instances, even if there are
     * {@code IOException} failures in one of more of the wrapped logs.  This method will throw
     * an IOException if any wrapped instance throws an IOException.  If any wrapped instance
     * throws an unchecked exception, this method will throw the exception immediately.
     */
    @Override
    public synchronized void stop() throws IOException {
        IOException exception = null;
        
        for (Log log : logs) {
            try {
                log.stop();
            } catch (IOException e) {
                exception = e;
                LOG.warn("Error stopping log instance " + log + ".", e);
            }
        }
        
        if (exception != null) {
            IOException toThrow = new IOException(
                    "One or more IOExceptions encountered while stopping "
                            + "wrapped logs.  Rethrowing last exception encountered.");
            toThrow.initCause(exception);
            
            throw toThrow;
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * This implementation will return {@code true} if one ore more of the wrapped instances
     * is currently logging.
     */
    @Override
    public synchronized boolean isLogging() {
        boolean logging = false;
        for (Log log : logs) {
            if (log.isLogging()) {
                logging = true;
            }
        }
        
        return logging;
    }

    @Override
    public List<Log> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    @Override
    public synchronized void write(Megasquirt ms) {
        for (Log log : logs) {
            try {
                log.write(ms);
            } catch (IOException e) {
                // TODO error handling
                LOG.error("Error writing to log instance " + log + ".", e);
            }
        }
    }

    @Override
    public synchronized void mark() {
        for (Log log : logs) {
            try {
                log.mark();
            } catch (IOException e) {
                // TODO error handling
                LOG.error("Error marking to log instance " + log + ".", e);
            }
        }
    }

    @Override
    public synchronized void mark(String message) {
        for (Log log : logs) {
            try {
                log.mark(message);
            } catch (IOException e) {
                // TODO error handling
                LOG.error("Error marking to log instance " + log + ".", e);
            }
        }
    }

    @Override
    public boolean isMarkSupported() {
        return true;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }
}
