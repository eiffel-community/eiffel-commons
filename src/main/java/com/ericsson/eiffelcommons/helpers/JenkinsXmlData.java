/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.eiffelcommons.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.eiffelcommons.utils.Utils;

/**
 * This class is a builder for jenkins xml data string.
 *
 * @author Ericsson 2019
 *
 */
public class JenkinsXmlData {

    private static final String JENKINS_TEMPLATE_FILE_NAME = "jenkinsJobTemplate.xml";
    private static final String XML_VERSION = "<?xml version='1.1' encoding='UTF-8'?>";
    private static final String HUDSON_PARAMETERS_DEFINITION_KEY = "hudson.model.ParametersDefinitionProperty";
    private static final String PARAMETER_DEFINITION_KEY = "parameterDefinitions";
    private static final String GROOVY_SCRIPT_PLUGIN_VERSION = "2.1";
    private static final String GROOVY_SCRIPT_SECURITY_PLUGIN_VERSION = "1.51";

    private String hudsonPluginsGroovyGroovyKey = "hudson.plugins.groovy.Groovy plugin='groovy@%s'";
    private String hudsonPluginGroovySystemGroovyKey = "hudson.plugins.groovy.SystemGroovy plugin='groovy@%s'";
    private String scriptPluginForScriptSecurityKey = "script plugin='script-security@%s'";

    private String sourceForGroovyKey = "source class='hudson.plugins.groovy.StringSystemScriptSource'";
    private String scriptSourceforGroovyKey = "scriptSource class='hudson.plugins.groovy.StringScriptSource'";

    private JSONObject xmlJsonData;
    private JSONObject builders;
    private JSONObject properties;

    /**
     * Constructor: creates the base XML data from template.
     *
     * @throws IOException
     */
    public JenkinsXmlData() throws FileNotFoundException {
        String xmlJsonDataString = Utils.getResourceFileAsString(JENKINS_TEMPLATE_FILE_NAME);
        xmlJsonData = XML.toJSONObject(xmlJsonDataString);
        builders = new JSONObject();
        properties = new JSONObject();
    }

    /**
     * This function converts xmlJsonData to XML format and adds the xml version tag.
     *
     * @return
     */
    public String getXmlAsString() {
        xmlJsonData.getJSONObject("project")
                   .put("builders", builders);
        xmlJsonData.getJSONObject("project")
                   .put("properties", properties);

        String xmlDataString = XML.toString(xmlJsonData);
        xmlDataString = removeExtraGroovyParams(xmlDataString);

        return (XML_VERSION + xmlDataString);
    }

    /**
     * This function adds the job token to the XML data.
     *
     * @param token
     * @return this JenkinsXmlData
     */
    public JenkinsXmlData addJobToken(String token) {
        xmlJsonData.getJSONObject("project")
                   .put("authToken", token);
        return this;
    }

    /**
     * This function adds bash script to the XML data.
     *
     * @param script
     * @return this JenkinsXmlData
     */
    public JenkinsXmlData addBashScript(String script) {
        String husdonShellKey = "hudson.tasks.Shell";
        boolean hasKeyShell = builders.has(husdonShellKey);

        if (!hasKeyShell) {
            JSONArray hudsonTasksShell = new JSONArray();
            builders.put(husdonShellKey, hudsonTasksShell);
        }

        JSONObject newCommand = new JSONObject();
        newCommand.put("command", script);

        builders.getJSONArray(husdonShellKey)
                .put(newCommand);
        return this;
    }

    /**
     * This function adds groovy script to the XML data.
     *
     * @param script
     * @return this JenkinsXmlData
     */
    public JenkinsXmlData addGrovyScript(String script) {
        hudsonPluginsGroovyGroovyKey = String.format(hudsonPluginsGroovyGroovyKey, GROOVY_SCRIPT_PLUGIN_VERSION);

        boolean hasKeyGroovy = builders.has(hudsonPluginsGroovyGroovyKey);
        if (!hasKeyGroovy) {
            JSONArray hudsonTasksGroovy = new JSONArray();
            builders.put(hudsonPluginsGroovyGroovyKey, hudsonTasksGroovy);
        }

        JSONObject newGroovyCommand = BuildGroovyObject(script);

        builders.getJSONArray(hudsonPluginsGroovyGroovyKey)
                .put(newGroovyCommand);
        return this;
    }

    /**
     * This function adds a system groovy script to the XML data.
     * Only one system groovy script may be added to a single jenkins job at the moment.
     *
     * @param script
     * @param sandbox
     * @return this JenkinsXmlData
     * @throws Exception
     */
    public JenkinsXmlData addSystemGrovyScript(String script, boolean sandbox) throws Exception {
        hudsonPluginGroovySystemGroovyKey = String.format(hudsonPluginGroovySystemGroovyKey,
                GROOVY_SCRIPT_PLUGIN_VERSION);

        boolean hasKeyGroovy = builders.has(hudsonPluginGroovySystemGroovyKey);
        if (!hasKeyGroovy) {
            JSONArray hudsonSystemGroovy = new JSONArray();
            builders.put(hudsonPluginGroovySystemGroovyKey, hudsonSystemGroovy);
        } else {
            throw new Exception("Currently only one system Groovy script supported.");
        }
        JSONObject systemGroovyScript = buildSystemGroovyObject(script, sandbox);

        JSONArray systemGroovyContainer = new JSONArray();
        systemGroovyContainer.put(systemGroovyScript);

        builders.getJSONArray(hudsonPluginGroovySystemGroovyKey)
                .put(systemGroovyScript);

        return this;
    }

    /**
     * This function adds a parameter key to the job data, the user must specify what type the parameter will receive,
     * currently only String.class and boolean.class is supported.
     *
     * @param key
     *            :: The parameter key
     * @param defaultValue
     *            :: The default value if parameter is empty
     * @param description
     *            :: Description
     * @param trim
     *            :: Trim white spaces
     * @return this JenkinsXmlData
     * @throws Exception
     */
    public JenkinsXmlData addBuildParameter(String key, String defaultValue, String description, boolean trim)
            throws Exception {
        String parametertypeKey = "hudson.model.StringParameterDefinition";

        validatePropertiesObject(parametertypeKey);

        JSONObject param = new JSONObject();
        param.put("name", key);
        param.put("description", description);
        param.put("defaultValue", defaultValue);
        param.put("trim", false);

        properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY)
                  .getJSONObject(PARAMETER_DEFINITION_KEY)
                  .getJSONArray(parametertypeKey)
                  .put(param);
        return this;
    }

    /**
     * This function adds a parameter key to the job data, the user must specify what type the parameter will receive,
     * currently only String.class and boolean.class is supported. No default value, no description and trim false
     *
     * @param key
     *            :: The parameter key
     * @return this JenkinsXmlData
     * @throws Exception
     */
    public JenkinsXmlData addBuildParameter(String key) throws Exception {
        addBuildParameter(key, "", "", false);
        return this;
    }

    /**
     * Ensures the XML properties is added correctly, should be <hudson.model.ParametersDefinitionProperty>
     * <parameterDefinitions> <our-input-parameter-typ> </our-input-parameter-typ> </parameterDefinitions>
     * </hudson.model.ParametersDefinitionProperty>
     *
     * @param parametertypeKey
     */
    private void validatePropertiesObject(String parametertypeKey) {
        boolean hasHudsonParametersDefinitionKey = properties.has(HUDSON_PARAMETERS_DEFINITION_KEY);
        if (!hasHudsonParametersDefinitionKey) {
            JSONObject parametersDefinitionProperty = new JSONObject();
            properties.put(HUDSON_PARAMETERS_DEFINITION_KEY, parametersDefinitionProperty);
        }

        boolean hasParameterDefinitions = properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY)
                                                    .has(PARAMETER_DEFINITION_KEY);
        if (!hasParameterDefinitions) {
            JSONObject parameterDefinitions = new JSONObject();
            properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY)
                      .put(PARAMETER_DEFINITION_KEY, parameterDefinitions);
        }

        boolean hasParametertypeKey = properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY)
                                                .getJSONObject(PARAMETER_DEFINITION_KEY)
                                                .has(parametertypeKey);
        if (!hasParametertypeKey) {
            JSONArray parametertype = new JSONArray();
            properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY)
                      .getJSONObject(PARAMETER_DEFINITION_KEY)
                      .put(parametertypeKey, parametertype);
        }
    }

    /**
     * This function creates the Groovy script structure needed by jenkins.
     *
     * @param script
     * @return
     */
    private JSONObject BuildGroovyObject(String script) {
        JSONObject groovyScriptSource = new JSONObject();
        groovyScriptSource.put("command", script);
        JSONObject groovyScript = new JSONObject();
        groovyScript.put("scriptParameters", "");
        groovyScript.put("javaOpts", "");
        groovyScript.put(scriptSourceforGroovyKey, groovyScriptSource);
        groovyScript.put("classPath", "");
        groovyScript.put("groovyName", "(Default)");
        groovyScript.put("parameters", "");
        groovyScript.put("properties", "");

        return groovyScript;
    }

    /**
     * This function creates the System Groovy script structure needed by jenkins.
     *
     * @param script
     * @param sandbox
     * @return
     */
    private JSONObject buildSystemGroovyObject(String script, boolean sandbox) {
        scriptPluginForScriptSecurityKey = String.format(scriptPluginForScriptSecurityKey,
                GROOVY_SCRIPT_SECURITY_PLUGIN_VERSION);

        JSONObject systemGroovyScriptValues = new JSONObject();
        systemGroovyScriptValues.put("script", script);
        systemGroovyScriptValues.put("sandbox", sandbox);

        JSONArray systemGroovyScriptArray = new JSONArray();
        systemGroovyScriptArray.put(systemGroovyScriptValues);

        JSONObject systemGroovyScriptObject = new JSONObject();
        systemGroovyScriptObject.put(scriptPluginForScriptSecurityKey, systemGroovyScriptArray);

        JSONObject systemGroovySource = new JSONObject();
        systemGroovySource.put(sourceForGroovyKey, systemGroovyScriptObject);

        return systemGroovySource;
    }

    /**
     * This function removes the params given in the groove start node from tne end node
     * <abc param='abc'></abc param='def'> becomes <abc param='def'></abc>
     *
     * @param xmlDataString
     * @return
     */
    private String removeExtraGroovyParams(String xmlDataString) {
        // Groovy script keys
        String hudsonPluginsGroovyGroovyKeyEnd = convertToEndNode(hudsonPluginsGroovyGroovyKey);
        String scriptSourceforGroovyKeyEnd = convertToEndNode(scriptSourceforGroovyKey);

        hudsonPluginsGroovyGroovyKeyEnd = regexify(hudsonPluginsGroovyGroovyKeyEnd);
        scriptSourceforGroovyKeyEnd = regexify(scriptSourceforGroovyKeyEnd);

        // System groovy script keys
        String hudsonSystemGroovyKeyEnd = convertToEndNode(hudsonPluginGroovySystemGroovyKey);
        String systemGroovyScriptKeyEnd = convertToEndNode(scriptPluginForScriptSecurityKey);
        String sourceForGroovyKeyEnd = convertToEndNode(sourceForGroovyKey);

        hudsonSystemGroovyKeyEnd = regexify(hudsonSystemGroovyKeyEnd);
        systemGroovyScriptKeyEnd = regexify(systemGroovyScriptKeyEnd);
        sourceForGroovyKeyEnd = regexify(sourceForGroovyKeyEnd);

        // String replace section
        xmlDataString = xmlDataString.replaceAll(hudsonPluginsGroovyGroovyKeyEnd, "</hudson.plugins.groovy.Groovy>");
        xmlDataString = xmlDataString.replaceAll(scriptSourceforGroovyKeyEnd, "</scriptSource>");

        xmlDataString = xmlDataString.replaceAll(hudsonSystemGroovyKeyEnd, "</hudson.plugins.groovy.SystemGroovy>");
        xmlDataString = xmlDataString.replaceAll(sourceForGroovyKeyEnd, "</source>");
        xmlDataString = xmlDataString.replaceAll(systemGroovyScriptKeyEnd, "</script>");

        return xmlDataString;
    }

    /**
     * Function that adds </ and > to a string
     *
     * @param string
     * @return
     */
    private String convertToEndNode(String string) {
        return "</" + string + ">";
    }

    /**
     * Function that makes a string usable as regex.
     *
     * @param string
     * @return
     */
    private String regexify(String string) {
        String backSlash = "\\\\";
        List<String> charsToRegexify = Arrays.asList("'", "\\.", " ", "=", "/", "<", ">");
        for (String charcters : charsToRegexify) {
            string = string.replaceAll(charcters, backSlash + charcters);
        }
        return string;
    }

}
