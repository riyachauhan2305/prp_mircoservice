package com.prp.authservice.repository;

import com.prp.authservice.model.User;
import com.arangodb.springframework.repository.ArangoRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ArangoRepository<User, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);
}
