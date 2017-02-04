package fm.mox;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class E01FluxMonoTest {

    @Test
    public void nothing() throws Exception {
        Flux<Integer> just = Flux.just(1, 2, 3);
    }

    @Test
    public void empty() throws Exception {

        Flux<String> flux = emptyFlux();

        StepVerifier.create(flux) //automatically subscribes, request data
                .expectComplete()
                .verify();
    }

    private Flux<String> emptyFlux() {
        return Flux.empty();
    }

    @Test
    public void emptyM() {
        Mono<String> mono = emptyMono();
        StepVerifier.create(mono)
                .expectComplete()
                .verify();
    }

    // TODO Return an empty Mono
    Mono<String> emptyMono() {
        return Mono.empty(); // TO BE REMOVED
    }

    @Test
    public void fromValue() {
        Mono<String> mono = fooMono();
        StepVerifier.create(mono)
                .expectNext("foo")
                .expectComplete()
                .verify();
    }

    // TODO Return a Mono that contains a "foo" value
    Mono<String> fooMono() {
        return Mono.just("foo"); // TO BE REMOVED
    }

    @Test
    public void twoValues() throws Exception {
        Flux<String> flux = twoVals();

        StepVerifier.create(flux) //can control bounded demand. requesting 1 would block
                .expectNext("1", "2")
                .expectComplete()
                .verify();

    }

    private Flux<String> twoVals() {
//		return Flux.fromIterable(Arrays.asList("1", "3"));
// 		.log()
        return Flux.just("1", "2");
    }

    @Test
    public void errorF() {
        Flux<String> flux = errorFlux();
        StepVerifier.create(flux)
                .expectError(IllegalStateException.class)
                .verify();
    }

    // TODO Create a Flux that emits an IllegalStateException
    Flux<String> errorFlux() {
        return Flux.error(new IllegalStateException()); // TO BE REMOVED
    }

    @Test
    public void errorM() {
        Mono<String> mono = errorMono();
        StepVerifier.create(mono)
                .expectError(IllegalStateException.class)
                .verify();
    }

    // TODO Create a Mono that emits an IllegalStateException
    Mono<String> errorMono() {
        return Mono.error(new IllegalStateException()); // TO BE REMOVED
    }

    @Test
    public void countEach100ms() {
        Flux<Long> flux = counter();
        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
                .expectComplete()
                .verify();
    }

    // TODO Create a Flux that emits increasing values from 0 to 9 each 100ms
    Flux<Long> counter() {
        return Flux
                .interval(Duration.ofMillis(100))
                .take(10);  // TO BE REMOVED
    }

    @Test
    public void noSignal() {
        Mono<String> mono = monoWithNoSignal();
        StepVerifier
                .create(mono)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .thenCancel()
                .verify();
    }

    // TODO Return an Mono that never emit any signal
    Mono<String> monoWithNoSignal() {
        return Mono.never(); // TO BE REMOVED
    }

}
