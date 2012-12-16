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
package net.tracknalysis.ecu.ms;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.tracknalysis.common.io.IoManager;
import net.tracknalysis.common.io.IoManagerResult;
import net.tracknalysis.common.io.IoProtocolHandler;
import net.tracknalysis.common.io.command.DefaultIoCommandManager;
import net.tracknalysis.common.io.command.IoCommand;
import net.tracknalysis.common.io.command.IoCommandManager;
import net.tracknalysis.common.notification.DefaultNotificationListenerManager;
import net.tracknalysis.common.notification.NotificationListener;
import net.tracknalysis.common.notification.NotificationListenerManager;
import net.tracknalysis.common.notification.NotificationListenerRegistry;
import net.tracknalysis.ecu.ms.common.Constant;
import net.tracknalysis.ecu.ms.common.MSUtils;
import net.tracknalysis.ecu.ms.common.MsController;
import net.tracknalysis.ecu.ms.common.MsEcu;
import net.tracknalysis.ecu.ms.common.OutputChannel;
import net.tracknalysis.ecu.ms.ecu.factory.MsEcuFactory;
import net.tracknalysis.ecu.ms.io.Crc32IoProtocolHandler;
import net.tracknalysis.ecu.ms.log.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class for interacting with a Megasquirt.  Adapted from original MSLogger code
 * to be independent of Android specific libraries.
 * 
 * @author David Smith
 * @author David Valeri
 */
public class Megasquirt implements MsController,
		NotificationListenerRegistry<MegasquirtNotificationType> {
	
	/**
	 * Lifecycle states that a {@link Megasquirt} can have.
	 */
	public static enum MegasquirtState {
        CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    };    
    
    private static final Logger LOG = LoggerFactory.getLogger(Megasquirt.class);
    private static final Crc32IoProtocolHandler MS_CRC32_PROTOCOL_HANDLER = new Crc32IoProtocolHandler();
    private static final AtomicInteger MEGASQUIRT_INSTANCE_COUNTER = new AtomicInteger();
	
    private final NotificationListenerManager<MegasquirtNotificationType> notificationListenerManager =
    		new DefaultNotificationListenerManager<MegasquirtNotificationType>(
    				MegasquirtNotificationType.DISCONNECTED, null);
    
    /**
     * The current lifecycle state.
     */
    private volatile MegasquirtState currentState = MegasquirtState.DISCONNECTED;
    
    /**
     * Flag indicating if we are logging.
     */
    private volatile boolean logging;
    
    /**
     * The implementation class used to deal with specifics of the Megasquirt firmware that
     * we are communicating with.
     */
    private volatile MsEcu ecuImplementation;
    
    /**
	 * Flag indicating if the constants have been loaded from the Megasquirt.
	 */
    private volatile boolean constantsLoaded;
    
    /**
     * The true signature of the Megasquirt firmware we are communicating with.
     */
    private volatile String trueSignature = "Unknown";
    
    /**
     * Milliseconds from epoch when the current logging session started.
     */
    private volatile long logStartTime;
    
    private final IoCommandManager ioCommandManager;
    private final IoManager ioManager;
    private final Log log;
    private final TableManager tableManager;
    private final MsConfiguration configuration;
    private final File debugLogDirectory;
    private final int instanceNumber;
    
    private final ReadWriteLock currentStateLock = new ReentrantReadWriteLock(true);
    
    public Megasquirt(IoManager ioManager, TableManager tableManager,
            Log logManager, MsConfiguration configuration, File debugLogDirectory) {
        this.ioManager = ioManager;
    	this.tableManager = tableManager;
        this.log = logManager;
        this.configuration = configuration;
        this.debugLogDirectory = debugLogDirectory;
        ioCommandManager = new DefaultIoCommandManager(ioManager);
        instanceNumber = MEGASQUIRT_INSTANCE_COUNTER.getAndIncrement();
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle Methods
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Starts the communication to/from the Megasquirt asynchronously.  This method will return immediately.
     * Use notifications to receive status updates.  Does nothing if already started.
     */
    public void start() {
    	currentStateLock.readLock().lock();
    	try {
	    	if (currentState == MegasquirtState.DISCONNECTED) {
		    	ioCommandManager.start();
		    	ioCommandManager.enqueue(new ConnectIoCommand());
	    	}
    	} finally {
    		currentStateLock.readLock().unlock();
    	}
    }

    /**
     * Stops the communication to/from the Megasquirt and terminates logging if it was enabled.
     * Does nothing if already stopped.
     */
    public void stop() {
    	stopLogging();
    	try {
    		ioCommandManager.enqueue(new DisconnectCommand());
    	} catch (IllegalStateException e) {
    		// Ignore, it just means we aren't running at the moment.
    	}	
    }

    /**
     * Returns true if we're connected to an ECU, false otherwise.
     */
    public boolean isConnected() {
    	currentStateLock.readLock().lock();
    	try {
    		return (currentState == MegasquirtState.CONNECTED);
    	} finally {
    		currentStateLock.readLock().unlock();
    	}
    }
    
    /**
     * Enables logging, if connected.  Logging is disabled by default.
     */
    public void startLogging() {
    	currentStateLock.writeLock().lock();
    	try {
	        if (!logging && currentState == MegasquirtState.CONNECTED) {
	        	logging = true;
	        	ioCommandManager.enqueue(new LogRuntimeVarsCommand());
	        }
    	} finally {
    		currentStateLock.writeLock().unlock();
    	}
    }

    /**
     * Disables logging, even if not connected.
     */
    public void stopLogging() {
    	logging = false;
    }
    
    /**
     * Returns true if we're data logging the ECU real-time stream, false otherwise.
     */
    public boolean isLogging() {
        return logging;
    }
    
	//////////////////////////////////////////////////////////////////////////////////////
	// MSControllerInterface Methods
	//////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public int table(final double d1, final String name) {
    	return tableManager.table((int) d1, name);
    }
    
    @Override
    public double tempCvt(final double t) {
    	if (configuration.isSet("CELCIUS")) {
            return (t - 32.0d) * 5.0d / 9.0d;
        } else {
            return t;
        }
    }
    
    @Override
    public double timeNow() {
        return (System.currentTimeMillis() - logStartTime) / 1000.0d;
    }
    
    @Override
    public double round(final double v) {
        return Math.floor((v * 100) + .5) / 100;
    }
    
    @Override
    public boolean isSet(final String name) {
        return configuration.isSet(name);
    }
    
    @Override
	public byte[] loadPage(final int pageNo, final int pageOffset,
			final int pageSize, final byte[] select, final byte[] read) throws IOException {

		final byte[] buffer = new byte[pageSize];
		
		LOG.debug("Loading constants from page {}.", pageNo);
		getPage(ioManager, ecuImplementation, buffer, select, read);
		logPageToFile(pageNo, buffer);
		LOG.debug("Loaded constants from page {}.", pageNo);
		
		return buffer;
	}
    
    @Override
	public int[][] loadByteArray(final byte[] pageBuffer, final int offset,
			final int width, final int height, final boolean signed) {
		final int[][] destination = new int[width][height];
		int index = offset;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int value = signed ? MSUtils.getSignedByte(
						pageBuffer, index) : MSUtils.getByte(
						pageBuffer, index);
				destination[x][y] = value;
				index = index + 1;
			}
		}
		return destination;
	}

	@Override
	public int[] loadByteVector(final byte[] pageBuffer, final int offset,
			final int width, final boolean signed) {
		final int[] destination = new int[width];
		int index = offset;
		for (int x = 0; x < width; x++) {
			final int value = signed ? MSUtils.getSignedByte(
					pageBuffer, index) : MSUtils.getByte(pageBuffer,
					index);
			destination[x] = value;
			index = index + 1;
		}

		return destination;
	}

	@Override
	public int[][] loadWordArray(final byte[] pageBuffer, final int offset,
			final int width, final int height, final boolean signed) {
		final int[][] destination = new int[width][height];
		int index = offset;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int value = signed ? MSUtils.getSignedWord(
						pageBuffer, index) : MSUtils.getWord(
						pageBuffer, index);
				destination[x][y] = value;
				index = index + 2;
			}
		}

		return destination;
	}

	@Override
	public int[] loadWordVector(final byte[] pageBuffer, final int offset,
			final int width, final boolean signed) {
		final int[] destination = new int[width];
		int index = offset;
		for (int x = 0; x < width; x++) {
			final int value = signed ? MSUtils.getSignedWord(
					pageBuffer, index) : MSUtils.getWord(pageBuffer,
					index);
			destination[x] = value;
			index = index + 2;
		}

		return destination;
	}
    
	//////////////////////////////////////////////////////////////////////////////////////
	// ListenerRegistry Methods
	//////////////////////////////////////////////////////////////////////////////////////
    
    @Override
	public void addListener(
			NotificationListener<MegasquirtNotificationType> listener) {
		notificationListenerManager.addListener(listener);
	}

	@Override
	public void removeListener(
			NotificationListener<MegasquirtNotificationType> listener) {
		notificationListenerManager.removeListener(listener);
	}

	@Override
	public void addWeakReferenceListener(
			NotificationListener<MegasquirtNotificationType> listener) {
		notificationListenerManager.addWeakReferenceListener(listener);
	}

	@Override
	public void removeWeakReferenceListener(
			NotificationListener<MegasquirtNotificationType> listener) {
		notificationListenerManager.removeWeakReferenceListener(listener);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// General Public Methods
	//////////////////////////////////////////////////////////////////////////////////////
	
	public String getLogHeader() {
		return ecuImplementation.getLogHeader();
	}
	
	public String getLogRow() {
		return ecuImplementation.getLogRow();
	}
	
	public byte[] getLogData() {
		return ecuImplementation.getLogData();
	}
    
	/**
	 * Helper function to know if a constant name exists.
	 * 
	 * @param name
	 *            the name of the constant
	 * @return true if the constant exists, false otherwise
	 */
	public boolean isConstantExists(final String name) {
		return ecuImplementation.isConstantExists(name);
	}

	/**
	 * Get a constant from the ECU class.
	 * 
	 * @param name
	 *            The name of the constant
	 * @return the constant object
	 */
	public Constant getConstantByName(final String name) {
		return ecuImplementation.getConstantByName(name);
	}

	/**
	 * Get an output channel from the ECU class.
	 * 
	 * @param name
	 *            the name of the output channel
	 * @return the output channel object
	 */
	public OutputChannel getOutputChannelByName(final String name) {
		return ecuImplementation.getOutputChannelByName(name);
	}
	
	public int getBlockSize() {
		return ecuImplementation.getBlockSize();
	}

	public int getCurrentTPS() {
		return ecuImplementation.getCurrentTPS();
	}

	public void refreshFlags() {
		ecuImplementation.refreshFlags();
	}

	public String[] getControlFlags() {
		return ecuImplementation.getControlFlags();
	}

	public List<String> getRequiresPowerCycle() {
		return ecuImplementation.getRequiresPowerCycle();
	}

	/**
	 * Helper functions to get specific value out of ECU.  Different MS versions
	 * have different names for the same thing this these methods help to get the
	 * right one depending on the MS version we're connected to.
	 */

	/**
	 * @return the current ECU cylinders count
	 */
	public int getCylindersCount() {
		return (int) (isConstantExists("nCylinders") ? getField("nCylinders")
				: getField("nCylinders1"));
	}

	/**
	 * @return the current ECU injectors count
	 */
	public int getInjectorsCount() {
		return (int) (isConstantExists("nInjectors") ? getField("nInjectors")
				: getField("nInjectors1"));
	}

	/**
	 * @return the current ECU divider
	 */
	public int getDivider() {
		return (int) (isConstantExists("divider") ? getField("divider")
				: getField("divider1"));
	}

	/**
	 * Return the current ECU injector staging.
	 * 
	 * @return 0 = Simultaneous, 1 = Alternating
	 */
	public int getInjectorStaging() {
		return (int) (isConstantExists("alternate") ? getField("alternate")
				: getField("alternate1"));
	}

	/**
	 * Returns the true signature of the Megasquirt firmware with which we are
	 * communicating.
	 */
	public String getTrueSignature() {
		return trueSignature;
	}
	
    public void setField(final String channelName, final int value) {
        final Class<?> c = ecuImplementation.getClass();

		try {
			final Field f = c.getDeclaredField(channelName);
			f.setInt(ecuImplementation, value);
		} catch (NoSuchFieldException e) {
			LOG.error("Failed to set value to " + value + " for " + channelName
					+ ".", e);
		} catch (IllegalArgumentException e) {
			LOG.error("Failed to set value to " + value + " for " + channelName
					+ ".", e);
		} catch (IllegalAccessException e) {
			LOG.error("Failed to set value to " + value + " for " + channelName
					+ ".", e);
		}
    }
    
    public double getField(final String channelName) {
		// TODO: This implementation seems wrong, even though it is copied from DataManager in MSLogger
		OutputChannel outputChannel = getOutputChannelByName(channelName);
		
		if (outputChannel != null) {
			return outputChannel.getValue();
		} else {
			return 0;
		}
	}
	
	//////////////////////////////////////////////
	
	private void onConnecting() {
		LOG.debug("Megasquirt {}: Connecting.", instanceNumber);
		currentState = MegasquirtState.CONNECTING;
		notificationListenerManager.sendNotification(MegasquirtNotificationType.CONNECTING);
	}
	
	private void onConnected() {
		LOG.debug("Megasquirt {}: Connected.", instanceNumber);
		currentState = MegasquirtState.CONNECTED;
		notificationListenerManager.sendNotification(MegasquirtNotificationType.CONNECTED);
	}
	
	private void onConnectionFailed(Exception e) {
		LOG.error("Megasquirt " + instanceNumber + ": Connection failed with exception.", e);
		notificationListenerManager.sendNotification(MegasquirtNotificationType.CONNECTION_FAILED);
	}
	
	private void onLoggingStarting() {
		LOG.debug("Megasquirt {}: Logging starting.", instanceNumber);
		notificationListenerManager.sendNotification(MegasquirtNotificationType.LOGGING_STARTING);
	}
	
	private void onLoggingStarted() {
		LOG.debug("Megasquirt {}: Logging started.", instanceNumber);
		notificationListenerManager.sendNotification(MegasquirtNotificationType.LOGGING_STARTED);
	}
	
	private void onLoggingStopping() {
		LOG.debug("Megasquirt {}: Logging stopping.", instanceNumber);
		notificationListenerManager.sendNotification(MegasquirtNotificationType.LOGGING_STOPPING);
	}
	
	private void onLoggingStopped() {
		LOG.debug("Megasquirt {}: Logging stopped.", instanceNumber);
		notificationListenerManager.sendNotification(MegasquirtNotificationType.LOGGING_STOPPED);
	}
	
	private void onLoggingFailed(Exception e) {
		LOG.error("Megasquirt " + instanceNumber + ": Logging failed with exception.", e);
		notificationListenerManager.sendNotification(MegasquirtNotificationType.LOGGING_FAILED);
	}
	
	private void onDisconnecting() {
		LOG.debug("Megasquirt {}: Disconnecting.", instanceNumber);
		currentState = MegasquirtState.DISCONNECTING;
		notificationListenerManager.sendNotification(MegasquirtNotificationType.DISCONNECTING);
	}
	
	private void onDisconnected() {
		LOG.debug("Megasquirt {}: Disconnected.", instanceNumber);
		currentState = MegasquirtState.DISCONNECTED;
		notificationListenerManager.sendNotification(MegasquirtNotificationType.DISCONNECTED);
	}

	/**
	 * Returns the protocol handler needed for the Megasquirt, or {@code null} if
	 * one is not needed.
	 */
	private IoProtocolHandler getProtocolHandler() {
    	if (ecuImplementation.isCRC32Protocol()) {
    		return MS_CRC32_PROTOCOL_HANDLER;
    	} else {
    		return null;
    	}
    }
    
    /**
	 * Read a page of constants from the Megasquirt into a byte buffer. MS1 uses a
	 * select/read combo, MS2 just does a read.
	 * 
	 * @param ioManager
	 *            the IO manager to use for performing IO operations
	 * @param ecuImplementation
	 *            the implementation class for the Megasquirt firmware in
	 *            use
	 * @param pageBuffer
	 *            the buffer to read into
	 * @param pageSelectCommand
	 *            the command to select the page to read
	 * @param pageReadCommand
	 *            the command to read the page
	 * @throws IOException
	 *             if there is an error processing the request
	 */
	private void getPage(IoManager ioManager,
			MsEcu ecuImplementation, final byte[] pageBuffer,
			final byte[] pageSelectCommand, final byte[] pageReadCommand)
			throws IOException {

		try {
			ioManager.flushAll();

			final long delay = ecuImplementation.getPageActivationDelay();
			if (pageSelectCommand != null) {
				ioManager.write(pageSelectCommand, getProtocolHandler());

				if (delay > 0) {
					Thread.sleep(delay);
				}
			}

			if (pageReadCommand != null) {
				ioManager.write(pageReadCommand, getProtocolHandler());
				if (delay > 0) {
					Thread.sleep(delay);
				}
			}

			ioManager.read(pageBuffer, 2000, getProtocolHandler());
		} catch (InterruptedException e) {
			throw new IOException("Interrupted during page retrieval.", e);
		}
	}
    
    /**
     * Dumps a loaded page to file storage for analysis if {@link #debugLogDirectory} is
     * set.
     * 
     * @param pageNo the page number being dumped
     * @param buffer the buffer of content to dump
     */
    private void logPageToFile(final int pageNo, final byte[] buffer) {
    	if (debugLogDirectory != null) {
	        try {
	            if (!debugLogDirectory.exists()) {
	                final boolean mkDirs = debugLogDirectory.mkdirs();
	                if (!mkDirs) {
						LOG.error("Unable to create directory for Megasquirt IO page log at {}.",
								debugLogDirectory.getAbsolutePath());
	                }
	            }
	
	            final String fileName = ecuImplementation.getClass().getName() + ".firmware";
	            final File outputFile = new File(debugLogDirectory, fileName);
	            BufferedOutputStream out = null;
	            try {
	                final boolean append = !(pageNo == 1);
	                out = new BufferedOutputStream(new FileOutputStream(outputFile, append));
	                LOG.info("Saving page {}. Append = {}.", pageNo, append);
	                out.write(buffer);
	                out.flush();
	            } finally {
	                if (out != null) {
	                    try {
	                    	out.close();
	                    } catch (IOException e) {
	                    	LOG.warn("Error closing page output file.", e);
	                    }
	                }
	            }
	        } catch (IOException e) {
	        	LOG.error("Error attempting to save page.", e);
	        }
    	}
    }
    
    /**
     * Base IO command class containing common IO operations.
     */
    private abstract class AbstractIoCommand implements IoCommand {
    	
    	/**
		 * Get the current variables from the ECU.
		 * 
		 * @param ioManager
		 *            the IO manager to use for performing IO operations
		 * @param ecuImplementation
		 *            the implementation class for the Megasquirt firmware in
		 *            use
		 * 
		 * @throws IOException
		 *             if there is an error processing the request
		 */
		protected IoManagerResult getRuntimeVars(IoManager ioManager,
				MsEcu ecuImplementation) throws IOException {
			final byte[] buffer = new byte[ecuImplementation.getBlockSize()];

			int delay = ecuImplementation.getInterWriteDelay();
			delay += 500;

			IoManagerResult result = ioManager.writeAndRead(
					ecuImplementation.getOchCommand(), buffer, delay,
					getProtocolHandler());
			return result;
		}
    }
    
    /**
     * Command to connect to the Megasquirt and initialize state.
     */
    private class ConnectIoCommand extends AbstractIoCommand implements IoCommand {
		@Override
		public void execute(IoManager ioManager) {
			currentStateLock.writeLock().lock();
			try {
				if (currentState == MegasquirtState.DISCONNECTED) {
	                onConnecting();
	                ioManager.connect();
	
	                try {
	                    Thread.sleep(500);
	                } catch (InterruptedException e) {
	                	LOG.warn("Interrupted while sleeping after initialization.");
	                }
	                
	            	ioManager.flushAll();
	                initialiseImplementation(ioManager);
	                
					/*
					 * Make sure we have calculated runtime vars at least once
					 * before refreshing flags. The reason is that the
					 * refreshFlags() function also triggers the creation of
					 * menus/dialogs/tables/curves/etc that use variables such
					 * as {clthighlim} in curves that need to have their value
					 * assigned before being used.
					 */
	                final IoManagerResult result = getRuntimeVars(ioManager, ecuImplementation);
	                ecuImplementation.calculate(result.getResult());
	
	                ecuImplementation.refreshFlags();
	
	                if (!constantsLoaded) {
	                    // Only do this once so reconnects are quicker
	                    ecuImplementation.loadConstants();
	                    constantsLoaded = true;
	                }
	                
	                onConnected();
				}
            } catch (Exception e) {
            	ecuImplementation = null;
            	onConnectionFailed(e);
            	onDisconnected();
        	} finally {
        		currentStateLock.writeLock().unlock();
        	}
		}
		
		/**
	     * Probes the ECU for the firmware signature and instantiates the current implementation
	     * class to talk to the ECU.
	     *
	     * @throws Exception if there is an error initializing the implementation
	     */
	    private void initialiseImplementation(IoManager ioManager) throws Exception {
	    	LOG.debug("Checking your ECU.");

	    	MsEcuFactory factory = MsEcuFactory.getInstance();
	    	String signature = factory.getSignature(ioManager);
	    	ecuImplementation = factory.getMegasquirt(signature, Megasquirt.this);
	    	
			if (!signature.equals(ecuImplementation.getSignature())) {
				trueSignature = ecuImplementation.getSignature();
				if (LOG.isInfoEnabled()) {
					LOG.info(
							"Got unsupported signature from Megasquirt \"{}\""
									+ "but found a similar supported signature \"{}\"",
							trueSignature, signature);
				}
			}
	    }
    }
    
    /**
     * Command to disconnect from the Megasquirt.
     */
    private class DisconnectCommand implements IoCommand {
    	@Override
    	public void execute(IoManager ioManager) {
    		currentStateLock.writeLock().lock();
    		try {
    			if (log.isLogging()) {
    				onLoggingStopping();
                    logging = false;
                    
	                try {
	                    log.stop();
	                } catch (Exception e) {
	                    LOG.error("Megasquirt " + instanceNumber + ": Error stopping log.", e);
	                }
	                
	                onLoggingStopped();
                }

    			onDisconnecting();
	    		ecuImplementation = null;
	    		// Need to do this in another thread since this command is being executed in the thread
	    		// that is trying to be stopped by ioCommandManager.stop(); 
	    		Thread t = new Thread() {
	    			public void run() {
	    				currentStateLock.writeLock().lock();
	    				try {
	    					ioCommandManager.stop();
	    				} finally {
	    					onDisconnected();
	    					currentStateLock.writeLock().unlock();
	    				}
	    			};
	    		};
	    		t.run();
    		} finally {
    			currentStateLock.writeLock().unlock();
    		}
    	}
    }
    
    /**
     * Command to log runtime variables.
     */
    private class LogRuntimeVarsCommand extends AbstractIoCommand implements IoCommand {
    	private int consecutiveErrors = 0;
    	
    	@Override
    	public void execute(IoManager ioManager) {
    		
    		currentStateLock.readLock().lock();
    		try {
    			if (currentState == MegasquirtState.CONNECTED) {
		    		if (logging) {
		    			if (!log.isLogging()) {
		    				// Start the log if not already done.  If this fails, stop logging.
		    				try {
				            	onLoggingStarting();
				            	logStartTime = System.currentTimeMillis();
				                log.start();
				                onLoggingStarted();
				            } catch (Exception e) {
				            	onLoggingFailed(e);
				                logging = false;
				            }
		    			}
		    		}
		    		
		    		if (logging) {
		    			// If the log is already started, and we are still logging.
						try {
							final IoManagerResult result = getRuntimeVars(ioManager, ecuImplementation);
							ecuImplementation.calculate(result.getResult());
							log.write(Megasquirt.this);
							// Put us back in the queue to execute again since we are still logging.
							if (!ioCommandManager.enqueue(this)) {
								LOG.error("Megasquirt {}: Error enquing log runtime vars command.  Stopping logging.");
								logging = false;
							}
						} catch (Exception e) {
							consecutiveErrors++;
							
							if (consecutiveErrors > 5) {
								onLoggingFailed(e);
								logging = false;
							} else {
								LOG.warn("Megasquirt " + instanceNumber + ": Error logging runtime vars.", e);
								// Put us back in the queue to execute again since we are still logging.
								ioCommandManager.enqueue(this);
							}
						}
		        	} 
		    		
		    		if (!logging) {
		    			// Either we were told to stop logging or something went wrong so we need to stop logging.
		        		onLoggingStopping();
		                logging = false;
		                
		                if (log.isLogging()) {
			                try {
			                    log.stop();
			                } catch (Exception e) {
			                    LOG.error("Megasquirt " + instanceNumber + ": Error stopping log.", e);
			                }
		                }
		                
		                onLoggingStopped();
		        	}
    			}
    		} finally {
    			currentStateLock.readLock().unlock();
    		}
    	}
    }
}
