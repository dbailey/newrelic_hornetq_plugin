HornetQ Plugin
==========================================================
- - -
The HornetQ plugin collects JMX data from HornetQ servers and reports to New Relic.


##Prerequisites

*    A New Relic account. If you are not already a New Relic user, you can signup for a free account at http://newrelic.com
*    Obtain the New Relic Generic Log Reader plugin
*    A server that generates log files with apache common log format.
*    A configured Java Runtime (JRE) environment Version 1.7.
*    Network access to New Relic


##Additional Plugin Details:

*	This plugin read JMX metrics from a HornetQ message broker, and reports these metrics to New Relic.


##Installation

Linux example:

*    $ mkdir /path/to/newrelic-plugin
*    $ cd /path/to/newrelic-plugin
*    $ tar xfz newrelic-*-plugin-*.tar.gz
   


## Configure the agent environment
New Relic plugins run an agent processes to collect and report metrics to New Relic. In order for that you need to configure your New Relic license and plugin-specific properties. Additionally you can set the logging properties.


### Configure your New Relic license
Specify your license key in a file by the name 'newrelic.properties' in the 'config' directory.
Your license key can be found under "Account Settings" at https://rpm.newrelic.com. See https://newrelic.com/docs/subscriptions/license-key for more help.

Linux example:

*    $ cp config/template_newrelic.properties config/newrelic.properties
*    $ Edit config/newrelic.properties and paste in your license key

### Configure plugin properties
Each running the plugin agent requires a JSON configuration file defining the access to the monitored HornetQ message broker. An example file is provided in the config directory.

Edit config/hornetq.instance.json and specify the necessary property values. Change the values for all the properties that apply to your server and environment. The value of the "pluginName" field will appear in the New Relic user interface for the log file reader instance (i.e. "Local JBoss Instance"). 

    [
      {
    	"serverType" 		: "jboss",
		"pluginName" 		: "Local JBoss Instance",
		"host"   			: "localhost",
		"port"   			: "4447",
		"jmsServers" 		: "all",
		"queueObjName" 		: "jboss.as:subsystem=messaging,hornetq-server=*,jms-queue=*,*",
		"topicObjName" 		: "jboss.as:subsystem=messaging,hornetq-server=*,jms-topic=*,*",
		"ignoreJMSServers" 	: "",
		"ignoreQueues" 		: "",
		"ignoreTopics" 		: "",
		"user"   			: "system",
		"passwd" 			: "system"
       }
    ]

  * serverType			- Type of the server (jboss or standalone hornetq)
  * pluginName			- A friendly name that will show up in the New Relic Dashboard.
  * host 				- Hostname of the server being monitored.
  * port 				- Port number for the server being monitored.
  * jmsServers 			- 
  * queueObjName 		- 
  * topicObjName 		- 
  * ignoreJMSServers 	- 
  * ignoreQueues 		- 
  * ignoreTopics 		- 
  * user 				- 
  * passwd 				- 

**Note:** Specify the above set of properties for each plugin instance. You will have to follow the syntax (embed the properties for each instance of the plugin in a pair of curley braces separated by a comma).

**Note:** If you would like to monitor multiple HornetQ instances, copy the block of JSON properties (separated by comma), and change the values accordingly. Example:

    [
      {
    	"serverType" 		: "jboss",
		"pluginName" 		: "Local JBoss Instance",
		"host"   			: "localhost",
		"port"   			: "4447",
		"jmsServers" 		: "all",
		"queueObjName" 		: "jboss.as:subsystem=messaging,hornetq-server=*,jms-queue=*,*",
		"topicObjName" 		: "jboss.as:subsystem=messaging,hornetq-server=*,jms-topic=*,*",
		"ignoreJMSServers" 	: "",
		"ignoreQueues" 		: "",
		"ignoreTopics" 		: "",
		"user"   			: "system",
		"passwd" 			: "system"
      },
      {
    	"serverType" 		: "jboss",
		"pluginName" 		: "My Other Local JBoss Instance",
		"host"   			: "localhost",
		"port"   			: "4449",
		"jmsServers" 		: "all",
		"queueObjName" 		: "jboss.as:subsystem=messaging,hornetq-server=*,jms-queue=*,*",
		"topicObjName" 		: "jboss.as:subsystem=messaging,hornetq-server=*,jms-topic=*,*",
		"ignoreJMSServers" 	: "",
		"ignoreQueues" 		: "",
		"ignoreTopics" 		: "",
		"user"   			: "system",
		"passwd" 			: "system"
      }
    ]


### Configure logging properties
The plugin checks for the logging properties in config/logging.properties file. You can copy example_logging.properties and edit it if needed. By default he properties in this file are configured to log data at 'info' level to th console. You can edit the file and enable file logging.

Linux example:

*    $ cp config/example_loging.properties config/logging.properties


## Running the agent
To run the plugin in from the command line: 

*    `$ java -jar newrelic_*_plugin-*.jar`

If your host needs a proxy server to access the Internet, you can specify a proxy server & port: 

*    `$ java -Dhttps.proxyHost="PROXY_HOST" -Dhttps.proxyPort="PROXY_PORT" -jar newrelic_*_plugin-*.jar`

To run the plugin from the command line and detach the process so it will run in the background:

*    `$ nohup java -jar newrelic_*_plugin-*.jar &`

**Note:** At present there are no [init.d](http://en.wikipedia.org/wiki/Init) scripts to start the this plugin at system startup. You can create your own script, or use one of the services below to manage the process and keep it running:

*    [Upstart](http://upstart.ubuntu.com/)
*    [Systemd](http://www.freedesktop.org/wiki/Software/systemd/)
*    [Runit](http://smarden.org/runit/)
*    [Monit](http://mmonit.com/monit/)

