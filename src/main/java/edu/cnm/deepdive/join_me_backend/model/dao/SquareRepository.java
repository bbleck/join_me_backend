package edu.cnm.deepdive.join_me_backend.model.dao;

import edu.cnm.deepdive.join_me_backend.model.entity.Square;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 * The interface Square repository.
 */
public interface SquareRepository extends CrudRepository<Square, Long> {

  List<Square> findAll();

}
