package Domain.Store.Discounts;

import Domain.Store.Conditions.Condition;
import Domain.Store.Inventory.Product;
import Domain.Store.Inventory.ProductDTO;
import Utilities.Response;

import java.util.List;
import java.util.Map;

public class MaxDiscount implements Discount{
    private final Discount discount1;
    private final Discount discount2;
    private final Integer discountID;
    private final DiscountType discountType;

    public MaxDiscount(Discount discount1, Discount discount2, Integer discountID) {
        this.discount1 = discount1;
        this.discount2 = discount2;
        this.discountID = discountID;
        this.discountType = DiscountType.MAX;

    }

    @Override
    public String getType() {
        return null;
    }


    @Override
    public Response<Double> CalculatorDiscount(Map<ProductDTO, Integer> products) {
        Response<Double> response1 = discount1.CalculatorDiscount(products);
        Response<Double> response2 = discount2.CalculatorDiscount(products);
        if(!response1.isSuccess() || !response2.isSuccess()){
            return new Response<>(false, "Failed to calculate discount");
        }
        double discount1 = response1.getData();
        double discount2 = response2.getData();
        return new Response<>(true, "Calculator Discount",Math.max(discount1, discount2));
    }

    @Override
    public Integer getDiscountID() {
        return discountID;
    }
    @Override
    public Integer getStoreID() {
        return discount1.getStoreID();
    }
    @Override
    public DiscountType getDiscountType() {
        return DiscountType.MAX;
    }
    @Override
    public Double getPercent() {
        return null;
    }
    @Override
    public Integer getProductID() {
        return null;
    }
    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public Discount getDiscount1() {
        return discount1;
    }

    @Override
    public Discount getDiscount2() {
        return discount2;
    }

    @Override
    public Condition getCondition() {
        return null;
    }

    @Override
    public String toString() {
        return "Maximum of: (" + discount1.toString() + ") and (" + discount2.toString() + ")";
    }



    }
