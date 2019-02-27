package abnamro.bank;

import abstractGateWay.abstractGateway;
import loanclient.loanclient.MessageReceiverGateway;
import loanclient.loanclient.MessageSenderGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import sun.misc.Request;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoanBrokerAppGateway extends abstractGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("BankBrokerReplyQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("abnBrokerRequestQ");
    private BankSerializer serializer = new BankSerializer();
    private String RequestID = null;
    private Map<BankInterestRequest,String> RequestWithIDList = new HashMap<BankInterestRequest,String>();
    private List<RequestReply> requestReplyList = new ArrayList<RequestReply>();
    private BankInterestRequest request;
    private int aggregationID;

    private PropertyChangeSupport support;


    public LoanBrokerAppGateway(){
        //support = new PropertyChangeSupport(this);
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
                    RequestID = message.getJMSCorrelationID();
                    System.out.println("correlationID:" + message.getJMSCorrelationID());
                    System.out.println("messageID" + message.getJMSMessageID());
                    aggregationID = message.getIntProperty("aggregationID");
                    OnLoanRequestArrived(serializer.requestFromString(textMessage));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void SendReply(BankInterestReply reply, BankInterestRequest request){
        request = this.request;
        RequestReply requestReply = new RequestReply(request,reply);
        requestReplyList.add(requestReply);
        //setRequestList(requestReplyList);
        Message message = sender.createTextMessage(serializer.replyToString(reply));
        String id = RequestWithIDList.get(request);
        try {
            message.setJMSCorrelationID(RequestID);
            message.setIntProperty("aggregationID", aggregationID);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        sender.send(message);
    }


    public BankInterestRequest OnLoanRequestArrived(BankInterestRequest request){
        this.request = request;
        System.out.println("BankInterest Request: " + request.toString());
        RequestWithIDList.put(request,RequestID);
        requestReplyList.add(new RequestReply(request,null));
        //setRequestList(requestReplyList);
        return request;
    }

    public void setRequestList(List<RequestReply> value) {
        support.firePropertyChange("RequestReply", requestReplyList, value);
        this.requestReplyList = value;
    }
}
