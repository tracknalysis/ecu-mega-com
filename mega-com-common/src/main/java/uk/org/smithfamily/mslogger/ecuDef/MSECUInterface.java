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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MSECUInterface
{
    Map<String,Constant> constants = new HashMap<String,Constant>();

    Map<String,TableEditor> tableEditors = new HashMap<String,TableEditor>();
    
    Map<String,CurveEditor> curveEditors = new HashMap<String,CurveEditor>();
    
    Map<String,List<Menu>> menus = new HashMap<String,List<Menu>>();
    
    Map<String,MSDialog> dialogs = new HashMap<String,MSDialog>();
    
    Map<String,Boolean> userDefinedVisibilityFlags = new HashMap<String,Boolean>();
    
    Map<String,Boolean> menuVisibilityFlags = new HashMap<String,Boolean>();
    
    Map<String,OutputChannel> outputChannels = new HashMap<String,OutputChannel>();
    
    List<SettingGroup> settingGroups = new ArrayList<SettingGroup>();
    
    Map<String,String> controllerCommands = new HashMap<String,String>();

    void setFlags();
    String getSignature();

    byte[] getOchCommand();

    byte[] getSigCommand();

    void loadConstants() throws IOException;

    void calculate(byte[] ochBuffer);

    String getLogHeader();

    String getLogRow();

    int getBlockSize();

    int getSigSize();

    int getPageActivationDelay();
    
    List<String> getPageIdentifiers();
    
    List<byte[]> getPageActivates();
    
    List<String> getPageValueWrites();
    
    List<String> getPageChunkWrites();

    int getInterWriteDelay();

    int getCurrentTPS();

    void refreshFlags();

    boolean isCRC32Protocol();

    void createTableEditors();
    
    void createCurveEditors();
    
    void createMenus();
    
    void createDialogs();
    
    void setUserDefinedVisibilityFlags();
    
    void setMenuVisibilityFlags();
    
    String[] getControlFlags();

    void createSettingGroups();
    
    List<SettingGroup> getSettingGroups();
    
    List<String> getRequiresPowerCycle();
    
    void createControllerCommands();
    
    Map<String, String> getControllerCommands();
    
}
