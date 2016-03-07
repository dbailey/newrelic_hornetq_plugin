package com.newrelic.plugins.hornetq.instance;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

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

        return new HornetqAgent(new Configuration(properties));

    }

}
