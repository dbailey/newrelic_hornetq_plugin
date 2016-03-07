package com.newrelic.plugins.hornetq.instance;

import java.util.Arrays;
import java.util.Map;

/**
 * Read and store all configurations from hornetq.instance.json file
 */
public class Configuration {

    public static final String AGENT_DEFAULT_HOST = "localhost"; // Default values for hornetq Agent
    public static final String AGENT_DEFAULT_PORT = "9990"; // Default values for hornetq port in jboss
    public static final String AGENT_DEFAULT_USER = "newrelic";
    public static final String AGENT_DEFAULT_PASSWD = "securepassword";

    private final String serverType;
    private final String name;
    private final String host;
    private final String port;
    private final String user;
    private final String passwd;
    private final String[] ignoreQueues;
    private final String[] ignoreTopics;

    public Configuration(Map<String, Object> properties) {
        String serverType = "serverType";
        this.serverType = getString(properties, serverType);
        name = getString(properties, "pluginName");
        host = getString(properties, "host", AGENT_DEFAULT_HOST);
        port = getString(properties, "port", AGENT_DEFAULT_PORT);
        user = getString(properties, "user", AGENT_DEFAULT_USER);
        passwd = getString(properties, "passwd", AGENT_DEFAULT_PASSWD);
        ignoreQueues = getString(properties, "ignoreQueues").split(",");
        ignoreTopics = getString(properties, "ignoreTopics").split(",");

        Arrays.sort(ignoreQueues);
        Arrays.sort(ignoreTopics);

    }

    private String getString(Map<String, ?> properties, String propertyName, String defaultValue) {
        String value = (String) properties.get(propertyName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    private String getString(Map<String, ?> properties, String propertyName) {
        return getString(properties, propertyName, "");
    }

    public boolean isIgnoredQueue(String queueName) {
        return Arrays.binarySearch(ignoreQueues, queueName) >= 0;
    }


    public boolean isIgnoredTopic(String topicName) {
        return Arrays.binarySearch(ignoreTopics, topicName) >= 0;
    }

    public String[] getIgnoreQueues() {
        return ignoreQueues;
    }

    public String[] getIgnoreTopics() {
        return ignoreTopics;
    }

    public String getServerType() {
        return serverType;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPasswd() {
        return passwd;
    }

    public boolean isLocalhost() {
        return this.host.equals("localhost");
    }

}
