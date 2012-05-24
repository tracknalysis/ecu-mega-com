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
package net.tracknalysis.ms.ecu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Smith
 * @author David Valeri
 */
public class DefaultTableManager implements TableManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTableManager.class);
    private static final String ASSET_DIR = "tables";
    private static final Pattern LINE_PATTERN = Pattern.compile("\\s*[Dd][BbWw]\\s*(\\d*).*");
    
    private Map<String, List<Integer>>	tables	= new HashMap<String, List<Integer>>();
    
    private String assetOverrideDir;
    
    public DefaultTableManager() {
        this(null);
    }
    
    public DefaultTableManager(String assetOverrideDir) {
        this.assetOverrideDir = assetOverrideDir;
    }

    @Override
    public synchronized void flushTable(String name) {
        tables.remove(name);
    }

    @Override
    public synchronized int table(int index, String name) {
        List<Integer> table = tables.get(name);
        if (table == null) {
            table = new ArrayList<Integer>();
            readTable(name, table);
            tables.put(name, table);
        }
        return table.get(index);
    }
    
    protected Map<String, List<Integer>> getTables() {
        return tables;
    }

    protected String getAssetOverrideDir() {
        return assetOverrideDir;
    }

    protected void readTable(String fileName, List<Integer> values) {
        
		File  override = new File(assetOverrideDir, fileName);

        BufferedReader input = null;
        try {
            try {
                InputStream data = null;
                if (override.canRead()) {
                    data = new FileInputStream(override);
                } else {
                    data = getClass().getClassLoader().getResourceAsStream(ASSET_DIR + "/" + fileName);
                }

                input = new BufferedReader(new InputStreamReader(data));
                String line;

                while ((line = input.readLine()) != null) {
                    Matcher matcher = LINE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String num = matcher.group(1);

                        if (num != null) {
                            values.add(Integer.valueOf(num));
                        }
                    }
                }

            } finally {
                if (input != null) {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException e) {
                        LOG.warn("Error closing table inputstream.", e);
                    }
                }
            }

        } catch (IOException e) {
            LOG.error("Error loading table " + fileName + ".", e);
        }
    }
}
