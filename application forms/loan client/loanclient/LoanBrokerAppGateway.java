package loanclient;

import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("ClientReqQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("ClientRepQ");
    private LoanSerializer serializer = new LoanSerializer();

    public void applyForLoan(LoanRequest request){
        sender.send(
                sender.createTextMessage(
                        serializer.requestToString(request)));
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    String textMessage = ((TextMessage) message).getText();
                    onLoanReplyArrived(serializer.requestFromtring(textMessage),serializer.replyFromString(textMessage));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onLoanReplyArrived(LoanRequest request, LoanReply reply){
        System.out.println("request: " + request.toString() + " Reply: " + reply.toString());
    }
}
