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
package uk.org.smithfamily.utils.normaliser.curveeditor;

public class CurveXBins extends CurveItem
{
    private String readOnly;
    private String bins1;
    private String bins2;

    public CurveXBins(String bins1, String bins2, String readonly)
    {
        this.bins1 = bins1;
        this.bins2 = bins2;
        this.readOnly = (readonly == null ? "false" : readonly.equals("readonly") ? "true" : "false");
    }

    public String getReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(String readOnly)
    {
        this.readOnly = readOnly;
    }

    public String getBins2()
    {
        return bins2;
    }

    public void setBins2(String bins2)
    {
        this.bins2 = bins2;
    }

    public String getBins1()
    {
        return bins1;
    }

    public void setBins1(String bins1)
    {
        this.bins1 = bins1;
    }

    @Override
    public String toString()
    {
        return String.format("c.setXBins(%s,\"%s\",%s,%s);", bins1, bins1, bins2, readOnly);
    }
}
