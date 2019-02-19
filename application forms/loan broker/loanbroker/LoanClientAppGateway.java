package loanbroker;

import loanclient.LoanSerializer;
import loanclient.MessageReceiverGateway;
import loanclient.MessageSenderGateway;
import model.loan.LoanReply;
import model.loan.LoanRequest;

public class LoanClientAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private LoanSerializer serializer;

    public void onLoanRequestArrived(LoanRequest request){

    }

    public void sendLoanReply(LoanRequest request, LoanReply reply){

    }
}
