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

import java.util.ArrayList;
import java.util.List;

public class MSDialog
{
    private String name = "";
    private String label = "";
    private String axis = "";
    
    private List<DialogField> fieldsList = new ArrayList<DialogField>();
    private List<DialogPanel> panelsList = new ArrayList<DialogPanel>();

    public MSDialog(String name, String label, String axis)
    {
        this.name = name;
        this.label = label;
        this.axis = axis;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getAxis()
    {
        return axis;
    }

    public void setAxis(String axis)
    {
        this.axis = axis;
    }

    public List<DialogField> getFieldsList() {
        return fieldsList;
    }
    
    public void addField(DialogField field) {
        this.fieldsList.add(field);
    }
    
    public List<DialogPanel> getPanelsList() {
        return panelsList;
    }
    
    public void addPanel(DialogPanel panel) {
        this.panelsList.add(panel);
    }
    
    @Override
    public String toString()
    {
        return "MSDialog [name=" + name + ", label=" + label + ", axis=" + axis + ", fieldsList=" + fieldsList + ", panelsList=" + panelsList + "]";
    }
}
