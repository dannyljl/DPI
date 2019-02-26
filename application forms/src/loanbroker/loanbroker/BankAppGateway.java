package loanbroker.loanbroker;

import abnamro.bank.BankSerializer;
import abstractGateWay.abstractGateway;
import loanclient.loanclient.MessageReceiverGateway;
import loanclient.loanclient.MessageSenderGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
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

public class BankAppGateway extends abstractGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("BankBrokerRequestQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("BankBrokerReplyQ");
    private BankSerializer serializer = new BankSerializer();
    private LoanClientAppGateway loanClientApp;
    private String RequestID = null;
    private Map<String,String> IdList = new HashMap<String, String>();

    private PropertyChangeSupport support;

    public BankAppGateway(LoanClientAppGateway loanClientApp){
        this.loanClientApp = loanClientApp;
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
                    RequestID = message.getJMSCorrelationID();
                    OnBankReplyArrived(serializer.replyFromString(textMessage),serializer.requestFromString(textMessage));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void OnBankReplyArrived(BankInterestReply reply, BankInterestRequest request){
        LoanReply reply2 = new LoanReply();
        reply2.setQuoteID(reply.getQuoteId());
        reply2.setInterest(reply.getInterest());

        LoanRequest request2 = new LoanRequest();
        request2.setAmount(request.getAmount());
        request2.setTime(request.getTime());

        System.out.println("Receiving Bankinterestreply: " + reply.toString() + "Receiving BankinterestRequest: " + request.toString());
        loanClientApp.sendLoanReply(request2,reply2,RequestID);
    }
    public void sendBankRequest(BankInterestRequest request, String messageID){
        Message message = sender.createTextMessage(serializer.requestToString(request));
        try {
            message.setJMSCorrelationID(messageID);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        sender.send(message);
        System.out.println("sending  BankInterestRequest:" + request.toString());
    }


    public void setRequestList(List<RequestReply> value) {

    }
}
