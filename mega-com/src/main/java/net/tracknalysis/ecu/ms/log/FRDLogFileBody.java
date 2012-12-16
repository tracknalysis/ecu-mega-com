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

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Adaptation of the original code from MSLogger.
 *
 * @author David Smith
 * @author David Valeri
 */
public class FRDLogFileBody {
    int outpc = 0;
    private FRDLogFileRecord currentRecord;
    private FRDLogFile parent;

    public FRDLogFileBody(FRDLogFile p) {
        this.parent = p;
    }

    public void addRecord(byte[] ochBuffer) {
        currentRecord = new FRDLogFileRecord(this, ochBuffer);
    }

    public FRDLogFileRecord getCurrentRecord() {
        return currentRecord;
    }

    public void read(FileInputStream is) throws IOException {
        currentRecord = new FRDLogFileRecord(this, is);

    }

    public FRDLogFile getParent() {
        return parent;
    }

}
