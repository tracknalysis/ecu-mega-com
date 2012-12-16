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
package net.tracknalysis.ecu.ms.common;

import java.io.IOException;

/**
 * Interface describing the low level operations that occur on a Megasquirt ECU.
 */
public interface MsController {
    
	/**
	 * Returns if a flag has been set in the configuration for the Megasquirt
	 * controller.
	 * 
	 * @param name
	 *            the name of the flag
	 * @return true if the flag is set
	 */
    boolean isSet(String string);

    byte[] loadPage(int i, int j, int k, byte[] bs, byte[] bs2) throws IOException;

    /**
     * Load a byte vector contained in pageBuffer from the specified offset and width
     * 
     * @param pageBuffer The buffer where the byte vector is located
     * @param offset The offset where the byte vector is located
     * @param width The width of the byte vector
     * @param signed Is the data signed ?
     * 
     * @return
     */
    int[] loadByteVector(byte[] pageBuffer, int offset, int width, boolean signed);

    /**
     * Load a byte array contained in pageBuffer from the specified offset and width
     * 
     * @param pageBuffer The buffer where the byte array is located
     * @param offset The offset where the byte array is located
     * @param width The width of the byte array
     * @param height The height of the byte array
     * @param signed Is the data signed ?
     * 
     * @return
     */
    int[][] loadByteArray(byte[] pageBuffer, int offset, int width, int height, boolean signed);

    /**
     * Load a word vector contained in pageBuffer from the specified offset and width
     * 
     * @param pageBuffer The buffer where the word vector is located
     * @param offset The offset where the word vector is located
     * @param width The width of the word vector
     * @param signed Is the data signed ?
     * 
     * @return
     */
    int[] loadWordVector(byte[] pageBuffer, int offset, int width, boolean signed);

    /**
     * Load a word array contained in pageBuffer from the specified offset and width
     * 
     * @param pageBuffer The buffer where the word array is located
     * @param offset The offset where the word array is located
     * @param width The width of the word array
     * @param height The height of the word array
     * @param signed Is the data signed ?
     * 
     * @return
     */
    int[][] loadWordArray(byte[] pageBuffer, int offset, int width, int height, boolean signed);

    double round(double x);

    /**
	 * Shortcut function to access data tables. Makes the INI->Java translation
	 * a little simpler.
	 * 
	 * @param i1
	 *            index into table
	 * @param name
	 *            table name
	 * @return value from table
	 */
    int table(double x, String t);

    /**
     * @return the difference between the current time and logging start time in
     * seconds.
     */
    double timeNow();

    /**
     * Converts a temperature in Fahrenheit to Celcius if units is in Celcius.
     * 
     * @param t
     *            the input temperature in Fahrenheit
     * 
     * @return {@code t} if configured for Fahrenheit, otherwise the Celcious
     *         equivalent of {@code t}
     */
    double tempCvt(double x);
}
