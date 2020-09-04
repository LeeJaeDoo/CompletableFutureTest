package com.company;

import org.w3c.dom.ls.LSOutput;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {


    private static List<Shop> shops = Arrays.asList(new Shop("BestPrice"),
                                     new Shop("LetsSaveBig"),
                                     new Shop("MyFavoriteShop"),
                                     new Shop("BuyItAll"),
                                     new Shop("ShopEasy"));

    private static final Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100), //  상점 수만큼의 스레드를 갖는 풀을 생성.(스레드 수의 범위는 0 ~ 100)
                                                                   new ThreadFactory() {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable);
            t.setDaemon(true);      //  프로그램 종료를 방해하지 않는 데몬 스레드를 사용
            return t;
        }
    });

    public static void main(String[] args) {
	    long start = System.nanoTime();
        CompletableFuture[] futures = findPricesStream("myPhone25s")
            .map(f -> f.thenAccept(s -> System.out.println(s + " (done in " + ((System.nanoTime() - start) / 1_000_000) + " msecs)")))
            .toArray(size -> new CompletableFuture[size]);
        CompletableFuture.anyOf(futures).join();
//        System.out.println(completableFuturefindPrices("myPhone25s"));
        System.out.println("All shops have now responded in " + ((System.nanoTime() - start) / 1_000_000) + " msecs");
    }

    public static List<String> findPrices(String product) {
        return shops.stream()
                    .map(shop -> shop.getPrice(product))
                    .map(Quote::parse)
                    .map(Discount::applyDiscount)
                    .collect(Collectors.toList());
    }

    public static List<String> parallelfindPrices(String product) {
        return shops.parallelStream()
                    .map(shop -> shop.getPrice(product))
                    .map(Quote::parse)
                    .map(Discount::applyDiscount)
                    .collect(Collectors.toList());
    }

    public static List<String> completableFuturefindPrices(String product) {
        List<CompletableFuture<String>> priceFutures
            = shops.stream()
                   .map(shop -> CompletableFuture.supplyAsync(  //  각 상점에서 할인 전 가격을 비동기적으로 얻는다.
                       () -> shop.getPrice(product), executor))
                   .map(future -> future.thenApply(Quote::parse))   // 상점에서 반환한 문자열을 Quote 객체로 변환
                   .map(future -> future.thenCompose(quote ->   //  결과 Future를 다른 비동기 작업과 조합해서 할인 코드를 적용
                       CompletableFuture.supplyAsync(
                           () -> Discount.applyDiscount(quote), executor)))
                   .collect(Collectors.toList());

        return priceFutures.stream()
                           .map(CompletableFuture::join)    //  스트림의 모든 Future가 종료되길 기다렸다가 각각의 결과를 추출
                           .collect(Collectors.toList());
    }

    public static Stream<CompletableFuture<String>> findPricesStream(String product) {
        return shops.stream()
                    .map(shop -> CompletableFuture.supplyAsync(
                        () -> shop.getPrice(product), executor
                    ))
                    .map(future -> future.thenApply(Quote::parse))
                    .map(future -> future.thenCompose(quote -> CompletableFuture.supplyAsync(
                        () -> Discount.applyDiscount(quote), executor)));
    }
}
