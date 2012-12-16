/**
 * Copyright 2012 David Valeri.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for an {@link MsEcu}.  Introduced as the original MSLogger interface
 * used static collections to hold information (generallt a bad idea).
 *
 * @author David Valeri
 */
public abstract class AbstractMsEcu implements
		MsEcu {
	protected final Map<String,Constant> constants = new HashMap<String,Constant>();

	protected final Map<String,OutputChannel> outputChannels = new HashMap<String,OutputChannel>();
    
	protected void registerOutputChannel(final OutputChannel outputChannel) {
		outputChannels.put(outputChannel.getName(), outputChannel);
    }
}
