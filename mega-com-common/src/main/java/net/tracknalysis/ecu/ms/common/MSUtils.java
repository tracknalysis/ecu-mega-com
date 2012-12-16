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

import java.util.List;

/**
 * Megasquirt utilities class heavily used in the ECU definition Java class.  Modified from
 * original in MSLogger to contain only Megasquirt related code and to be a utility class.
 */
public final class MSUtils {

	/**
	 * Hidden in utility class.
	 */
	private MSUtils() {
	}

	/**
	 * Get the long for the specified index in the buffer
	 * 
	 * @param ochBuffer
	 * @param i
	 * @return
	 */
	public static int getLong(byte[] ochBuffer, int i) {
		return getWord(ochBuffer, i) * 65536 + getWord(ochBuffer, i + 2);
	}

	/**
	 * Get the word for the specified index in the buffer
	 * 
	 * @param ochBuffer
	 * @param i
	 * @return
	 */
	public static int getWord(byte[] ochBuffer, int i) {
		return (ochBuffer[i] << 8) | getByte(ochBuffer, i + 1);
	}

	/**
	 * 
	 * Get the byte for the specified index in the buffer
	 * 
	 * @param ochBuffer
	 * @param i
	 * @return
	 */
	public static int getByte(byte[] ochBuffer, int i) {
		return (int) ochBuffer[i] & 0xFF;
	}

	/**
	 * Get the signed long for the specified index in the buffer
	 * 
	 * @param ochBuffer
	 * @param i
	 * @return
	 */
	public static int getSignedLong(byte[] ochBuffer, int i) {
		int x = getLong(ochBuffer, i);
		if (x > 2 << 32 - 1) {
			x = 2 << 32 - x;
		}
		return x;
	}

	/**
	 * Get the signed byte for the specified index in the buffer
	 * 
	 * @param ochBuffer
	 * @param i
	 * @return
	 */
	public static int getSignedByte(byte[] ochBuffer, int i) {
		int x = getByte(ochBuffer, i);
		if (x > 127) {
			x = 256 - x;
		}
		return x;
	}

	/**
	 * Get the signed word for the specified index in the buffer
	 * 
	 * @param ochBuffer
	 * @param i
	 * @return
	 */
	public static int getSignedWord(byte[] ochBuffer, int i) {
		int x = getWord(ochBuffer, i);
		if (x > 32767) {
			x = 32768 - x;
		}
		return x;
	}

	/**
	 * Get bits at the specified index for the page buffer
	 * 
	 * @param pageBuffer
	 *            Page buffer of data
	 * @param i
	 *            Index where the value is
	 * @param _bitLo
	 * @param _bitHi
	 * @param bitOffset
	 * @return
	 */
	public static int getBits(byte[] pageBuffer, int i, int _bitLo, int _bitHi,
			int bitOffset) {
		int val = 0;
		byte b = pageBuffer[i];

		long mask = ((1 << (_bitHi - _bitLo + 1)) - 1) << _bitLo;
		val = (int) ((b & mask) >> _bitLo) + bitOffset;

		return val;
	}

	/**
	 * Take an MS command like this one 119,0,4,0,20,0,2,0,250 (to set cranking
	 * RPM to 250rpm on MS2Extra) and convert it to byte array that can be used
	 * with Megasquirt.writeCommand()
	 * 
	 * @param command
	 *            The command to convert to byte array
	 * @return A byte array with every byte of the command
	 */
	public static byte[] commandStringtoByteArray(String command) {
		String[] split = command.split(",");

		byte[] bytes = new byte[split.length];

		for (int i = 0; i < split.length; i++) {
			bytes[i] = Byte.valueOf(split[i]);
		}

		return bytes;
	}

	/**
	 * Round a double number to a specific number of decimals
	 * 
	 * @param number
	 *            The number to round
	 * @param decimals
	 *            The number of decimals to keep
	 * 
	 * @return The rounded number
	 */
	public static double roundDouble(double number, int decimals) {
		double p = (double) Math.pow(10, decimals);
		number = number * p;
		double tmp = Math.round(number);
		return tmp / p;
	}
	
static String digits = "0123456789abcdef";
    
    /**
     * Method that return the width and height of an array from it's shape (from the INI)
     * 
     * @param shape Shape defined in the INI
     * @return width and height
     */
    public static int[] getArraySize(String shape)
    {
        String arraySpec = shape.replace("[", "").replace("]", "");
        String[] sizes = arraySpec.split("x");
        int width = Integer.parseInt(sizes[0].trim());
        int height = sizes.length == 2 ? Integer.parseInt(sizes[1].trim()) : -1;
        
        int[] size = { width, height };
        
        return size;
    }
    
    /**
     * Used with pageReadCommand, pageValueWrite and pageChunkWrite to translate the ini command to a command the MegaSquirt ECU will understand
     * 
     * @param listPageCommand The list of page command
     * @param stringToConvert The page command to translate
     * @param offset The offset (often represented by "%o" in the page command)
     * @param count The count (often represented by "%c" in the page command)
     * @param value The value(s) (often represented by "%v" in the page command)
     * @param pageNo The page number
     * 
     * @return The command to send to the MegaSquirt with the place holder replaced
     */
    public static String hexStringToBytes(List<String> listPageCommand, String stringToConvert, int offset, int count, int[] value, int pageNo)
    {
        String ret = "";
        boolean first = true;
        stringToConvert = stringToConvert.replace("$tsCanId", "x00");
        for (int positionInString = 0; positionInString < stringToConvert.length(); positionInString++)
        {
            if (!first)
            {
                ret += ",";
            }
            
            char currentCharacter = stringToConvert.charAt(positionInString);
            switch (currentCharacter)
            {
            case '\\':
                positionInString++;
                currentCharacter = stringToConvert.charAt(positionInString);
                if (currentCharacter == '0')
                {
                    ret += OctalByteToDec(stringToConvert.substring(positionInString));
                }
                else
                {
                    ret += HexByteToDec(stringToConvert.substring(positionInString));
                }
                positionInString = positionInString + 2;
                break;

            case '%':
                positionInString++;
                currentCharacter = stringToConvert.charAt(positionInString);

                if (currentCharacter == '2')
                {
                    positionInString++;
                    currentCharacter = stringToConvert.charAt(positionInString);
                    if (currentCharacter == 'o')
                    {
                        ret += bytes(offset);
                    }
                    else if (currentCharacter == 'c')
                    {
                        ret += bytes(count);
                    }
                    else if (currentCharacter == 'i')
                    {
                        String identifier = listPageCommand.get(pageNo - 1);
    
                        ret += hexStringToBytes(listPageCommand, identifier, offset, count, value, pageNo);
                    }
                }
                // MS1
                else if (currentCharacter == 'o')
                {
                    ret += bytes(offset);
                }
                // MS1
                else if (currentCharacter == 'c')
                {
                    ret += bytes(count);
                }
                else if (currentCharacter == 'v')
                {
                    // Loop over all the values we received
                    for (int i = 0; i < value.length; i++)
                    {
                        ret += bytes(value[i]) + ",";
                    }
                }
                
                break;

            default:
                ret += Byte.toString((byte) currentCharacter);
                break;
            }
            first = false;
        }
        return ret;
    }
    
    public static String HexStringToBytes(String stringToConvert)
    {
        String ret = "";
        boolean first = true;
        stringToConvert = stringToConvert.replace("$tsCanId", "x00");
        for (int positionInString = 0; positionInString < stringToConvert.length(); positionInString++)
        {
            if (!first)
            {
                ret += ",";
            }
            
            char currentCharacter = stringToConvert.charAt(positionInString);
            switch (currentCharacter)
            {
            case '\\':
                positionInString++;
                currentCharacter = stringToConvert.charAt(positionInString);
                if (currentCharacter == '0')
                {
                    ret += OctalByteToDec(stringToConvert.substring(positionInString));
                }
                else
                {
                    ret += bytes(HexByteToDec(stringToConvert.substring(positionInString)));
                }
                positionInString = positionInString + 2;
                break;
            
            default:
                ret += Byte.toString((byte) currentCharacter);
                break;
            }
            first = false;
        }
        
        return ret;
    }
    
    /**
     * Convert a string from hexadecimal to decimal
     * 
     * @param s String containing hexadecimal
     * @return The decimal in integer type
     */
    public static int HexByteToDec(String s)
    {
        int i = 0;
        char c = s.charAt(i++);
        assert c == 'x';
        c = s.charAt(i++);
        c = Character.toLowerCase(c);
        int val = 0;
        int digit = digits.indexOf(c);
        val = digit * 16;
        c = s.charAt(i++);
        c = Character.toLowerCase(c);
        digit = digits.indexOf(c);
        val = val + digit;
        return val;
    }
    
    /**
     * Convert a string from octal to decimal
     * 
     * @param s String containing octal
     * @return The decimal in integer type
     */
    public static int OctalByteToDec(String s)
    {
        int i = 0;
        char c = s.charAt(i++);
        assert c == '0';
        c = s.charAt(i++);
        c = Character.toLowerCase(c);
        int val = 0;
        int digit = digits.indexOf(c);
        val = digit * 8;
        c = s.charAt(i++);
        c = Character.toLowerCase(c);
        digit = digits.indexOf(c);
        val = val + digit;
        return val;
    }
    
    /**
     * Take a value and return its hi, low representation
     * 
     * @param val Value to convert
     * @return hi, low representation
     */
    private static String bytes(int val)
    {
        int hi = val / 256;
        int low = val % 256;
        if (hi > 127)
            hi -= 256;
        if (low > 127)
            low -= 256;
        
        return "" + hi + "," + low;
    }
}
