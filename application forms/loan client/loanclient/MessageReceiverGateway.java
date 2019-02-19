package loanclient;


import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


public class MessageReceiverGateway {
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer consumer;

    public MessageReceiverGateway(String channelName){
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or
            //“topic.myFirstDestination”
            props.put(("queue."+channelName), channelName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            Destination receiveDestination = (Destination) jndiContext.lookup(channelName);
            consumer = session.createConsumer(receiveDestination);

            connection.start(); // this is needed to start receiving messages

        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    public void setListener(MessageListener ml){
        try {
            consumer.setMessageListener(ml);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
