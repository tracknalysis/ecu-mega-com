/**
 * Copyright 2012 the original author or authors.
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
package net.tracknalysis.ecu.ms.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.tracknalysis.common.io.SocketManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An implementation that separates the actual input from the input used for read operations
 * with a piped stream in order to allow interrupts of read operations.
 *
 * @author David Valeri
 */
public final class PipedMegasquirtIoManager extends AbstractMegasquirtIoManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(PipedMegasquirtIoManager.class);
    
    private PipedInputStream pis;
    private PipedOutputStream pos;
    private ReadThread readThread;
    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private volatile boolean run = true;
    
    public PipedMegasquirtIoManager(SocketManager delegate) {
        super(delegate);
        pis = new PipedInputStream();
        this.readThread = new ReadThread();
        this.readThread.start();
    }
    
    @Override
    protected void finalize() throws Throwable {
        // TODO stop the reader thread if it is running
        run = false;
        super.finalize();
    }
    
    @Override
    public synchronized void read(byte[] result, long timeout) throws IOException {
        
        int bytesRead = 0;
        ReadCallable callable = new ReadCallable(result, timeout, pis);
        FutureTask<Integer> readTask = new FutureTask<Integer>(callable);
        
        pool.execute(readTask);
        IOException exception = null;
        
        try {
            bytesRead = readTask.get(timeout, TimeUnit.MILLISECONDS);
            
            if (bytesRead != result.length) {
                throw new IOException("Error fulfilling read request.  Read " + bytesRead
                        + " bytes of " + result.length + ".");
            }
            
        } catch (InterruptedException e) {
            callable.scheduleStop();
            readTask.cancel(true);
            exception = new IOException("Interrupted while waiting for read task.");
            exception.initCause(e);
        } catch (ExecutionException e) {
            callable.scheduleStop();
            exception = new IOException("Error performing read.");
            exception.initCause(e.getCause());
        } catch (TimeoutException e) {
            callable.scheduleStop();
            readTask.cancel(true);
            exception = new IOException("Error fulfilling read request.  Exceeded timeout of "
                    + timeout + "ms.");
        }
        
        if (exception != null) {
            throw exception;
        }
    }
    
    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                pos = new PipedOutputStream(pis);
                InputStream is = getDelegate().getInputStream();
                byte[] readBuffer = new byte[1024];
                int bytesRead;
            
                while (run) {
                    bytesRead = is.read(readBuffer, 0, 1024);
                    pos.write(readBuffer, 0, bytesRead);
                    if (bytesRead == -1) {
                        break;
                    }
                }
            } catch (IOException e) {
                // TODO
                LOG.error("BAD", e);
            }
        }
    }
    
    private static class ReadCallable implements Callable<Integer> {
        
        private final byte[] result;
        private final long timeout;
        private final InputStream is;
        private volatile boolean run = true;
        
        public ReadCallable(byte[] result, long timeout, InputStream is) {
            this.result = result;
            this.timeout = timeout;
            this.is = is;
        }

        @Override
        public Integer call() throws Exception {
            long start = System.currentTimeMillis();
            long elapsedTime = 0;
            int nBytes = result.length;
            int bytesRead = 0;
            int available = is.available();
            
            try {
                
                while (bytesRead < nBytes && run) {
                    
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Read {} bytes of {}.  {} bytes available.  "
                                + "~{}ms elapsed.  ~{}ms to go before timeout",
                                new Object[] {
                                        bytesRead,
                                        nBytes,
                                        available,
                                        elapsedTime,
                                        timeout - elapsedTime});
                    }
                    
                    int bytesJustRead = 
                            is.read(result, bytesRead, nBytes - bytesRead);
                    if (bytesJustRead == -1) {
                        break;
                    }
                    
                    bytesRead += bytesJustRead;
                    
                    elapsedTime = System.currentTimeMillis() - start;
                    available = is.available();
                }
                
                return bytesRead;
            } finally {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Read {} bytes of {} in {}ms: ", new Object[] { bytesRead,
                            nBytes, elapsedTime, result});
                }
            }
        }
        
        public void scheduleStop() {
            run = false;
        }
    }
    
    @Override
    protected InputStream getInputStream() {
        return pis;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
