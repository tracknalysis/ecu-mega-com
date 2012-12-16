/**
 * Copyright 2011, 2012 David Smith.
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

import java.io.FileInputStream;
import java.io.IOException;

import net.tracknalysis.ecu.ms.Megasquirt;


/**
 * Adaptation of the original code from MSLogger.
 *
 * @author David Smith
 * @author David Valeri
 */
public class FRDLogFileHeader {

    private final byte[] fileFormat = { 0x46, 0x52, 0x44, 0x00, 0x00, 0x00 };
    private final byte[] formatVersion = { 0x00, 0x01 };

    private final byte[] timeStamp = { 0, 0, 0, 0 };
    private final byte[] firmware = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0 };
    private final byte[] beginIndex = { 0, 0, 0, 81 };
    private final byte[] outputLength = { 0, 0 };

    private FRDLogFile parent;

    private int blockSize;

    public FRDLogFileHeader(FRDLogFile frdLogFile, Megasquirt ms) {
        this.parent = frdLogFile;
        String sig = "NOECU";
        if (ms != null) {
            sig = ms.getTrueSignature();
            blockSize = ms.getBlockSize();
        }
        System.arraycopy(sig.getBytes(), 0, firmware, 0, sig.length());

        int now = (int) (System.currentTimeMillis() / 1000l);
        timeStamp[0] = (byte) (now >> 24);
        timeStamp[1] = (byte) (now >> 16);
        timeStamp[2] = (byte) (now >> 8);
        timeStamp[3] = (byte) (now);

        outputLength[0] = (byte) (blockSize >> 8);
        outputLength[1] = (byte) (blockSize);

    }

    public FRDLogFileHeader(FRDLogFile frdLogFile, FileInputStream is)
            throws IOException {
        this.parent = frdLogFile;
        is.read(fileFormat);
        is.read(formatVersion);
        is.read(timeStamp);
        is.read(firmware);
        is.read(beginIndex);
        is.read(outputLength);
        blockSize = outputLength[0] << 8 + outputLength[1];
    }

    public byte[] getHeaderRecord() {
        byte[] result = concatAll(fileFormat, formatVersion, timeStamp,
                firmware, beginIndex, outputLength);

        return result;

    }

    public byte[] getFileformat() {
        return fileFormat;
    }

    public byte[] getFormatversion() {
        return formatVersion;
    }

    public byte[] getTimeStamp() {
        return timeStamp;
    }

    public byte[] getFirmware() {
        return firmware;
    }

    public byte[] getBeginindex() {
        return beginIndex;
    }

    public byte[] getOutputlength() {
        return outputLength;
    }

    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        System.arraycopy(first, 0, result, 0, first.length);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public FRDLogFile getParent() {
        return parent;
    }

    public int getBlockSize() {
        return blockSize;
    }

}
