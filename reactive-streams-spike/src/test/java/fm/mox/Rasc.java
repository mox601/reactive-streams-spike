package fm.mox;


import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
public class Rasc {

    private static int i = 0;

    public static void main(String[] args) {
        final List<String> res = Observable.create(onSubscribe)
                .flatMap(strings -> Observable.just(strings)
                        .subscribeOn(Schedulers.from(Executors.newFixedThreadPool(3)))
                        .map(Rasc::longCalculation)
                )
                .buffer(8)
                .map(Rasc::calculateList)
                .toList()
                .toBlocking()
                .single();

        System.out.println("strings = " + res);
    }

    static final Observable.OnSubscribe<String> onSubscribe = subscriber -> {
        subscriber.onStart();
        IntStream.range(1, 20).forEach(i1 -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log("Generating %s", i1);
            subscriber.onNext("input-" + i1);
        });
        subscriber.onCompleted();
    };

    private static String calculateList(List<String> s) {
        log("batch %s", s);
        return s.stream().collect(Collectors.joining("-"));
    }

    private static String longCalculation(String s) {
        log("%s - long", s);
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log("%s - end long", s);
        return s.toUpperCase();
    }

    static void log(String s, Object... params) {
        System.out.println(String.format("[%s] %s", Thread.currentThread().getName(), String.format(s, params)));
    }

}
