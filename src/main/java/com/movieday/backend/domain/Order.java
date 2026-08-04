package com.movieday.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId; // 주문한 사람 ID

    @Column(nullable = false)
    private String videoStyle; // 영상 스타일

    @Column(nullable = false)
    private String plan; // 상품 등급 (BASIC / STANDARD / PREMIUM)

    @Column(nullable = false)
    private Long price; // 결제 금액

    @Column(nullable = false)
    private boolean bgmYn = false;

    @Column(nullable = false)
    private boolean narrationYn = false;

    @Column(columnDefinition = "TEXT")
    private String textStory;

    @Column(nullable = false)
    private String orderStatus = "PENDING";

    // 💡 [추가] 등록/수정 정보 컬럼
    @Column(nullable = false)
    private String inputId;

    @Column(nullable = false)
    private LocalDateTime inputDt = LocalDateTime.now();

    private String modifyId;

    private LocalDateTime modifyDt;

    // Order.java 엔티티 내부
    private String completedFileName; // 💡 관리자가 업로드한 완료 비디오 파일명 저장용
}