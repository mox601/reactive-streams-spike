package fm.mox;

import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RetryTest {

    @Test
    public void retryThree() throws Exception {

        Flux<String> flux =
                Flux.<String>error(new IllegalArgumentException())
                        .retryWhen(companion -> companion
                                .zipWith(Flux.range(1, 4),
                                        (error, index) -> {
                                            if (index < 4) return index;
                                            else throw Exceptions.propagate(error);
                                        })
                        );

        StepVerifier.create(flux)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void exponentialBackoff() throws Exception {

        Flux<String> flux =
                Flux.<String>error(new IllegalArgumentException())
                        .retryWhen(companion -> companion
                                .doOnNext(s -> log.info(s + " at " + LocalTime.now()))
                                .zipWith(Flux.range(1, 4), (error, index) -> {
                                    if (index < 4) return index;
                                    else throw Exceptions.propagate(error);
                                })
                                .flatMap(index -> Mono.delayMillis(index * 100))
                                .doOnNext(s -> log.info("retried at " + LocalTime.now()))
                        );

        StepVerifier.create(flux)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void throttling() throws Exception {

        Flux<Long> everySecond = Flux.fromIterable(Arrays.asList(1L, 2L, 3L))
                .map(aLong -> {
                    try {
                        Thread.sleep(1_000L);
                    } catch (InterruptedException e) {
                        //
                    }
                    return aLong;
                });

        Flux<Long> everyHundredMillis = Flux.interval(Duration.ofMillis(10));

        everyHundredMillis
                .map(a -> System.nanoTime())
                .onBackpressureDrop()
                .zipWith(everySecond)
                .subscribe(aLong -> log.info(aLong + ""));

        //http://tomstechnicalblog.blogspot.it/2016/02/rxjava-understanding-observeon-and.html

    }

    @Test
    public void throttlingRxJavaFluxTuples() throws Exception {

        Observable<Long> everySecond = Observable.fromIterable(Arrays.asList(1L, 2L, 3L))
                .map(aLong -> {
                    try {
                        Thread.sleep(1_000L);
                    } catch (InterruptedException e) {
                        //
                    }
                    return aLong;
                });

        Observable<Long> everyMillis = Observable.interval(1, TimeUnit.MILLISECONDS);

        everyMillis
                .map(a -> System.nanoTime())
                .zipWith(everySecond, Tuples::of)
                .subscribe(aLong -> log.info(aLong + ""));

    }

    @Test
    public void fromA() throws Exception {

        Observable<Long> everySecond = Observable.fromIterable(Arrays.asList(1L, 2L, 3L));

        Observable<Long> everyMillis = Observable.interval(0L, 1_000L,
                TimeUnit.MILLISECONDS);

        Observable<Long> longObservable = everySecond.zipWith(everyMillis, (x, y) -> x);

        longObservable
                .subscribeOn(io.reactivex.schedulers.Schedulers.trampoline())
                .subscribe(aLong -> log.info(aLong + ""));
    }

    @Test
    public void withCdl() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        Flux.fromIterable(Arrays.asList(1L, 2L, 3L)).onBackpressureBuffer(100)
                .zipWith(Flux.interval(Duration.ofSeconds(1)))
                .doOnComplete(cdl::countDown)
                .subscribe(aLong -> log.info(aLong + ""));

        log.info("waiting");
        cdl.await();
        log.info("finishing");
    }

}
