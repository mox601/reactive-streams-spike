package fm.mox;

import lombok.Value;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class Users {

    public static final User ALICE = new User("Alice", "French");
    public static final User BOB = new User("Bob", "Marshall");
    public static final User CARL = new User("Carl", "Wilson");
    public static final User DAVE = new User("Dave", "Jackson");

    @Value
    public static class User {
        private final String name;
        private final String surname;
    }
}
