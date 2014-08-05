package com.appdynamics.extensions.wmb;


import com.appdynamics.extensions.wmb.config.ConfigUtil;
import com.appdynamics.extensions.wmb.config.Configuration;
import com.appdynamics.extensions.wmb.config.ResourceStatTopic;
import com.appdynamics.extensions.wmb.resourcestats.xml.ResourceIdentifier;
import com.appdynamics.extensions.wmb.resourcestats.xml.ResourceStatistics;
import com.appdynamics.extensions.wmb.resourcestats.xml.ResourceType;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;

public class WmbMonitor {

    public static final Logger logger = Logger.getLogger(WmbMonitor.class);
    public static final String CONFIG_FILENAME = "conf/config.yml";
    private final static ConfigUtil<Configuration> configUtil = new ConfigUtil<Configuration>();

    public static void main(String[] args){
        logger.info("Starting the WMB Monitor");
        WmbMonitor wmbMonitor = new WmbMonitor();
        wmbMonitor.execute();
        logger.info("Terminating the WMB Monitor");
    }

    public void execute(){
        //#TODO in pom make sure log4j.xml is propagating in the zip. Also, in start up command pass log4j.xml
        try {
            // load the config file
            Configuration configuration = configUtil.readConfig(CONFIG_FILENAME,Configuration.class);
            try {
                //create connection
                Connection conn = createConnection(configuration.getHost(), configuration.getPort(), configuration.getClientId());
                //build parser
                Unmarshaller parser = new ParserBuilder().getParser(ResourceStatistics.class, ResourceIdentifier.class, ResourceType.class);
                //register subscribers
                registerSubscribers(conn,configuration,parser);
                //start connection
                conn.start();
                //wait indefinitely
                while(true){
                    Thread.sleep(configuration.getSleepTime() * 1000);
                }

            } catch (JMSException e) {
                logger.error("Unable to connect or subscribe ::" + configuration.getHost() + "," + configuration.getPort(), e);
            } catch (JAXBException e) {
                logger.error("Unable to initialize the XML unmarshaller " + e);
            } catch (InterruptedException e) {
                logger.error("Insomniac! " + e);
            }
        } catch (FileNotFoundException e) {
            logger.error("Config file not found :: " + CONFIG_FILENAME, e);
        }


    }

    private void registerSubscribers(Connection conn, Configuration config,Unmarshaller parser) throws JMSException {
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if(config != null && config.getResourceStatTopics() != null){
            for(ResourceStatTopic resTopic : config.getResourceStatTopics()){
                Topic topic = session.createTopic(resTopic.getName());
                TopicSubscriber topicSub = session.createDurableSubscriber(topic,resTopic.getSubscriberName());
                topicSub.setMessageListener(new ResourceStatMessageListener(config,parser));
            }
        }
    }



    private Connection createConnection(String host, int port, String clientId) throws JMSException {
        MQConnectionFactory cf = new MQConnectionFactory();
        cf.setHostName(host);
        cf.setPort(port);
        cf.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
        Connection connection =  cf.createConnection();
        connection.setClientID(clientId);
        return connection;
    }

}
