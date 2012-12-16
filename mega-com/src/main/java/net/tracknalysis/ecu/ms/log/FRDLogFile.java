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
package net.tracknalysis.ecu.ms.log;

import net.tracknalysis.ecu.ms.Megasquirt;

/**
 * Adaptation of the original code from MSLogger.
 *
 * @author David Smith
 * @author David Valeri
 */
public class FRDLogFile {
    private FRDLogFileHeader header;
    private FRDLogFileBody body = new FRDLogFileBody(this);

    public FRDLogFile(Megasquirt ms) {
        header = new FRDLogFileHeader(this, ms);
    }

    public FRDLogFileHeader getHeader() {
        return header;
    }

    public FRDLogFileBody getBody() {
        return body;
    }
}
