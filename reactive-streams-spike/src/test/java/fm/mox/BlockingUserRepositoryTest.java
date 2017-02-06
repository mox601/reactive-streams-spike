package fm.mox;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
public class BlockingUserRepositoryTest {

    @Test
    public void findAll() throws Exception {

        List<User> users = Arrays.asList(User.ALICE, User.BOB, User.CARL);
        UserRepository blockingUserRepository = new BlockingUserRepository(1, users);

        PageRequest zeroPageRequest = new PageRequest(0, 1);
        Page<User> pageZero = blockingUserRepository.findAll(zeroPageRequest);
        assertEquals(users.size(), pageZero.getTotalElements());
        assertEquals(0, pageZero.getNumber());
        assertEquals(1, pageZero.getNumberOfElements());
        assertEquals(1, pageZero.getSize());
        assertEquals(User.ALICE, pageZero.getContent().get(0));

        Pageable one = zeroPageRequest.next();
        Page<User> pageOne = blockingUserRepository.findAll(one);
        assertEquals(users.size(), pageOne.getTotalElements());
        assertEquals(1, pageOne.getNumber());
        assertEquals(1, pageOne.getNumberOfElements());
        assertEquals(1, pageOne.getSize());
        assertEquals(User.BOB, pageOne.getContent().get(0));

        Pageable two = one.next();
        Page<User> pageTwo = blockingUserRepository.findAll(two);
        assertEquals(users.size(), pageTwo.getTotalElements());
        assertEquals(2, pageTwo.getNumber());
        assertEquals(1, pageTwo.getNumberOfElements());
        assertEquals(1, pageTwo.getSize());
        assertEquals(User.CARL, pageTwo.getContent().get(0));

        Pageable three = two.next();
        Page<User> pageThree = blockingUserRepository.findAll(three);
        assertNull(pageThree);

        PageRequest zeroPageTenRequest = new PageRequest(0, 10);
        Page<User> pageZeroTen = blockingUserRepository.findAll(zeroPageTenRequest);
        assertEquals(users.size(), pageZeroTen.getTotalElements());
        assertEquals(0, pageZeroTen.getNumber());
        assertEquals(3, pageZeroTen.getNumberOfElements());
        assertEquals(10, pageZeroTen.getSize());
        assertEquals(User.ALICE, pageZeroTen.getContent().get(0));

    }

}