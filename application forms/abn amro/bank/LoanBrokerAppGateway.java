package bank;

import loanclient.MessageReceiverGateway;
import loanclient.MessageSenderGateway;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("BankBrokerReplyQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("BankBrokerRequestQ");
    private BankSerializer serializer = new BankSerializer();

    public LoanBrokerAppGateway(){
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
                    OnLoanRequestArrived(serializer.requestFromString(textMessage));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void SendReply(BankInterestReply reply){
        sender.send(
                sender.createTextMessage(
                        serializer.replyToString(reply)
        )
        );
        System.out.println("Sending BankinterestReply: " + reply.toString());
    }

    public BankInterestRequest OnLoanRequestArrived(BankInterestRequest request){
        System.out.println("BankInterest Request: " + request.toString());
        return request;
    }
}
