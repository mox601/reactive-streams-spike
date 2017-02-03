package fm.mox;

import lombok.Value;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class Users {

    public static final User ALICE = new User("Alice", "French");
    public static final User BOB = new User("Bob", "Marshall");

    @Value
    public static class User {
        private final String name;
        private final String surname;
    }

}
