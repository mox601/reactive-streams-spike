package fm.mox;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public interface BlockingRepository<T> {

    void save(T t);

    T findFirst();

    Iterable<T> findAll();

    T findById(String id);
}
