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
package uk.org.smithfamily.utils.normaliser.menu;

public class SubMenuDefinition extends MenuItem
{
    private String label = "";
    private String name = "";
    private String randomNumber = "0";
    
    public SubMenuDefinition(String name, String label, String randomNumber)
    {
        this.name = name;
        if (label != null) label = label.replace("&", "");
        this.label = label;
        this.randomNumber = randomNumber;
    }
    
    public String getLabel()
    {
        return label;
    }
    
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public String getRandomNumber()
    {
        return randomNumber;
    }

    public void setRandomNumber(String randomNumber)
    {
        this.randomNumber = randomNumber;
    }
    
    @Override
    public String toString()
    {
        return String.format("m.addSubMenu(new SubMenu(\"%s\",\"%s\",\"%s\"));", name, label, randomNumber);
    }
}
