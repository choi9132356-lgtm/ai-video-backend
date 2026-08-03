package com.movieday.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user") // MySQL의 `user` 테이블과 매핑
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx") // DB의 PK 칼럼명과 일치시킵니다.
    private Long userIdx;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "address_detail", length = 150)
    private String addressDetail;

    @Column(nullable = false)
    private String role = "USER"; // 기본값은 일반 고객(USER). 관리자는 "ADMIN"으로 지정할 겁니다.

    @Column(name = "input_id", nullable = false, length = 50)
    private String inputId;

    @Column(name = "input_dt", nullable = false)
    private LocalDateTime inputDt;

    @Column(name = "modify_id", nullable = false, length = 50)
    private String modifyId;

    @Column(name = "modify_dt", nullable = false)
    private LocalDateTime modifyDt;


}