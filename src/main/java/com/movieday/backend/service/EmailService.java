package com.movieday.backend.service;

import com.movieday.backend.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${admin.notify.email}")
    private String adminEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 새 주문 접수 시 관리자에게 이메일 알림 발송 (Resend HTTP API)
     */
    @Async
    public void sendNewOrderNotification(Order order) {
        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", "무비데이 <onboarding@resend.dev>");
            body.put("to", adminEmail);
            body.put("subject", "[무비데이] 새 주문 접수 - " + order.getVideoStyle() + " 스타일");
            body.put("text",
                "🎬 새로운 주문이 접수되었습니다!\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                "▶ 주문번호: " + order.getId() + "\n" +
                "▶ 영상 스타일: " + order.getVideoStyle() + "\n" +
                "▶ 플랜: " + order.getPlan() + "\n" +
                "▶ 금액: " + String.format("%,d", order.getPrice()) + "원\n" +
                "▶ BGM: " + (order.isBgmYn() ? "포함" : "미포함") + "\n" +
                "▶ 나레이션: " + (order.isNarrationYn() ? "포함" : "미포함") + "\n" +
                "▶ 주문자: " + order.getUserId() + "\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "📝 스토리:\n" + order.getTextStory() + "\n\n" +
                "관리자 페이지에서 확인하세요.\n" +
                "https://www.movieday.co.kr/admin"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info("관리자 이메일 알림 발송 완료 - 주문번호: {}, 응답: {}", order.getId(), response.getStatusCode());
        } catch (Exception e) {
            log.error("이메일 발송 실패 - 주문번호: {}, 에러: {}", order.getId(), e.getMessage());
        }
    }
}
