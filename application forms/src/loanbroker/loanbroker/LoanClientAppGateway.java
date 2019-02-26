package loanbroker.loanbroker;

import abstractGateWay.abstractGateway;
import loanclient.loanclient.LoanSerializer;
import loanclient.loanclient.MessageReceiverGateway;
import loanclient.loanclient.MessageSenderGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoanClientAppGateway extends abstractGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("ClientBrokerReplyQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("ClientBrokerRequestQ");
    private LoanSerializer serializer = new LoanSerializer();
    private BankAppGateway appGateway = new BankAppGateway(this);
    private String RequestID = null;
    private Map<String,String> IdList = new HashMap<String, String>();

    private PropertyChangeSupport support;

    public LoanClientAppGateway(){
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
                    RequestID = message.getJMSMessageID();
                    System.out.println(RequestID);
                    onLoanRequestArrived(serializer.requestFromtring(textMessage), RequestID);

                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onLoanRequestArrived(LoanRequest request, String requestID){
        BankInterestRequest bankRequest = new BankInterestRequest();
        bankRequest.setTime(request.getTime());
        bankRequest.setAmount(request.getAmount());
        System.out.println("Arrived LoanRequest: " + request.toString());
        appGateway.sendBankRequest(bankRequest, requestID);
    }

    public void sendLoanReply(LoanReply reply, String requestId){
        Message message = sender.createTextMessage(serializer.replyToString(reply));
        try {
            message.setJMSCorrelationID(requestId);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            IdList.put(message.getJMSMessageID(), requestId);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        sender.send(message);
        System.out.println(" sending Loanreply: " + reply.toString());
    }

    public void setRequestList(List<RequestReply> value) {

    }
}
