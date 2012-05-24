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
package net.tracknalysis.ms.ecu;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.tracknalysis.ms.ecu.log.Log;
import net.tracknalysis.ms.io.MegasquirtIoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author David Valeri
 */
public class DefaultMegasquirtFactory extends MegasquirtFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMegasquirtFactory.class);
    
    Map<String, Class<? extends Megasquirt>> signatureMap = 
            new HashMap<String, Class<? extends Megasquirt>>();
    
    public DefaultMegasquirtFactory() {
        registerSignatures(ZZMS2ExtraSerial321.sigs, ZZMS2ExtraSerial321.class);
    }

    @Override
    public Megasquirt getMegasquirt(String signature,
            MegasquirtIoManager msIoManager, TableManager tableManager,
            Log logManager, MegasquirtConfiguration configuration) {
        
        Class<? extends Megasquirt> clazz = signatureMap.get(signature);
        return getMegasquirt(clazz, msIoManager, tableManager, 
                logManager, configuration);
    }
    
    protected Megasquirt getMegasquirt(Class<? extends Megasquirt> clazz,
            MegasquirtIoManager msIoManager, TableManager tableManager,
            Log logManager, MegasquirtConfiguration configuration) {
        
        Megasquirt megasquirt = null;
        
        if (clazz != null) {
            Constructor<? extends Megasquirt> constructor = null;
            try {
                constructor = clazz.getConstructor(MegasquirtIoManager.class,
                        DefaultTableManager.class, Log.class, MegasquirtConfiguration.class);
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if (constructor != null) {
                try {
                    megasquirt = constructor.newInstance(msIoManager,
                            tableManager, logManager, configuration);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return megasquirt;
    }
    
    protected void registerSignatures(Set<String> signatures, Class<? extends Megasquirt> clazz) {
        for (String sig : signatures) {
            LOG.debug("Registering signature {} to implementation {}.", sig, clazz);
            Object oldClazz = signatureMap.put(sig, clazz);
            if (oldClazz != null) {
                LOG.warn(
                        "Replaced implementation class {} with different implementation class "
                                + "{} for signature {}.", 
                        new Object[] {oldClazz, clazz, sig});
            }
        }
    }
}
