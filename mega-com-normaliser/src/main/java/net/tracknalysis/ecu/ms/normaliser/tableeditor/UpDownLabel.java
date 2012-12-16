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
package net.tracknalysis.ecu.ms.normaliser.tableeditor;

public class UpDownLabel extends TableItem
{
	private String down;
	private String up;

	public UpDownLabel(String up, String down)
	{
		this.up = up;
		this.down = down;
	}

	public String getDown()
	{
		return down;
	}

	public void setDown(String down)
	{
		this.down = down;
	}

	public String getUp()
	{
		return up;
	}

	public void setUp(String up)
	{
		this.up = up;
	}

	public String toString()
	{
	    return String.format("t.setUpDownLabel(\"%s\",\"%s\");",up,down);
	}
}
