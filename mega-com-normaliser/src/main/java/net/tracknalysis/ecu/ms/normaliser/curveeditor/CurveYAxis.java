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

public class CurveYAxis  extends CurveItem
{
    private String yAxis1;
    private String yAxis2;
    private String yAxis3;

    public CurveYAxis(String xAxis1, String xAxis2, String xAxis3)
    {
        this.yAxis1 = xAxis1;
        this.yAxis2 = xAxis2;
        this.yAxis3 = xAxis3;
    }

    public String getxAxis1()
    {
        return yAxis1;
    }

    public void setxAxis1(String xAxis1)
    {
        this.yAxis1 = xAxis1;
    }

    public String getxAxis2()
    {
        return yAxis2;
    }

    public void setxAxis2(String xAxis2)
    {
        this.yAxis2 = xAxis2;
    }

    public String getxAxis3()
    {
        return yAxis3;
    }

    public void setxAxis3(String xAxis3)
    {
        this.yAxis3 = xAxis3;
    }

    @Override
    public String toString()
    {
        return String.format("c.setyAxis(new double[] {%s,%s,%s});",yAxis1,yAxis2,yAxis3);
    }  
}
