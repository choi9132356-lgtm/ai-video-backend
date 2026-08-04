package com.movieday.backend.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private String userId;
    private String videoStyle;
    private String plan;
    private Long price;
    private boolean bgmYn;
    private boolean narrationYn;
    private String textStory;
    private String orderStatus;

    // 💡 관리 공통 컬럼 포함
    private String inputId;
    private LocalDateTime inputDt;
    private String modifyId;
    private LocalDateTime modifyDt;

    // 📎 해당 주문에 묶인 첨부파일 목록들을 담아줄 리스트
    private List<OrderFile> files;

    // 🎯 관리자가 업로드 완료한 파일명을 담을 변수 추가
    private String completedFileName;
}