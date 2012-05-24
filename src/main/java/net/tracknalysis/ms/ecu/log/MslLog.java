/**
 * Copyright 2011 the original author or authors.
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
package net.tracknalysis.ms.ecu.log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import net.tracknalysis.ms.ecu.Megasquirt;


/**
 * Log implementation for writing standard MSL files.
 * 
 * @author David Smith
 * @author David Valeri
 */
public class MslLog extends AbstractFileLog {

    private static final String ENCODING = "UTF-8";

    private int markCounter = 1;
    private StringBuilder sb = new StringBuilder();

    public MslLog(File logFolder) {
        super(logFolder);
    }

    @Override
    protected void writeHeader(Megasquirt ms, OutputStream out)
            throws IOException {

        sb.setLength(0);

        sb.append("\"").append(ms.getTrueSignature()).append("\"\r\n");
        sb.append(ms.getLogHeader()).append("\r\n");

        out.write(sb.toString().getBytes(ENCODING));
    }

    @Override
    protected void write(Megasquirt ms, OutputStream out) throws IOException {

        sb.setLength(0);
        sb.append(ms.getLogRow()).append("\r\n");

        out.write(sb.toString().getBytes(ENCODING));
    }

    @Override
    protected void mark(String msg, OutputStream out)
            throws IOException {

        out.write(String.format("MARK  %03d - %s - %tc\r\n", markCounter++,
                msg, System.currentTimeMillis()).getBytes(ENCODING));

        if (markCounter > 999) {
            markCounter = 1;
        }
    }

    public boolean isMarkSupported() {
        return true;
    }

    protected String getFileExtension() {
        return "msl";
    }
}
