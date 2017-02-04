package fm.mox;

import lombok.Value;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
@Value
public class User {
    public static final User ALICE = new User("Alice", "French");
    public static final User BOB = new User("Bob", "Marshall");
    public static final User CARL = new User("Carl", "Wilson");
    public static final User DAVE = new User("Dave", "Jackson");

    private final String name;
    private final String surname;
}
