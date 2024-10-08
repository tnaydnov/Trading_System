package Presentation.application.View;

import Domain.Store.Inventory.ProductDTO;
import Presentation.application.CookiesHandler;
import Presentation.application.Presenter.PaymentPresenter;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Route("payment")
@PageTitle("Payment")
@StyleSheet("context://styles.css")
public class PaymentView extends VerticalLayout implements HasUrlParameter<String>, BeforeEnterObserver {
    private Double totalPrice = 0.0;
    private final PaymentPresenter paymentPresenter;
    private TextField cardNumberField;
    private TextField expirationField;
    private PasswordField securityCodeField;
    private TextField nameField;
    private TextField streetAddressField;
    private TextField cityField;
    private TextField stateField;
    private TextField zipCodeField;
    private TextField IdField;
    private Div creditCard;
    private TextField totalPriceField;
    private Span timerLabel;
    private ScheduledExecutorService executor;
    private int timeLeft = 10 * 60; // 10 minutes in seconds
    private UI ui;
    private List<ProductDTO> products;

    public PaymentView(PaymentPresenter paymentPresenter) {
        setClassName("page-view");
        setSizeFull();  // Ensure the VerticalLayout takes the full height
        getStyle().set("display", "flex");
        getStyle().set("justify-content", "center");
        getStyle().set("align-items", "center");

        this.paymentPresenter = paymentPresenter;
        paymentPresenter.attachView(this);

        // Payment Information Title
        H1 title = new H1("Payment Information");
        title.addClassName("title");

        // Credit Card Component
        creditCard = createCreditCardComponent();

        // Form Container
        Div formContainer = createFormContainer();

        // Cancel Payment Button
        Button cancelButton = new Button("Cancel Payment");
        cancelButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            cancelPayment();
        });
        cancelButton.getStyle()
                .set("background-color", "#FF0000") // Red color for cancel button
                .set("color", "#ffffff")
                .set("border", "none")
                .set("padding", "10px 20px")
                .set("border-radius", "5px");

        // Adding components to the layout
        add(title, creditCard, formContainer, cancelButton);

        // Timer Title
        H1 timerTitle = new H1("Time left to pay");
        timerTitle.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("right", "10px")
                .set("font-size", "20px")  // Set the font size
                .set("color", "red");  // Set the color to red

        // Timer Label
        timerLabel = new Span();
        timerLabel.getStyle()
                .set("position", "absolute")
                .set("top", "40px")  // Adjust the top position to make room for the title
                .set("right", "20px")
                .set("font-size", "30px")  // Increase the font size
                .set("color", "red")  // Change the color to red
                .set("border", "2px solid red")  // Add a border
                .set("padding", "10px")  // Add some padding
                .set("border-radius", "5px")  // Add rounded corners
                .set("font-family", "'Courier New', Courier, monospace");  // Change the font
        timerLabel.setText(formatTime(timeLeft)); // Update the timer label immediately

        // Add the title and timer to the layout
        add(timerTitle, timerLabel);

        UI.getCurrent().getPage().executeJs(
                "document.body.addEventListener('click', function() {" +
                        "    $0.$server.handleUserAction();" +
                        "});",
                getElement()
        );
    }

    @ClientCallable
    public void handleUserAction() {
        if (!paymentPresenter.isLoggedIn() || !isLoggedIn()) {
            Notification.show("Token has timed out! Navigating you to login page...");
            UI.getCurrent().navigate(LoginView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ui = UI.getCurrent();  // Store the UI instance
        startTimer();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void cancelPayment() {
        // Logic to handle payment cancellation
        String token = CookiesHandler.getTokenFromCookies(getRequest());
        String user = CookiesHandler.getUsernameFromCookies(getRequest());
        paymentPresenter.pay(user, 0.0, products, null, "", "", "", "", token, ""); // Added empty string for payment ID
    }

    public boolean hasTimerEnded() {
        return timeLeft <= 0;
    }

    private void startTimer() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                timeLeft--;
                ui.access(() -> {  // Use the stored UI instance
                    timerLabel.setText(formatTime(timeLeft));
                    ui.push();  // Use the stored UI instance
                });
                if (timeLeft <= 0) {
                    executor.shutdown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            this.totalPrice = Double.parseDouble(parameter);
            totalPriceField.setValue(String.format("%.2f", totalPrice));
        } catch (NumberFormatException e) {
            this.totalPrice = 0.0;
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!paymentPresenter.isLoggedIn() || !isLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
        // Get the route parameter
        Optional<String> parameter = event.getRouteParameters().get("totalPrice");
        if (parameter.isPresent()) {
            setParameter(event, parameter.get());
        }
        products = paymentPresenter.getProducts();
    }

    private boolean isLoggedIn() {
        // Retrieve the current HTTP request
        HttpServletRequest request = (HttpServletRequest) VaadinService.getCurrentRequest();

        if (request != null) {
            // Retrieve cookies from the request
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        // Assuming a valid token indicates a logged-in user
                        return isValidToken(cookie.getValue());
                    }
                }
            }
        }

        // If no valid token is found, the user is not logged in
        return false;
    }

    private boolean isValidToken(String token) {
        return token != null && !token.isEmpty();
    }

    private Div createCreditCardComponent() {
        Div creditCard = new Div();
        creditCard.addClassName("credit-card");
        creditCard.getStyle()
                .set("background-color", "#E6DCD3")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                .set("margin-bottom", "20px")
                .set("text-align", "center")
                .set("font-family", "'Roboto', sans-serif");

        // Example SVG content with IDs for placeholders
        String svgContent = "<svg width=\"300\" height=\"200\" viewBox=\"0 0 300 200\" xmlns=\"http://www.w3.org/2000/svg\">" +
                "<rect width=\"300\" height=\"200\" rx=\"15\" ry=\"15\" fill=\"#BDA18C\" />" +
                "<text x=\"20\" y=\"40\" font-size=\"24\" fill=\"#ffffff\">VISA</text>" +
                "<text x=\"20\" y=\"100\" font-size=\"18\" fill=\"#ffffff\" id=\"card-number\">XXXX XXXX XXXX XXXX</text>" +
                "<text x=\"20\" y=\"150\" font-size=\"18\" fill=\"#ffffff\" id=\"card-name\">Cardholder Name</text>" +
                "<text x=\"220\" y=\"150\" font-size=\"18\" fill=\"#ffffff\" id=\"card-expiry\">MM/YY</text>" +
                "</svg>";

        // Set the SVG content
        creditCard.getElement().setProperty("innerHTML", svgContent);

        return creditCard;
    }

    private Div createFormContainer() {
        Div formContainer = new Div();
//        formContainer.addClassName("form-container");
        formContainer.getStyle()
                .set("background-color", "#E6DCD3")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)");

        // Name Field
        nameField = createField("Name", "Enter your name");
        nameField.addValueChangeListener(e -> updateCardHolderName(e.getValue()));
        formContainer.add(nameField);

        // Card Number Field
        cardNumberField = createField("Card Number", "Enter your card number");
        cardNumberField.setMaxLength(19); // 16 digits + 3 spaces for hyphens
        cardNumberField.addValueChangeListener(e -> updateCardNumber(e.getValue()));
        cardNumberField.addKeyPressListener(keyPressEvent -> {
            String value = cardNumberField.getValue().replaceAll("\\s+", "");
            if (value.length() > 0 && value.length() % 4 == 0 && value.length() < 16) {
                cardNumberField.setValue(value + " ");
            }
        });
        formContainer.add(cardNumberField);

        // Expiration Date and Security Code Fields
        HorizontalLayout expirationAndSecurityLayout = new HorizontalLayout();
        expirationField = createField("Expiration (MM/YY)", "MM/YY");
        expirationField.setMaxLength(5);
        expirationField.addValueChangeListener(e -> updateCardExpiry(e.getValue()));
        expirationField.addKeyPressListener(keyPressEvent -> {
            String value = expirationField.getValue().replaceAll("\\s+", "");
            if (value.length() == 2) {
                expirationField.setValue(value + "/");
            }
        });
        securityCodeField = createPasswordField("Security Code", "Enter security code");
        securityCodeField.setMaxLength(3);
        expirationAndSecurityLayout.add(expirationField, securityCodeField);
        formContainer.add(expirationAndSecurityLayout);

        // Street Address Field
        streetAddressField = createField("Street Address", "Enter your street address");
        formContainer.add(streetAddressField);

        // City Field
        cityField = createField("City", "Enter your city");
        formContainer.add(cityField);

        // State Field
        stateField = createField("State", "Enter your state");
        formContainer.add(stateField);

        // Zip Code Field
        zipCodeField = createField("Zip Code", "Enter your zip code");
        formContainer.add(zipCodeField);

        // Payment ID Field
        IdField = createField("ID", "Enter your ID"); // New payment ID field
        formContainer.add(IdField);

        // Total Price Field
        totalPriceField = new TextField("Total Price");
        totalPriceField.setReadOnly(true); // Make it read-only
        totalPriceField.setValue(String.format("%.2f", totalPrice)); // Set initial value

        // Submit Button
        Button submitButton = new Button("Submit");
        submitButton.addClassName("button");
        submitButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            // Check if all fields are filled
            if (nameField.isEmpty() || cardNumberField.isEmpty() || expirationField.isEmpty() || securityCodeField.isEmpty() || streetAddressField.isEmpty() || cityField.isEmpty() || stateField.isEmpty() || zipCodeField.isEmpty() || IdField.isEmpty()) {
                showNotification("Please fill in all fields before submitting.");
            } else {
                String token = CookiesHandler.getTokenFromCookies(getRequest());
                String user = CookiesHandler.getUsernameFromCookies(getRequest());
                String cardNumber = cardNumberField.getValue();
                String expirationDate = expirationField.getValue();
                String cvv = securityCodeField.getValue();
                String fullName = nameField.getValue();
                String streetAddress = streetAddressField.getValue();
                String city = cityField.getValue();
                String state = stateField.getValue();
                String zipCode = zipCodeField.getValue();
                String address = streetAddress + ", " + city + ", " + state + ", " + zipCode;
                String paymentId = IdField.getValue(); // Get payment ID value
                paymentPresenter.pay(user, totalPrice, products, cardNumber, expirationDate, cvv, fullName, address, token, paymentId);
            }
        });
        submitButton.getStyle()
                .set("border", "none")
                .set("padding", "10px 20px")
                .set("border-radius", "5px")
                .set("margin-top", "30px") // Add top margin
                .set("margin-left", "50px"); // Add left margin

        // Submit Button Layout
        HorizontalLayout submitButtonLayout = new HorizontalLayout();
        submitButtonLayout.add(submitButton);
        submitButtonLayout.setJustifyContentMode(JustifyContentMode.START); // Align to the left

        // Total Price Layout
        HorizontalLayout totalPriceLayout = new HorizontalLayout();
        totalPriceLayout.add(totalPriceField);
        totalPriceLayout.setJustifyContentMode(JustifyContentMode.END); // Align to the right

        // Total Price and Submit Button Layout
        FlexLayout totalPriceAndSubmitLayout = new FlexLayout();
        totalPriceAndSubmitLayout.add(submitButtonLayout, totalPriceLayout);
        totalPriceAndSubmitLayout.setJustifyContentMode(JustifyContentMode.BETWEEN); // Space between
        formContainer.add(totalPriceAndSubmitLayout);

        return formContainer;
    }

    private void updateCardHolderName(String name) {
        creditCard.getElement().executeJs("this.querySelector('#card-name').textContent = $0", name);
    }

    private void updateCardNumber(String number) {
        // Format the number with spaces after every 4 digits
        String formattedNumber = number.replaceAll("\\s+", "").replaceAll(".{4}(?!$)", "$0 ");
        creditCard.getElement().executeJs("this.querySelector('#card-number').textContent = $0", formattedNumber);
    }

    private void updateCardExpiry(String expiry) {
        creditCard.getElement().executeJs("this.querySelector('#card-expiry').textContent = $0", expiry);
    }

    public HttpServletRequest getRequest() {
        return ((VaadinServletRequest) VaadinRequest.getCurrent()).getHttpServletRequest();
    }

    private TextField createField(String label, String placeholder) {
        TextField textField = new TextField(label);
        textField.setPlaceholder(placeholder);
        textField.setRequired(true);
        textField.getStyle()
                .set("width", "100%")
                .set("margin-bottom", "10px");
        return textField;
    }

    private PasswordField createPasswordField(String label, String placeholder) {
        PasswordField passwordField = new PasswordField(label);
        passwordField.setPlaceholder(placeholder);
        passwordField.setRequired(true);
        passwordField.getStyle()
                .set("width", "100%")
                .set("margin-bottom", "10px");
        return passwordField;
    }

    public void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }


    public void navigateToShoppingCart() {
        getUI().ifPresent(ui -> ui.navigate("shopping-cart"));
    }

    public void navigateToHome() {
        getUI().ifPresent(ui -> ui.navigate(""));
    }
}
