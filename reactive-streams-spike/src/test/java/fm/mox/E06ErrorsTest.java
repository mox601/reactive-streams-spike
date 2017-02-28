package fm.mox;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class E06ErrorsTest {

    @Test
    public void errors() throws Exception {
        Flux<String> flux = errorFlux();

        StepVerifier.create(flux)
//                .expectError(IllegalStateException.class)
            .verifyError(IllegalStateException.class);
    }

    //throwing won't work in an async context. exception might bubble up in different threads
    private Flux<String> errorFlux() {
//		throw new IllegalStateException(); // can't do this
        return Flux.error(new IllegalStateException());
    }
    //publishers flux and mono are try-catch-block all included


    @Test
    public void afterPublishingSomeThenError() throws Exception {

        StepVerifier.create(publishOneThenError())
            .expectNext("foo")
            .verifyError(IllegalStateException.class);
//                .verify();
    }

    private Flux<String> publishOneThenError() {
        return Flux.concat(Mono.just("foo"), Flux.error(new IllegalStateException()));
    }
}
