/**
 * 
 */
package com.newrelic.plugins.hornetq.instance;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.plugins.hornetq.instance.HornetqAgentFactory;

/**
 * @author shahram
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        Runner runner = new Runner();
        runner.add(new HornetqAgentFactory());

        try {
            runner.setupAndRun();           // Never returns
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.err.println("Error configuring New Relic Agent");
            System.exit(1);
        }

	}

}
