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
import java.util.Arrays;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.common.io.IoProtocolHandler;


/**
 * Helper class used for communication with MS3 ECU that support CRC32 validated
 * protocol. See <a
 * href="http://www.msextra.com/doc/ms3/files/ms3_serial_protocol_0.05.pdf">MS
 * Documentation</a> for more info.
 * 
 * Modification of original class from MSLogger to conform to generic IO
 * interfaces and support use in a standard JRE.  Note that this protocol
 * is asymmetrical and that the response from the MS has an extra type field
 * in it that is not provided in the request. 
 * 
 * @author David Smith
 */
public class MsCrc32ProtocolHandler implements IoProtocolHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(MsCrc32ProtocolHandler.class);
	
    private final static int PAYLOAD_LENGTH = 2;
    private final static int TYPE_LENGTH    = 1;
    private final static int CRC32_LENGTH   = 4;

    /**
     * Wrap an array of bytes into a CRC-32 validated array of bytes.
     */
    @Override
    public byte[] wrapRequest(byte[] naked) throws IOException {
        byte[] wrapped = new byte[getWrappedRequestLength(naked.length)];
        wrapped[0] = 0;
        wrapped[1] = (byte) naked.length;
        System.arraycopy(naked, 0, wrapped, 2, naked.length); // Copy wrapped into naked

        CRC32 check = new CRC32();
        check.update(naked);

        long crc32value = check.getValue();
        int crcIndex = wrapped.length - CRC32_LENGTH;
        wrapped[crcIndex] = (byte) ((crc32value >> 24) & 0xff);
        wrapped[crcIndex + 1] = (byte) ((crc32value >> 16) & 0xff);
        wrapped[crcIndex + 2] = (byte) ((crc32value >> 8) & 0xff);
        wrapped[crcIndex + 3] = (byte) ((crc32value >> 0) & 0xff);

        return wrapped;
    }
    
    @Override
    public int getWrappedRequestLength(int length) {
    	return PAYLOAD_LENGTH + CRC32_LENGTH + length;  // Add 2 bytes for payload size and 4 bytes for CRC32
    }

    /**
     * Take a wrapped array of bytes and unwraps it while performing validation.
     * 
     * @param wrapped the wrapped array of bytes
     *
     * @return the unwrapped and validated 
     */
    @Override
    public byte[] unwrapResponse(byte[] wrapped) throws IOException
    {
        int notDataLength = PAYLOAD_LENGTH + TYPE_LENGTH + CRC32_LENGTH;

        if (wrapped.length < notDataLength) { 
        	// Bail out
            return wrapped;
        }
        
        if (!check(wrapped)) {
        	throw new MsCrc32Exception();
        }

        byte[] naked = new byte[wrapped.length - notDataLength];
        System.arraycopy(wrapped, 3, naked, 0, wrapped.length - notDataLength);
        return naked;
    }
    
    @Override
    public int getWrappedResponseLength(int length) {
    	return PAYLOAD_LENGTH + TYPE_LENGTH + CRC32_LENGTH + length;
    }
    
    /**
     * Checks if a wrapped array of bytes is valid based on CRC-32.
     * 
     * @param wrapped the wrapped bytes
     *
     * @return true if the checksum checks out
     */
    public boolean check(byte[] wrapped) {

        // The type is included in the CRC calculation
        int notDataLength = PAYLOAD_LENGTH + CRC32_LENGTH;

        // Not enough data to do a check
        if (wrapped.length < notDataLength)
        {
            return true;
        }

        // Extract crc32
        byte[] crc32 = new byte[CRC32_LENGTH];
        System.arraycopy(wrapped, wrapped.length - CRC32_LENGTH, crc32, 0, CRC32_LENGTH); // Copy crc32 from wrapped into crc32 bytes array

        // Remove payload size, only keep the data
        byte[] data = new byte[wrapped.length - notDataLength];
        System.arraycopy(wrapped, 2, data, 0, wrapped.length - notDataLength);

        // Generate CRC32 on data
        CRC32 check = new CRC32();
        check.update(data);

        long crc32value = check.getValue();
        // Copy the value into a byte buffer as sign changes can cause weirdness, just dodge that bullet.
        byte[] crcBytes = new byte[4];
        crcBytes[0] = (byte) ((crc32value >> 24) & 0xff);
        crcBytes[1] = (byte) ((crc32value >> 16) & 0xff);
        crcBytes[2] = (byte) ((crc32value >> 8) & 0xff);
        crcBytes[3] = (byte) ((crc32value >> 0) & 0xff);

        // Compare crc32 we generated from the data with what we got to see if it match
        if (crc32[0] == crcBytes[0] && crc32[1] == crcBytes[1] && crc32[2] == crcBytes[2] && crc32[3] == crcBytes[3])
        {
            return true;
        }

        if (LOG.isDebugEnabled()) {
        	StringBuilder builder = new StringBuilder();
        	builder.append("CRC32 mismatch from MS3!: ")
        			.append(Arrays.toString(wrapped)).append("/r/n")
        			.append("CRC32 mismatch crc32: ").append(crc32[0]).append(" ==? ").append(crcBytes[0])
        			.append("CRC32 mismatch crc32: ").append(crc32[1]).append(" ==? ").append(crcBytes[1])
        			.append("CRC32 mismatch crc32: ").append(crc32[2]).append(" ==? ").append(crcBytes[2])
        			.append("CRC32 mismatch crc32: ").append(crc32[3]).append(" ==? ").append(crcBytes[3]);
        	
        }
        
        return false;
    }
}
