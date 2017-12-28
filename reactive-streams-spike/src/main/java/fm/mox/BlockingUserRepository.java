package fm.mox;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class BlockingUserRepository implements UserRepository {

    private final ReactiveRepository<User, String> reactiveRepository;

    private int callCount;

    public BlockingUserRepository() {
        this.reactiveRepository = new ReactiveUserRepository();
    }

    public BlockingUserRepository(long delayInMs) {
        this.reactiveRepository = new ReactiveUserRepository(delayInMs);
    }

    public BlockingUserRepository(List<User> users) {
        this.reactiveRepository = new ReactiveUserRepository(users);
    }

    public BlockingUserRepository(long delayInMs, List<User> users) {
        this.reactiveRepository = new ReactiveUserRepository(delayInMs, users);
        this.callCount = 0;
    }

    @Override
    public User save(User user) {
        this.callCount++;
        return reactiveRepository.save(Mono.just(user)).block();
    }

    @Override
    public <S extends User> Iterable<S> saveAll(Iterable<S> iterable) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public Optional<User> findById(String username) {
        this.callCount++;
        return Optional.ofNullable(reactiveRepository.findById(username).block());
    }

    @Override
    public boolean existsById(String s) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public Iterable<User> findAll() {
        this.callCount++;
        return this.reactiveRepository.findAll().toIterable();
    }

    @Override
    public Iterable<User> findAllById(Iterable<String> iterable) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public long count() {
        return this.reactiveRepository.findAll().count().block();
    }

    @Override
    public void delete(User s) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void deleteAll(Iterable<? extends User> iterable) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public int getCallCount() {
        return this.callCount;
    }

    @Override
    public Iterable<User> findAll(Sort sort) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return this.reactiveRepository.findAll(Mono.just(pageable)).block();
    }
}
