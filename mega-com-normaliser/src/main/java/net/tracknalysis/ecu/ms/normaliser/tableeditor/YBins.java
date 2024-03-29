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
package net.tracknalysis.ecu.ms.normaliser.tableeditor;

public class YBins extends TableItem
{
    private String bins;
    private String outputChannel;
    private String readOnly;

    public YBins(String bins, String outputChannel, String readOnly)
    {
        this.bins = bins;
        this.outputChannel = outputChannel;
        this.readOnly = (readOnly == null ? "false" : readOnly.equals("readonly") ? "true" : "false");

    }

    public String getBins()
    {
        return bins;
    }

    public void setBins(String bins)
    {
        this.bins = bins;
    }

    public String getLabel()
    {
        return outputChannel;
    }

    public void setLabel(String label)
    {
        this.outputChannel = label;
    }

    public String getReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(String readOnly)
    {
        this.readOnly = readOnly;
    }

    @Override
    public String toString()
    {
        return String.format("t.setYBins(%s,\"%s\",%s);", bins, outputChannel, readOnly);
    }

}
