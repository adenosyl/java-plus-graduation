package ru.practicum.ewm.users.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.users.model.User;


import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    User getUserEntityById(Long id);

    List<User> findAllByIdIn(Collection<Long> ids, Pageable pageable);

    List<User> findAllBy(Pageable pageable);

}
