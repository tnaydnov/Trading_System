package Presentation.application.Presenter.Store;

import Presentation.application.CookiesHandler;
import Presentation.application.View.Store.StorePurchaseHistory;
import Service.OrderService;
import Service.ServiceInitializer;
import Domain.OrderDTO;
import Service.UserService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StorePurchaseHistoryPresenter {
    private StorePurchaseHistory storePurchaseHistory;
    private final OrderService orderService;
    private final UserService userService;
    private final HttpServletRequest request;

    public StorePurchaseHistoryPresenter(HttpServletRequest request) {
        orderService = ServiceInitializer.getInstance().getOrderService();
        userService = ServiceInitializer.getInstance().getUserService();
        this.request = request;
    }

    public void attachView(StorePurchaseHistory view) {
        this.storePurchaseHistory = view;
    }

    public void getStoreHistory(Integer storeID, Grid<OrderDTO> ordersGrid) {
        if (storeID == null) {
            Notification.show("Please enter a Store ID");
            return;
        }

        List<OrderDTO> storeOrders = orderService.getOrdersHistory(storeID).getData();
        if (storeOrders == null || storeOrders.isEmpty()) {
            Notification.show("No orders found for Store ID: " + storeID);
        } else {
            ordersGrid.setItems(storeOrders);
        }
    }

    public void fetchStoreHistory(Integer storeID, Grid<OrderDTO> ordersGrid) {
        getStoreHistory(storeID, ordersGrid);
    }

    public boolean isLoggedIn() {
        String username = CookiesHandler.getUsernameFromCookies(request);
        String token = CookiesHandler.getTokenFromCookies(request);
        return userService.isValidToken(token, username);
    }
}
