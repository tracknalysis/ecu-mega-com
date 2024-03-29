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
package net.tracknalysis.ecu.ms.normaliser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tracknalysis.ecu.ms.common.Constant;
import net.tracknalysis.ecu.ms.common.OutputChannel;
import net.tracknalysis.ecu.ms.common.SettingGroup;

import net.tracknalysis.ecu.ms.normaliser.controllercommand.ControllerCommand;
import net.tracknalysis.ecu.ms.normaliser.curveeditor.CurveTracker;
import net.tracknalysis.ecu.ms.normaliser.menu.MenuTracker;
import net.tracknalysis.ecu.ms.normaliser.tableeditor.TableTracker;
import net.tracknalysis.ecu.ms.normaliser.userdefined.UserDefinedTracker;

public class ECUData
{
    private List<String> runtime = new ArrayList<String>();
    private List<String> logHeader = new ArrayList<String>();
    private List<String> logRecord = new ArrayList<String>();
    private List<TableTracker> tableDefs = new ArrayList<TableTracker>();
    private List<CurveTracker> curveDefs = new ArrayList<CurveTracker>();
    private List<MenuTracker> menuDefs = new ArrayList<MenuTracker>();
    private List<UserDefinedTracker> dialogDefs = new ArrayList<UserDefinedTracker>();
    private Map<String, String> fieldControlExpressions;
    private Map<String, String> menuControlExpressions;
    private Map<String, String> runtimeVars;
    private Map<String, String> evalVars;
    private Map<String, String> constantVars;
    private List<String> defaults;
    private List<String> requiresPowerCycle;
    private Set<String> flags;
    private String fingerprintSource;
    private ArrayList<String> gaugeDoc;
    private ArrayList<Constant> constants;
    private ArrayList<OutputChannel> outputChannels;
    private ArrayList<ControllerCommand> controllerCommands;
    private ArrayList<String> pageSizes;
    private ArrayList<String> pageIdentifiers;
    private ArrayList<String> pageActivateCommands;
    private ArrayList<String> pageReadCommands;
    private List<String> pageValueWrites;
    private List<String> pageChunkWrites;
    private String signatureDeclaration;
    private String queryCommandStr;
    private String ochGetCommandStr;
    private String ochBlockSizeStr;
    private ArrayList<String> defaultGauges;
    private boolean isCRC32Protocol;
    private int currentPage = 0;
    private int interWriteDelay;
    private int pageActivationDelayVal;
    private String classSignature;
    private ArrayList<SettingGroup> settingGroups;

    public List<String> getRuntime()
    {
        return runtime;
    }

    public void setRuntime(List<String> runtime)
    {
        this.runtime = runtime;
    }

    public List<String> getLogHeader()
    {
        return logHeader;
    }

    public void setLogHeader(List<String> logHeader)
    {
        this.logHeader = logHeader;
    }

    public List<String> getLogRecord()
    {
        return logRecord;
    }

    public void setLogRecord(List<String> logRecord)
    {
        this.logRecord = logRecord;
    }

    public void reset()
    {
        runtime = new ArrayList<String>();
        logHeader = new ArrayList<String>();
        logRecord = new ArrayList<String>();
        fieldControlExpressions = new HashMap<String, String>();
        menuControlExpressions = new HashMap<String, String>();
        runtimeVars = new HashMap<String, String>();
        evalVars = new HashMap<String, String>();
        constantVars = new HashMap<String, String>();
        defaults = new ArrayList<String>();
        requiresPowerCycle = new ArrayList<String>();
        constants = new ArrayList<Constant>();
        outputChannels = new ArrayList<OutputChannel>();
        controllerCommands = new ArrayList<ControllerCommand>();
        flags = new HashSet<String>();
        gaugeDoc = new ArrayList<String>();
        defaultGauges = new ArrayList<String>();
        pageActivateCommands = new ArrayList<String>();
        pageValueWrites = new ArrayList<String>();
        pageChunkWrites = new ArrayList<String>();
        pageIdentifiers = new ArrayList<String>();
        tableDefs = new ArrayList<TableTracker>();
        curveDefs = new ArrayList<CurveTracker>();
        menuDefs = new ArrayList<MenuTracker>();
        dialogDefs = new ArrayList<UserDefinedTracker>();
        settingGroups = new  ArrayList<SettingGroup>();
        fingerprintSource = "";
        currentPage = 0;
        isCRC32Protocol = false;
        // Default for those who don't define it. I'm looking at you
        // megasquirt-I.ini!
        ochGetCommandStr = "byte [] ochGetCommand = new byte[]{'A'};";
        evalVars.put("veTuneValue", "int");
    }

    public Map<String, String> getRuntimeVars()
    {
        return runtimeVars;
    }

    public void setRuntimeVars(Map<String, String> runtimeVars)
    {
        this.runtimeVars = runtimeVars;
    }

    public Map<String, String> getEvalVars()
    {
        return evalVars;
    }

    public void setEvalVars(Map<String, String> evalVars)
    {
        this.evalVars = evalVars;
    }

    public Map<String, String> getConstantVars()
    {
        return constantVars;
    }

    public void setConstantVars(Map<String, String> constantVars)
    {
        this.constantVars = constantVars;
    }

    public List<String> getDefaults()
    {
        return defaults;
    }

    public void setDefaults(List<String> defaults)
    {
        this.defaults = defaults;
    }
    
    public List<String> getRequiresPowerCycle()
    {
        return requiresPowerCycle;
    }
    
    public void setRequiresPowerCycle(List<String> requiresPowerCycle)
    {
        this.requiresPowerCycle = requiresPowerCycle;
    }

    public Set<String> getFlags()
    {
        return flags;
    }

    public void setFlags(Set<String> flags)
    {
        this.flags = flags;
    }

    public String getFingerprintSource()
    {
        return fingerprintSource;
    }

    public void setFingerprintSource(String fingerprintSource)
    {
        this.fingerprintSource = fingerprintSource;
    }

    public ArrayList<String> getGaugeDoc()
    {
        return gaugeDoc;
    }

    public void setGaugeDoc(ArrayList<String> gaugeDoc)
    {
        this.gaugeDoc = gaugeDoc;
    }

    public ArrayList<Constant> getConstants()
    {
        return constants;
    }
    
    public void setConstants(ArrayList<Constant> constants)
    {
        this.constants = constants;
    }
    
    public ArrayList<OutputChannel> getOutputChannels()
    {
        return outputChannels;
    }
    
    public void setOutputChannels(ArrayList<OutputChannel> outputChannels)
    {
        this.outputChannels = outputChannels;
    }
    
    public ArrayList<ControllerCommand> getControllerCommands()
    {
        return controllerCommands;
    }
    
    public void setControllerCommands(ArrayList<ControllerCommand> controllerCommands)
    {
        this.controllerCommands = controllerCommands;
    }

    public ArrayList<String> getPageSizes()
    {
        return pageSizes;
    }

    public void setPageSizes(ArrayList<String> pageSizes)
    {
        this.pageSizes = pageSizes;
    }

    public ArrayList<String> getPageIdentifiers()
    {
        return pageIdentifiers;
    }

    public void setPageIdentifiers(ArrayList<String> pageIdentifiers)
    {
        this.pageIdentifiers = pageIdentifiers;
    }

    public ArrayList<String> getPageActivateCommands()
    {
        return pageActivateCommands;
    }

    public void setPageActivateCommands(ArrayList<String> pageActivateCommands)
    {
        this.pageActivateCommands = pageActivateCommands;
    }

    public ArrayList<String> getPageReadCommands()
    {
        return pageReadCommands;
    }

    public void setPageReadCommands(ArrayList<String> pageReadCommands)
    {
        this.pageReadCommands = pageReadCommands;
    }

    public List<String> getPageValueWrites()
    {
        return pageValueWrites;
    }

    public void setPageValueWrites(List<String> pageValueWrites)
    {
        this.pageValueWrites = pageValueWrites;
    }
    
    public List<String> getPageChunkWrites()
    {
        return pageChunkWrites;
    }

    public void setPageChunkWrites(List<String> pageChunkWrites)
    {
        this.pageChunkWrites = pageChunkWrites;
    }
    
    public String getSignatureDeclaration()
    {
        return signatureDeclaration;
    }

    public void setSignatureDeclaration(String signatureDeclaration)
    {
        this.signatureDeclaration = signatureDeclaration;
    }

    public String getQueryCommandStr()
    {
        return queryCommandStr;
    }

    public void setQueryCommandStr(String queryCommandStr)
    {
        this.queryCommandStr = queryCommandStr;
    }

    public String getOchGetCommandStr()
    {
        return ochGetCommandStr;
    }

    public void setOchGetCommandStr(String ochGetCommandStr)
    {
        this.ochGetCommandStr = ochGetCommandStr;
    }

    public String getOchBlockSizeStr()
    {
        return ochBlockSizeStr;
    }

    public void setOchBlockSizeStr(String ochBlockSizeStr)
    {
        this.ochBlockSizeStr = ochBlockSizeStr;
    }

    public ArrayList<String> getDefaultGauges()
    {
        return defaultGauges;
    }

    public void setDefaultGauges(ArrayList<String> defaultGauges)
    {
        this.defaultGauges = defaultGauges;
    }

    public boolean isCRC32Protocol()
    {
        return isCRC32Protocol;
    }

    public void setCRC32Protocol(boolean isCRC32Protocol)
    {
        this.isCRC32Protocol = isCRC32Protocol;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    public int getInterWriteDelay()
    {
        return interWriteDelay;
    }

    public void setInterWriteDelay(int interWriteDelay)
    {
        this.interWriteDelay = interWriteDelay;
    }

    public int getPageActivationDelayVal()
    {
        return pageActivationDelayVal;
    }

    public void setPageActivationDelayVal(int pageActivationDelayVal)
    {
        this.pageActivationDelayVal = pageActivationDelayVal;
    }

    public String getClassSignature()
    {
        return classSignature;
    }

    public void setClassSignature(String classSignature)
    {
        this.classSignature = classSignature;
    }

    public List<TableTracker> getTableDefs()
    {
        return tableDefs;
    }

    public void setTableDefs(List<TableTracker> tableDefs)
    {
        this.tableDefs = tableDefs;
    }

    public List<CurveTracker> getCurveDefs()
    {
        return curveDefs;
    }

    public void setCurveDefs(List<CurveTracker> curveDefs)
    {
        this.curveDefs = curveDefs;
    }

    public List<MenuTracker> getMenuDefs()
    {
        return menuDefs;
    }

    public void setMenuDefs(List<MenuTracker> menuDefs)
    {
        this.menuDefs = menuDefs;
    }

    public List<UserDefinedTracker> getDialogDefs()
    {
        return dialogDefs;
    }

    public void setDialogDefs(List<UserDefinedTracker> dialogDefs)
    {
        this.dialogDefs = dialogDefs;
    }

    public Map<String, String> getFieldControlExpressions()
    {
        return fieldControlExpressions;
    }
    
    public Map<String, String> getMenuControlExpressions()
    {
        return menuControlExpressions;
    }

    public ArrayList<SettingGroup> getSettingGroups()
    {
        return settingGroups;
    }
}