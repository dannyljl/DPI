package loanbroker;

import bank.BankSerializer;
import loanclient.MessageReceiverGateway;
import loanclient.MessageSenderGateway;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

public class BankAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    BankSerializer serializer;

    public void OnBankReplyArrived(BankInterestReply reply, BankInterestRequest request){

    }
    public void sendBankRequest(BankInterestRequest request){

    }
}
