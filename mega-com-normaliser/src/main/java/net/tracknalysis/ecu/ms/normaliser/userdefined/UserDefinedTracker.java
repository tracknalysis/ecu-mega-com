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
package net.tracknalysis.ecu.ms.normaliser.userdefined;

import java.util.ArrayList;
import java.util.List;

public class UserDefinedTracker
{
	private List<UserDefinedItem> items = new ArrayList<UserDefinedItem>();
	private String name;

	public void addItem(UserDefinedItem x)
	{
		items.add(x);
	}

	public List<UserDefinedItem> getItems()
	{
		return items;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
