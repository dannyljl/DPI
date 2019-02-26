package loanclient.loanclient;

import com.owlike.genson.Genson;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

public class LoanSerializer {
    private Genson genson = new Genson();

    public String requestToString(LoanRequest request)
    {
        if (request != null){
            return genson.serialize(request);
        }
        return null;
    }

    public LoanRequest requestFromtring(String str)
    {
        return genson.deserialize(str,LoanRequest.class);

    }
    public String replyToString(LoanReply reply){
        return genson.serialize(reply);
    }

    public LoanReply replyFromString(String str)
    {
        return genson.deserialize(str,LoanReply.class);

    }

    public String requestReplyToString(RequestReply requestReply){
        return genson.serialize(requestReply);
    }

    public RequestReply requestReplyFromString(String str){
        return genson.deserialize(str,RequestReply.class);
    }

}
