package loanbroker;

import loanclient.LoanSerializer;
import loanclient.MessageReceiverGateway;
import loanclient.MessageSenderGateway;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class LoanClientAppGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("ClientBrokerReplyQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("ClientBrokerRequestQ");
    private LoanSerializer serializer = new LoanSerializer();
    private BankAppGateway appGateway = new BankAppGateway(this);

    public LoanClientAppGateway(){
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
                    onLoanRequestArrived(serializer.requestFromtring(textMessage));

                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onLoanRequestArrived(LoanRequest request){
        BankInterestRequest bankRequest = new BankInterestRequest();
        bankRequest.setTime(request.getTime());
        bankRequest.setAmount(request.getAmount());
        System.out.println("Arrived LoanRequest: " + request.toString());
        appGateway.sendBankRequest(bankRequest);
    }

    public void sendLoanReply(LoanRequest request, LoanReply reply){
        sender.send(
                sender.createTextMessage(
                        serializer.requestToString(request) + serializer.replyToString(reply))
        );
        System.out.println("sending  Loanrequest:" + request.toString() + " sending Loanreply: " + reply.toString());
    }
}
