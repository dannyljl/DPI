package bank;

import loanclient.MessageReceiverGateway;
import loanclient.MessageSenderGateway;

public class LoanBrokerAppGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;
    private BankSerializer serialer;
}
