package com.newrelic.plugins.hornetq.instance;

import java.util.logging.Logger;
import java.util.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.*;
import javax.management.remote.*;
import javax.management.openmbean.*;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.processors.EpochCounter;

@SuppressWarnings("unused")
public class HornetqAgent extends Agent {

    private static final String GUID = "com.newrelic.plugins.hornetq.instance";
    private static final String version = "0.0.1-beta";

    public static final String AGENT_DEFAULT_HOST = "localhost"; // Default values for hornetq Agent
    public static final String AGENT_DEFAULT_PORT = "4447"; // Default values for hornetq port in jboss
    public static final String AGENT_DEFAULT_USER = "newrelic";
    public static final String AGENT_DEFAULT_PASSWD = "securepassword";
    public static final String AGENT_DEFAULT_INSTANCE = "newrelic";

    public static final String AGENT_CONFIG_FILE = "hornetq.instance.json";

    
    
    public static final String COMMA = ",";
    public static final String SEPARATOR = "/";

    private String pluginName; // Agent Name

    private String host; // HornetQ Connection parameters
    private String port;
    private String user;
    private String passwd;


    final Logger logger;

    private boolean firstHarvest = true;
    long harvestCount = 0;

    
    private String serverType;
    private Map<String,String[]> env = new HashMap<String, String[]>();
    private String urlStr = null;
    MBeanServerConnection mbs = null;
    private ObjectName queueObjName = null;
    private ObjectName topicObjName = null;
    private String queueAttributes[];
    private String topicAttributes[];

    private Map<String, EpochCounter> countMetrics = new HashMap<String, EpochCounter>();

    
    
    /**
     * Default constructor to create a new Oracle Agent
     * 
     * @param map
     * @param String Human name for Agent
     * @param String Oracle Instance host:port
     * @param String Oracle user
     * @param String Oracle user password
     * @param String CSVm List of metrics to be monitored
     */
    public HornetqAgent(String serverType, String pluginName, String host, String port, String user, String passwd) {
        super(GUID, version);

        this.serverType = serverType;
        this.pluginName = pluginName; // Set local attributes for new class object
        this.host = host;
        this.port = port;
        this.user = user;
        this.passwd = passwd;

        logger = Context.getLogger(); // Set logging to current Context

        if(this.serverType.equals("hornetq")) { // HornetQ Standalone
            urlStr = "service:jmx:rmi:///jndi/rmi://" + host+":" + port + "/jmxrmi";
        	try {
				queueObjName = new ObjectName("org.hornetq:module=JMS,type=Queue,name=*,*");
	        	topicObjName = new ObjectName("org.hornetq:module=JMS,type=Topic,name=*,*");
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            queueAttributes = new String[] {
                "MessageCount",
                "MessagesAdded",
                "DeliveringCount",
                "ConsumerCount",
                "ScheduledCount"
            };

            topicAttributes = new String[] {
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
        else if(this.serverType.equals("jboss")) { // HornetQ running in JBoss container
            urlStr = "service:jmx:remoting-jmx://" + host + ":" + port;
            try {
				queueObjName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=*,jms-queue=*,*");
	            topicObjName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=*,jms-topic=*,*");
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            queueAttributes = new String[] {
                "messageCount",
                "messagesAdded",
                "deliveringCount",
                "consumerCount",
                "scheduledCount"
            };

            topicAttributes = new String[] {
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
        else {
        	System.err.println("Server type can only be \"hornetq\" or \"jboss\".");
        	System.err.println("Please correct the \"serverType\" property in " + AGENT_CONFIG_FILE + " and restart the plugin agent.\n\n");
        	System.exit(1);
        }

        // Connecting to the jmx server
        JMXServiceURL address = null;
		try {
			address = new JMXServiceURL(urlStr);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        JMXConnector connector = null;
		try {
			connector = JMXConnectorFactory.connect(address, env);
			mbs = connector.getMBeanServerConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
    @Override
	public void pollCycle() {
		// TODO Auto-generated method stub
        logger.info("Gathering HornetQ metrics. " + getAgentInfo());
        logger.info("Reporting HornetQ metrics: harvest cycle " + (++harvestCount) + ".");
        try {
			collectMetrics(mbs, queueObjName, "Queue", queueAttributes);
	        collectMetrics(mbs, topicObjName, "Topic", topicAttributes);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //List<String> results = gatherMetrics(); // Gather Wait metrics
        //reportMetrics(results); // Report Wait metrics to New Relic
        firstHarvest = false;

	}

    public void collectMetrics(MBeanServerConnection mbs, ObjectName objName, String messageType, String[] attributes) throws Throwable {

        Set<ObjectName> mbeans = mbs.queryNames(objName, null);
        //System.out.println(">> mbeans size: " + mbeans.size());
        String metricName = null;
        String unit = null;
        Number metricValue = 0;
        Number totalDepth = 0;
        Number totalCount = 0;

        for( ObjectName mbean : mbeans )
        {
        	String name = null;
            MBeanInfo mbeanInfo = mbs.getMBeanInfo(mbean);
            logger.info(">>> objectname: " + mbean.getCanonicalName());
            //System.out.println(">>> classname : " + mbeanInfo.getClassName());

            String mbeanServerName = (serverType.equals("hornetq")) ? host+"_hornetq" : mbean.getKeyProperty("hornetq-server");
            String mbeanQueueName = (serverType.equals("hornetq")) ? mbean.getKeyProperty("name") : mbean.getKeyProperty("jms-" + messageType.toLowerCase());
            String mbeanDeployment = mbean.getKeyProperty("deployment");

            //  Get attributes
            //try {
            
                AttributeList  attrList = mbs.getAttributes(mbean, attributes);
                
                for(int i=0; i<attrList.size(); i++) {
                	
                    Attribute attr = (Attribute) attrList.get(i);
                    String attrName = attr.getName();
                    String attrValue=attr.getValue().toString();
                    if(serverType.equals("jboss") &&(attrName.equals("queueAddress") || attrName.equals("topicAddress"))) {
                        attrValue = attrValue.substring(attrValue.lastIndexOf(".") + 1);
                    }

                	metricName = "JMS" + SEPARATOR + mbeanServerName + SEPARATOR + messageType + SEPARATOR + mbeanQueueName + SEPARATOR + attrName;
                	if(attrName.endsWith("Added")) {
                		metricValue = getMetricData(metricName).process(Long.parseLong(attrValue));
                		if (metricValue == null)
                			metricValue = 0;
                		totalCount = totalCount.longValue() + metricValue.longValue();
                		unit = "count/second";
                	}
                	else {
                		metricValue = Long.parseLong(attrValue);
                    	if(attrName.equals("messageCount"))
                    		totalDepth = totalDepth.longValue() + metricValue.longValue();
                		unit = "count";
                	}
                	reportMetric(metricName, "count", metricValue.longValue());
                	if(metricValue.longValue() != 0)
                		logger.info("Metric " + metricName + " (count) -- value: " + metricValue.longValue());

                }
                
            //} catch (Throwable e) {
            //    System.out.println("exception: " + e.getMessage());
            //}
        }
        
        reportMetric("JMS" + SEPARATOR + messageType + SEPARATOR + "TotalDepth", "count", totalDepth.longValue());
        logger.info("Metric " + "JMS" + SEPARATOR + messageType + SEPARATOR + "TotalDepth -- value: " + totalDepth.longValue());
        
        reportMetric("JMS" + SEPARATOR + messageType + SEPARATOR + "TotalCount", "count", totalCount.longValue());
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
        return "Agent Name: " + pluginName + ". Agent Version: " + version;
    }

	@Override
	public String getComponentHumanLabel() {
		// TODO Auto-generated method stub
		return pluginName;
	}

}
