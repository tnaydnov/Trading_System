package Configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import Service.UserService;
import Service.StoreService;
import Utilities.Response;

@Configuration
@Profile("dev")
public class DevSeedConfiguration {

    @Bean
    public ApplicationRunner demoData(UserService userService, StoreService storeService) {
    return args -> {
            if (!userService.userExists("alice")) {
                userService.register("alice", "Password123!");
            }
            if (!userService.userExists("bob")) {
                userService.register("bob", "Password123!");
            }
            
            Response<String> login = userService.loginAsSubscriber("alice", "Password123!");
            if (login.isSuccess()) {
                String token = login.getData();
                if (storeService.getAllStores("alice", token).getData().isEmpty()) {
                    storeService.addStore("DemoStore", "alice", token);
                }
                userService.logoutAsSubscriber("alice");
            }
        };
    }
}
