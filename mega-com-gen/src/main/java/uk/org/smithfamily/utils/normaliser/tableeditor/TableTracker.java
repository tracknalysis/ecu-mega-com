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
package uk.org.smithfamily.utils.normaliser.tableeditor;

import java.util.ArrayList;
import java.util.List;

public class TableTracker
{
    private String name;
    private List<TableItem> items = new ArrayList<TableItem>();
    private int interestingItemCount = 0;
    private int curlyBracketCount = 0;

    public void addItem(TableItem x)
    {
        items.add(x);

        if (!(x instanceof PreProcessor))
        {
            interestingItemCount++;
        } else
        {
            if (x.toString().contains("{"))
            {
                curlyBracketCount++;
            }
            if (x.toString().contains("}"))
            {
                curlyBracketCount--;
            }
        }
    }

    public List<TableItem> getItems()
    {
        return items;
    }

    public boolean isDefinitionCompleted()
    {
        return (interestingItemCount >= 4 && curlyBracketCount == 0);
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
