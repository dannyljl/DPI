package loanclient;

import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("ClientBrokerRequestQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("ClientBrokerReplyQ");
    private LoanSerializer serializer = new LoanSerializer();

    public void applyForLoan(LoanRequest request){
        sender.send(
                sender.createTextMessage(
                        serializer.requestToString(request)));
        System.out.println("sending request : " + request.toString());
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    String textMessage = ((TextMessage) message).getText();
                    System.out.println("incoming textmessage: " + textMessage);
                    onLoanReplyArrived(serializer.requestFromtring(textMessage),serializer.replyFromString(textMessage));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply){
        System.out.println("receiving request and reply: " + request.toString() + " Reply: " + reply.toString());
    }
}
