package fm.mox;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class BlockingUserRepository implements BlockingRepository<User> {


    private final ReactiveRepository<User> reactiveRepository;

    private int callCount = 0;

    public BlockingUserRepository() {
        reactiveRepository = new ReactiveUserRepository();
    }

    public BlockingUserRepository(long delayInMs) {
        reactiveRepository = new ReactiveUserRepository(delayInMs);
    }

    public BlockingUserRepository(List<User> users) {
        reactiveRepository = new ReactiveUserRepository(users);
    }

    public BlockingUserRepository(long delayInMs, List<User> users) {
        reactiveRepository = new ReactiveUserRepository(delayInMs, users);
    }


    @Override
    public void save(User user) {
        callCount++;
        reactiveRepository.save(Mono.just(user)).block();
    }

    @Override
    public User findFirst() {
        callCount++;
        return reactiveRepository.findFirst().block();
    }

    @Override
    public Iterable<User> findAll() {
        callCount++;
        return reactiveRepository.findAll().toIterable();
    }

    @Override
    public User findById(String username) {
        callCount++;
        return reactiveRepository.findById(username).block();
    }

    public int getCallCount() {
        return callCount;
    }

}
