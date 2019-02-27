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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankAppGateway extends abstractGateway {
    private MessageSenderGateway abnsender = new MessageSenderGateway("abnBrokerRequestQ");
    private MessageSenderGateway ingsender = new MessageSenderGateway("ingBrokerRequestQ");
    private MessageSenderGateway rabosender = new MessageSenderGateway("raboBrokerRequestQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("BankBrokerReplyQ");
    private BankSerializer serializer = new BankSerializer();
    private LoanClientAppGateway loanClientApp;
    private String RequestID = null;
    private Map<Integer,String> IdList = new HashMap<Integer, String>();
    private int aggregationid = 1;

    private List aggregationList = new ArrayList<Message>();


    private PropertyChangeSupport support;

    public BankAppGateway(LoanClientAppGateway loanClientApp){
        this.loanClientApp = loanClientApp;
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
                    System.out.println(message.toString());
                    RequestID = message.getJMSCorrelationID();
                    aggregationList.add(message);
                    BankInterestReply reply = CheckBanks(message.getIntProperty("aggregationID"));
                    if (reply != null){
                        OnBankReplyArrived(reply);
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void OnBankReplyArrived(BankInterestReply reply){

        LoanReply reply2 = new LoanReply();
        reply2.setQuoteID(reply.getQuoteId());
        reply2.setInterest(reply.getInterest());

        System.out.println("Receiving Bankinterestreply: " + reply.toString());
        loanClientApp.sendLoanReply(reply2,RequestID);
    }
    public void sendBankRequest(BankInterestRequest request, String messageID){
        Message message = abnsender.createTextMessage(serializer.requestToString(request));
        try {
            System.out.println("gonna send this to the bank message ID:" + messageID);
            message.setJMSCorrelationID(messageID);
            message.setIntProperty("aggregationID",aggregationid);
            IdList.put(aggregationid,messageID);
            aggregationid++;
        } catch (JMSException e) {
            e.printStackTrace();
        }
        abnsender.send(message);
        ingsender.send(message);
        rabosender.send(message);
        try {
            System.out.println("sending  BankInterestRequest:" + request.toString() + "with correlationID: " + message.getJMSCorrelationID());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    public void setRequestList(List<RequestReply> value) {

    }

    private BankInterestReply CheckBanks(int id){

        List chosenMessages = new ArrayList<Message>();
        List<Message> newAggregationList = new ArrayList<>();
        int amount = 0;
        Message bestMessage = null;
        for (Message message : (List<Message>)aggregationList){
            try {
                if(message.getIntProperty("aggregationID") == id){
                    chosenMessages.add(message);
                }
                else{
                    newAggregationList.add(message);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        if(chosenMessages.size() >= 3){
            aggregationList = newAggregationList;
            return ChooseBestBank(chosenMessages);
        }


        return null;
    }

    private BankInterestReply ChooseBestBank(List<Message> messages){
        if (messages.size() < 3){
            return null;
        }
        List<BankInterestReply> replyList = new ArrayList<BankInterestReply>();
        for (Message message : messages){
            String textMessage = null;
            try {
                textMessage = ((TextMessage) message).getText();
                replyList.add(serializer.replyFromString(textMessage));
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
        BankInterestReply bestReply = null;

        for (BankInterestReply reply : replyList){
            if (bestReply == null) {
                bestReply = reply;
            }
            else if(bestReply.getInterest() > reply.getInterest()){
                bestReply = reply;
            }
        }
        return bestReply;
    }
}
