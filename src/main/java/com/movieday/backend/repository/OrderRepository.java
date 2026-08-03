package com.movieday.backend.repository;

import com.movieday.backend.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 💡 나중에 "내 주문 내역" 조회할 때 "특정 유저가 주문한 것만 싹 긁어와줘!" 하기 위해 미리 만들어 둡니다.
    List<Order> findByUserId(String userId);
}