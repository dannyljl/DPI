package loanclient;

import com.owlike.genson.Genson;
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

}
