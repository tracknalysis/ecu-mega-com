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

public class CurveXAxis extends CurveItem
{
    private String xAxis1;
    private String xAxis2;
    private String xAxis3;

    public CurveXAxis(String xAxis1, String xAxis2, String xAxis3)
    {
    	this.xAxis1 = xAxis1;
        this.xAxis2 = xAxis2;
        this.xAxis3 = xAxis3;
    }

    public String getxAxis1()
    {
        return xAxis1;
    }
    public String getxAxis2()
    {
        return xAxis2;
    }

    public String getxAxis3()
    {
        return xAxis3;
    }
    @Override
    public String toString()
    {
        return String.format("c.setxAxis(new double[] {%s,%s,%s});",xAxis1,xAxis2,xAxis3);
    }  
}
