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
package net.tracknalysis.ecu.ms.normaliser.curveeditor;

import java.util.ArrayList;
import java.util.List;

public class CurveTracker
{
    private String          name;
    private List<CurveItem> items                = new ArrayList<CurveItem>();
    private int             interestingItemCount = 0;
    private int             curlyBracketCount    = 0;

    public void addItem(CurveItem x)
    {
        items.add(x);

        if (!(x instanceof CurvePreProcessor))
        {
            interestingItemCount++;
        }
        else
        {
            if(x.toString().contains("{"))
            {
                curlyBracketCount++;
            }
            if(x.toString().contains("}"))
            {
                curlyBracketCount--;
            }
        }
    }

    public List<CurveItem> getItems()
    {
        return items;
    }

    public boolean isDefinitionCompleted()
    {
        return (interestingItemCount >= 5 && curlyBracketCount == 0);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
