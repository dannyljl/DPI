package bank;

import com.owlike.genson.Genson;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

public class BankSerializer {
    private Genson genson = new Genson();

    public String requestToString(BankInterestRequest request)
    {
        return genson.serialize(request);
    }

    public BankInterestRequest requestFromString(String str)
    {
        return genson.deserialize(str,BankInterestRequest.class);
    }

    public String replyToString(BankInterestReply reply){
        return genson.serialize(reply);
    }

    public BankInterestReply replyFromString(String str){
        return genson.deserialize(str,BankInterestReply.class);
    }

}
