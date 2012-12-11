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
package uk.org.smithfamily.mslogger.ecuDef;

public interface MSUtilsInterface
{
    public int getLong(byte[] ochBuffer, int i);

    public int getWord(byte[] ochBuffer, int i);

    public int getByte(byte[] ochBuffer, int i);

    public int getSignedLong(byte[] ochBuffer, int i);

    public int getSignedByte(byte[] ochBuffer, int i);

    public int getSignedWord(byte[] ochBuffer, int i);

    public int getBits(byte[] pageBuffer, int i, int _bitLo, int _bitHi, int j);

    public double getLatitude();

    public double getLongitude();

    public double getSpeed();

    public double getBearing();

    public double getAccuracy();

    public long getTime();

    public String getLocationLogHeader();

    public String getLocationLogRow();

}
