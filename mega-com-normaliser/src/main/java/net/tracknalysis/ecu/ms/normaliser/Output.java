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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.tracknalysis.ecu.ms.common.Constant;
import net.tracknalysis.ecu.ms.common.MSUtils;
import net.tracknalysis.ecu.ms.common.OutputChannel;

import org.apache.commons.lang3.StringUtils;

public class Output
{
    static final String        TAB          = "    ";
    private static final int   MAX_LINES    = 100;
    private static Set<String> alwaysInt    = new HashSet<String>(Arrays.asList(new String[] {}));
    private static Set<String> alwaysDouble = new HashSet<String>(Arrays.asList(new String[] { "pulseWidth", "throttle",
            "accDecEnrich", "accDecEnrichPcnt", "accEnrichPcnt", "accEnrichMS", "decEnrichPcnt", "decEnrichMS", "time",
            "egoVoltage", "egoVoltage2", "egoCorrection", "veCurr", "lambda", "TargetLambda" }));

    static void outputGaugeDoc(ECUData ecuData, PrintWriter writer)
    {
        writer.println("/*");
        for (String gauge : ecuData.getGaugeDoc())
        {
            writer.println(gauge);
        }

        writer.println("*/");
    }

    static void outputConstructor(ECUData ecuData, PrintWriter writer, String className)
    {
        writer.println(TAB+"private MsController parent;");
        writer.println(TAB+"private byte[] logData;");
        writer.println(TAB + "public " + className + "(MsController parent)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "this.parent = parent;");
        writer.println(TAB + TAB + "initOutputChannels();");
        writer.println(TAB + "}");
        writer.println(TAB +"private double table(double x,String t)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return parent.table(x,t);");
        writer.println(TAB + "}");
        writer.println(TAB +"private double round(double x)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return parent.round(x);");
        writer.println(TAB + "}");
        writer.println(TAB +"private double tempCvt(double x)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return parent.tempCvt(x);");
        writer.println(TAB + "}");
        writer.println(TAB +"private double timeNow()");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return parent.timeNow();");
        writer.println(TAB + "}");
                
        writer.println(TAB +"public boolean isConstantExists(final String name)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return constants.containsKey(name);");
        writer.println(TAB + "}");
        
        writer.println(TAB +"public Constant getConstantByName(final String name)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return constants.get(name);");
        writer.println(TAB + "}");
        
        writer.println(TAB +"public OutputChannel getOutputChannelByName(final String name)");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return outputChannels.get(name);");
        writer.println(TAB + "}");
    }

    /**
     * This is nasty. We need to have a set of methods to init the constants as there is a hard limit of 64K on the size of a method, and MS3 will break that. We also need to ensure that if there is
     * any preprocessor type logic that we don't stop and start the next method in the middle of it.
     * 
     * @param ecuData
     * @param writer
     */
    static void outputFlagsAndConstants(ECUData ecuData, PrintWriter writer)
    {
        int constantMethodCount = 0;
        int lineCount = 0;
        int bracketNesting = 0;
        boolean needDeclaration = true;
        int lookahead = 3;
        for (Constant c : ecuData.getConstants())
        {
            if (needDeclaration)
            {
                constantMethodCount++;
                writer.println(TAB + "private void initConstants" + constantMethodCount + "()\n" + TAB + "{\n");
                needDeclaration = false;
            }
            if (bracketNesting == 0 && lookahead > 0)
            {
                lookahead--;
            }
            lineCount++;

            if (c.getName().contains("{"))
            {
                bracketNesting++;
            }
            if (c.getName().contains("}"))
            {
                bracketNesting--;
            }

            if ("PREPROC".equals(c.getType()))
            {
                writer.println(TAB + TAB + c.getName());
                lookahead = 3;
            }
            else
            {
                writer.println(TAB + TAB + "constants.put(\"" + c.getName() + "\", new " + c.toString() + ");");
            }

            if (lineCount > MAX_LINES && bracketNesting == 0 && lookahead == 0)
            {
                writer.println(TAB + "}\n");
                needDeclaration = true;
                lineCount = 0;
            }
        }
        if (!needDeclaration)
        {
            writer.println(TAB + "}\n");
        }

        writer.println(TAB + "@Override");
        writer.println(TAB + "public void setFlags()");
        writer.println(TAB + "{");
        
        Map<String, String> vars = ecuData.getConstantVars();
        
        for (String flag : ecuData.getFlags())
        {
            // INI_VERSION_2 should always be true
            if (flag.equals("INI_VERSION_2"))
            {
                writer.println(TAB + TAB + "INI_VERSION_2 = true;");
            }
            // MEMPAGES, LOGPAGES and MSLVV_COMPATIBLE should always be false
            else if (flag.equals("MEMPAGES") || flag.equals("LOGPAGES") || flag.equals("MSLVV_COMPATIBLE"))
            {
                writer.println(TAB + TAB + flag + " = false;");
            }
            else if (flag.equals("SPEED_DENSITY"))
            {
                String varName = "algorithm1";
                
                // MS1 B&G
                if (vars.containsKey("algorithm"))
                {
                    varName = "algorithm";
                }
                
                writer.println(TAB + TAB + "SPEED_DENSITY = (" + varName + " == 1);"); 
            }
            else if (flag.equals("ALPHA_N"))
            {
                String varName = "algorithm1";
                
                // MS1 B&G
                if (vars.containsKey("algorithm"))
                {
                    varName = "algorithm";
                }
                
                writer.println(TAB + TAB + "ALPHA_N = (" + varName + " == 2);"); 
            }
            else if (flag.equals("AIR_FLOW_METER"))
            {
                
                if (vars.containsKey("AFMUse"))
                {
                    writer.println(TAB + TAB + "AIR_FLOW_METER = (AFMUse == 2);");
                }
                // MS1 B&G doesn't support MAF
                else
                {
                    writer.println(TAB + TAB + "AIR_FLOW_METER = false;");
                }
            }
            else
            {
                writer.println(TAB + TAB + flag + " = parent.isSet(\"" + flag + "\");");
            }
        }
        writer.println(TAB + "}");

        writer.println(TAB + "@Override");
        writer.println(TAB + "public void refreshFlags()");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "setFlags();");
        writer.println(TAB + TAB + "initOutputChannels();");
        for (int i = 1; i <= constantMethodCount; i++)
        {
            writer.println(TAB + TAB + "initConstants" + i + "();");
        }
        writer.println(TAB + "}");

    }

    static void outputOutputChannels(ECUData ecuData, PrintWriter writer)
    {
        writer.println(TAB + "public void initOutputChannels()");
        writer.println(TAB + "{");

        for (OutputChannel op : ecuData.getOutputChannels())
        {
            if (op.getType().equals("PREPROC"))
            {
                writer.println(TAB + TAB + op.getName());
            }
            else
            {
                writer.println(TAB + TAB + "registerOutputChannel( new " + op.toString() + ");");
            }
        }
        
        writer.println(TAB + "}");
    }
    
    static void outputPackageAndIncludes(ECUData ecuData, PrintWriter writer)
    {
        writer.println("package net.tracknalysis.ecu.ms.ecu;");
        writer.println("");
        writer.println("import java.io.IOException;");
        writer.println("import java.util.*;");
        writer.println("");
        writer.println("");
        writer.println("import net.tracknalysis.ecu.ms.common.*;");
    }

    private static String getType(String name, Map<String, String> vars)
    {
        String type = vars.get(name);
        if (alwaysInt.contains(name))
        {
            type = "int";
        }
        else if (alwaysDouble.contains(name))
        {
            type = "double";
        }
        return type;
    }

    static void outputGlobalVars(ECUData ecuData, PrintWriter writer)
    {
        writer.println("//Flags");
        
        List<String> flags = new ArrayList<String>();
        for (String flag : ecuData.getFlags())
        {
            if (!flag.equals("INI_VERSION_2") && !flag.equals("MEMPAGES") && !flag.equals("LOGPAGES") 
                    && !flag.equals("SPEED_DENSITY") && !flag.equals("ALPHA_N") && !flag.equals("MSLVV_COMPATIBLE")
                    && !flag.equals("AIR_FLOW_METER"))
            {
                flags.add("\"" + flag + "\"");
            }
        }
        
        writer.println(TAB + "public String[] flags = {" + StringUtils.join(flags,",") + "};");
        for (String name : ecuData.getFlags())
        {
            writer.println(TAB + "public boolean " + name + ";");
        }
        writer.println("//Defaults");
        for (String d : ecuData.getDefaults())
        {
            writer.println(TAB + "public " + d);
        }
        Map<String, String> vars = new TreeMap<String, String>();
        vars.putAll(ecuData.getRuntimeVars());
        vars.putAll(ecuData.getEvalVars());
        for (String v : vars.keySet())
        {
            ecuData.getConstantVars().remove(v);
        }
        writer.println("//Variables");
        for (String name : vars.keySet())
        {
            String type = getType(name, vars);
            
            // Force secl to be double
            if (name.equals("secl"))
            {
                type = "double";
            }
            
            writer.println(TAB + "public " + type + " " + name + ";");
        }
        writer.println("\n//Constants");
        for (String name : ecuData.getConstantVars().keySet())
        {
            String type = getType(name, ecuData.getConstantVars());
            writer.println(TAB + "public " + type + " " + name + ";");
        }
        writer.println("\n");
        
        writer.println("\n" + TAB + "@Override");
        writer.println(TAB + "public String[] getControlFlags()");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return flags;");
        writer.println(TAB + "}");
        

    }

    static void outputRequiresPowerCycle(ECUData ecuData, PrintWriter writer)
    {
        writer.println("\n    //Fields that requires power cycle");
        
        writer.println("    public List<String> getRequiresPowerCycle()");
        writer.println("    {");
        writer.println(TAB + TAB + "List<String> requiresPowerCycle = new ArrayList<String>();");
        
        for (String field : ecuData.getRequiresPowerCycle())
        {
            writer.println(TAB + TAB + "requiresPowerCycle.add(\"" + field + "\");");
        }
        
        writer.println(TAB + TAB + "return requiresPowerCycle;"); 
        writer.println(TAB + "}\n");
    }
    
    static void outputRTCalcs(ECUData ecuData, PrintWriter writer)
    {
        writer.println("    @Override");
        writer.println("    public void calculate(byte[] ochBuffer)");
        writer.println("    {");
        writer.println("        logData = ochBuffer;");
        for (String defn : ecuData.getRuntime())
        {
            writer.println(TAB + TAB + defn);
            // System.out.println(defn);
        }
        writer.println(TAB + "}");
    }

    static void outputLogInfo(ECUData ecuData, PrintWriter writer)
    {
        writer.println(TAB + "@Override");
        writer.println(TAB + "public String getLogHeader()");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "StringBuffer b = new StringBuffer();");
        for (String header : ecuData.getLogHeader())
        {
            writer.println(TAB + TAB + header);
        }
        writer.println(TAB + TAB + "return b.toString();\n" + TAB + "}\n");
        writer.println(TAB + "@Override");
        writer.println(TAB + "public String getLogRow()");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "StringBuffer b = new StringBuffer();");

        for (String record : ecuData.getLogRecord())
        {
            writer.println(TAB + TAB + record);
        }
        writer.println(TAB + TAB + "return b.toString();\n" + TAB + "}\n");
        writer.println(TAB + "public byte[] getLogData()");
        writer.println(TAB + "{");
        writer.println(TAB + TAB + "return logData;");
        writer.println(TAB + "}\n");
    }

    static void outputLoadConstants(ECUData ecuData, PrintWriter writer)
    {
        int pageNo = 0;

        List<Integer> pageNumbers = new ArrayList<Integer>();

        for (Constant c : ecuData.getConstants())
        {
            if (c.getPage() != pageNo)
            {
                if (pageNo > 0)
                {
                    writer.println(TAB + "}");
                }
                pageNo = c.getPage();
                pageNumbers.add(pageNo);
                writer.println(TAB + "public void loadConstantsPage" + pageNo + "() throws IOException");
                writer.println(TAB + "{");
                writer.println(TAB + TAB + "byte[] pageBuffer = null;");

                int pageSize = Integer.parseInt(ecuData.getPageSizes().get(pageNo - 1).trim());
                String activateCommand = null;
                if (pageNo - 1 < ecuData.getPageActivateCommands().size())
                {
                    activateCommand = ecuData.getPageActivateCommands().get(pageNo - 1);
                }
                String readCommand = null;
                if (pageNo - 1 < ecuData.getPageReadCommands().size())
                {
                    readCommand = ecuData.getPageReadCommands().get(pageNo - 1);
                }

                outputLoadPage(ecuData, pageNo, 0, pageSize, activateCommand, readCommand, writer);
            }
            // getScalar(String bufferName,String name, String dataType, String
            // offset, String scale, String numOffset)
            String name = c.getName();

            if (!"PREPROC".equals(c.getType()))
            {
                String def;
                if ("bits".equals(c.getClassType()))
                {
                    String bitspec = StringUtils.remove(StringUtils.remove(c.getShape(), '['), ']');
                    String[] bits = bitspec.split(":");
                    int offset = c.getOffset();
                    String start = bits[0];
                    String end = bits[1];
                    String ofs = "0";
                    if (end.contains("+"))
                    {
                        String[] parts = end.split("\\+");
                        end = parts[0];
                        ofs = parts[1];
                    }
                    def = (name + " = MSUtils.getBits(pageBuffer," + offset + "," + start + "," + end + "," + ofs + ");");
                }
                else if ("array".equals(c.getClassType()))
                {
                    def = generateLoadArray(ecuData, c);
                }
                else
                {
                    def = getScalar("pageBuffer", ecuData.getConstantVars().get(name), name, c.getType(), "" + c.getOffset(), "1", "0"); // scale of 1 and translate of 0
                }
                writer.println(TAB + TAB + def);
            }
            else
            {
                if (pageNo > 0)
                {
                    writer.println(TAB + TAB + name);
                }
            }

        }
        writer.println(TAB + "}");
        writer.println(TAB + "@Override");
        writer.println(TAB + "public void loadConstants() throws IOException");
        writer.println(TAB + "{");
        for (int i : pageNumbers)
        {
            writer.println(TAB + TAB + "loadConstantsPage" + i + "();");
        }
        writer.println(TAB + TAB + "refreshFlags();");
        writer.println(TAB + "}");
    }

    private static String generateLoadArray(ECUData ecuData, Constant c)
    {
        String loadArray = "";
        
        int[] size = MSUtils.getArraySize(c.getShape());
        int width = size[0];
        int height = size[1];
        
        String functionName = "parent.loadByte";
        String signed = "false";
        if (c.getType().contains("16"))
        {
            functionName = "parent.loadWord";
        }
        if (c.getType().contains("S"))
        {
            signed = "true";
        }
        if (height == -1)
        {
            functionName += "Vector";
            loadArray = String.format("%s = %s(pageBuffer, %d, %d, %s);", c.getName(), functionName, c.getOffset(),
                    width, signed);

        }
        else
        {
            functionName += "Array";
            loadArray = String.format("%s = %s(pageBuffer, %d, %d, %d, %s);", c.getName(), functionName, c.getOffset(),
                    width, height, signed);
        }
        return loadArray;
    }

    static void outputLoadPage(ECUData ecuData, int pageNo, int pageOffset, int pageSize, String activate, String read, PrintWriter writer)
    {
        if (activate != null)
        {
            activate = processStringToBytes(ecuData, activate, pageOffset, pageSize, pageNo);
        }
        if (read != null)
        {
            read = processStringToBytes(ecuData, read, pageOffset, pageSize, pageNo);
        }
        
        writer.println(TAB + TAB + String.format("pageBuffer = parent.loadPage(%d,%d,%d,%s,%s);", pageNo, pageOffset, pageSize, activate, read));
    }

    static void outputOverrides(ECUData ecuData, PrintWriter writer)
    {        
        String pageIdentifierOutput = "";
        for (String pageIdentifier : ecuData.getPageIdentifiers())
        {
            pageIdentifierOutput += TAB + TAB + "pageIdentifiers.add(\"" + pageIdentifier.replace("\\", "\\\\") + "\");\n";
        }
        
        String pageActivateOutput = "";
        int[] value = {0};
        for (String pageActivate : ecuData.getPageActivateCommands())
        {
            pageActivateOutput += TAB + TAB + "pageActivates.add(new byte[] {" + MSUtils.hexStringToBytes(new ArrayList<String>(), pageActivate, 0, 0, value, 0) + "});\n";
        }
        
        String pageValueWriteOutput = "";
        for (String pageValueWrite : ecuData.getPageValueWrites())
        {
            pageValueWriteOutput += TAB + TAB + "pageValueWrites.add(" + pageValueWrite + ");\n";
        }
        
        String pageChunkWriteOutput = "";
        
        for (String pageChunkWrite : ecuData.getPageChunkWrites())
        {
            pageChunkWriteOutput += TAB + TAB + "pageChunkWrites.add(" + pageChunkWrite + ");\n";
        }
        
        String overrides = TAB + "@Override\n" + TAB + "public String getSignature()\n" + TAB + "{\n" + TAB + TAB
                + "return signature;\n" + "}\n" + TAB + "@Override\n" + TAB + "public byte[] getOchCommand()\n" + TAB + "{\n" + TAB
                + TAB + "return this.ochGetCommand;\n" + TAB + "}\n" +

                TAB + "@Override\n" + TAB + "public byte[] getSigCommand()\n" + TAB + "{\n" + TAB + TAB
                + "return this.queryCommand;\n" + TAB + "}\n" +

                TAB + "@Override\n" + TAB + "public int getBlockSize()\n" + TAB + "{\n" + TAB + TAB + "return this.ochBlockSize;\n"
                + TAB + "}\n" +

                TAB + "@Override\n" + TAB + "public int getSigSize()\n" + TAB + "{\n" + TAB + TAB + "return signature.length();\n"
                + TAB + "}\n" +

                TAB + "@Override\n" + TAB + "public int getPageActivationDelay()\n" + TAB + "{\n" + TAB + TAB + "return "
                + ecuData.getPageActivationDelayVal() + ";\n" + TAB + "}\n" +
                
                TAB + "@Override\n" +
                TAB + "public List<String> getPageValueWrites()\n" +
                TAB + "{\n" +
                TAB + TAB + "List<String> pageValueWrites = new ArrayList<String>();\n\n" +
                            pageValueWriteOutput +
                "\n" + TAB + TAB + "return pageValueWrites;\n" +
                TAB + "}\n" +
                
                TAB + "@Override\n" +
                TAB + "public List<String> getPageChunkWrites()\n" +
                TAB + "{\n" +
                TAB + TAB + "List<String> pageChunkWrites = new ArrayList<String>();\n\n" +
                            pageChunkWriteOutput +
                "\n" + TAB + TAB + "return pageChunkWrites;\n" +
                TAB + "}\n" +
                
                TAB + "@Override\n" +
                TAB + "public List<String> getPageIdentifiers()\n" +
                TAB + "{\n" +
                TAB + TAB + "List<String> pageIdentifiers = new ArrayList<String>();\n\n" +
                            pageIdentifierOutput +
                "\n" + TAB + TAB + "return pageIdentifiers;\n" +
                TAB + "}\n" +
                
                TAB + "@Override\n" +
                TAB + "public List<byte[]> getPageActivates()\n" +
                TAB + "{\n" +
                TAB + TAB + "List<byte[]> pageActivates = new ArrayList<byte[]>();\n\n" +
                            pageActivateOutput +
                "\n" + TAB + TAB + "return pageActivates;\n" +
                TAB + "}\n" +
                
                TAB + "@Override\n" + TAB + "public int getInterWriteDelay()\n" + TAB + "{\n" + TAB + TAB + "return "
                + ecuData.getInterWriteDelay() + ";\n" + TAB + "}\n" + TAB + "@Override\n" + TAB
                + "public boolean isCRC32Protocol()\n" + TAB + "{\n" + TAB + TAB + "return " + ecuData.isCRC32Protocol() + ";\n"
                + TAB + "}\n" +

                TAB + "@Override\n" + TAB + "public int getCurrentTPS()\n" + TAB + "{\n";
        if (ecuData.getRuntimeVars().containsKey("tpsADC"))
        {
            overrides += TAB + TAB + "return (int)tpsADC;\n";
        }
        else
        {
            overrides += TAB + TAB + "return 0;\n";
        }

        overrides += TAB + "}\n";

        writer.println(overrides);
    }

    private static String processStringToBytes(ECUData ecuData, String s, int offset, int count, int pageNo)
    {
        String ret = "new byte[]{";

        int[] value = {0};
        ret += MSUtils.hexStringToBytes(ecuData.getPageIdentifiers(), s, offset, count, value, pageNo);

        ret += "}";
        return ret;
    }

    static String getScalar(String bufferName, String javaType, String name, String dataType, String offset, String scale, String numOffset)
    {
        if (javaType == null)
        {
            javaType = "int";
        }
        String definition = name + " = (" + javaType + ")((MSUtils.get";
        if (dataType.startsWith("S"))
        {
            definition += "Signed";
        }
        int size = Integer.parseInt(dataType.substring(1).trim());
        switch (size)
        {
        case 8:
            definition += "Byte";
            break;
        case 16:
            definition += "Word";
            break;
        case 32:
            definition += "Long";
            break;
        default:
            definition += dataType;
            break;
        }
        definition += "(" + bufferName + "," + offset + ") + " + numOffset + ") * " + scale + ");";
        return definition;
    }
}
