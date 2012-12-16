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

import java.io.IOException;
import java.util.List;

public interface MsEcu
{
	/**
	 * Indicates if a constant is defined for the ECU class.
	 *
	 *@param name
	 *            the name of the constant
	 * @return true if a constant exists with name {@code name}
	 */
	boolean isConstantExists(final String name);
	
	/**
	 * Get a constant from the ECU class.
	 * 
	 * @param name
	 *            the name of the constant
	 * @return the constant object
	 */
	Constant getConstantByName(final String name);

	/**
	 * Get an output channel from the ECU class.
	 * 
	 * @param name
	 *            the name of the output channel
	 * @return the output channel object
	 */
	OutputChannel getOutputChannelByName(final String name);
	
    void setFlags();
    String getSignature();

    byte[] getOchCommand();

    byte[] getSigCommand();

    void loadConstants() throws IOException;

    void calculate(byte[] ochBuffer);

    String getLogHeader();

    String getLogRow();
    
    byte[] getLogData();

    int getBlockSize();

    int getSigSize();

    int getPageActivationDelay();
    
    List<String> getPageIdentifiers();
    
    List<byte[]> getPageActivates();
    
    List<String> getPageValueWrites();
    
    List<String> getPageChunkWrites();

    int getInterWriteDelay();

    int getCurrentTPS();

    void refreshFlags();

    boolean isCRC32Protocol();

    String[] getControlFlags();

    List<String> getRequiresPowerCycle();    
}
