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
package net.tracknalysis.ms.io;

import java.io.IOException;
import java.io.InputStream;

import net.tracknalysis.common.io.SocketManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An implementation that performs input directly on the socket input and trusts
 * in {@link InputStream#available()} to behave ideally for detecting timeouts.
 * 
 * @author David Smith
 * @author David Valeri
 */
public class DirectMegasquirtIoManager extends AbstractMegasquirtIoManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(DirectMegasquirtIoManager.class);

    public DirectMegasquirtIoManager(SocketManager delegate) {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     *
     * Based on the implementation from uk.org.smithfamily.mslogger.comms.Connection, original
     * copyright David Smith.  Licensed under the Apache License, Version 2.0.
     */
    @Override
    public synchronized void readBytes(byte[] result, long timeout) throws IOException {
        InputStream is = getInputStream();
        
        long start = System.currentTimeMillis();
        long elapsedTime = 0;
        int nBytes = result.length;
        int bytesRead = 0;
        int available = is.available();
        
        try {
            
            while (bytesRead < nBytes) {
                
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
                
                if (available > 0) {
                    int bytesJustRead = 
                            is.read(result, bytesRead, nBytes - bytesRead);
                    if (bytesJustRead == -1) {
                        break;
                    }
                    
                    bytesRead += bytesJustRead;
                }
                
                elapsedTime = System.currentTimeMillis() - start;
                
                if (bytesRead != nBytes && elapsedTime > timeout) {
                    throw new IOException("Error fulfilling read request.  Read " + bytesRead
                            + " bytes of " + nBytes + " before exceeding timeout of "
                            + timeout + "ms.");
                }
                
                available = is.available();
            }
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Read {} bytes of {} in {}ms: ", new Object[] { bytesRead,
                        nBytes, elapsedTime, result});
            }
        }
        
        if (bytesRead != result.length) {
            throw new IOException("Error fulfilling read request.  Read " + bytesRead
                    + " bytes of " + result.length + ".");
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
