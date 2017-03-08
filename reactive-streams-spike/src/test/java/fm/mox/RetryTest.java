package fm.mox;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

        Flux.interval(Duration.ofMillis(100))
            .map(a -> System.nanoTime())
//            .log()
            .onBackpressureDrop()
            .zipWith(Flux.interval(Duration.ofSeconds(1)))
//            .log()
            .subscribe(aLong -> log.info(aLong + ""));

        Thread.sleep(10_000);

    }
}
