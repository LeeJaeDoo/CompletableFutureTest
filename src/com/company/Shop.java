package com.company;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Jaedoo Lee
 */
public class Shop {

    private String name;
    private String price;

    public Shop(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPrice(String product) {
        Random random = new Random();
        double price = calculatePrice(product);
        Discount.Code code = Discount.Code.values()[
            random.nextInt(Discount.Code.values().length)];
        return String.format("%s:%.2f:%s", name, price, code);
    }

    private double calculatePrice(String product) {
        randomDelay();
        Random random = new Random();
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    public static void delay() {
        try{
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void randomDelay () {
        Random random = new Random();
        int delay = 500 + random.nextInt(2000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Future<Double> getPriceAsync(String product) {
        return CompletableFuture.supplyAsync(() -> calculatePrice(product));
    }
}
