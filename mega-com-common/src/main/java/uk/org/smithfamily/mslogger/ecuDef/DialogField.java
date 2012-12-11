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

public class DialogField
{
    private String label = "";
    private String name = "";
    
    private boolean displayOnly = false;
    private boolean commandButton = false;
    
    private String commandOnClose;
    
    public DialogField(String label, String name, boolean displayOnly, boolean commandButton, String commandOnClose)
    {
        this.label = label;
        this.name = name;
        this.displayOnly = displayOnly;
        this.commandButton = commandButton;
        this.commandOnClose = commandOnClose;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isDisplayOnly() {
        return displayOnly;
    }
    
    public void setDisplayOnly(boolean displayOnly) {
        this.displayOnly = displayOnly;
    }
    
    public boolean isCommandButton()
    {
        return commandButton;
    }

    public void setCommandButton(boolean commandButton)
    {
        this.commandButton = commandButton;
    }

    public String getCommandOnClose()
    {
        return commandOnClose;
    }

    public void setCommandOnClose(String commandOnClose)
    {
        this.commandOnClose = commandOnClose;
    }
    
    public String generateCode()
    {
        return String.format("        d.addField(new DialogField(\"%s\",\"%s\",%s,%s,\"%s\"));\n", label, name, displayOnly, commandButton, commandOnClose);
    }
    
    @Override
    public String toString()
    {
        return "DialogField [label=" + label + ", name=" + name + ", displayOnly=" + displayOnly + ", commandButton=" + commandButton + ", commandOnClose=" + commandOnClose + "]";
    }
}
