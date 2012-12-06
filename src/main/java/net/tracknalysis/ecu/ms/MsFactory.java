/**
 * Copyright 2012 the original author or authors.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.tracknalysis.common.io.IoManager;
import net.tracknalysis.common.io.IoManagerResult;
import net.tracknalysis.ecu.ms.log.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author David Smith
 * @author David Valeri
 */
public abstract class MsFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(MsFactory.class);
    
    private static MsFactory INSTANCE;
    
    private static final byte[] BOOT_COMMAND = new byte[] {'X'};
    
    public static final List<byte[]> DEFAULT_QUERY_COMMANDS = Collections
            .unmodifiableList(Arrays.asList(new byte[] {'Q'}, new byte[] {'S'}));
    
    public static synchronized MsFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultMsFactory();
        }
        return INSTANCE;
    }
    
    /**
     * Constructs a new {@link Megasquirt} instance of the type appropriate
     * for the given signature.
     *
     * @param signature the signature of the firmware being communicated with
     * @param msIoManager the IO manager to use for communicating with the ECU
     * @param tableManager the table manager to use
     * @param logManager the log manager to use
     * @param configuration the configuration to use
     *
     * @throws MsFactoryException if there is an error constructing the instance
     */
    public abstract Megasquirt getMegasquirt(String signature,
    		IoManager msIoManager, TableManager tableManager,
            Log logManager, MsConfiguration configuration)
            throws MsFactoryException;
    
    /**
     * Constructs a new {@link Megasquirt} instance of the type appropriate
     * for the ECU connected through the IO manager.
     *
     * @param msIoManager the IO manager to use for communicating with the ECU
     * @param tableManager the table manager to use
     * @param logManager the log manager to use
     * @param configuration the configuration to use
     *
     * @throws MsFactoryException if there is an error constructing the instance
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the ECU signature
     */
    public Megasquirt getMegasquirt(
    		IoManager msIoManager, TableManager tableManager,
            Log logManager, MsConfiguration configuration)
            throws MsFactoryException, SignatureException, IOException {
        
        String signature = getSignature(msIoManager);
        return getMegasquirt(signature, msIoManager, tableManager, logManager,
                configuration);
    }
    
    /**
     * Retrieves the signature of the ECU connected through the IO manager
     * using query commands defined in {@link #DEFAULT_QUERY_COMMANDS}..
     * Retries up to 20 times to query for the signature, absent any IO errors.
     *
     * @param msIoManager the IO manager to use for querying the ECU
     *
     * @return the signature of the ECU
     *
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the ECU signature
     */
    public String getSignature(IoManager msIoManager)
            throws SignatureException, IOException {
        return getSignature(msIoManager, 20);
    }
    
    /**
     * Retrieves the signature of the ECU connected through the IO manager
     * using query commands defined in {@link #DEFAULT_QUERY_COMMANDS}.
     * Retries up to ${code retryCount} times to query for the signature, 
     * absent any IO errors.
     *
     * @param msIoManager the IO manager to use for querying the ECU
     * @param retryCount the number of times to retry
     *
     * @return the signature of the ECU
     *
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the ECU signature
     */
    public String getSignature(IoManager msIoManager,
            int retryCount) throws SignatureException, IOException {
        return getSignature(msIoManager, retryCount, DEFAULT_QUERY_COMMANDS);
        
    }
    
    /**
     * Retrieves the signature of the ECU connected through the IO manager
     * using query commands defined in {@code queryCommands}.
     * Retries up to ${code retryCount} times to query for the signature, 
     * absent any IO errors.
     *
     * @param msIoManager the IO manager to use for querying the ECU
     * @param retryCount the number of times to retry
     * @param queryCommands
     *            optional ordered list of commands to use for querying. If
     *            {@code null}, {@link #DEFAULT_QUERY_COMMANDS} is used. 
     *
     * @return the signature of the ECU
     *
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the ECU signature
     */
    public String getSignature(IoManager msIoManager, int retryCount,
            List<byte[]> queryCommands) throws SignatureException, IOException {
        int tryCounter = 0;
        Exception error = null;
        String fingerprint = null;
        
        while ((fingerprint == null && retryCount == -1)
                || (fingerprint == null && tryCounter < retryCount)) {
            try {
                fingerprint = queryForFingerprint(msIoManager, queryCommands);
            } catch (BootException e) {
                LOG.info("ECU needs to boot.  Sending boot command.");
                msIoManager.write(BOOT_COMMAND);
                delay(500);
                error = e;
            } catch (SignatureException e) {
                LOG.info("Received bad signature.", e);
                error = e;
            }
            // Add a little delay
            delay(100);
            tryCounter++;
        }
        
        if (fingerprint == null && error != null) {
            throw new SignatureException("Could not determine signature.", error);
        } else {
            return fingerprint;
        }
    }
    
    /**
     * Interrogate the controller for a signature of known formatting.
     * 
     * @param msIoManager the IO manager to use to query the ECU
     * @param queryCommands
     *            optional ordered list of commands to use for querying. If
     *            {@code null}, {@link #DEFAULT_QUERY_COMMANDS} is used.
     * 
     * @return the signature value
     * 
     * @throws BootException if the ECU needs to boot
     * @throws InvalidSignatureException if the signature is not a known format or is not returned
     */
    protected String queryForFingerprint(IoManager msIoManager, 
            List<byte[]> queryCommands) 
            throws IOException, BootException, SignatureException {
        
        List<byte[]> commandsToUse = queryCommands;
        if (commandsToUse == null) {
            commandsToUse = DEFAULT_QUERY_COMMANDS;
        }
        
        for (byte[] command : commandsToUse) {
            IoManagerResult result = msIoManager.writeAndRead(command, 500);
            byte[] response = result.getOut();
            if (response.length > 1) {
                return processSignature(response);
            }
        }

        throw new NoSignatureException();
    }

    /**
     * Processes a supposed signature for known formatting.
     *
     * @param response the bytes returned from the ECU
     *
     * @return the signature value
     *
     * @throws BootException if the ECU needs a reboot
     * @throws InvalidSignatureException if the signature is not a known format
     */
    protected String processSignature(byte[] response) throws BootException,
            InvalidSignatureException {
        String result = new String(response);

        if (result.contains("Boot>")) {
            throw new BootException();
        } else if (
                (response.length == 1 && response[0] != 20)
                || (response.length <= 1)
                || ((response[0] != 'M' && response[0] != 'J')
                        || (response[1] != 'S' && response[1] != 'o' && response[1] != 'i'))) {
            throw new InvalidSignatureException("'" + result + "' is not a valid signature.");
        } else {
            return result;
        }
    }
    
    /**
     * Causes the current thread to delay for at least 
     * {@code delayPeriod} milliseconds.
     */
    protected void delay(long delayPeriod) {
        try {
            Thread.sleep(delayPeriod);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted during delay.", e);
        }
    }
}
