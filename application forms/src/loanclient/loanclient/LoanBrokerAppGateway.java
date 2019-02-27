package loanclient.loanclient;

import abstractGateWay.abstractGateway;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

public class LoanBrokerAppGateway extends abstractGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("ClientBrokerRequestQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("ClientBrokerReplyQ");
    private LoanSerializer serializer = new LoanSerializer();
    private Map<String,LoanRequest> requestIdList = new HashMap<String, LoanRequest>();

    public void applyForLoan(LoanRequest request){
        Message message = sender.createTextMessage(serializer.requestToString(request));
        sender.send(message);
        try {
            requestIdList.put(message.getJMSMessageID(),request);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("sending request : " + request.toString());
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    String textMessage = ((TextMessage) message).getText();
                    System.out.println("incoming textmessage: " + textMessage + "MessageCorrelationID: " + message.getJMSCorrelationID());
                    onLoanReplyArrived(serializer.replyFromString(textMessage), message.getJMSCorrelationID());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onLoanReplyArrived(LoanReply reply, String messageID){
        LoanRequest request = requestIdList.get(messageID);
        System.out.println("receiving request and reply: " + request.toString() + " Reply: " + reply.toString());
    }
}
