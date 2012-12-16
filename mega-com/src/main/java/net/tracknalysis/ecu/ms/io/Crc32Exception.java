/**
 * Copyright 2012 David Smith.
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
package net.tracknalysis.ecu.ms.io;

import java.io.IOException;

/**
 * Adaption of the original exception class from MSLogger.  Represents an error
 * relating to CRC validation in {@link Crc32IoProtocolHandler}.
 *
 * @author David Smith
 * @author David Valeri
 */
public class Crc32Exception extends IOException {

	private static final long serialVersionUID = 6144546200357281333L;
	
	public Crc32Exception() {
		super();
	}
}
