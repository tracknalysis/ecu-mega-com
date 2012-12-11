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
    public abstract String getSignature();

    public abstract byte[] getOchCommand();

    public abstract byte[] getSigCommand();

    public abstract void loadConstants(boolean simulated) throws IOException;

    public abstract void calculate(byte[] ochBuffer);

    public abstract String getLogHeader();

    public abstract String getLogRow();

    public abstract int getBlockSize();

    public abstract int getSigSize();

    public abstract int getPageActivationDelay();
    
    public abstract List<String> getPageIdentifiers();
    
    public abstract List<byte[]> getPageActivates();
    
    public abstract List<String> getPageValueWrites();
    
    public abstract List<String> getPageChunkWrites();

    public abstract int getInterWriteDelay();

    public abstract int getCurrentTPS();

    public abstract void refreshFlags();

    public abstract boolean isCRC32Protocol();

    public abstract void createTableEditors();
    
    public abstract void createCurveEditors();
    
    public abstract void createMenus();
    
    public abstract void createDialogs();
    
    public abstract void setUserDefinedVisibilityFlags();
    
    public abstract void setMenuVisibilityFlags();
    
    public abstract String[] getControlFlags();

    public abstract void createSettingGroups();
    
    public abstract List<SettingGroup> getSettingGroups();
    
    public abstract List<String> getRequiresPowerCycle();
    
    public abstract void createControllerCommands();
    
    public abstract Map<String, String> getControllerCommands();
    
}
