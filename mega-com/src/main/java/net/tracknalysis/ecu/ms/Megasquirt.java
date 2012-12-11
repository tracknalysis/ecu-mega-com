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
package net.tracknalysis.ecu.ms;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.tracknalysis.common.concurrent.GracefulShutdownThread;
import net.tracknalysis.common.io.IoManager;
import net.tracknalysis.ecu.ms.log.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author David Smith
 * @author David Valeri
 */
public abstract class Megasquirt
{
    private static final Logger LOG = LoggerFactory.getLogger(Megasquirt.class);
    
    private static final AtomicInteger LOG_THREAD_INSTANCE_COUNTER = new AtomicInteger();
    
    private LogThread logThread;
    private volatile boolean connected;
    private volatile boolean logging;
    private volatile String trueSignature = null;
    
    private volatile byte[] ochBuffer;
    private volatile long logStartTime;
    
    private Log log;
    private IoManager msIoManager;
    private TableManager tableManager;
    private MsConfiguration configuration;
    
    public Megasquirt(IoManager msIoManager, TableManager tableManager,
            Log logManager, MsConfiguration configuration) {
        this.msIoManager = msIoManager;
        this.tableManager = tableManager;
        this.log = logManager;
        this.configuration = configuration;
    }
    
    /**
     * Starts the communication to/from the ECU.
     *
     * @throws IOException if there is a communication error loading signature or constant data
     * @throws SignatureException if there is a problem with the signature
     */
    public synchronized void start() throws IOException, SignatureException {
        LOG.info("Starting MegaSquirt.");
        
        if (!connected) {
            ochBuffer = new byte[getBlockSize()];
            refreshFlags();
            initializeConnection();

            verifySignature();
            
            loadConstants();
            
            connected = true;
        }
    }
    
    /**
     * Stops the communication to/from the ECU and terminates logging if it was enabled.
     * Does nothing if already stopped.
     */
    public synchronized void stop() {
        LOG.info("Stopping MegaSquirt.");
        
        if (connected) {
            stopLogging();
            refreshFlags();
            trueSignature = null;
            connected = false;
        }
    }
    
    public boolean isRunning() {
        return connected;
    }

    /**
     * Resets the instance to be as if newly constructed.  Restarting it if it was running.
     *
     * @see #start()
     * @see #stop()
     */
    public synchronized void reset() throws IOException, SignatureException {
        boolean restart = isRunning();
        
        stop();
        
        if (restart) {
            start();
        }
    }    
    
    
    /**
     * Enables logging, if connected.  Logging is disabled by default.
     */
    public synchronized void startLogging() {
        if (!logging && connected) {
            LOG.debug("Starting logging at {}.", new Date());
            logging = true;
            
            try {
                log.start();
                
                logThread = new LogThread();
                logThread.start();
            } catch (IOException e) {
                LOG.error("Error starting logging.", e);
                try {
                    log.stop();
                } catch (IOException e2) {
                    LOG.warn("Error stopping logging after failed start.", e2);
                }
                
                logging = false;
            }
        }
    }

    /**
     * Disables logging, even if not connected.
     */
    public synchronized void stopLogging() {
        if (logging) {
            Date now = new Date();
            LOG.debug("Stapping logging at {}.  Logging was active for {}ms.",
                    now, now.getTime() - logStartTime);
            logging = false;
            
            if (!logThread.cancel()) {
                LOG.error("Error stopping the logging thread cleanly.  Closing log(s) anyway.");
            }
            
            try {
                log.stop();
            } catch (IOException e) {
                LOG.error("Error stopping logging.", e);
            }
        }
    }

    /**
     * Returns true if logging is enabled.
     */
    public boolean isLogging() {
        return logging;
    }
    
    /**
     * Returns a CSV formatted header row for inclusion in a log file.
     */
    public abstract String getLogHeader();

    /**
     * Returns a CSV formatted row of values for inclusion in a log file.
     */
    public abstract String getLogRow();

    /**
     * Returns a copy of the current OCH buffer contents.  Note that if not logging real-time
     * values, this data will be stale.
     */
    public byte[] getOchBuffer() {
        // TODO copying a moving target here since ochBuffer is used to read MS output.
        byte[] copy = new byte[ochBuffer.length];
        System.arraycopy(ochBuffer, 0, copy, 0, ochBuffer.length);
        return copy;
    }
    
    public double getValue(String channel) {
        double value = 0;
        Class<?> c = this.getClass();
        try {
            Field f = c.getDeclaredField(channel);
            value = f.getDouble(this);
        } catch (Exception e) {
            LOG.error("Failed to get value for " + channel + ".", e);
        }

        return value;
    }
    
    /**
     * Returns the signature of the actual ECU that this instance is communicating with.
     * Returns {@code null} if not started or if there was an error detecting the signature.
     */
    public String getTrueSignature() {
        return trueSignature;
    }
    
    /**
     * Returns the set of firmware signatures for firmware versions that the
     * instance can communicate with.
     */
    public abstract Set<String> getSignatures();
    
    /**
     * Returns the command to send to the ECU to solicit the firmware signature.
     */
    public abstract byte[] getSigCommand();
    
    /**
     * Returns the size of the official firmware signature that this 
     * implementation can communicate with.
     */
    public abstract int getSignatureSize();

    /**
     * Returns the command to send to the ECU to solicit a page of real-time data.
     */
    public abstract byte[] getOchCommand();
    
    /**
     * Returns the size of an OCH block.
     */
    public abstract int getBlockSize();
    
   /**
    * Returns the delay in milliseconds to use after a burn command.
    */
   public abstract int getPageActivationDelay();

   // TODO find out what this really means
   public abstract int getInterWriteDelay();
   
   /**
    * Returns the timeout in milliseconds to read a page of data.
    */
   public abstract int getBlockReadTimeout();
   
   /**
    * Reload flags from the configuration.  A configuration need not be immutable
    * so this may be used to alter the configuration of an existing instance.
    */
   public abstract void refreshFlags();

    /**
     * Loads the constant data from the ECU.  This is basically the set of
     * configuration options.
     * 
     * @throws IOException if there is an IO error while reading the data
     */
    public abstract void loadConstants() throws IOException;

    /**
     * Calculate the value of the real-time values based on a page of real-time data
     * from the ECU.
     *
     * @param ochBuffer the buffer containing the raw page data
     */
    public abstract void calculate(byte[] ochBuffer);

    // TODO what does this return?  I think it is the scaling factor for ADC.
    public abstract int getCurrentTPS();

    protected boolean isSet(String flag) {
        return configuration.isSet(flag);
    }
    
    /**
     * Returns the value as retrieved from the {@link DefaultTableManager}.
     *
     * @param index into the table to retrieve
     * @param name table name to lookup the value in
     */
    protected int table(int index, String name) {
        return tableManager.table(index, name);
    }

    /**
     * Converts a temperature in Fahrenheit to Celcius if units is in Celcius.
     * 
     * @param t
     *            the input temperature in Fahrenheit
     * 
     * @return {@code t} if configured for Fahrenheit, otherwise the Celcious
     *         equivalent of {@code t}
     */
    protected double tempCvt(int t) {
        if (configuration.isSet("CELCIUS")) {
            return (t - 32.0) * 5.0 / 9.0;
        } else {
            return t;
        }
    }
    
    /**
     * Returns the difference between the current time and logging start time in
     * seconds.
     */
    protected double timeNow() {
        return (System.currentTimeMillis() - logStartTime) / 1000.0;
    }

    /**
     * Write the current state to the logs if logging is enabled.
     */
    protected void logValues() {
        if (logging) {
            try {
                log.write(this);
            } catch (IOException e) {
                LOG.error("Error writing to log.", e);
            }
        }        
    }

    protected double round(double v) {
        return Math.floor(v * 100 + .5) / 100;
    }
    
    protected void initializeConnection() throws IOException {
        msIoManager.flushAll();
    }
    
    protected void verifySignature() throws IOException, SignatureException {
        boolean verified = false;

        Set<String> signatures = getSignatures();

        String msSig = getSignature();
        verified = signatures.contains(msSig);
        
        if (verified) {
            trueSignature = msSig;
        } else {
            throw new MismatchedSignatureException("Signature verification failed.  "
                    + "Signature '" + msSig + "' is not one of " + getSignatures() + ".");
        }
    }
    
    /**
     * Retrieves the signature of the connected ECU.
     *
     * @throws IOException if there is an error communicating with the ECU
     * @throws SignatureException if the signature of the ECU is invalid
     */
    protected String getSignature() throws IOException, SignatureException {
        byte[] sigCommand = getSigCommand();
        return MsFactory.getInstance().getSignature(msIoManager, 20,
                Arrays.asList(sigCommand));
    }
    
    private void getRuntimeVars() throws IOException {
        msIoManager.writeAndRead(getOchCommand(), ochBuffer, getBlockReadTimeout());
    }

    private void calculateValues() throws IOException {
        calculate(ochBuffer);
    }

    protected final byte[] loadPage(int pageNo, int pageOffset, int pageSize,
            byte[] select, byte[] read) throws IOException {

        byte[] buffer = new byte[pageSize];
        
        getPage(buffer, select, read);
        
        return buffer;
    }
    
    private void getPage(byte[] pageBuffer, byte[] pageSelectCommand,
            byte[] pageReadCommand) throws IOException {

        msIoManager.flushAll();
        
        if (pageSelectCommand != null) {
            // TODO look for code that actually has a page select command and clean this up to 1 call.
            msIoManager.write(pageSelectCommand);
        }
        
        if (pageReadCommand != null) {
            msIoManager.write(pageReadCommand);
        }
        
        msIoManager.read(pageBuffer, 1500);
    }        

    private class LogThread extends GracefulShutdownThread {
        
        private boolean run = true;
        
        public LogThread() {
            super("ECU Log Thread " + LOG_THREAD_INSTANCE_COUNTER.getAndIncrement());
        }

        public void run() {
            int consecutiveErrorCount = 0;
            
            try {
                logStartTime = System.currentTimeMillis();
                while (run) {
                    try {
                        getRuntimeVars();
                        calculateValues();
                        logValues();
                        consecutiveErrorCount = 0;
                    } catch (Exception e) {
                        if (consecutiveErrorCount > 5) {
                            throw e;
                        } else {
                            consecutiveErrorCount += 1;
                            LOG.warn("Encountered " + consecutiveErrorCount
                                    + " consecutive error(s) in logging thread.", e);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Fatal error in log thread.", e);
                // TODO deal with shutdown without throwing an exception and crashing the app.  Also, notifications!
            }
        }
    }
}
