package Domain.Users.StateOfSubscriber;

import Domain.Store.Store;
import Utilities.Response;


public class StoreCreator extends SubscriberState {

    public StoreCreator(Store store, String subscriberID) {
        super(subscriberID, store);
    }

    @Override
    public Response<String> changeState(Store store, String SubscriberID, SubscriberState newState) {
        if (newState instanceof StoreManager || newState instanceof NormalSubscriber || newState instanceof StoreOwner) {
            store.setState(SubscriberID, newState);
            return Response.success("State changed successfully to " + newState.getClass().getSimpleName(), null);
        } else {
            return Response.error("Invalid state transition", null);
        }
    }

    @Override
    public String toString() {
        return "CREATOR";
    }
}
