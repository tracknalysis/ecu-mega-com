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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * Simulator that can replay/validate communications based on the contents of a debug log.
 *
 * @author David Valeri
 * @see DebugLogWriterMegasquirtIoManager
 */
public class DebugLogReaderMegasquirtIoManager implements MegasquirtIoManager {

    private final Base64 codec = new Base64();
    private final BufferedReader reader;
    
    public DebugLogReaderMegasquirtIoManager(InputStream logFileInputStream) throws IOException {
        reader = new BufferedReader(new InputStreamReader(logFileInputStream)); 
    }
    
    @Override
    public void connect() throws IOException {
        // No-op
    }

    @Override
    public void disconnect() throws IOException {
        // No-op
    }
    
    @Override
    public void write(byte[] command) throws IOException {
        writeInternal(command);
    }

    @Override
    public byte[] writeAndRead(byte[] command, long delay) throws IOException {
        delay(delay);
        return writeAndReadInternal(command);
    }

    @Override
    public void writeAndRead(byte[] command, byte[] result, long timeout) throws IOException {
        delay(timeout/2);
        
        byte [] interimResult = writeAndReadInternal(command);
        if (interimResult.length != result.length) {
            throw new IOException("Expected [" + result.length
                    + "] result bytes but have [" + interimResult.length
                    + "] bytes");
        } else {
            System.arraycopy(interimResult, 0, result, 0, interimResult.length);
        }
    }

    @Override
    public byte[] read() throws IOException {
        return readInternal();
    }

    @Override
    public void read(byte[] result, long timeout) throws IOException {
        delay(timeout/2);
        
        byte[] interimResult = readInternal();
        
        if (interimResult.length != result.length) {
            throw new IOException("Expected [" + result.length
                    + "] result bytes but have [" + interimResult.length
                    + "] bytes");
        } else {
            System.arraycopy(interimResult, 0, result, 0, interimResult.length);
        }
    }

    @Override
    public void flushAll() throws IOException {
        // Do nothing for this in simulation
    }
    
    private void delay(long time) throws IOException {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while delaying.", e);
        }
    }
    
    private void writeInternal(byte[] command) throws IOException {
        String line = reader.readLine();
        
        if (DebugLogWriterMegasquirtIoManager.WRITE_COMMENT.equals(line)) {
            String outLine = reader.readLine();
            
            byte[] controlCommand = lineToBytes(outLine);
            
            if (!Arrays.equals(controlCommand, command)) {
                throw new IOException("Command [" + command
                        + "] not equal to expected command ["
                        + controlCommand + "].");
            }
        } else {
            throw new IOException("The line didn't start with ["
                    + DebugLogWriterMegasquirtIoManager.WRITE_COMMENT
                    + "].  Line was [" + line + "]");
        }
    }
    
    private byte[] writeAndReadInternal(byte[] command) throws IOException {
        String line = reader.readLine();
        
        if (DebugLogWriterMegasquirtIoManager.WRITE_AND_READ_COMMENT.equals(line)) {
            String outLine = reader.readLine();
            
            byte[] controlCommand = lineToBytes(outLine);
            
            if (!Arrays.equals(controlCommand, command)) {
                throw new IOException("Command [" + command
                        + "] not equal to expected command ["
                        + controlCommand + "].");
            } else {
                return lineToBytes(reader.readLine());                    
            }
            
        } else {
            throw new IOException("The line didn't start with ["
                    + DebugLogWriterMegasquirtIoManager.WRITE_AND_READ_COMMENT
                    + "].  Line was [" + line + "]");
        }
    }
    
    private byte[] readInternal() throws IOException {
        String line = reader.readLine();
        
        if (DebugLogWriterMegasquirtIoManager.READ_COMMENT.equals(line)) {
            return lineToBytes(reader.readLine());
        } else {
            throw new IOException("The line didn't start with ["
                    + DebugLogWriterMegasquirtIoManager.READ_COMMENT
                    + "].  Line was [" + line + "]");
        }
    }
    
    private byte[] lineToBytes(String line) throws IOException {
        if (line == null) {
            throw new IOException();
        } else {
            byte[] lineBase64Bytes = line.getBytes("UTF-8");
            return codec.decode(lineBase64Bytes);
        }
    }
}
