package fm.mox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class BlockingUserRepository implements BlockingRepository<Users.User> {

    private final List<Users.User> users;

    private int callCount;

    private final long interval;

    public BlockingUserRepository() {
        this(new ArrayList<>());
    }

    public BlockingUserRepository(List<Users.User> users) {
        this(users, 0L);
    }

    public BlockingUserRepository(List<Users.User> users, long interval) {
        this.users = users;
        this.callCount = 0;
        this.interval = interval;
    }

    @Override
    public void save(Users.User user) {
        this.callCount++;
        sleep();
        this.users.add(user);
    }

    @Override
    public Users.User findFirst() {
        return null;
    }

    @Override
    public Iterable<Users.User> findAll() {
        this.callCount++;
        sleep();
        return this.users;
    }

    @Override
    public Users.User findById(String id) {
        sleep();
        return null;
    }

    @Override
    public int getCallCount() {
        return this.callCount;
    }

    private void sleep() {
        try {
            Thread.sleep(this.interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
