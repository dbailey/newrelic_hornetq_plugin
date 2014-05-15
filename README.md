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

*	 
*	



##Installation

Linux example:

*    $ mkdir /path/to/newrelic-plugin
*    $ cd /path/to/newrelic-plugin
*    $ tar xfz newrelic-*-plugin-*.tar.gz
   


## Configure the agent environment



### Configure your New Relic license
Specify your license key in a file by the name 'newrelic.properties' in the 'config' directory.
Your license key can be found under "Account Settings" at https://rpm.newrelic.com. See https://newrelic.com/docs/subscriptions/license-key for more help.

Linux example:

*    $ cp config/template_newrelic.properties config/newrelic.properties
*    $ Edit config/newrelic.properties and paste in your license key

### Configure plugin properties



### Configure logging properties
The plugin checks for the logging properties in config/logging.properties file. You can copy example_logging.properties and edit it if needed

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

## For support
Plugin support for troubleshooting assistance can be obtained by visiting New Relic support web site: (https://support.newrelic.com)


