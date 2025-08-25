package Service;

/**
 * Test-scope compatibility shim replacing the old singleton that tests relied on.
 * Creates fresh service instances on reset() and wires their mutual dependencies
 * exactly like the legacy implementation so existing tests keep working after
 * migrating the production code to Spring DI.
 */
public class ServiceInitializer {
    private static ServiceInitializer instance;

    private final UserService userService;
    private final StoreService storeService;
    private final AdminService adminService;
    private final OrderService orderService;

    private ServiceInitializer() {
        // Instantiate core services (using their no-arg constructors)
        userService = new UserService();
        storeService = new StoreService();
        adminService = new AdminService();
        orderService = new OrderService();

        // Wire circular / cross dependencies (mirrors runtime wiring)
        userService.setStoreService(storeService);
        userService.setAdminService(adminService);

        storeService.setUserService(userService);
        storeService.setAdminService(adminService);

        adminService.setUserService(userService);
        adminService.setStoreService(storeService);
        adminService.setOrderService(orderService);

        orderService.setUserService(userService);
        // (OrderService currently does not expose setters for store/admin; add if needed later.)
    }

    public static synchronized ServiceInitializer getInstance() {
        if (instance == null) {
            instance = new ServiceInitializer();
        }
        return instance;
    }

    /** Legacy signature retained for tests that passed a Configuration object (ignored now). */
    public static synchronized ServiceInitializer getInstance(Object legacyConfiguration) {
        // Ignore the passed configuration (production code handles this differently now)
        return getInstance();
    }

    /** Reset the singleton so each test can start from a clean in-memory state. */
    public static synchronized void reset() {
        instance = null;
    }

    public UserService getUserService() { return userService; }
    public StoreService getStoreService() { return storeService; }
    public AdminService getAdminService() { return adminService; }
    public OrderService getOrderService() { return orderService; }
}
