package com.prp.authservice.repository;

// import com.prp.authservice.model.User;
import com.prp.authservice.entity.UserEntity;
import com.arangodb.springframework.repository.ArangoRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ArangoRepository<UserEntity, String> {
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
}
