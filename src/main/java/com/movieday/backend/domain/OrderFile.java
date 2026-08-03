package com.movieday.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "order_files") // 💡 이미지뿐만 아니라 모든 포맷을 수용하는 파일 테이블
public class OrderFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId; // 연관된 주문 번호

    @Column(nullable = false)
    private String originalFileName; // 유저가 올린 원본 파일명 (예: 나의사진.png)

    @Column(nullable = false)
    private String storedFileName; // 서버 하드디스크에 저장된 난수 파일명 (예: uuid_나의사진.png)

    @Column(nullable = false)
    private Long fileSize; // 💡 파일 크기 (Byte 단위)

    @Column(nullable = false)
    private String fileExtension; // 💡 파일 확장자 (예: png, mp4, mp3)

    // 💡 공통 관리 컬럼 (요청하신 4개 컬럼 완전 반영)
    @Column(nullable = false)
    private String inputId;

    @Column(nullable = false)
    private LocalDateTime inputDt;

    @Column(nullable = false)
    private String modifyId;

    @Column(nullable = false)
    private LocalDateTime modifyDt;
}