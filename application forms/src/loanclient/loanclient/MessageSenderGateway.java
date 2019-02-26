package loanclient.loanclient;

import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.Gson;

public class MessageSenderGateway {
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;

    public MessageSenderGateway(String channelName){
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue."+channelName),channelName );

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            destination = (Destination) jndiContext.lookup(channelName);
            producer = session.createProducer(destination);


        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    public Message createTextMessage(String body)
    {
        try {
            Message msg = session.createTextMessage(body);
            return msg;
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void send(Message msg){
        try {
            producer.send(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
