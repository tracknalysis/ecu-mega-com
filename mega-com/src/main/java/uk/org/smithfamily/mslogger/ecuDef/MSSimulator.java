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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uk.org.smithfamily.mslogger.ApplicationSettings;
import android.content.res.AssetManager;

/**
 * Replays a captured log file for testing purposes
 */
public enum MSSimulator
{
	INSTANCE;
	private final byte[]	fileFormat		= { 0x46, 0x52, 0x44, 0x00, 0x00, 0x00 };
	private final byte[]	formatVersion	= { 0x00, 0x01 };

	private final byte[]	timeStamp		= { 0, 0, 0, 0 };
	private final byte[]	firmware		= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private final byte[]	beginIndex		= { 0, 0, 0, 81 };
	private final byte[]	outputLength	= { 0, 0 };
	private int				blockSize		= 0;
	private List<byte[]>	records			= new ArrayList<byte[]>();
	private int record_pointer = 0;
	
	
	/**
	 * 
	 */
	MSSimulator()
	{
		AssetManager assetManager = ApplicationSettings.INSTANCE.getContext().getResources().getAssets();

		InputStream input = null;
		try
		{
			input = assetManager.open("test/20111115184701.MS1HR.frd");
			input.read(fileFormat);
			input.read(formatVersion);
			input.read(timeStamp);
			input.read(firmware);
			input.read(beginIndex);
			input.read(outputLength);
			blockSize = (outputLength[0] * 256 + outputLength[1]);
			byte[] buffer = new byte[blockSize + 2];
			while ((input.read(buffer)) != -1)
			{
				byte[] record = new byte[blockSize];
				System.arraycopy(buffer, 2, record, 0, blockSize);
				records.add(record);
			}
		}
		catch (IOException e)
		{

		}

	}

	/**
	 * 
	 * @param ochBuffer
	 */
	public void getNextRTV(byte[] ochBuffer)
	{
		if(record_pointer >= records.size() )
		{
			record_pointer = 0;
		}
		byte[] record = records.get(record_pointer++);
		System.arraycopy(record, 0, ochBuffer, 0, ochBuffer.length);
	}

}
