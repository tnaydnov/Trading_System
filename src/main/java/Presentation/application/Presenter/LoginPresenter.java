package Presentation.application.Presenter;

import Presentation.application.View.LoginView;
import Service.UserService;
import Utilities.Response;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginPresenter {
    private LoginView view;
    private final UserService userService;

    public LoginPresenter(UserService userService) {
        this.userService = userService;
    }

    public void attachView(LoginView view) {
        this.view = view;
    }

    public void loginAsSubscriber(String username, String password) {
        System.out.println("[DEBUG LOGIN][Presenter] Invoked loginAsSubscriber with username='" + username + "'");
        try {
            Response<String> response = userService.loginAsSubscriber(username, password);
            System.out.println("[DEBUG LOGIN][Presenter] Response success=" + response.isSuccess() + ", message='" + response.getMessage() + "', token=" + response.getData());
            if (response.isSuccess()) {
                String token = response.getData();
                if (token != null) {
                    System.out.println("[DEBUG LOGIN][Presenter] Token not null, updating view");
                    view.getUI().ifPresent(ui -> ui.access(() -> view.loginSuccessful(username, token)));
                } else {
                    System.out.println("[DEBUG LOGIN][Presenter] Token null despite success");
                    view.getUI().ifPresent(ui -> ui.access(() -> view.showError("Invalid username or password")));
                }
            } else {
                System.out.println("[DEBUG LOGIN][Presenter] Response indicates failure");
                view.getUI().ifPresent(ui -> ui.access(() -> view.showError("Invalid username or password")));
            }
        } catch (Exception e) {
            System.out.println("[DEBUG LOGIN][Presenter] Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            view.getUI().ifPresent(ui -> ui.access(() -> view.showError("An error occurred during login")));
        }
    }

    public void loginAsGuest() {
        try {
            Response<List<String>> response = userService.loginAsGuest();
            String username = response.getData().get(0);
            String token = response.getData().get(1);
            if (token != null) {
                view.getUI().ifPresent(ui -> ui.access(() -> view.loginSuccessful(username, token)));
            } else {
                view.getUI().ifPresent(ui -> ui.access(() -> view.showError("An error occurred during login")));
            }
        } catch (Exception e) {
            view.getUI().ifPresent(ui -> ui.access(() -> view.showError("An error occurred during login")));
        }
    }
}
