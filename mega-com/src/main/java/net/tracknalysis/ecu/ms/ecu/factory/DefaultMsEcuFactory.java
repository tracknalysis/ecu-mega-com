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

import java.lang.reflect.Constructor;

import net.tracknalysis.ecu.ms.common.MsController;
import net.tracknalysis.ecu.ms.common.MsEcu;
import net.tracknalysis.ecu.ms.ecu.EcuRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the factory based on the generated {@link ECURegistry}.
 *
 * @author David Smith
 * @author David Valeri
 */
public class DefaultMsEcuFactory extends MsEcuFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMsEcuFactory.class);
    
    @Override
	public MsEcu getMegasquirt(String signature,
			MsController parent) throws MsEcuFactoryException {
        
    	MsEcu result;
    	
    	final Class<? extends MsEcu> ecuClass = EcuRegistry.INSTANCE
				.findEcu(signature);
    	
    	Constructor<? extends MsEcu> constructor;
		try {
			constructor = ecuClass.getConstructor(MsController.class);

			result = constructor.newInstance(parent);
		} catch (Exception e) {
			LOG.error("Error constructing instance of {}.", ecuClass);
			throw new MsEcuFactoryException(e);
		}
		
		return result;
    }
}
