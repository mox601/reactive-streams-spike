package fm.mox;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
public interface UserRepository extends PagingAndSortingRepository<User, String> {
}
