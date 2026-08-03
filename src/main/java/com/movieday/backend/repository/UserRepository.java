package com.movieday.backend.repository;

import com.movieday.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 대신 실제 로그인 ID인 user_id로 유저를 찾는 기능을 장착합니다.
    Optional<User> findByUserId(String userId);
}