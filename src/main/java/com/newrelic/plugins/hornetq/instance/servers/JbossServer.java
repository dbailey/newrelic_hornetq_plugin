package com.newrelic.plugins.hornetq.instance.servers;

import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.plugins.hornetq.instance.Configuration;
import com.newrelic.plugins.hornetq.instance.MessageType;
import com.newrelic.plugins.hornetq.instance.Server;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configurações do servidor Jboss/WildFly
 */
public class JbossServer implements Server {


    private static final String URL_FORMAT = "service:jmx:http-remoting-jmx://%s:%s";
    private final Configuration configuration;

    public JbossServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public MBeanServerConnection getConnection() throws ConfigurationException {

        String urlStr = String.format(URL_FORMAT, configuration.getHost(), configuration.getPort());

        Map<String, String[]> env = new HashMap<String, String[]>();
        env.put("org.jboss.remoting-jmx.timeout", new String[]{"20"});

        if (!configuration.isLocalhost()) {
            env.put(JMXConnector.CREDENTIALS, new String[]{configuration.getUser(), configuration.getPasswd()});
        }

        try {

            JMXServiceURL address = new JMXServiceURL(urlStr);
            JMXConnector connector = JMXConnectorFactory.connect(address, env);
            return connector.getMBeanServerConnection();

        } catch (MalformedURLException e) {
            throw new ConfigurationException("Invalid connection url: " + urlStr, e);
        } catch (IOException e) {
            throw new ConfigurationException("JMX connection error", e);
        }

    }

    public ObjectName getQueueObjectName() throws ConfigurationException {
        try {
            return new ObjectName("jboss.as:subsystem=messaging,hornetq-server=*,jms-queue=*,*");
        } catch (MalformedObjectNameException e) {
            throw new ConfigurationException(e);
        }
    }

    public ObjectName getTopicObjectName() throws ConfigurationException {
        try {
            return new ObjectName("jboss.as:subsystem=messaging,hornetq-server=*,jms-topic=*,*");
        } catch (MalformedObjectNameException e) {
            throw new ConfigurationException(e);
        }
    }

    public String[] getQueueAttributes() {
        return new String[]{
                "messageCount",
                "messagesAdded",
                "deliveringCount",
                "consumerCount",
                "scheduledCount"
        };
    }

    public String[] getTopicAttributes() {
        return new String[]{
                "messageCount",
                "messagesAdded",
                "deliveringCount",
                "subscriptionCount",
                "durableSubscriptionCount",
                "nonDurableSubscriptionCount",
                "durableMessageCount",
                "nonDurableMessageCount"
        };
    }

    public String getMBeanServerName(MessageType type, ObjectName mbean) {
        return mbean.getKeyProperty("hornetq-server");
    }

    public String getMBeanQueueName(MessageType type, ObjectName mbean) {
        return mbean.getKeyProperty("jms-" + type.name().toLowerCase());
    }
}
