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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.ecu.ms.Megasquirt;

/**
 * Adaptation of the original log writing code in MSLogger for handling generic file based log
 * logic.
 *
 * @author David Smith
 * @author David Valeri
 */
public abstract class AbstractFileLog implements FileLog {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFileLog.class);
    
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private volatile long startTime = 0;
    private File logFile;
    private OutputStream out;
    private File logFolder;
    private boolean wroteHeader = false;
    
    public AbstractFileLog(File logFolder) {
        super();
        this.logFolder = logFolder;
    }

    @Override
    public synchronized void start() throws IOException {
        if (out == null) {
            
            startTime = System.currentTimeMillis();
            try {
                createLogFile();
            } catch (IOException e) {
                out = null;
                logFile = null;
                throw e;
            }
        }
        
        LOG.debug("Started logging.");
    }
    
    @Override
    public synchronized void stop() throws IOException {
        if (out != null) {
            out.flush();
            out.close();
            out = null;
            wroteHeader = false;
            // Note: Intentionally not clearing the log file so 
            //       getLogFileAbsolutePath honors contract.
        }
        
        LOG.debug("Stopped logging.");
    }
    
    public final synchronized void mark(String message) throws IOException {
        if (out != null) {
            mark(message, out);
        }
    }

    @Override
    public final synchronized void mark() throws IOException {
        mark("No comment.");
    }

    @Override
    public final long getStartTime() {
        return startTime;
    }
    
    @Override
    public final synchronized String getLogFileAbsolutePath() {
        if (logFile != null) {
            return logFile.getAbsolutePath();
        } else {
            return null;
        }        
    }
    
    @Override
    public final synchronized void write(Megasquirt ms) throws IOException {
        
        if (out != null) {
            if (!wroteHeader) {
                writeHeader(ms, out);
                wroteHeader = true;
            }
            
            write(ms, out);
        }
    }
    
    @Override
    public final synchronized boolean isLogging() {
        return out != null;
    }
    
    protected abstract void mark(String message, OutputStream out) throws IOException;
    
    /**
     * Writes the header line(s) to the log file.  Called on the first write to
     * a log file.
     */
    protected abstract void writeHeader(Megasquirt ms, OutputStream out) throws IOException;
    
    /**
     * Write the 
     * @param ms
     * @throws IOException
     */
    protected abstract void write(Megasquirt ms, OutputStream out) throws IOException;
    
    /**
     * Returns the file extension to append to the default file name.
     */
    protected abstract String getFileExtension();
    
    private void createLogFile() throws FileNotFoundException {
        String fileName = dateFormat.format(new Date(getStartTime()))
                .toString() + "." + getFileExtension();
        
        logFile = new File(logFolder, fileName);
        
        LOG.debug("Creating output stream to log file '{}'.",
                logFile.getAbsolutePath());
        
        out = new BufferedOutputStream(new FileOutputStream(logFile));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractFileLog [startTime=");
        builder.append(startTime);
        builder.append(", logFile=");
        builder.append(logFile);
        builder.append(", out=");
        builder.append(out);
        builder.append(", logFolder=");
        builder.append(logFolder);
        builder.append(", wroteHeader=");
        builder.append(wroteHeader);
        builder.append("]");
        return builder.toString();
    }
}
