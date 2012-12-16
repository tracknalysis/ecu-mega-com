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
package net.tracknalysis.ecu.ms.ecu.factory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.tracknalysis.common.io.IoManager;
import net.tracknalysis.common.io.IoManagerResult;
import net.tracknalysis.ecu.ms.common.MsController;
import net.tracknalysis.ecu.ms.common.MsEcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Abstraction of the code originally contained in the MSLogger Megasquirt class.  This refactoring
 * splits the ECU retrieval and signature probing logic into its own set of classes to simplify the
 * main Megasquirt communication classes and to make this logic more accessible.
 *
 * @author David Smith
 * @author David Valeri
 */
public abstract class MsEcuFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(MsEcuFactory.class);
    
    private static MsEcuFactory INSTANCE;
    
    private static final byte[] BOOT_COMMAND = new byte[] {'X'};
    
    public static final List<byte[]> DEFAULT_QUERY_COMMANDS = Collections
            .unmodifiableList(Arrays.asList(new byte[] {'Q'}, new byte[] {'S'}));
    
    public static synchronized MsEcuFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultMsEcuFactory();
        }
        return INSTANCE;
    }
    
    /**
     * Constructs a new {@link MsEcu} instance of the type appropriate
     * for the given signature.
     *
     * @param signature the signature of the firmware being communicated with
     * @param parent the parent controller instance to use
     *
     * @throws MsEcuFactoryException if there is an error constructing the instance
     */
    public abstract MsEcu getMegasquirt(String signature, MsController parent)
            throws MsEcuFactoryException;
    
    /**
     * Constructs a new {@link MsEcu} instance of the type appropriate
     * for the Megasquirt connected through the IO manager.
     *
     * @param ioManager the IO manager to use for communicating with the Megasquirt
     * @param parent the parent controller instance to use
     *
     * @throws MsEcuFactoryException if there is an error constructing the instance
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the Megasquirt signature
     */
    public MsEcu getMegasquirt(IoManager ioManager, MsController parent)
            throws MsEcuFactoryException, SignatureException, IOException {
        
        String signature = getSignature(ioManager);
        return getMegasquirt(signature, parent);
    }
    
    /**
     * Retrieves the signature of the Megasquirt connected through the IO manager
     * using query commands defined in {@link #DEFAULT_QUERY_COMMANDS}.
     * Retries up to 20 times to query for the signature, absent any IO errors.
     *
     * @param ioManager the IO manager to use for querying the Megasquirt
     *
     * @return the signature of the ECMegasquirtU
     *
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the Megasquirt signature
     */
    public String getSignature(IoManager ioManager)
            throws SignatureException, IOException {
        return getSignature(ioManager, 20);
    }
    
    /**
     * Retrieves the signature of the Megasquirt connected through the IO manager
     * using query commands defined in {@link #DEFAULT_QUERY_COMMANDS}.
     * Retries up to ${code retryCount} times to query for the signature, 
     * absent any IO errors.
     *
     * @param ioManager the IO manager to use for querying the Megasquirt
     * @param retryCount the number of times to retry
     *
     * @return the signature of the Megasquirt
     *
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the Megasquirt signature
     */
    public String getSignature(IoManager ioManager,
            int retryCount) throws SignatureException, IOException {
        return getSignature(ioManager, retryCount, DEFAULT_QUERY_COMMANDS);
        
    }
    
    /**
     * Retrieves the signature of the Megasquirt connected through the IO manager
     * using query commands defined in {@code queryCommands}.
     * Retries up to ${code retryCount} times to query for the signature, 
     * absent any IO errors.
     *
     * @param ioManager the IO manager to use for querying the Megasquirt
     * @param retryCount the number of times to retry
     * @param queryCommands
     *            optional ordered list of commands to use for querying. If
     *            {@code null}, {@link #DEFAULT_QUERY_COMMANDS} is used. 
     *
     * @return the signature of the Megasquirt
     *
     * @throws SignatureException if there is an error determining the signature
     * @throws IOException if there is a communication error determining the Megasquirt signature
     */
    public String getSignature(IoManager ioManager, int retryCount,
            List<byte[]> queryCommands) throws SignatureException, IOException {
        int tryCounter = 0;
        Exception error = null;
        String signature = null;
        
        while ((signature == null && retryCount == -1)
                || (signature == null && tryCounter < retryCount)) {
            try {
                signature = queryForSignature(ioManager, queryCommands);
            } catch (BootException e) {
                LOG.info("ECU needs to boot.  Sending boot command.");
                ioManager.write(BOOT_COMMAND);
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
        
        if (signature == null && error != null) {
            throw new SignatureException("Could not determine signature.", error);
        } else {
        	LOG.info("Received signature: {}.", signature);
            return signature;
        }
    }
    
    /**
     * Interrogate the Megasquirt for a signature of known formatting.
     * 
     * @param msIoManager the IO manager to use to query the Megasquirt
     * @param queryCommands
     *            optional ordered list of commands to use for querying. If
     *            {@code null}, {@link #DEFAULT_QUERY_COMMANDS} is used.
     * 
     * @return the signature value
     * 
     * @throws BootException if the Megasquirt needs to boot
     * @throws InvalidSignatureException if the signature is not a known format or is not returned
     * @throws NoSignatureException if we could not get a response from the Megasquirt
     */
    protected String queryForSignature(IoManager msIoManager, 
            List<byte[]> queryCommands) 
            throws IOException, BootException, SignatureException {
        
        List<byte[]> commandsToUse = queryCommands;
        if (commandsToUse == null) {
            commandsToUse = DEFAULT_QUERY_COMMANDS;
        }
        
        for (byte[] command : commandsToUse) {
            IoManagerResult result = msIoManager.writeAndRead(command, 500);
            byte[] response = result.getResult();
            if (response.length > 1) {
                return processSignature(response);
            }
        }

        throw new NoSignatureException();
    }

    /**
     * Processes a supposed signature for known formatting.
     *
     * @param response the bytes returned from the Megasquirt
     *
     * @return the signature value
     *
     * @throws BootException if the Megasquirt needs a reboot
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
