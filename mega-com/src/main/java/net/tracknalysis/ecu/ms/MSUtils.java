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
package net.tracknalysis.ecu.ms;

/**
 * @author David Smith
 */
public class MSUtils
{
    /**
     * Hidden on utility class.
     */
    private MSUtils() {
    }
    
    public static int getLong(byte[] ochBuffer, int i)
    {
        return getWord(ochBuffer, i) * 65536 + getWord(ochBuffer, i + 2);
    }

    public static int getWord(byte[] ochBuffer, int i)
    {

        return (getByte(ochBuffer,i) * 256 + getByte(ochBuffer,i+1));
    }

    public static int getByte(byte[] ochBuffer, int i)
    {
        return (int) ochBuffer[i] & 0xFF;
    }

    public static int getSignedLong(byte[] ochBuffer, int i)
    {
        int x = getLong(ochBuffer, i);
        if (x > 2 << 32 - 1)
        {
            x = 2 << 32 - x;
        }
        return x;
    }

    public static int getSignedByte(byte[] ochBuffer, int i)
    {

        int x = getByte(ochBuffer, i);
        if (x > 127)
        {
            x = 256 - x;
        }
        return x;
    }

    public static int getSignedWord(byte[] ochBuffer, int i)
    {
        int x = getWord(ochBuffer, i);
        if (x > 32767)
        {
            x = 32768 - x;
        }
        return x;
    }
    public static int getBits(byte[] pageBuffer, int i, int _bitLo, int _bitHi,int bitOffset)
    {
        int val = 0;
        byte b = pageBuffer[i];

        long mask = ((1 << (_bitHi - _bitLo + 1)) - 1) << _bitLo;
        val = (int) ((b & mask) >> _bitLo)+bitOffset;

        return val;
    }
}
