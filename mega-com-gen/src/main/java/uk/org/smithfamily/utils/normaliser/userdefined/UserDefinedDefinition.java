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
package uk.org.smithfamily.utils.normaliser.userdefined;

public class UserDefinedDefinition extends UserDefinedItem
{

	private String name;
	private String label;
	private String axis;

	public UserDefinedDefinition(UserDefinedTracker parent, String name, String label, String axis)
	{
		this.name = name;
		this.label = label;
		this.axis = axis;
		parent.setName(name);
	}

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}
	
	public String getAxis()
	{
	    return axis;
	}
	
	@Override
	public String toString()
	{
        return String.format("d = new MSDialog(\"%s\",\"%s\",\"%s\"); dialogs.put(\"%s\",d);", name, label, axis, name);
	}
}
