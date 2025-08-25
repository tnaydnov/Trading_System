package Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import Service.*;

@Configuration
public class ServiceWiringConfig {

    // After Spring instantiates the services (they have default constructors),
    // wire their circular dependencies via a post-construction runner.
    @Bean
    public ApplicationRunner wireServices(UserService userService,
                                          StoreService storeService,
                                          AdminService adminService,
                                          OrderService orderService) {
        return args -> {
            // bidirectional / cross references
            userService.setStoreService(storeService);
            userService.setAdminService(adminService);
            storeService.setUserService(userService);
            storeService.setAdminService(adminService);
            adminService.setUserService(userService);
            adminService.setStoreService(storeService);
            adminService.setOrderService(orderService);
            orderService.setUserService(userService);
        };
    }
}
