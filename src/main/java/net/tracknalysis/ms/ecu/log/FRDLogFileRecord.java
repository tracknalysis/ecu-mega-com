/**
 * Copyright 2011 the original author or authors.
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
package net.tracknalysis.ms.ecu.log;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author David Smith
 * @author David Valeri
 */
public class FRDLogFileRecord {
    private byte[] buffer;
    private FRDLogFileBody body;

    public FRDLogFileRecord(FRDLogFileBody body, byte[] ochBuffer) {
        this.body = body;
        buffer = new byte[ochBuffer.length + 2];
        buffer[0] = 1;
        buffer[1] = (byte) (body.outpc++);
        System.arraycopy(ochBuffer, 0, buffer, 2, ochBuffer.length);

    }

    public FRDLogFileRecord(FRDLogFileBody body, FileInputStream is)
            throws IOException {
        this.body = body;
        int blockSize = body.getParent().getHeader().getBlockSize();
        buffer = new byte[blockSize + 2];
        is.read(buffer, 0, blockSize + 2);
    }

    public byte[] getBytes() {
        return buffer;
    }

    public byte[] getOchBuffer() {
        int blockSize = body.getParent().getHeader().getBlockSize();
        byte[] ochBuffer = new byte[blockSize];
        System.arraycopy(buffer, 2, ochBuffer, 0, blockSize);

        return ochBuffer;
    }
}
