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
package com.ericsson.ec.helpers;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.ec.utils.Utils;

/**
 * This class is a builder for jenkins xml data string.
 *
 * @author Ericsson 2019
 *
 */
public class JenkinsXmlData {

    private static final String JENKINS_JOB_TEMPLATE_FILE_PATH = "/jenkinsJobTemplate.xml";
    private static final String XML_VERSION = "<?xml version='1.1' encoding='UTF-8'?>";
    private static final String HUDSON_PARAMETERS_DEFINITION_KEY = "hudson.model.ParametersDefinitionProperty";
    private static final String PARAMETER_DEFINITION_KEY = "parameterDefinitions";

    private JSONObject xmlJsonData;
    private JSONObject builders;
    private JSONObject properties;

    /**
     * Constructor: creates the base XML data from template.
     *
     * @throws IOException
     */
    public JenkinsXmlData() throws IOException {
        String xmlDataString = Utils.getStringFromFile(JENKINS_JOB_TEMPLATE_FILE_PATH);
        xmlJsonData = XML.toJSONObject(xmlDataString);
        builders = new JSONObject();
        properties = new JSONObject();
    }

    /**
     * This function converts xmlJsonData to XML format and adds the xml version
     * tag.
     *
     * @return
     */
    public String getXmlAsString() {
        xmlJsonData.getJSONObject("project").put("builders", builders);
        xmlJsonData.getJSONObject("project").put("properties", properties);

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
        xmlJsonData.getJSONObject("project").put("authToken", token);
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

        builders.getJSONArray(husdonShellKey).put(newCommand);
        return this;
    }

    /**
     * This function adds groovy script to the XML data.
     *
     * @param script
     * @return this JenkinsXmlData
     */
    public JenkinsXmlData addGrovyScript(String script) {
        String hudsonGroovyKey = "hudson.plugins.groovy.Groovy plugin='groovy@2.1'";
        boolean hasKeyGroovy = builders.has(hudsonGroovyKey);

        if (!hasKeyGroovy) {
            JSONArray hudsonTasksGroovy = new JSONArray();
            builders.put(hudsonGroovyKey, hudsonTasksGroovy);
        }

        JSONObject newGroovyCommand = BuildGroovyObject(script);

        builders.getJSONArray(hudsonGroovyKey).put(newGroovyCommand);
        return this;
    }

    /**
     * This function adds a parameter key to the job data, the user must specify
     * what type the parameter will receive, currently only String.class and
     * boolean.class is supported.
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
    public JenkinsXmlData addBuildParameter(String key, String defaultValue, String description, boolean trim) throws Exception {
        String parametertypeKey = "hudson.model.StringParameterDefinition";

        validatePropertiesObject(parametertypeKey);

        JSONObject param = new JSONObject();
        param.put("name", key);
        param.put("description", description);
        param.put("defaultValue", defaultValue);
        param.put("trim", false);

        properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY).getJSONObject(PARAMETER_DEFINITION_KEY)
                .getJSONArray(parametertypeKey).put(param);
        return this;
    }

    /**
     * This function adds a parameter key to the job data, the user must specify
     * what type the parameter will receive, currently only String.class and
     * boolean.class is supported. No default value, no description and trim
     * false
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
     * Ensures the XML properties is added correctly, should be
     * <hudson.model.ParametersDefinitionProperty> <parameterDefinitions>
     * <our-input-parameter-typ> </our-input-parameter-typ>
     * </parameterDefinitions> </hudson.model.ParametersDefinitionProperty>
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
            properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY).put(PARAMETER_DEFINITION_KEY,
                    parameterDefinitions);
        }

        boolean hasParametertypeKey = properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY)
                .getJSONObject(PARAMETER_DEFINITION_KEY).has(parametertypeKey);
        if (!hasParametertypeKey) {
            JSONArray parametertype = new JSONArray();
            properties.getJSONObject(HUDSON_PARAMETERS_DEFINITION_KEY).getJSONObject(PARAMETER_DEFINITION_KEY)
                    .put(parametertypeKey, parametertype);
        }
    }

    /**
     * This function creates the croovy script structure needed by jenkins.
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
        groovyScript.put("scriptSource class='hudson.plugins.groovy.StringScriptSource'", groovyScriptSource);
        groovyScript.put("classPath", "");
        groovyScript.put("groovyName", "(Default)");
        groovyScript.put("parameters", "");
        groovyScript.put("properties", "");

        return groovyScript;
    }

    /**
     * This function removes the params given in the groove start node from tne
     * end node <abc param='abc'></abc param='def'> becomes
     * <abc param='def'></abc>
     *
     * @param xmlDataString
     * @return
     */
    private String removeExtraGroovyParams(String xmlDataString) {
        xmlDataString = xmlDataString.replaceAll(
                "\\<\\/hudson\\.plugins\\.groovy\\.Groovy\\ plugin\\=\\'groovy\\@2\\.1\\'",
                "</hudson.plugins.groovy.Groovy>");
        xmlDataString = xmlDataString.replaceAll(
                "\\<\\/scriptSource\\ class\\=\\'hudson\\.plugins\\.groovy\\.StringScriptSource\\'\\>",
                "</scriptSource>");
        return xmlDataString;
    }

}
