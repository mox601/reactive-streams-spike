package fm.mox;


import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

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
        Scheduler from = Schedulers.from(Executors.newFixedThreadPool(3));
        final List<String> res = Observable.create(onSubscribe)
                .flatMap(strings -> Observable.just(strings)
                        .subscribeOn(from)
                        .map(Rasc::longCalculation)
                )
                .buffer(8)
                .map(Rasc::calculateList)
                .toList()
                .blockingGet();

        System.out.println("strings = " + res);
    }

    private static final ObservableOnSubscribe<String> onSubscribe = observableEmitter -> {
//        observableEmitter.onStart();
        IntStream.range(1, 20).forEach(i1 -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log("Generating %s", i1);
            observableEmitter.onNext("input-" + i1);
        });
        observableEmitter.onComplete();
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

    private static void log(String s, Object... params) {
        System.out.println(String.format("[%s] %s", Thread.currentThread().getName(), String.format(s, params)));
    }

}
