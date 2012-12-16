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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import net.tracknalysis.ecu.ms.Megasquirt;


/**
 * Adaptation of the FRD log logic from MSLogger to logging abstractions.  Logs raw data buffer to
 * the log file.
 *
 * @author David Smith
 * @author David Valeri
 */
public class FRDLog extends AbstractFileLog {

    private FRDLogFile frdLog;
    private FRDLogFileHeader header;
    private FRDLogFileBody body;

    public FRDLog(File logFolder) {
        super(logFolder);
    }

    @Override
    public boolean isMarkSupported() {
        return false;
    }

    @Override
    protected void mark(String message, OutputStream out) throws IOException {
        // No-op
    }

    @Override
    protected void writeHeader(Megasquirt ms, OutputStream out)
            throws IOException {
        frdLog = new FRDLogFile(ms);
        header = frdLog.getHeader();
        body = frdLog.getBody();

        out.write(header.getHeaderRecord());
    }

    @Override
    protected void write(Megasquirt ms, OutputStream out) throws IOException {
        body.addRecord(ms.getLogData());
        out.write(body.getCurrentRecord().getBytes());
    }

    @Override
    protected String getFileExtension() {
        return "frd";
    }
}
