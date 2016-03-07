package com.newrelic.plugins.hornetq.instance;

import com.newrelic.metrics.publish.configuration.ConfigurationException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Server Type interface
 */
public interface Server {

    MBeanServerConnection getConnection() throws ConfigurationException;

    ObjectName getQueueObjectName() throws ConfigurationException;

    ObjectName getTopicObjectName() throws ConfigurationException;

    String[] getQueueAttributes();

    String[] getTopicAttributes();

    String getMBeanServerName(MessageType type, ObjectName mbean);

    String getMBeanObjectName(MessageType type, ObjectName mbean);
}
