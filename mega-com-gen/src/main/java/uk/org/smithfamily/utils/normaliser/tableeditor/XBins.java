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

public class XBins extends TableItem
{
	private String readOnly;
	private String outputChannel;
	private String bins;

	public XBins(String bins, String outputChannel, String readOnly)
	{
		this.bins = bins;
		this.outputChannel = outputChannel;
		this.readOnly = (readOnly == null ? "false" : readOnly.equals("readonly") ? "true" : "false");
	}

	public String getReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly(String readOnly)
	{
		this.readOnly = readOnly;
	}

	public String getOutputChannel()
	{
		return outputChannel;
	}

	public void setOutputChannel(String outputChannel)
	{
		this.outputChannel = outputChannel;
	}

	public String getBins()
	{
		return bins;
	}

	public void setBins(String bins)
	{
		this.bins = bins;
	}

	@Override
	public String toString()
	{
	    return String.format("t.setXBins(%s,\"%s\",%s);", bins, outputChannel, readOnly);
	}
}
