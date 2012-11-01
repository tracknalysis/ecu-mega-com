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

/**
 * Utility API for managing IO operations to the ECU.
 * 
 * @author David Smith
 * @author David Valeri
 */
public interface MegasquirtIoManager {

    /**
     * Closes any underlying resources, such as a socket, in order to be stop
     * for communication and release resources.
     * 
     * @throws IOException
     *             if there is an error
     */
    void connect() throws IOException;

    /**
     * Opens any underlying resources, such as a socket, in order to be ready for communication.
     *
     * @throws IOException if there is an error
     */
    void disconnect() throws IOException;
    
    /**
     * Writes a command to the output stream, flushing the stream afterwards.
     * Does not flush the stream before writing.
     * 
     * @param command
     *            the command bytes to write
     */
    void write(byte[] command) throws IOException;

    /**
     * Flushes all streams, writes a command to the output stream, flushing the
     * stream afterwards, waits for {@code delay} milliseconds and then reads
     * all available bytes on the input stream.
     * 
     * @param command
     *            the command bytes to write
     * @param delay
     *            the delay in milliseconds to wait before reading the response
     * 
     * @return the bytes read, never {@code null}
     */
    byte[] writeAndRead(byte[] command, long delay) throws IOException;

    /**
     * Flushes all streams, writes a command to the output stream, flushing the
     * stream afterwards, and then reads {@code result.length} bytes on the
     * input stream into {@code result} throwing an {@code IOException} if
     * reading takes longer than {@code timeout}.
     * 
     * @param command
     *            the command bytes to write
     * @param result
     *            the buffer to read the results into
     * @param timeout
     *            the time in, milliseconds, to wait for the full response to
     *            arrive before throwing an exception
     */
    void writeAndRead(byte[] command, byte[] result, long timeout)
            throws IOException;

    /**
     * Read all available bytes on the input stream.
     * 
     * @return the bytes read, never {@code null}
     */
    byte[] read() throws IOException;

    /**
     * Reads {@code result.length} bytes on the input stream into {@code result}
     * throwing an {@code IOException} if reading takes longer than
     * {@code timeout}.
     * 
     * @param result
     *            the buffer to store the read bytes in
     * @param timeout
     *            the time in, milliseconds, to wait for the full response to
     *            arrive before throwing an exception
     */
    void read(byte[] result, long timeout) throws IOException;

    /**
     * Flushes the output stream and then discards all available input on
     * the input stream.
     */
    void flushAll() throws IOException;

}