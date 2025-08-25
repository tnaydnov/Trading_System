package Presentation.application;

import Service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds demo users at startup using the singleton UserService.
 */
@Component
public class DemoDataLoader implements ApplicationRunner {
    private final UserService userService;

    public DemoDataLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userService.getUserFacade().getUserRepository().isUserExist("mia")) {
            userService.register("mia", "Password123!");
            userService.register("manager", "Password123!");
            userService.register("owner", "Password123!");
        }
    }
}
