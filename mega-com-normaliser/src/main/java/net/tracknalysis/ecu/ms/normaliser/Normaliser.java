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

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Normaliser
{
    enum Section
    {
        None, Header, Expressions, Logs, FrontPage, Constants, PcVariables, ConstantsExtensions, Menu, TableEditor, CurveEditor, ControllerCommands, UserDefined, SettingGroups
    }

    private static final String        TAB          = "    ";
    private static ECUData             ecuData      = new ECUData();
    private static Map<String, String> classList    = new TreeMap<String, String>();
    private static Section             currentSection;
    private static String              className;
    private static File                outputDirectory;


    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws IOException
    {
        File f = new File(args[0]);
        outputDirectory = new File(args[1]);
        outputDirectory.mkdirs();

        BufferedReader br = new BufferedReader(new FileReader(f));

        String line;

        while ((line = br.readLine()) != null)
        {
            if (!line.startsWith("#"))
            {
                preProcess(f.getParent(), line);
            }
        }
        outputRegistry(f.getParentFile());
        br.close();
    }

    private static void outputRegistry(File baseLocation) throws IOException
    {
        File srcDirectory = new File(outputDirectory, "net/tracknalysis/ecu/ms/ecu/");
        File or = new File(srcDirectory, "EcuRegistry.java");
        PrintWriter w = new PrintWriter(new FileWriter(or));

        InputStream is = Normaliser.class.getResourceAsStream("/EcuRegistry.java.template");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        while ((line = br.readLine()) != null)
        {
            if (line.trim().equals("<LIST>"))
            {
                for (String name : classList.keySet())
                {
                    String sig = classList.get(name);
                    w.println(String.format("        registerEcu(%s.class,%s);", name, sig));
                }
            }
            else
            {
                w.println(line);
            }
        }
        br.close();
        w.close();
    }

    /**
     * Initialise our stores to start a new class definition
     * 
     * @param filename
     * @param line
     * @throws IOException
     */
    private static void preProcess(String directory, String filename) throws IOException
    {
        ecuData.setSignatureDeclaration("");
        ecuData.setQueryCommandStr("");
        ecuData.reset();
        currentSection = Section.None;

        File f = new File(directory, filename);
        if (f.isDirectory())
            return;

        className = f.getName();
        process(f, false);
    }

    /**
     * Iterate over the file to read it
     * 
     * @param f
     * @param subRead
     * @throws IOException
     */
    private static void process(File f, boolean subRead) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "CP1252"));

        String line;

        while ((line = br.readLine()) != null)
        {
            if (line.startsWith("#include "))
            {
                handleImport(line, f);
                continue;
            }

            if (line.trim().equals("[MegaTune]"))
            {
                currentSection = Section.Header;
                continue;
            }
            else if (line.trim().equals("[OutputChannels]"))
            {
                currentSection = Section.Expressions;
                continue;
            }
            else if (line.trim().equals("[Datalog]"))
            {
                currentSection = Section.Logs;
                continue;

            }
            else if (line.trim().equals("[Menu]"))
            {
                currentSection = Section.Menu;
                continue;
            }
            else if (line.trim().equals("[TableEditor]"))
            {
                currentSection = Section.TableEditor;
                continue;
            }
            else if (line.trim().equals("[CurveEditor]"))
            {
                currentSection = Section.CurveEditor;
                continue;
            }
            else if (line.trim().equals("[UserDefined]"))
            {
                currentSection = Section.UserDefined;
                continue;
            }
            else if (line.trim().equals("[FrontPage]"))
            {
                currentSection = Section.FrontPage;
            }
            else if (line.trim().equals("[Constants]"))
            {
                currentSection = Section.Constants;
            }
            else if (line.trim().equals("[PcVariables]"))
            {
                currentSection = Section.PcVariables;
            }
            else if (line.trim().equals("[ConstantsExtensions]"))
            {
                currentSection = Section.ConstantsExtensions;
            }
            else if (line.trim().equals("[SettingGroups]"))
            {
                currentSection = Section.SettingGroups;
            }
            else if (line.trim().equals("[ControllerCommands]"))
            {
                currentSection = Section.ControllerCommands;
            }
            else if (line.trim().startsWith("["))
            {
                currentSection = Section.None;
                continue;
            }

            switch (currentSection)
            {
            case Expressions:
                Process.processExpr(ecuData,line);
                break;
            case Logs:
                Process.processLogEntry(ecuData,line);
                break;
            case Header:
                Process.processHeader(ecuData,line);
                break;
            case FrontPage:
                Process.processFrontPage(ecuData,line);
                break;
            case Constants:
                Process.processConstants(ecuData,line);
                break;
            case PcVariables:
                Process.processPcVariables(ecuData,line);
                break;
            case ConstantsExtensions:
                Process.processConstantsExtensions(ecuData,line);
                break;
            case Menu:
                Process.processMenu(ecuData,line);
                break;
            case TableEditor:
                Process.processTableEditor(ecuData,line);
                break;
            case CurveEditor:
                Process.processCurveEditor(ecuData,line);
                break;
            case ControllerCommands:
                Process.processControllerCommands(ecuData,line);
                break;
            case UserDefined:
                Process.processUserDefined(ecuData,line);
                break;
            case SettingGroups:
                Process.processSettingGroups(ecuData,line);
            case None:
                break;
            default:
                break;
            }

        }
        br.close();
        if (!subRead)
        {
            writeFile(className);
        }
    }

    /**
     * Process an included/imported file
     * 
     * @param line
     * @param parentFile
     * @throws IOException
     */
    private static void handleImport(String line, File parentFile) throws IOException
    {
        Pattern importFile = Pattern.compile(".*\\\"(.*)\\\"");

        Matcher importFileM = importFile.matcher(line);
        if (importFileM.matches())
        {
            String fileName = importFileM.group(1);
            File imported = new File(parentFile.getAbsoluteFile().getParentFile(), fileName);
            process(imported, true);
        }

    }
 
    private static void writeFile(String className) throws IOException
    {
        className = StringUtils.capitalize(className);
        className = StringUtils.remove(className, ".");
        className = StringUtils.remove(className, "ini");
        className = StringUtils.replace(className, " ", "_");
        className = StringUtils.replace(className, "-", "_");
        className = StringUtils.replace(className, "&", "_");
        String classFile = "";

        classList.put(className, ecuData.getClassSignature());
        String directory = outputDirectory.getAbsolutePath() + File.separator + "net/tracknalysis/ecu/ms/ecu/";
        new File(directory).mkdirs();
        classFile = directory + className + ".java";
        System.out.println("Writing to " + classFile);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(classFile), "CP1252"));
        String fingerprint = getFingerprint();
        System.out.println(fingerprint + " : " + className);

        Output.outputPackageAndIncludes(ecuData,writer);

        writer.println("/*");
        writer.println("Fingerprint : " + fingerprint);
        writer.println("*/");

        writer.println("@SuppressWarnings(\"unused\")");
        writer.println("public class " + className + " extends AbstractMsEcu implements MsEcu, DataSource\n{");
        Output.outputConstructor(ecuData,writer, className);
        Output.outputOutputChannels(ecuData, writer);
        writer.println(TAB + "private Map<String,Double> fields = new HashMap<String,Double>();");
        writer.println(TAB + ecuData.getQueryCommandStr());
        writer.println(TAB + ecuData.getSignatureDeclaration());
        writer.println(TAB + ecuData.getOchGetCommandStr());
        writer.println(TAB + ecuData.getOchBlockSizeStr());
        Output.outputRequiresPowerCycle(ecuData,writer);
        Output.outputRTCalcs(ecuData, writer);
        Output.outputLogInfo(ecuData, writer);
        
        Output.outputOverrides(ecuData,writer);
        Output.outputLoadConstants(ecuData,writer);
        Output.outputFlagsAndConstants(ecuData, writer);
        Output.outputGlobalVars(ecuData,writer);
        writer.println("\n}\n");

        writer.close();
    }

    private static String getFingerprint()
    {
        StringBuffer b = new StringBuffer();
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(ecuData.getFingerprintSource().getBytes());
            for (int i = 0; i < array.length; i++)
            {
                b.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return b.toString();
    }

 

}
