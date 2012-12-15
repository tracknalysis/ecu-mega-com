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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.smithfamily.mslogger.ecuDef.MSControllerInterface;
import uk.org.smithfamily.mslogger.ecuDef.MSECUInterface;
import uk.org.smithfamily.mslogger.ecuDef.MSUtils;
import uk.org.smithfamily.mslogger.ecuDef.MSUtilsInterface;
import uk.org.smithfamily.mslogger.ecuDef.gen.ECURegistry;


/**
 * Concrete implementation of the factory based on the generated {@link ECURegistry}.
 *
 * @author David Smith
 * @author David Valeri
 */
public class DefaultMsEcuInterfaceFactory extends MsEcuInterfaceFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMsEcuInterfaceFactory.class);
    
    @Override
	public MSECUInterface getMegasquirt(String signature,
			MSControllerInterface parent) throws MsEcuInterfaceFactoryException {
        
    	MSECUInterface result;
    	
    	final Class<? extends MSECUInterface> ecuClass = ECURegistry.INSTANCE
				.findEcu(signature);
    	
    	Constructor<? extends MSECUInterface> constructor;
		try {
			constructor = ecuClass.getConstructor(MSControllerInterface.class,
					MSUtilsInterface.class);

			result = constructor.newInstance(parent,
					MSUtils.INSTANCE);
		} catch (Exception e) {
			LOG.error("Error constructing instance of {}.", ecuClass);
			throw new MsEcuInterfaceFactoryException(e);
		}
		
		return result;
    }
}
