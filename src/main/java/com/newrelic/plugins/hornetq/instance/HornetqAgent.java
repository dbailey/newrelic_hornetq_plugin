package com.newrelic.plugins.hornetq.instance;

import java.util.logging.Logger;
import java.util.*;

import javax.management.*;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.processors.EpochCounter;
import com.newrelic.plugins.hornetq.instance.servers.HornetqServer;
import com.newrelic.plugins.hornetq.instance.servers.JbossServer;
import org.apache.commons.lang3.Validate;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@SuppressWarnings("unused")
public class HornetqAgent extends Agent {

    private static final String GUID = "com.newrelic.plugins.hornetq.instance";
    private static final String version = "0.0.1-beta";

    public static final String AGENT_DEFAULT_USER = "newrelic";
    public static final String AGENT_DEFAULT_INSTANCE = "newrelic";

    public static final String AGENT_CONFIG_FILE = "hornetq.instance.json";


    public static final String COMMA = ",";
    public static final String SEPARATOR = "/";
    private static final String HORNETQ = "hornetq";
    private static final String JBOSS = "jboss";
    private static final String MESSAGE_TYPE_QUEUE = "Queue";
    private static final String MESSAGE_TYPE_TOPIC = "Topic";
    private static final String ATTRIBUTE_MESSAGE_COUNT = "messageCount";
    private static final String IGNORED_OBJECT_LOG = "%s %s ignored!";
    private static final String READING_ATTRIBUTE_LOG = "Reading attribute %s, original value: %s";
    private static final String DEFAULT_UNIT = "count";
    private static final String REPORTED_METRIC_LOG = "Metric %s (%s) -- value: %d";

    final Logger logger;
    private final Configuration configuration;

    private boolean firstHarvest = true;
    long harvestCount = 0;


    MBeanServerConnection mbs = null;

    private Map<String, EpochCounter> countMetrics = new HashMap<String, EpochCounter>();
    private Server server = null;


    /**
     * Cria uma nova instancia do plugin
     */
    public HornetqAgent(Configuration configuration) throws ConfigurationException {
        super(GUID, version);

        this.configuration = configuration;

        logger = Context.getLogger(); // Set logging to current Context

        if (HORNETQ.equals(configuration.getServerType())) { // HornetQ Standalone
            server = new HornetqServer(configuration);
        } else if (JBOSS.equals(configuration.getServerType())) { // HornetQ running in JBoss container
            server = new JbossServer(configuration);
        } else {
            System.err.println("Server type can only be \"hornetq\" or \"jboss\".");
            System.err.println("Please correct the \"serverType\" property in " + AGENT_CONFIG_FILE + " and restart the plugin agent.\n\n");
            System.exit(1);
        }

        mbs = server.getConnection();
    }




    @Override
    public void pollCycle() {
        logger.info("Gathering HornetQ metrics. " + getAgentInfo());
        logger.info("Reporting HornetQ metrics: harvest cycle " + (++harvestCount) + ".");
        try {
            collectMetrics(mbs, server.getQueueObjectName(), MessageType.QUEUE, server.getQueueAttributes());
            collectMetrics(mbs, server.getTopicObjectName(), MessageType.TOPIC, server.getTopicAttributes());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        firstHarvest = false;

    }

    public void collectMetrics(MBeanServerConnection mbs, ObjectName objName, MessageType messageType, String[] attributes) throws Throwable {

        Validate.notNull(messageType, "MessageType is required.");

        Set<ObjectName> mbeans = mbs.queryNames(objName, null);
        Number totalDepth = 0;
        Number totalCount = 0;

        for (ObjectName mbean : mbeans) {
            String name = null;
            MBeanInfo mbeanInfo = mbs.getMBeanInfo(mbean);

            logger.info(">>> processing object : " + mbean.getCanonicalName());

            String mbeanServerName = server.getMBeanServerName(messageType, mbean);
            String mbeanObjectName = server.getMBeanObjectName(messageType, mbean);

            if (messageType.isIgnoredObject(configuration, mbeanObjectName)) {
                logger.info(format(IGNORED_OBJECT_LOG, messageType, mbeanObjectName));
                continue;
            }

            AttributeList attrList = mbs.getAttributes(mbean, attributes);

            for (int i = 0; i < attrList.size(); i++) {

                Attribute attr = (Attribute) attrList.get(i);
                String attrName = attr.getName();
                String attrValue = attr.getValue().toString();
                Number metricValue;

                logger.fine(format(READING_ATTRIBUTE_LOG, attrName, attrValue));

                String metricName = "JMS" + SEPARATOR + mbeanServerName + SEPARATOR + messageType + SEPARATOR + mbeanObjectName + SEPARATOR + attrName;

                if (attrName.endsWith("Added")) {
                    metricValue = firstNonNull(getMetricData(metricName).process(Long.parseLong(attrValue)), 0);
                    totalCount = totalCount.longValue() + metricValue.longValue();
                } else {
                    metricValue = Long.parseLong(attrValue);
                    if (attrName.equals(ATTRIBUTE_MESSAGE_COUNT))
                        totalDepth = totalDepth.longValue() + metricValue.longValue();
                }

                reportMetric(metricName, DEFAULT_UNIT, metricValue.longValue());
                logger.info(format(REPORTED_METRIC_LOG, metricName, DEFAULT_UNIT, metricValue.longValue()));

            }

        }

        reportMetric("JMS" + SEPARATOR + messageType + SEPARATOR + "TotalDepth", DEFAULT_UNIT, totalDepth.longValue());
        logger.info("Metric " + "JMS" + SEPARATOR + messageType + SEPARATOR + "TotalDepth -- value: " + totalDepth.longValue());

        reportMetric("JMS" + SEPARATOR + messageType + SEPARATOR + "TotalCount", DEFAULT_UNIT, totalCount.longValue());
        logger.info("Metric " + "JMS" + SEPARATOR + messageType + SEPARATOR + "TotalCount -- value: " + totalCount.longValue());

    }

    private EpochCounter getMetricData(String metricName) {
        EpochCounter ec = countMetrics.get(metricName);
        if (ec == null) {
            ec = new EpochCounter();
            ec.process(0);
            countMetrics.put(metricName, ec);
        }
        return ec;
    }

    private String getAgentInfo() {
        return "Agent Name: " + configuration.getName() + ". Agent Version: " + version;
    }

    @Override
    public String getComponentHumanLabel() {
        return configuration.getName();
    }

}
