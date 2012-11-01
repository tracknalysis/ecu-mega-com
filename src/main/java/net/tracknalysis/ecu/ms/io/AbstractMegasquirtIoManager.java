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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.tracknalysis.common.io.SocketManager;

import org.slf4j.Logger;


/**
 * @author David Smith
 * @author David Valeri
 */
public abstract class AbstractMegasquirtIoManager implements MegasquirtIoManager {
    
    private final SocketManager delegate;
    
    public AbstractMegasquirtIoManager(SocketManager delegate) {
        super();
        this.delegate = delegate;
    }
    
    @Override
    public synchronized final void connect() throws IOException {
        delegate.connect();
    }
    
    @Override
    public synchronized final void disconnect() throws IOException {
        delegate.disconnect();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Based on the implementation from uk.org.smithfamily.mslogger.comms.Connection, original
     * copyright David Smith.  Licensed under the Apache License, Version 2.0.
     */
    @Override
    public synchronized void write(byte[] command) throws IOException {
        OutputStream os = getOutputStream();
        
        if (command[0] == 'r') {
            os.write(command, 0, 3);
            os.flush();
            // Per MS serial doc examples, wait after selecting table index.
            delay(200);
            os.write(command, 3, 4);
            os.flush();
        } else {
            // Everything else just gets shoved onto the stream 
            os.write(command);
            os.flush();
        }
        
        getLogger().debug("Wrote bytes {}", command);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Based on the implementation from uk.org.smithfamily.mslogger.comms.Connection, original
     * copyright David Smith.  Licensed under the Apache License, Version 2.0.
     */
    @Override
    public synchronized byte[] writeAndRead(byte[] command, long delay) throws IOException {
        flushAll();
        write(command);
        delay(delay);
        byte[] result = read();
        return result;
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Based on the implementation from uk.org.smithfamily.mslogger.comms.Connection, original
     * copyright David Smith.  Licensed under the Apache License, Version 2.0.
     */
    @Override
    public synchronized void writeAndRead(byte[] command, byte[] result, long timeout) throws IOException {
        flushAll();
        write(command);
        read(result, timeout);
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Based on the implementation from uk.org.smithfamily.mslogger.comms.Connection, original
     * copyright David Smith.  Licensed under the Apache License, Version 2.0.
     */
    @Override
    public synchronized byte[] read() throws IOException {
        InputStream is = getInputStream();
        List<Byte> read = new ArrayList<Byte>();
        
        while (is.available() > 0) {
            byte b = (byte) is.read();
            read.add(b);
        }
    
        byte[] result = new byte[read.size()];
        int i = 0;
        for (Byte b : read) {
            result[i++] = b;
        }
    
        getLogger().debug("Read bytes {}", result);
    
        return result;
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * Based on the implementation from uk.org.smithfamily.mslogger.comms.Connection, original
     * copyright David Smith.  Licensed under the Apache License, Version 2.0.
     */
    @Override
    public synchronized void flushAll() throws IOException {
        getOutputStream().flush();
        
        InputStream is = getInputStream();
        Logger log = getLogger();
        
        if (is.available() > 0) {
            StringBuilder b = null;
            int bytesRead = 0;       
            if (getLogger().isDebugEnabled()) {
                b = new StringBuilder();
            }
            while(is.available() > 0) {
                if (log.isDebugEnabled()) {
                    b.append(String.format("%02x ", is.read()));
                    bytesRead++;
                } else {
                    is.read();
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Discarded {} bytes: {}", bytesRead, b);
            }
        }
        
        log.debug("Flushed streams.");
    }
    
    /**
     * Causes the current thread to delay for at least 
     * {@code delayPeriod} milliseconds.
     * <p/>
     * Based on uk.org.smithfamily.mslogger.comms.Connection, original copyright David
     * Smith. Licensed under the Apache License, Version 2.0. 
     */
    protected void delay(long delayPeriod) {
        try {
            Thread.sleep(delayPeriod);
        } catch (InterruptedException e) {
            getLogger().warn("Interrupted during delay.", e);
        }
    }
    
    protected final SocketManager getDelegate() {
        return this.delegate;
    }
    
    protected OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }
    
    protected InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }
    
    protected abstract Logger getLogger();
}
