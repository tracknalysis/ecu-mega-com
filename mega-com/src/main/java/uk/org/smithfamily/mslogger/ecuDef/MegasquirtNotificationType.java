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
package uk.org.smithfamily.mslogger.ecuDef;

import java.util.HashMap;
import java.util.Map;

import net.tracknalysis.common.notification.NotificationType;

/**
 * @author David Valeri
 */
public enum MegasquirtNotificationType implements NotificationType {
	CONNECTING,
	CONNECTED,
	CONNECTION_FAILED,
	LOGGING_STARTING,
	LOGGING_STARTED,
	LOGGING_STOPPING,
	LOGGING_STOPPED,
	DISCONNECTING,
	DISCONNECTED,
	DISCONNECTION_FAILED;

	private static final Map<Integer, MegasquirtNotificationType> intToTypeMap = 
			new HashMap<Integer, MegasquirtNotificationType>();
    
    static {
        for (MegasquirtNotificationType type : MegasquirtNotificationType.values()) {
            intToTypeMap.put(type.ordinal(), type);
        }
    }

    public static MegasquirtNotificationType fromInt(int i) {
    	MegasquirtNotificationType type = intToTypeMap.get(Integer.valueOf(i));
        if (type == null) {
            throw new IllegalArgumentException(
                    "No enum const " + i);
        }
        return type;
    }

    @Override
    public int getNotificationTypeId() {
        return ordinal();
    }	
}
