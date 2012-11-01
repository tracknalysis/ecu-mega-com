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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

/**
 * Decorator that adds logging of the bytes read and written in base 64 format.
 *
 * @author David Valeri
 * @see DebugLogReaderMegasquirtIoManager
 */
public class DebugLogWriterMegasquirtIoManager implements MegasquirtIoManager {
    
    public static final String WRITE_COMMENT = "# Write:";
    
    public static final String WRITE_AND_READ_COMMENT = "# Write and read:";
    
    public static final String READ_COMMENT = "# Read:";
    
    private final MegasquirtIoManager delegate;
    private final Base64 codec = new Base64();
    
    private final Writer writer; 
    
    public DebugLogWriterMegasquirtIoManager(MegasquirtIoManager delegate,
            OutputStream outputStream) throws IOException {
        this.delegate = delegate;
        writer = new OutputStreamWriter(new BufferedOutputStream(outputStream));
    }
    
    @Override
    public void connect() throws IOException {
        delegate.connect();   
    }
    
    @Override
    public void disconnect() throws IOException {
        delegate.disconnect();
    }

    @Override
    public void write(byte[] command) throws IOException {
        logWrite(command);
        delegate.write(command);
    }

    @Override
    public byte[] writeAndRead(byte[] command, long delay) throws IOException {
        byte[] bytes = delegate.writeAndRead(command, delay);
        logWriteAndRead(command, bytes);
        return bytes;
    }

    @Override
    public void writeAndRead(byte[] command, byte[] result, long timeout)
            throws IOException {
        delegate.writeAndRead(command, result, timeout);
        logWriteAndRead(command, result);
    }

    @Override
    public byte[] read() throws IOException {
        byte[] bytes = delegate.read();
        logRead(bytes);
        return bytes;
    }

    @Override
    public void read(byte[] result, long timeout) throws IOException {
        delegate.read(result, timeout);
        logRead(result);
    }

    @Override
    public void flushAll() throws IOException {
        delegate.flushAll();
    }
    
    private synchronized void logWrite(byte[] bytes) throws IOException {
        writer.write(WRITE_COMMENT);
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytes), Charset.forName("UTF-8")));
        writer.write("\r\n");
    }
    
    private synchronized void logWriteAndRead(byte[] bytesOut, byte[] bytesIn) throws IOException {
        writer.write(WRITE_AND_READ_COMMENT);
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytesOut), Charset.forName("UTF-8")));
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytesIn), Charset.forName("UTF-8")));
        writer.write("\r\n");
    }
    
    private synchronized void logRead(byte[] bytes) throws IOException {
        writer.write(READ_COMMENT);
        writer.write("\r\n");
        writer.write(new String(codec.encode(bytes), Charset.forName("UTF-8")));
        writer.write("\r\n");
    }
}
