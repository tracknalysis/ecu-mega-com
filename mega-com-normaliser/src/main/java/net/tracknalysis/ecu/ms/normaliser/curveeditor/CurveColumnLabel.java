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

public class CurveColumnLabel extends CurveItem
{
    private String xLabel;
    private String yLabel;

    public CurveColumnLabel(String xLabel, String yLabel)
    {
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }
    
    public String getxLabel()
    {
        return xLabel;
    }

    public void setxLabel(String xLabel)
    {
        this.xLabel = xLabel;
    }

    public String getyLabel()
    {
        return yLabel;
    }

    public void setyLabel(String yLabel)
    {
        this.yLabel = yLabel;
    }
    
    public String toString()
    {
        return String.format("c.setxLabel(\"%s\");c.setyLabel(\"%s\");",xLabel,yLabel);
    }
}
