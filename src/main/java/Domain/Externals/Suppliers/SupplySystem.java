package Domain.Externals.Suppliers;

import Domain.Store.Inventory.ProductDTO;

import java.util.List;
import java.util.Map;

public interface SupplySystem {
    int orderSupply(Map<Integer, Integer> items, String deliveryAddress, String name);
    boolean cancelSupply(int transactionId);
    boolean handshake();
}
