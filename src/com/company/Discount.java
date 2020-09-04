package com.company;

import static com.company.Shop.delay;
import static java.lang.String.format;

/**
 * @author Jaedoo Lee
 */
public class Discount {
    public enum Code {
        NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

        private final int percentage;

        Code(int percentage) {
            this.percentage = percentage;
        }
    }

    public static String applyDiscount(Quote quote) {
        return quote.getShopName() + " price is " +
               Discount.apply(quote.getPrice(), //  기존 가격에 할인 코드를 적용
                              quote.getDiscountCode());
    }

    private static String apply(double price, Code code) {
        delay();
        return format(String.valueOf(price * (100 - code.percentage) / 100));
    }

}
