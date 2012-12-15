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
package uk.org.smithfamily.mslogger.ecuDef;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.tracknalysis.common.concurrent.GracefulShutdownThread;
import net.tracknalysis.common.io.IoManager;
import net.tracknalysis.common.io.IoManagerResult;
import net.tracknalysis.common.io.IoProtocolHandler;
import net.tracknalysis.common.notification.DefaultNotificationListenerManager;
import net.tracknalysis.common.notification.NotificationListener;
import net.tracknalysis.common.notification.NotificationListenerManager;
import net.tracknalysis.common.notification.NotificationListenerRegistry;
import net.tracknalysis.common.notification.NotificationType;
import net.tracknalysis.ecu.ms.MsConfiguration;
import net.tracknalysis.ecu.ms.SignatureException;
import net.tracknalysis.ecu.ms.TableManager;
import net.tracknalysis.ecu.ms.ecu.factory.BootException;
import net.tracknalysis.ecu.ms.ecu.factory.MsEcuInterfaceFactory;
import net.tracknalysis.ecu.ms.io.MsCrc32Exception;
import net.tracknalysis.ecu.ms.io.MsCrc32ProtocolHandler;
import net.tracknalysis.ecu.ms.log.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.smithfamily.mslogger.ecuDef.gen.ECURegistry;

/**
 * Main class for interacting with a Megasquirt.  Adapted from original MSLogger code
 * to be independent of Android specific libraries.
 * 
 * @author David Smith
 * @author David Valeri
 */
public class Megasquirt implements MSControllerInterface,
		NotificationListenerRegistry<MegasquirtNotificationType> {
	
	/**
	 * Lifecycle states that a {@link Megasquirt} can have.
	 */
	public static enum MegasquirtState {
        CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    };    
    
    private static final Logger LOG = LoggerFactory.getLogger(Megasquirt.class);
    private static final AtomicInteger LOG_THREAD_INSTANCE_COUNTER = new AtomicInteger();
    private static final MsCrc32ProtocolHandler MS_CRC32_PROTOCOL_HANDLER = new MsCrc32ProtocolHandler();
	private static final int MAX_QUEUE_SIZE = 10;
    

    public static final int BURN_DATA = 10;

    public static final int MS3_SD_CARD_STATUS_WRITE = 50;
    public static final int MS3_SD_CARD_STATUS_READ = 51;
    public static final int MS3_SD_CARD_RESET_AND_GO = 52;
    public static final int MS3_SD_CARD_RESET_AND_WAIT = 53;
    public static final int MS3_SD_CARD_STOP_LOGGING = 54;
    public static final int MS3_SD_CARD_START_LOGGING = 55;
    public static final int MS3_SD_CARD_REINITIALISE_CARD = 56;
    public static final int MS3_SD_CARD_READ_DIRECTORY_WRITE = 57;
    public static final int MS3_SD_CARD_READ_DIRECTORY_READ = 58;
    public static final int MS3_SD_CARD_READ_STREAM = 59;
    public static final int MS3_SD_CARD_READ_RTC_WRITE = 60;
    public static final int MS3_SD_CARD_READ_RTC_READ = 61;

    
    private final BlockingQueue<InjectedCommand> injectionQueue = new ArrayBlockingQueue<InjectedCommand>(
			MAX_QUEUE_SIZE);
    
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
    private volatile MSECUInterface ecuImplementation;
    
    /**
	 * Flag indicating if the constants have been loaded from the Megasquirt.
	 */
    private volatile boolean constantsLoaded;
    
    /**
     * The true signature of the Megasquirt firmware we are communicating with.
     */
    private volatile String trueSignature = "Unknown";
    
    /**
     * The thread that handles communication with the Megasquirt.
     */
    private volatile ECUThread ecuThread;
    
    /**
     * Milliseconds from epoch when the current logging session started.
     */
    private volatile long logStartTime;
    
    private final Log log;
    private final IoManager ioManager;
    private final TableManager tableManager;
    private final MsConfiguration configuration;
    private final File debugLogDirectory;

    public Megasquirt(IoManager ioManager, TableManager tableManager,
            Log logManager, MsConfiguration configuration, File debugLogDirectory) {
        this.ioManager = ioManager;
        this.tableManager = tableManager;
        this.log = logManager;
        this.configuration = configuration;
        this.debugLogDirectory = debugLogDirectory;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle Methods
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Starts the communication to/from the ECU asynchronously.  This method will return immediately.
     * Use notifications to receive status updates.  Does nothing if already started.
     */
    public synchronized void start() {
    	if (currentState == MegasquirtState.DISCONNECTED) {
	    	LOG.info("Starting MegaSquirt.");
	
	    	ecuThread = new ECUThread();
	        ecuThread.start();
    	}
    }

    /**
     * Stops the communication to/from the ECU and terminates logging if it was enabled.
     * Does nothing if already stopped.
     */
    public synchronized void stop() {
    	if (currentState == MegasquirtState.CONNECTED || currentState == MegasquirtState.CONNECTING) {
	    	LOG.info("Stopping MegaSquirt.");
	    	stopLogging();
	    	ecuThread.cancel();
	    	ecuThread = null;
    	}
    }

    /**
     * Revert to initial state.
     */
    public void reset() {
        ecuImplementation.refreshFlags();
        constantsLoaded = false;
    }
    
    /**
     * @return true if we're connected to an ECU, false otherwise
     */
    public synchronized boolean isConnected() {
        return (currentState == MegasquirtState.CONNECTED);
    }
    
    /**
     * Enables logging, if connected.  Logging is disabled by default.
     */
    public synchronized void startLogging() {
        if (!logging && currentState == MegasquirtState.CONNECTED) {
            LOG.debug("Starting logging at {}.", new Date());
            try {
                log.start();
                logStartTime = System.currentTimeMillis();
                logging = true;
                // TODO send message
            } catch (IOException e) {
                LOG.error("Error starting logging.", e);
                try {
                    log.stop();
                } catch (IOException e2) {
                    LOG.warn("Error stopping logging after failed start.", e2);
                }
                
                // TODO send message
                
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
            
            // TODO stop logging and send messages
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
     * Returns true if we're data logging the ECU real-time stream, false otherwise.
     */
    public synchronized boolean isLogging() {
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
			final int pageSize, final byte[] select, final byte[] read) {

		final byte[] buffer = new byte[pageSize];
		
		try {
			LOG.debug("Loading constants from page {}.", pageNo);
			getPage(buffer, select, read);
			logPageToFile(pageNo, buffer);
			LOG.debug("Loaded constants from page {}.", pageNo);
		} catch (IOException e) {
			LOG.error("Error loading constants from page " + pageNo + ".", e);
			// TODO what do we do about the error?
		}
		
		return buffer;
	}
    
    @Override
	public int[][] loadByteArray(final byte[] pageBuffer, final int offset,
			final int width, final int height, final boolean signed) {
		final int[][] destination = new int[width][height];
		int index = offset;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int value = signed ? MSUtils.INSTANCE.getSignedByte(
						pageBuffer, index) : MSUtils.INSTANCE.getByte(
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
			final int value = signed ? MSUtils.INSTANCE.getSignedByte(
					pageBuffer, index) : MSUtils.INSTANCE.getByte(pageBuffer,
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
				final int value = signed ? MSUtils.INSTANCE.getSignedWord(
						pageBuffer, index) : MSUtils.INSTANCE.getWord(
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
			final int value = signed ? MSUtils.INSTANCE.getSignedWord(
					pageBuffer, index) : MSUtils.INSTANCE.getWord(pageBuffer,
					index);
			destination[x] = value;
			index = index + 2;
		}

		return destination;
	}
    
    @Override
    public void registerOutputChannel(final OutputChannel o) {
        DataManager.getInstance().addOutputChannel(o);

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
    
	/**
	 * Helper function to know if a constant name exists.
	 * 
	 * @param name
	 *            The name of the constant
	 * @return true if the constant exists, false otherwise
	 */
	public boolean isConstantExists(final String name) {
		return MSECUInterface.constants.containsKey(name);
	}

	/**
	 * Get a constant from the ECU class.
	 * 
	 * @param name
	 *            The name of the constant
	 * @return The constant object
	 */
	public Constant getConstantByName(final String name) {
		return MSECUInterface.constants.get(name);
	}

	/**
	 * Get an output channel from the ECU class.
	 * 
	 * @param name
	 *            The name of the output channel
	 * @return The output channel object
	 */
	public OutputChannel getOutputChannelByName(final String name) {
		return MSECUInterface.outputChannels.get(name);
	}

	/**
	 * Get a table editor from the ECU class.
	 * 
	 * @param name
	 *            The name of the table editor object
	 * @return The table editor object
	 */
	public TableEditor getTableEditorByName(final String name) {
		return MSECUInterface.tableEditors.get(name);
	}

	/**
	 * Get a curve editor from the ECU class.
	 * 
	 * @param name
	 *            The name of the curve editor object
	 * @return The curve editor object
	 */
	public CurveEditor getCurveEditorByName(final String name) {
		return MSECUInterface.curveEditors.get(name);
	}

	/**
	 * Get a list of menus from the ECU class.
	 * 
	 * @param name
	 *            The name of the menu tree
	 * @return A list of menus object
	 */
	public List<Menu> getMenusForDialog(final String name) {
		return MSECUInterface.menus.get(name);
	}

	/**
	 * Get a dialog from the ECU class.
	 * 
	 * @param name
	 *            The name of the dialog object
	 * @return The dialog object
	 */
	public MSDialog getDialogByName(final String name) {
		return MSECUInterface.dialogs.get(name);
	}

	/**
	 * Get a visibility flag for a user defined (dialog, field, panel, etc).  Used
	 * for field in dialog, for example.
	 * 
	 * @param name
	 *            The name of the user defined flag
	 * @return true if visible, false otherwise
	 */
	public boolean getUserDefinedVisibilityFlagsByName(final String name) {
		if (MSECUInterface.userDefinedVisibilityFlags.containsKey(name)) {
			return MSECUInterface.userDefinedVisibilityFlags.get(name);
		}

		return true;
	}

	/**
	 * Get a visibility flag for a menu.
	 * 
	 * @param name
	 *            The name of the menu flag
	 * @return true if visible, false otherwise
	 */
	public boolean getMenuVisibilityFlagsByName(final String name) {
		return MSECUInterface.menuVisibilityFlags.get(name);
	}

	/**
	 * Add a dialog to the list of dialogs in the ECU class.
	 * 
	 * @param dialog
	 *            The dialog object to add
	 */
	public void addDialog(final MSDialog dialog) {
		MSECUInterface.dialogs.put(dialog.getName(), dialog);
	}

	/**
	 * Add a curve to the list of curves in the ECU class.
	 * 
	 * @param curve
	 *            The curve object to add
	 */
	public void addCurve(final CurveEditor curve) {
		MSECUInterface.curveEditors.put(curve.getName(), curve);
	}

	/**
	 * Add a constant to the list of constants in the ECU class.
	 * 
	 * @param constant
	 *            The constant object to add
	 */
	public void addConstant(final Constant constant) {
		MSECUInterface.constants.put(constant.getName(), constant);
	}

	/**
	 * Used to get a list of all constants name used in a specific dialog.
	 * 
	 * @param dialog
	 *            The dialog to get the list of constants name
	 * @return A list of constants name
	 */
	public List<String> getAllConstantsNamesForDialog(final MSDialog dialog) {
		final List<String> constants = new ArrayList<String>();
		return buildListOfConstants(constants, dialog);
	}
	
	public int getBlockSize() {
		return ecuImplementation.getBlockSize();
	}

	public int getCurrentTPS() {
		return ecuImplementation.getCurrentTPS();
	}

	public String getLogHeader() {
		return ecuImplementation.getLogHeader();
	}

	public void refreshFlags() {
		ecuImplementation.refreshFlags();
	}

	public void setMenuVisibilityFlags() {
		ecuImplementation.setMenuVisibilityFlags();
	}

	public void setUserDefinedVisibilityFlags() {
		ecuImplementation.setUserDefinedVisibilityFlags();

	}

	public String[] getControlFlags() {
		return ecuImplementation.getControlFlags();
	}

	public List<String> getRequiresPowerCycle() {
		return ecuImplementation.getRequiresPowerCycle();
	}

	public List<SettingGroup> getSettingGroups() {
		ecuImplementation.createSettingGroups();
		return ecuImplementation.getSettingGroups();
	}

	public Map<String, String> getControllerCommands() {
		ecuImplementation.createControllerCommands();
		return ecuImplementation.getControllerCommands();
	}

	/**
	 * Helper functions to get specific value out of ECU Different MS version
	 * have different name for the same thing so get the right one depending on
	 * the MS version we're connected to
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

	public double getField(final String channelName) {
		return DataManager.getInstance().getField(channelName);
	}
    
	/**
	 * Add a command for the ECUThread to process when it can
	 * 
	 * @param command
	 */
	public void injectCommand(final InjectedCommand command) {
		injectionQueue.add(command);
	}

	/**
	 * Returns the true signature of the Megasquirt firmware with which we are
	 * communicating.
	 */
	public String getTrueSignature() {
		return trueSignature;
	}

	/**
	 * Write a constant back to the ECU
	 * 
	 * @param constant
	 *            the constant to write
	 */
	public void writeConstant(final Constant constant) {
		final List<String> pageIdentifiers = ecuImplementation
				.getPageIdentifiers();
		final List<String> pageValueWrites = ecuImplementation
				.getPageValueWrites();

		// Ex: U08, S16
		final String type = constant.getType();

		// 8 bits = 1 byte by default
		int size = 1;
		if (type.contains("16")) {
			size = 2; // 16 bits = 2 bytes
		}

		final int pageNo = constant.getPage();
		final int offset = constant.getOffset();

		int[] msValue = null;

		// Constant to write is of type scalar or bits
		if (constant.getClassType().equals("scalar")
				|| constant.getClassType().equals("bits")) {
			msValue = new int[1];
			msValue[0] = (int) getField(constant.getName());
		}
		// Constant to write to ECU is of type array
		else if (constant.getClassType().equals("array")) {
			final int shape[] = MSUtilsShared.getArraySize(constant.getShape());

			final int width = shape[0];
			final int height = shape[1];

			// Vector
			if (height == -1) {
				size *= width;
				msValue = getVector(constant.getName());
			}
			// Array
			else {
				// Flatten array into msValue
				final int[][] array = getArray(constant.getName());
				int i = 0;

				size *= width * height;
				msValue = new int[width * height];

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						msValue[i++] = array[x][y];
					}
				}

			}
		}

        // Make sure we have something to send to the MS
        if ((msValue != null) && (msValue.length > 0)) {
            final String writeCommand = pageValueWrites.get(pageNo - 1);
			final String command = MSUtilsShared.HexStringToBytes(
					pageIdentifiers, writeCommand, offset, size, msValue,
					pageNo);
            final byte[] byteCommand = MSUtils.INSTANCE.commandStringtoByteArray(command);

            if (LOG.isDebugEnabled()) {
	            LOG.debug(
	            		"Writing to MS: command: {} constant: {} msValue: {} pageValueWrite: {} "
	            				+ "offset: {} count: {} pageNo: {}",
        				new Object[] {command, constant.getName(), Arrays.toString(msValue), 
	            						writeCommand, offset, size, pageNo});
            }
            
            final List<byte[]> pageActivates = ecuImplementation.getPageActivates();

            try {
                final int delay = ecuImplementation.getPageActivationDelay();

                // MS1 use page select command
                if (pageActivates.size() >= pageNo) {
                    final byte[] pageSelectCommand = pageActivates.get(pageNo - 1);
                    
                    ioManager.write(pageSelectCommand, getProtocolHandler());
                    // TODO perform delay here using delay
                }

                final InjectedCommand writeToRAM = new InjectedCommand(byteCommand, 300, false, 0);
                injectCommand(writeToRAM);

                LOG.debug("Writing constant {} to Megasquirt.", constant.getName());
            } catch (IOException e) {
            	LOG.error("Error writing constant to Megasquirt.", e);
            }

            burnPage(pageNo);
        }
        // Nothing to send to the MS, maybe unsupported constant type ?
        else {
        	LOG.debug("Couldn't find any value to write, maybe unsupported constant type {}.", constant.getType());
        }
    }
    
	/**
	 * Get an array from the ECU.
	 * 
	 * @param channelName
	 *            the variable name to modify
	 */
    public int[][] getArray(final String channelName)
    {
        int[][] value = { { 0 }, { 0 } };
        final Class<?> c = ecuImplementation.getClass();
        try {
            final Field f = c.getDeclaredField(channelName);
            value = (int[][]) f.get(ecuImplementation);
        } catch (Exception e) {
        	// TODO what to do with the error?
        	LOG.error("Failed to get array value for " + channelName + ".", e);
        }
        return value;
    }

	/**
	 * Get a vector from the ECU.
	 * 
	 * @param channelName
	 *            the variable name to modify
	 */
    public int[] getVector(final String channelName)
    {
        int[] value = { 0 };
        final Class<?> c = ecuImplementation.getClass();
        try {
            final Field f = c.getDeclaredField(channelName);
            value = (int[]) f.get(ecuImplementation);
        } catch (final Exception e) {
        	// TODO what to do with the error?
        	LOG.error("Failed to get vector value for " + channelName + ".", e);
        }
        return value;
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

	/**
	 * Set a vector based value in the ECU class.
	 * 
	 * @param channelName
	 *            the variable name to modify
	 * @param xBins
	 *            the value to set
	 */
	public void setVector(final String channelName, final int[] xBins) {
		final Class<?> c = ecuImplementation.getClass();

		try {
			final Field f = c.getDeclaredField(channelName);
			f.set(ecuImplementation, xBins);
		} catch (final NoSuchFieldException e) {
			LOG.error("Failed to set value to " + xBins + " for " + channelName
					+ ".", e);

		} catch (final IllegalArgumentException e) {
			LOG.error("Failed to set value to " + xBins + " for " + channelName
					+ ".", e);
		} catch (final IllegalAccessException e) {
			LOG.error("Failed to set value to " + xBins + " for " + channelName
					+ ".", e);
		}
	}

	/**
	 * Set an array based value in the ECU class.
	 * 
	 * @param channelName
	 *            the variable name to modify
	 * @param zBins
	 *            the value to set
	 */
	public void setArray(final String channelName, final int[][] zBins) {
		final Class<?> c = ecuImplementation.getClass();

		try {
			final Field f = c.getDeclaredField(channelName);
			f.set(ecuImplementation, zBins);
		} catch (final NoSuchFieldException e) {
			LOG.error("Failed to set value to " + zBins + " for " + channelName
					+ ".", e);

		} catch (final IllegalArgumentException e) {
			LOG.error("Failed to set value to " + zBins + " for " + channelName
					+ ".", e);
		} catch (final IllegalAccessException e) {
			LOG.error("Failed to set value to " + zBins + " for " + channelName
					+ ".", e);
		}
	}
	
	/**
	 * Helper function for getAllConstantsNamesForDialog() which builds the
	 * array of constants name.
	 * 
	 * @param constants
	 * @param dialog
	 */
	private List<String> buildListOfConstants(final List<String> constants,
			final MSDialog dialog) {
		for (final DialogField df : dialog.getFieldsList()) {
			if (!df.getName().equals("null")) {
				constants.add(df.getName());
			}
		}

		for (final DialogPanel dp : dialog.getPanelsList()) {
			final MSDialog dialogPanel = this.getDialogByName(dp.getName());

			if (dialogPanel != null) {
				buildListOfConstants(constants, dialogPanel);
			}
		}

		return constants;
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
     * Output the current values to be logged.
     */
    private void logValues(final byte[] buffer) {
        if (isLogging()) {
        	try {
                log.write(this);
            } catch (IOException e) {
                LOG.error("Error writing to log.", e);
            } 
        }
    }
    
    /**
	 * Read a page of constants from the ECU into a byte buffer. MS1 uses a
	 * select/read combo, MS2 just does a read.
	 * 
	 * @param pageBuffer
	 *            the buffer to read into
	 * @param pageSelectCommand
	 *            the command to select the page to read
	 * @param pageReadCommand
	 *            the command to read the page
	 * @throws IOException
	 *             if there is an error processing the request
	 */
	protected void getPage(final byte[] pageBuffer,
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
	 * Get the current variables from the ECU.
	 * 
	 * @throws IOException
	 *             if there is an error processing the request
	 */
	private IoManagerResult getRuntimeVars() throws IOException {
		final byte[] buffer = new byte[ecuImplementation.getBlockSize()];

		final int delay = ecuImplementation.getInterWriteDelay();
		
		IoManagerResult result = ioManager.writeAndRead(
				ecuImplementation.getOchCommand(), buffer, delay, getProtocolHandler());
		return result;
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
						LOG.error("Unable to create directory MSLogger at {}.",
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
     * Burn a page from MS RAM to Flash.
     * 
     * @param pageNo The page number to burn
     */
    private void burnPage(final int pageNo) {
        // Convert from page to table index that the ECU understand
        final List<String> pageIdentifiers = ecuImplementation.getPageIdentifiers();

        final String pageIdentifier = pageIdentifiers.get(pageNo - 1).replace("\\$tsCanId\\", "");

        final byte tblIdx = (byte) MSUtilsShared.HexByteToDec(pageIdentifier);

		LOG.debug("Burning page {} (Page identifier: {} - Table index: {})",
				new Object[] { pageNo, pageIdentifier, tblIdx });

        // Send "b" command for the tblIdx
		final InjectedCommand burnToFlash = new InjectedCommand(new byte[] {
				98, 0, tblIdx }, 300, true, Megasquirt.BURN_DATA);
        injectCommand(burnToFlash);
    }
    
    /**
     * Probes the ECU for the firmware signature and instantiates the current implementation
     * class to talk to the ECU.
     *
     * @throws Exception if there is an error initializing the implementation
     */
    private void initialiseImplementation() throws Exception
    {
    	LOG.debug("Checking your ECU.");
    	
    	try {
	    	MsEcuInterfaceFactory factory = MsEcuInterfaceFactory.getInstance();
	    	String signature = factory.getSignature(ioManager);
	    	ecuImplementation = factory.getMegasquirt(signature, this);
	    	
			if (!signature.equals(ecuImplementation.getSignature())) {
				trueSignature = ecuImplementation.getSignature();
				if (LOG.isInfoEnabled()) {
					LOG.info(
							"Got unsupported signature from Megasquirt \"{}\""
									+ "but found a similar supported signature \"{}\"",
							trueSignature, signature);
				}
			}
		} catch (Exception e) {
			LOG.error("Error constructing instance of ecu implementation.", e);
			// TODO what to do here?
		}
    }
    
    

    private class ECUThread extends GracefulShutdownThread {
    	
    	private class LogThread extends GracefulShutdownThread {

    		public LogThread() {
    			super("ECU Log Thread "
    					+ LOG_THREAD_INSTANCE_COUNTER.getAndIncrement());
    		}

    		@Override
    		public void run() {
    			int consecutiveErrorCount = 0;

    			try {
    				try {
    					while (keepRunning()) {
    						final byte[] buffer = handshake.get();
    						if (ecuImplementation != null) {
    							ecuImplementation.calculate(buffer);
    							logValues(buffer);
    						}
    					}
    				} catch (Exception e) {
    					if (!keepRunning()) {
    						LOG.info("Error while stopping logging thread.");
    					} else {
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
    			}
    		}
    	}
		
        private class Handshake
        {
            private byte[] buffer;

            public void put(final byte[] buf)
            {
                buffer = buf;
                synchronized (this)
                {
                    notify();
                }
            }

            public byte[] get() throws InterruptedException
            {
                synchronized (this)
                {
                    wait();
                }
                return buffer;
            }
        }

        private Handshake handshake = new Handshake();
        private LogThread calculationThread = new LogThread();

        public ECUThread() {
            final String name = "ECUThread:" + System.currentTimeMillis();
            setName(name);
            LOG.debug("Creating ECUThread named " + name);
            calculationThread.start();
        }

        /**
         * The main loop of the connection to the ECU
         */
        @Override
        public void run() {
        	int consecutiveErrorCount = 0;
        	
            try {
                setState(Megasquirt.MegasquirtState.CONNECTING);
                LOG.debug("Starting connection {}.", getName());
                ioManager.connect();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                	LOG.warn("Interrupted while sleeping after initialization.");
                }

                try {
                	ioManager.flushAll();
                    initialiseImplementation();
					/*
					 * Make sure we have calculated runtime vars at least once
					 * before refreshing flags. The reason is that the
					 * refreshFlags() function also trigger the creation of
					 * menus/dialogs/tables/curves/etc that use variables such
					 * as {clthighlim} in curves that need to have their value
					 * assigned before being used.
					 */
                    try {
                        final IoManagerResult result = getRuntimeVars();
                        ecuImplementation.calculate(result.getResult());
                    } catch (IOException e) {
                    	LOG.error("Error reading initial runtime vars.", e);
                    	// TODO should we die here?
                    }

                    ecuImplementation.refreshFlags();

                    if (!constantsLoaded) {
                        // Only do this once so reconnects are quicker
                        ecuImplementation.loadConstants();
                        constantsLoaded = true;
                    }
                    
                    LOG.debug("Connected to " + getTrueSignature());
                    setState(Megasquirt.MegasquirtState.CONNECTED);
                    
					// This is the actual work. Outside influences will toggle
					// 'running' when we want this to stop
					while ((currentState == Megasquirt.MegasquirtState.CONNECTED)
							|| (currentState == Megasquirt.MegasquirtState.LOGGING)) {
                        
						if (injectionQueue.peek() != null) {
                            for (final InjectedCommand i : injectionQueue) {
                                processCommand(i);
                            }

                            injectionQueue.clear();
                        }
                        
                        final IoManagerResult result = getRuntimeVars(); 
                        handshake.put(result.getResult());
                    }
                } catch (final ArithmeticException e) {
                	// If we get a maths error, we probably have loaded duff constants and hit a divide by zero
                    // force the constants to reload in case it was just a bad data read
                	
                	if (consecutiveErrorCount > 5) {
                        throw e;
                	} else {
                		LOG.warn("Arithmetic error in ECU thread.  Attempting to reload constants.", e);
                        constantsLoaded = false;
                	}
                } catch (Exception e) {
                    if (consecutiveErrorCount > 5) {
                        throw e;
                    } else {
                        consecutiveErrorCount += 1;
                        LOG.warn("Encountered " + consecutiveErrorCount
                                + " consecutive error(s) in ecu thread.", e);
                    }
                }
            } finally {
                calculationThread.cancel();
                watch = null;
            }
        }

        private void processCommand(final InjectedCommand i) throws IOException
        {
        	ioManager.write(i.getCommand(), getProtocolHandler());
        	
        	if (i.getDelay() > 0) {
        		try {
					Thread.sleep(i.getDelay());
				} catch (InterruptedException e) {
					throw new IOException("Interrupted while processing command.", e);
				}
        	}

            // If we want to get the result back
            if (i.isReturnResult()) {
            	// TODO provide a response mechanism
//                final Intent broadcast = new Intent();
//                broadcast.setAction(INJECTED_COMMAND_RESULTS);

                final IoManagerResult result = ioManager.read();
                

//                broadcast.putExtra(INJECTED_COMMAND_RESULT_ID, i.getResultId());
//                broadcast.putExtra(INJECTED_COMMAND_RESULT_DATA, result);
//
//                sendBroadcast(broadcast);
            }
        }

        

		

		
    }
}
