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
package net.tracknalysis.ecu.ms.normaliser.menu;

import java.util.ArrayList;
import java.util.List;



public class MenuDefinition extends MenuItem
{
    private String dialog = "";
    private String label = "";
    private List<SubMenuDefinition> subMenus = new ArrayList<SubMenuDefinition>();
    
    public MenuDefinition(String dialog, String label)
    {
        this.dialog = dialog;
        if (label != null) label = label.replace("&", "");
        this.label = label;
    }

    public String getDialog()
    {
        return dialog;
    }

    public void setDialog(String dialog)
    {
        this.dialog = dialog;
    }
    
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        String output = String.format("m = new Menu(\"%s\",\"%s\");\n", dialog, label);
        
        for (SubMenuDefinition sub : subMenus)
        {
            output += sub.toString();
        }
        
        return output;
    }
}
