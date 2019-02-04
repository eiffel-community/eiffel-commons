package com.ericsson.esj.jenkins_manager_test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.ericsson.esj.JenkinsManager;

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

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        JenkinsManager jm = new JenkinsManager(protocol, host, port, username, password);
        System.out.println("Performing jenkins tests!");

        String jobXmlData = jm.getXmlJobData(null, null);

        for (String name : testJobNameList) {
            boolean created = jm.createJob(name, jobXmlData);
            System.out.println("Jenkins job [ " + name + " ] successfully created ::: " + created);
        }

        for (String name : testJobNameList) {
            boolean triggered = jm.triggerJob(name, null);
            System.out.println("Jenkins job [ " + name + " ] successfully triggered ::: " + triggered);
        }

        for (String name : testJobNameList) {
            int times = 0;
            while (times < 10) {
                times++;
                boolean jobTriggered = jm.jobHasBeenTriggered(name);
                System.out.println("Jenkins job [ " + name + " ] successfully **seen** triggered ::: " + jobTriggered);
                if (jobTriggered) {
                    break;
                }
                Thread.sleep(1000);
            }
        }

        for (String name : testJobNameList) {
            boolean deleted = jm.deleteJob(name);
            System.out.println("Jenkins job [ " + name + " ]  successfully deleted ::: " + deleted);
        }
    }

}
