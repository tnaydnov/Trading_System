package Presentation.application;

import Domain.Externals.InitFile.Configuration;
import Domain.Store.Store;
import Domain.Users.Subscriber.Subscriber;
import Service.OrderService;
import Service.ServiceInitializer;
import Service.StoreService;
import Service.UserService;
import Utilities.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@Push
@SpringBootApplication(scanBasePackages = {"Service", "Presentation"})
@Theme(value = "my-app")
@EnableJpaRepositories(basePackages = "Domain.Repo")
@EntityScan(basePackages = "Domain.Users")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        Configuration configuration = null;
        try {
            // Read the configuration file
            ObjectMapper mapper = new ObjectMapper();
            JsonNode configNode = mapper.readTree(new File("src/main/java/Domain/Externals/InitFile/Configuration.json"));

            // Initialize the Configuration object
            Configuration.init(configNode);
            configuration=new Configuration(configNode);
            serviceInitializer=ServiceInitializer.getInstance(configuration);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Exit if there is an error reading the configuration file
        }


        // Pass the configuration to SpringApplication.run
        SpringApplication.run(Application.class, args);
    }

    static ServiceInitializer serviceInitializer;
    static StoreService storeService;
    static UserService userService;
    static OrderService orderService;
    static Subscriber subscriber, owner, manager;
    static Store store, store2;
    public static void init(){
        ServiceInitializer.reset();
        serviceInitializer = ServiceInitializer.getInstance();
        userService = serviceInitializer.getUserService();
        //admin
        userService.register("mia","Password123!");
        Response<String> resLogin = userService.loginAsSubscriber("mia","Password123!");
        String token = resLogin.getData();
        subscriber = userService.getUserFacade().getUserRepository().getSubscriber("mia");
        storeService = serviceInitializer.getStoreService();
        orderService = serviceInitializer.getOrderService();
        userService.register("manager","Password123!");
        userService.loginAsSubscriber("manager","Password123!");
        userService.register("owner","Password123!");
        userService.loginAsSubscriber("owner","Password123!");
        owner=userService.getUserFacade().getUserRepository().getSubscriber("owner");
        manager = userService.getUserFacade().getUserRepository().getSubscriber("manager");
        storeService.addStore("newStore", "mia",subscriber.getToken());
        store = storeService.getStoreFacade().getStoreRepository().getActiveStore(0);
        storeService.addStore("newStore2", "mia",subscriber.getToken());
        store2 = storeService.getStoreFacade().getStoreRepository().getActiveStore(0);
        storeService.addProductToStore(0,"yair","d",20,30,"mia",subscriber.getToken());
        Response<Integer> res = userService.SendManagerNominationRequest(0, "mia", "manager", List.of("VIEW_PRODUCTS","MANAGE_PRODUCTS", "VIEW_DISCOUNTS_POLICIES"), token);
        userService.managerNominationResponse(res.getData(), "manager", true, manager.getToken());
        Response<Integer> res2 = userService.SendOwnerNominationRequest(0, "mia", "owner", token);
        userService.ownerNominationResponse(res2.getData(),"owner", true, owner.getToken());
        storeService.addProductToStore(0,"Bamba","Bamba",200.0,1, new ArrayList<>(List.of("test")),"mia",subscriber.getToken());
        storeService.addProductToStore(0,"VODKA","VODKA",200.0,1, new ArrayList<>(List.of("ALCOHOL")),"mia",subscriber.getToken());
        storeService.addProductToStore(0,"Bisli","Bisli",100.0,1,"mia",subscriber.getToken());
        storeService.addProductToStore(1,"CHIPS","CHIPS",200.0,1,"mia",subscriber.getToken());
        storeService.addProductToStore(1,"DORITOS","DORITOS",100.0,1,"mia",subscriber.getToken());
        userService.addProductToShoppingCart(0, 1, 1, "mia", subscriber.getToken());
        userService.addProductToShoppingCart(0, 2, 1, "mia", subscriber.getToken());
        userService.addProductToShoppingCart(1, 1, 1,"mia", subscriber.getToken());
        userService.logoutAsSubscriber("mia");
    }
}
