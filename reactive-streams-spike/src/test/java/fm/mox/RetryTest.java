package fm.mox;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalTime;

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
}
