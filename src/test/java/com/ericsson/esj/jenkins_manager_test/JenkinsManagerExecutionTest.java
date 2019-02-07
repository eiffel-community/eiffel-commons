package com.ericsson.esj.jenkins_manager_test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.ericsson.esj.JenkinsManager;
import com.ericsson.esj.helpers.JenkinsXmlData;

/**
 * This test is executed as java application within Eclipse. This is not the
 * final test but to be used as inspiration for when tests are created for
 * jenkins manager. To be able to execute this application you need to have a
 * basic jenkins instance up and running. Currently we use jenkins started from
 * eiffel intelligence docker-compose file. This class is not to be regarded as
 * a final test for jenkinsmanager but a code hand over to the next person who
 * develop the tests.
 *
 * It also provide examples for how to use jenkins manager until documentation
 * has been created.
 *
 * @author Test
 *
 */
public class JenkinsManagerExecutionTest {

    private static final String username = "admin";
    private static final String password = "admin";
    private static final String protocol = "http";
    private static final String host = "localhost";
    private static final int port = 8081;

    static List<String> testJobNameList = new ArrayList<>(
            Arrays.asList("Test_Job1", "Test_Job2", "Test_Job3", "Test_Job4", "Test_Job5", "Test_Job6", "Test_Job7",
                    "Test_Job8", "Test_Job9", "Test_Job10", "Test_Job11", "Test_Job12", "Test_Job13", "Test_Job14",
                    "Test_Job15", "Test_Job16", "Test_Job17", "Test_Job18", "Test_Job19", "Test_Job20"));
    static JenkinsManager jenkinsManager;

    public static void main(String[] args) throws Exception {
        jenkinsManager = new JenkinsManager(protocol, host, port, username, password);
        System.out.println("Performing jenkins tests!");

        ensureGroovyPluginInstalled();

        String token = "my_build_token";
        String parameter_1 = "param_string_1";
        String parameter_2 = "param_string_2";

        Map<String, String> parameters = new HashMap<>();
        parameters.put(parameter_1, "My pramaeter value");
        parameters.put(parameter_2, "My second pramaeter value");

        JenkinsXmlData jobXmlData = getXmlData(parameter_1, parameter_2, token);

        boolean failure = false;

        for (String name : testJobNameList) {
            boolean created = false;
            try {
                created = jenkinsManager.createJob(name, jobXmlData.getXmlAsString());
            } catch (Exception e) {
                // TODO: handle exception
            }
            System.out.println("Jenkins job [ " + name + " ] successfully created ::: " + created);
            if (!created) {
                failure = true;
            }
        }

        for (String name : testJobNameList) {
            boolean triggered = jenkinsManager.buildJobWithParameters(name, token, parameters);
            System.out.println("Jenkins job [ " + name + " ] successfully triggered ::: " + triggered);
            if (!triggered) {
                failure = true;
            }
        }

        if (!failure) {
            for (String name : testJobNameList) {
                int times = 0;
                while (times < 10) {
                    times++;
                    try {
                        JSONObject statusData = jenkinsManager.getJenkinsBuildStatusData(name);
                        System.out.println("Jenkins job [ " + name + " ] Build status data ::: " + statusData);
                        break;
                    } catch (Exception e) {
                        System.out.println("Jenkins job [ " + name + " ] Not triggered... Yet ");
                        Thread.sleep(1000);
                    }
                }
            }
        }

        for (String name : testJobNameList) {
            boolean deleted = jenkinsManager.deleteJob(name);
            System.out.println("Jenkins job [ " + name + " ]  successfully deleted ::: " + deleted);
        }
    }

    private static JenkinsXmlData getXmlData(String parameter_1, String parameter_2, String token) throws Exception {
        JenkinsXmlData jobXmlData = new JenkinsXmlData();
        jobXmlData.addJobToken(token);
        jobXmlData.addBashScript("echo test 1");
        jobXmlData.addBashScript("echo test 2");
        jobXmlData.addGrovyScript("My Groovy Script 1");
        jobXmlData.addGrovyScript("My Groovy Script 2");
        jobXmlData.addBuildParameter(parameter_1);
        jobXmlData.addBuildParameter(parameter_2);
        return jobXmlData;
    }

    private static void ensureGroovyPluginInstalled() throws Exception {
        String plugin = "Groovy";

        boolean pluginExists = jenkinsManager.pluginExists(plugin);
        System.out.println("Jenkins plugin [ " + plugin + " ] exists ::: " + pluginExists);

        if (pluginExists)
            return;

        boolean pluginAdded = jenkinsManager.installPlugin(plugin, "1.5.4");
        System.out.println("Jenkins plugin [ " + plugin + " ] successfully created ::: " + pluginAdded);

        boolean jenkinsRestarted = jenkinsManager.restartJenkins();
        System.out.println("Jenkins successfully restarted ::: " + jenkinsRestarted);

        pluginExists = jenkinsManager.pluginExists(plugin);
        System.out.println("Jenkins plugin [ " + plugin + " ] exists ::: " + pluginExists);

    }

}
