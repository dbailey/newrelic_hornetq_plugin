package com.newrelic.plugins.hornetq.instance;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.plugins.hornetq.instance.HornetqAgent;

public class HornetqAgentFactory extends AgentFactory {

    /**
     * Construct an Agent Factory based on the default properties file
     */
    public HornetqAgentFactory() {
        super(HornetqAgent.AGENT_CONFIG_FILE);
    }

    /**
     * Configure an agent based on an entry in the oracle json file. There may
     * be multiple agents per Plugin - one per oracle instance
     * 
     */
    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
        String serverType = (String) properties.get("serverType");
        String name = (String) properties.get("pluginName");
        String host = (String) properties.get("host");
        String port = (String) properties.get("port");
        String user = (String) properties.get("user");
        String passwd = (String) properties.get("passwd");
        

        /**
         * Use pre-defined defaults to simplify configuration
         */
        if (host == null || "".equals(host))
            host = HornetqAgent.AGENT_DEFAULT_HOST;
        if (port == null || "".equals(port))
            port = HornetqAgent.AGENT_DEFAULT_PORT;
        if (user == null || "".equals(user))
            user = HornetqAgent.AGENT_DEFAULT_USER;
        if (passwd == null)
            passwd = HornetqAgent.AGENT_DEFAULT_PASSWD;

        return new HornetqAgent(serverType, name, host, port, user, passwd);
    }

}
