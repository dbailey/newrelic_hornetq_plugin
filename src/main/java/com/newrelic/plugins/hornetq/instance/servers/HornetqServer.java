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
 * HornetQ Configuration
 */
public class HornetqServer implements Server {

    private Configuration configuration;

    public HornetqServer(Configuration configuration) {
        this.configuration = configuration;
    }

    public MBeanServerConnection getConnection() throws ConfigurationException {
        String urlStr = "service:jmx:rmi:///jndi/rmi://" + configuration.getHost() + ":" + configuration.getPort() + "/jmxrmi";
        Map<String, String[]> env = new HashMap<String, String[]>();

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
            return new ObjectName("org.hornetq:module=JMS,type=Queue,name=*,*");
        } catch (MalformedObjectNameException e) {
            throw new ConfigurationException(e);
        }
    }

    public ObjectName getTopicObjectName() throws ConfigurationException {
        try {
            return new ObjectName("org.hornetq:module=JMS,type=Topic,name=*,*");
        } catch (MalformedObjectNameException e) {
            throw new ConfigurationException(e);
        }
    }

    public String[] getQueueAttributes() {
        return new String[]{
                "MessageCount",
                "MessagesAdded",
                "DeliveringCount",
                "ConsumerCount",
                "ScheduledCount"
        };
    }

    public String[] getTopicAttributes() {
        return new String[]{
                "MessageCount",
                "MessagesAdded",
                "DeliveringCount",
                "SubscriptionCount",
                "DurableSubscriptionCount",
                "NonDurableSubscriptionCount",
                "DurableMessageCount",
                "NonDurableMessageCount"
        };
    }

    public String getMBeanServerName(MessageType type, ObjectName mbean) {
        return this.configuration.getHost()+ "_hornetq";
    }

    public String getMBeanQueueName(MessageType type, ObjectName mbean) {
        return mbean.getKeyProperty("name");
    }
}
