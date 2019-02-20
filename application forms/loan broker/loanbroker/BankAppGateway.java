package loanbroker;

import bank.BankSerializer;
import loanclient.MessageReceiverGateway;
import loanclient.MessageSenderGateway;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class BankAppGateway {
    private MessageSenderGateway sender = new MessageSenderGateway("BankBrokerRequestQ");
    private MessageReceiverGateway receiver = new MessageReceiverGateway("BankBrokerReplyQ");
    private BankSerializer serializer = new BankSerializer();
    private LoanClientAppGateway loanClientApp;

    public BankAppGateway(LoanClientAppGateway loanClientApp){
        this.loanClientApp = loanClientApp;
        receiver.setListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                String textMessage = null;
                try {
                    textMessage = ((TextMessage) message).getText();
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
        loanClientApp.sendLoanReply(request2,reply2);
    }
    public void sendBankRequest(BankInterestRequest request){
        sender.send(
                sender.createTextMessage(
                        serializer.requestToString(request))
        );
        System.out.println("sending  BankInterestRequest:" + request.toString());
    }
}
