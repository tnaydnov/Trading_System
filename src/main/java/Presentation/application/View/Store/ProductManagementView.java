package Presentation.application.View.Store;

import Domain.Store.Inventory.ProductDTO;
import Presentation.application.Presenter.Store.ProductManagementPresenter;
import Presentation.application.View.LoginView;
import Presentation.application.View.MainLayoutView;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Route(value = "products-management/:storeId", layout = MainLayoutView.class)
@PageTitle("Product Management")
@StyleSheet("context://styles.css")
public class ProductManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final ProductManagementPresenter presenter;
    private Grid<ProductDTO> productGrid;
    private Button addProductButton;
    private Button backButton;
    private Integer storeId;

    public ProductManagementView(ProductManagementPresenter presenter) {
        this.presenter = presenter;
        this.presenter.attachView(this);
        addClassName("page-view");

        // Adjusting layout to occupy full page
        setSizeFull();
        setMargin(false);
        setPadding(false);
        setSpacing(false);

        UI.getCurrent().getPage().executeJs(
                "document.body.addEventListener('click', function() {" +
                        "    $0.$server.handleUserAction();" +
                        "});",
                getElement()
        );
    }

    @ClientCallable
    public void handleUserAction() {
        if (!presenter.isLoggedIn() || !isLoggedIn()) {
            Notification.show("Token has timed out! Navigating you to login page...");
            UI.getCurrent().navigate(LoginView.class);
        }
    }

    public void setProducts(List<ProductDTO> products) {
        productGrid.setItems(products);
    }

    public void showError(String errorMessage) {
        Notification.show(errorMessage, 3000, Notification.Position.MIDDLE);
    }

    private void showEditProductDialog(ProductDTO product) {
        Dialog dialog = new Dialog();
        dialog.getElement().executeJs("this.$.overlay.$.overlay.style.backgroundColor = '#E6DCD3';");
        dialog.setWidth("600px"); // Adjusting dialog width for better appearance

        FormLayout form = new FormLayout();

        // Local variables to hold the updated values
        final String[] updatedName = {product.getProductName()};
        final String[] updatedDescription = {product.getDescription()};
        final double[] updatedPrice = {product.getPrice()};
        final int[] updatedQuantity = {product.getQuantity()};
        Wrapper<Set<String>> updatedCategories = new Wrapper<>(new HashSet<>(product.getCategories()));

        TextField nameField = new TextField("Name");
        nameField.setValue(product.getProductName());
        nameField.addValueChangeListener(event -> updatedName[0] = event.getValue());

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(product.getDescription());
        descriptionField.addValueChangeListener(event -> updatedDescription[0] = event.getValue());

        TextField priceField = new TextField("Price");
        priceField.setValue(String.valueOf(product.getPrice()));
        priceField.addValueChangeListener(event -> updatedPrice[0] = Double.parseDouble(event.getValue()));

        TextField quantityField = new TextField("Quantity");
        quantityField.setValue(String.valueOf(product.getQuantity()));
        quantityField.addValueChangeListener(event -> updatedQuantity[0] = Integer.parseInt(event.getValue()));

        CheckboxGroup<String> categoriesCheckboxGroup = new CheckboxGroup<>();
        categoriesCheckboxGroup.setLabel("Categories");
        categoriesCheckboxGroup.setItems(updatedCategories.getValue());
        categoriesCheckboxGroup.setValue(updatedCategories.getValue());

        TextField newCategoryField = new TextField();
        newCategoryField.setPlaceholder("Enter new category");

        Button addCategoryButton = new Button("Add Category", e -> {
            String newCategory = newCategoryField.getValue();
            if (!newCategory.isEmpty()) {
                updatedCategories.getValue().add(newCategory);
                categoriesCheckboxGroup.setItems(updatedCategories.getValue());
                categoriesCheckboxGroup.setValue(updatedCategories.getValue());
                newCategoryField.clear();
            }
        });
        addCategoryButton.addClassName("button");

        categoriesCheckboxGroup.addValueChangeListener(event -> {
            updatedCategories.setValue(event.getValue());
        });

        form.add(nameField, descriptionField, priceField, quantityField, categoriesCheckboxGroup);
        form.add(new HorizontalLayout(newCategoryField, addCategoryButton));

        Button closeButton = new Button("Cancel", event -> dialog.close());
        closeButton.addClassName("no_button");
        Button saveButton = new Button("Save", event -> {
            presenter.updateProductName(product.getProductID(), updatedName[0]);
            presenter.updateProductDescription(product.getProductID(), updatedDescription[0]);
            presenter.updateProductPrice(product.getProductID(), updatedPrice[0]);
            presenter.updateProductQuantity(product.getProductID(), updatedQuantity[0]);

            Set<String> oldCategories = product.getCategories() != null ? new HashSet<>(product.getCategories()) : new HashSet<>();
            oldCategories.stream()
                    .filter(category -> !updatedCategories.getValue().contains(category))
                    .forEach(category -> presenter.removeProductCategory(product.getProductID(), category));
            updatedCategories.getValue().stream()
                    .filter(category -> !oldCategories.contains(category))
                    .forEach(category -> presenter.addProductCategory(product.getProductID(), category));

            dialog.close();
        });
        saveButton.addClassName("yes_button");

        HorizontalLayout buttonsLayout = new HorizontalLayout(closeButton, saveButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setSpacing(true);
        form.add(buttonsLayout, 2);

        dialog.add(form);
        dialog.open();
    }


    private void showAddProductDialog() {
        Dialog dialog = new Dialog();
        dialog.getElement().executeJs("this.$.overlay.$.overlay.style.backgroundColor = '#E6DCD3';");
        dialog.setWidth("600px"); // Adjusting dialog width for better appearance

        FormLayout form = new FormLayout();
        TextField nameField = new TextField("Name");
        TextArea descriptionField = new TextArea("Description");
        TextField priceField = new TextField("Price");
        TextField quantityField = new TextField("Quantity");

        CheckboxGroup<String> categoriesCheckboxGroup = new CheckboxGroup<>();
        categoriesCheckboxGroup.setLabel("Categories");

        TextField newCategoryField = new TextField();
        newCategoryField.setPlaceholder("Enter new category");

        Button addCategoryButton = new Button("Add Category", e -> {
            String newCategory = newCategoryField.getValue();
            if (!newCategory.isEmpty()) {
                Set<String> currentCategories = new HashSet<>(categoriesCheckboxGroup.getValue());
                currentCategories.add(newCategory);
                categoriesCheckboxGroup.setItems(currentCategories);
                categoriesCheckboxGroup.setValue(currentCategories);
                newCategoryField.clear();
            }
        });

        addCategoryButton.addClassName("button");

        form.add(nameField, descriptionField, priceField, quantityField, categoriesCheckboxGroup);
        form.add(new HorizontalLayout(newCategoryField, addCategoryButton));

        Button closeButton = new Button("Cancel", event -> dialog.close());
        closeButton.addClassName("no_button");
        Button saveButton = new Button("Save", event -> {
            Set<String> categories = new HashSet<>(categoriesCheckboxGroup.getValue());
            presenter.addProduct(nameField.getValue(), descriptionField.getValue(), Double.parseDouble(priceField.getValue()), Integer.parseInt(quantityField.getValue()), categories);
            dialog.close();
        });
        saveButton.addClassName("yes_button");

        HorizontalLayout buttonsLayout = new HorizontalLayout(closeButton, saveButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setSpacing(true);
        form.add(buttonsLayout, 2);

        dialog.add(form);
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!presenter.isLoggedIn() || !isLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
        String id = event.getRouteParameters().get("storeId").orElse("");
        if (id.isEmpty()) {
            event.rerouteTo("");
        }
        storeId = Integer.parseInt(id);
        // Creating the header layout with back button and add product button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setPadding(true);

        backButton = new Button("Back to Store Management", event1 -> {
            RouteParameters routeParameters = new RouteParameters("storeId", storeId.toString());
            UI.getCurrent().navigate(StoreManagementView.class, routeParameters);
        });
        backButton.addClassName("button");
        headerLayout.add(backButton);
        if (presenter.hasPermission(storeId, "MANAGE_PRODUCTS") && presenter.isActiveStore(storeId) && !presenter.isSuspended()) {
            addProductButton = new Button("+ Add Product", event1 -> showAddProductDialog());
            addProductButton.addClassName("button");
            headerLayout.add(addProductButton);
        }

        // Setting up the product grid
        productGrid = new Grid<>(ProductDTO.class);
        productGrid.addClassName("custom-grid");
        productGrid.setSizeFull();
        productGrid.setColumns("productName", "description", "price", "quantity");
        productGrid.addColumn(product -> {
            ArrayList<String> categories = product.getCategories();
            return categories != null ? String.join(", ", categories) : "";
        }).setHeader("Categories");
        if (presenter.hasPermission(storeId, "MANAGE_PRODUCTS") && presenter.isActiveStore(storeId) && !presenter.isSuspended()) {
            productGrid.addComponentColumn(product -> {

                Button removeButton = new Button("Remove", e -> navigateToProductRemoving(product.getProductID()));
                removeButton.addClassName("button");
                return removeButton;
            }).setHeader("Actions");
            productGrid.addItemClickListener(event1 -> showEditProductDialog(event1.getItem()));
        }

        add(headerLayout, productGrid);
        expand(productGrid); // Allow the grid to expand and take available space
        presenter.loadProducts(storeId);
    }

    private void navigateToProductRemoving(Integer productID) {
        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setWidth("400px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        dialogLayout.add("Are you sure you want to remove this product?");

        Button confirmButton = new Button("Yes", e -> {
            presenter.removeProduct(productID);
            confirmationDialog.close();
            showSuccess("Product removed successfully");
        });
        confirmButton.addClassName("yes_button");

        Button cancelButton = new Button("No", e -> confirmationDialog.close());
        cancelButton.addClassName("no_button");

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmButton);
        buttonLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END); // Align buttons to the right

        dialogLayout.add(buttonLayout);
        confirmationDialog.add(dialogLayout);
        confirmationDialog.open();
    }

    public void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
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

    public Integer getStoreId() {
        return storeId;
    }

    // Wrapper class to hold the mutable value
    private static class Wrapper<T> {
        private T value;

        public Wrapper(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
