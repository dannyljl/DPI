package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;


public class LoanBrokerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

	private Connection connection;
	private Session session;

	private Destination receiveDestination;
	private MessageConsumer consumer;

	private Destination sendDestination;
	MessageProducer producer;

	BankInterestRequest BankRequest = new BankInterestRequest();
	BankInterestReply bankReply = new BankInterestReply();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {
		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);

		CreateConsumer("ClientReqQ");


		try {
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message msg) {
					Gson g = new Gson();
					String msgBody = null;
					try {
						msgBody = ((TextMessage) msg).getText();
					} catch (JMSException e) {
						e.printStackTrace();
					}
					BankRequest = g.fromJson(msgBody,BankInterestRequest.class);

					String queueName = "BankReqQ";

					try {
						Properties props = new Properties();
						props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
						props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

						// connect to the Destination called “myFirstChannel”
						// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
						props.put(("queue." + queueName), queueName);

						Context jndiContext = new InitialContext(props);
						ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
								.lookup("ConnectionFactory");
						connection = connectionFactory.createConnection();
						session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

						// connect to the sender destination
						sendDestination = (Destination) jndiContext.lookup(queueName);
						producer = session.createProducer(sendDestination);

						String body = BankRequest.toString(); //or serialize an object!
						// create a text message
						Message bankmessage = session.createTextMessage(body);
						bankmessage.setJMSReplyTo(session.createQueue("BankRepQ"));
						bankmessage.setJMSCorrelationID(msg.getJMSMessageID());
						// send the message
						producer.send(bankmessage);

					} catch (NamingException | JMSException e) {
						e.printStackTrace();
					}
				}
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}

		CreateConsumer("BankRepQ");

		try {
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message msg) {
					Gson g = new Gson();
					String msgBody = null;
					try {
						msgBody = ((TextMessage) msg).getText();
					} catch (JMSException e) {
						e.printStackTrace();
					}
					BankInterestReply bankreply = g.fromJson(msgBody,BankInterestReply.class);

					LoanReply loanReply = new LoanReply();
					loanReply.setInterest(bankreply.getInterest());
					loanReply.setQuoteID(bankreply.getQuoteId());



					String queueName = "ClientRepQ";

					try {
						Properties props = new Properties();
						props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
						props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

						// connect to the Destination called “myFirstChannel”
						// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
						props.put(("queue." + queueName), queueName);

						Context jndiContext = new InitialContext(props);
						ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
								.lookup("ConnectionFactory");
						connection = connectionFactory.createConnection();
						session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

						// connect to the sender destination
						sendDestination = (Destination) jndiContext.lookup(queueName);
						producer = session.createProducer(sendDestination);

						String body = loanReply.toString(); //or serialize an object!
						// create a text message
						Message loanreplymessage = session.createTextMessage(body);
						loanreplymessage.setJMSCorrelationID(msg.getJMSMessageID());
						// send the message
						producer.send(loanreplymessage);

					} catch (NamingException | JMSException e) {
						e.printStackTrace();
					}

				}
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}


		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);
	}

	 private JListLine getRequestReply(LoanRequest request){
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     
	     return null;
	   }
	
	public void add(LoanRequest loanRequest){		
		listModel.addElement(new JListLine(loanRequest));		
	}
	

	public void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);;
            list.repaint();
		}		
	}

	public void CreateProducer(String queueName, Message msg,String destinationReply){
		try {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

			// connect to the Destination called “myFirstChannel”
			// queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
			props.put(("queue." + queueName), queueName);

			Context jndiContext = new InitialContext(props);
			ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// connect to the sender destination
			sendDestination = (Destination) jndiContext.lookup(queueName);
			producer = session.createProducer(sendDestination);

			String body = BankRequest.toString(); //or serialize an object!
			// create a text message
			Message bankmessage = session.createTextMessage(body);
			if(destinationReply != null){
				bankmessage.setJMSReplyTo(session.createQueue(destinationReply));
			}
			bankmessage.setJMSCorrelationID(msg.getJMSMessageID());
			// send the message
			producer.send(bankmessage);

		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
	}

	public void CreateConsumer(String queueName){
		try {
			Properties props = new Properties();
			props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
					"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
			props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

			// connect to the Destination called “myFirstChannel”
			// queue or topic: “queue.myFirstDestination” or
			//“topic.myFirstDestination”
			props.put(("queue." + queueName), queueName);

			Context jndiContext = new InitialContext(props);
			ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// connect to the receiver destination
			receiveDestination = (Destination) jndiContext.lookup(queueName);
			consumer = session.createConsumer(receiveDestination);

			connection.start(); // this is needed to start receiving messages

		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}

	}


}
