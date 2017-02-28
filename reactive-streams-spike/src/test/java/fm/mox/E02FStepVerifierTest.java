package fm.mox;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class E02FStepVerifierTest {

    ///////////////////// write the verifier instead

    @Test
    public void expectElementsThenComplete() throws Exception {
        expectFooBarComplete(Flux.just("foo", "bar"));
    }

    //    any type of assertions lib
    private void expectFooBarComplete(Flux<String> flux) {
//		fail();
        StepVerifier.create(flux)
            .expectNext("foo", "bar")
            .verifyComplete();
//                .verify();
    }

    @Test
    public void askAllExpectTwo() throws Exception {
        ReactiveUserRepository reactiveUserRepository = new ReactiveUserRepository(Arrays.asList(User.BOB, User.ALICE));
        Flux<User> flux = reactiveUserRepository.findAll();
        StepVerifier verifier = requestAllExpectTwo(flux);
        verifier.verify();
    }

    private StepVerifier requestAllExpectTwo(Flux<User> flux) {
        return StepVerifier.create(flux).expectNextCount(2).expectComplete();
    }

    @Test
    public void askOneByOne() throws Exception {
        ReactiveUserRepository reactiveUserRepository = new ReactiveUserRepository(Arrays.asList(User.BOB, User.ALICE, User.CARL));
        Flux<User> flux = reactiveUserRepository.findAll();
        StepVerifier verifier = requestOneByOne(flux);
        verifier.verify();
    }

    private StepVerifier requestOneByOne(Flux<User> flux) {
        return StepVerifier.create(flux, 1)
            .expectNext(User.BOB)
            .thenRequest(1)
            .expectNext(User.ALICE)
            .thenCancel();
    }

}
