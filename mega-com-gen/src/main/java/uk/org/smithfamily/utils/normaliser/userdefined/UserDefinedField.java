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

public class UserDefinedField extends UserDefinedItem
{

	private String label;
	private String name;
	private boolean displayOnly;
	private boolean commandButton;
	private String commandOnClose;

	public UserDefinedField(String label, String name, boolean displayOnly, boolean commandButton, String commandOnClose)
    {
        this.label = label;
        this.name = name;
        this.displayOnly = displayOnly;
        this.commandButton = commandButton;
        this.commandOnClose = commandOnClose;
    }

	public String getLabel()
	{
		return label;
	}

	public String getName()
	{
		return name;
	}

	public boolean isDisplayOnly()
	{
		return displayOnly;
	}
	
    public void setDisplayOnly(boolean displayOnly)
    {
        this.displayOnly = displayOnly;
    }

    public String getCommandOnClose()
    {
        return commandOnClose;
    }

    public void setCommandOnClose(String commandOnClose)
    {
        this.commandOnClose = commandOnClose;
    }
    
	@Override
	public String toString()
	{
		return String.format("d.addField(new DialogField(\"%s\",\"%s\",%s,%s,\"%s\"));", label, name, displayOnly, commandButton, commandOnClose);
	}
}
