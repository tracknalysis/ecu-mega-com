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
package net.tracknalysis.ecu.ms.common;

public class Constant
{
    private int        digits;
    private String     high;
    private int        page;
    private String     classType;
    private String     type;
    private int        offset;
    private String     shape;
    private String     units;
    private double     scale;
    private double     translate;
    private String     low;
    private String     name;
    private String[]   values;
    
    private boolean    modified;

    public Constant(int page, String name, String classType, String type, int offset, String shape, String units, double scale,
            double translate, double low, double high, int digits, String[] values)
    {
        this.page = page;
        this.name = name;
        this.classType = classType;
        this.type = type;
        this.offset = offset;
        this.shape = shape;
        this.units = units;
        this.scale = scale;
        this.translate = translate;
        this.low = Double.toString(low);
        this.high = Double.toString(high);
        this.digits = digits;
        this.values = values;
    }
	
    public Constant(int page, String name, String classType, String type, int offset, String shape, String units, double scale,
            double translate, String low, String high, int digits, String[] values)
    {
        this.page = page;
        this.name = name;
        this.classType = classType;
        this.type = type;
        this.offset = offset;
        this.shape = shape;
        this.units = units;
        this.scale = scale;
        this.translate = translate;
        if (low == null || low.trim().equals(""))
        {
            low = "0";
        }
        this.low = low;
        if (high == null || high.trim().equals(""))
        {
            high="0";
        }
        this.high = high;
        this.digits = digits;
        this.values = values;
    }
    
    public Constant(int page, String name, String classType, String type, int offset, String shape, String units, double scale,
            double translate, String low, String high, int digits)
    {
        this.page = page;
        this.name = name;
        this.classType = classType;
        this.type = type;
        this.offset = offset;
        this.shape = shape;
        this.units = units;
        this.scale = scale;
        this.translate = translate;
        if (low == null || low.trim().equals(""))
        {
        	low = "0";
        }
        this.low = low;
        if (high == null || high.trim().equals(""))
        {
            high="0";
        }
        this.high = high;
        this.digits = digits;
    }
    
    public Constant(int page, String name, String classType, String type, int offset, String shape, String units, double scale,
            double translate, double low, double high, int digits)
    {
        this.page = page;
        this.name = name;
        this.classType = classType;
        this.type = type;
        this.offset = offset;
        this.shape = shape;
        this.units = units;
        this.scale = scale;
        this.translate = translate;
        this.low = Double.toString(low);
        this.high = Double.toString(high);
        this.digits = digits;
    }

	public int getDigits()
	{
		return digits;
	}

	public String getHigh()
	{
		return high;
	}

	public int getPage()
	{
		return page;
	}

	public String getClassType()
	{
		return classType;
	}

	public String getType()
	{
		return type;
	}

	public int getOffset()
	{
		return offset;
	}

	public String getShape()
	{
		return shape;
	}

	public String getUnits()
	{
		return units;
	}

	public double getScale()
	{
		return scale;
	}

	public double getTranslate()
	{
		return translate;
	}

	public String getLow()
	{
		return low;
	}

	public String getName()
	{
		return name;
	}
	
	public String[] getValues()
	{
	    return values;
	}
	
    @Override
    public String toString()
    {
        String valuesOutput = "";
        
        if (values != null)
        {
            for (int i = 0; i < values.length; i++)
            {
                valuesOutput += values[i];

                if (i < values.length - 1)
                {
                    valuesOutput += ",";
                }
            }
        }
      
        return String.format("Constant(%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",%f,%f,%s,%s,%d,new String[] {%s})", 
                page ,
                name,
                classType,
                type,
                offset,
                shape,
                units,
                scale,
                translate,
                low,
                high,
                digits,
                valuesOutput);
                
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified(boolean modified)
    {
        this.modified = modified;
    }
}
