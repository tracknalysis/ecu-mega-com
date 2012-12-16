/**
 * Copyright 2012 David Smith.
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
package net.tracknalysis.ecu.ms.io;

import java.io.IOException;
import java.io.OutputStream;

import net.tracknalysis.common.io.DirectIoManager;
import net.tracknalysis.common.io.IoManager;
import net.tracknalysis.common.io.IoManagerResult;
import net.tracknalysis.common.io.IoProtocolHandler;
import net.tracknalysis.common.io.SocketManager;

// TODO: Refactor support for the hack into the API so we don't need this class
/**
 * Custom {@link IoManager} that handles edge cases.  Adapted from MSLogger to handle edge cases
 * in IO with the Megasquirt. 
 *
 * @author David Smith
 * @author David Valeri
 */
public class MsIoManager extends DirectIoManager {

	public MsIoManager(SocketManager delegate) {
		super(delegate);
	}
	
	@Override
	public synchronized IoManagerResult write(byte[] command,
			IoProtocolHandler protocolHandler) throws IOException {
		
		IoManagerResult result = new IoManagerResult();
        result.setRequestStartTime(System.currentTimeMillis());
        
        OutputStream os = getOutputStream();
        
        byte[] bytesToSend = command;
        if (protocolHandler != null) {
        	bytesToSend = protocolHandler.wrapRequest(bytesToSend);
        }
        
        result.setRequestTxStartTime(System.currentTimeMillis());
        synchronized (this) {
        	
			if (bytesToSend.length == 7
					&& (bytesToSend[0] == 'r' || bytesToSend[0] == 'w' || bytesToSend[0] == 'e')) {
                // MS2 hack
                byte[] select = new byte[3];
                byte[] range = new byte[4];
                System.arraycopy(bytesToSend, 0, select, 0, 3);
                System.arraycopy(bytesToSend, 3, range, 0, 4);
                os.write(select);
                delay(200);
                os.write(range);
        	} else {
        		os.write(bytesToSend);
        	}
	        os.flush();
        }
        result.setRequestTxEndTime(System.currentTimeMillis());
        
        getLogger().debug("Wrote bytes {}", command);
        
        result.setRequestRxStartTime(result.getRequestTxEndTime());
        result.setRequestRxEndTime(result.getRequestTxEndTime());
        result.setRequestEndTime(System.currentTimeMillis());
        
        return result;
	}
}
