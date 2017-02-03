package fm.mox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class BlockingUserRepository implements BlockingRepository<Users.User> {

    private final List<Users.User> users;

    private int callCount;

    public BlockingUserRepository() {
        this(new ArrayList<>());
    }

    public BlockingUserRepository(List<Users.User> users) {
        this.users = users;
        this.callCount = 0;
    }

    @Override
    public void save(Users.User user) {
        this.callCount++;
        this.users.add(user);
    }

    @Override
    public Users.User findFirst() {
        return null;
    }

    @Override
    public Iterable<Users.User> findAll() {
        this.callCount++;
        return this.users;
    }

    @Override
    public Users.User findById(String id) {
        return null;
    }

    @Override
    public int getCallCount() {
        return this.callCount;
    }


}
