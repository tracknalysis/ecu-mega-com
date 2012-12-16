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

public class GridOrient extends TableItem
{
	private String x;
	private String y;
	private String z;

	public GridOrient(String x,String y, String z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String getX()
	{
		return x;
	}

	public void setX(String x)
	{
		this.x = x;
	}

	public String getY()
	{
		return y;
	}

	public void setY(String y)
	{
		this.y = y;
	}

	public String getZ()
	{
		return z;
	}

	public void setZ(String z)
	{
		this.z = z;
	}

	@Override
	public String toString()
	{
	    return String.format("t.setGridOrient(%s,%s,%s);",x,y,z);
	}
}
