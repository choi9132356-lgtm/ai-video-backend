package com.movieday.backend.service;

import com.movieday.backend.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.notify.email}")
    private String adminEmail;

    /**
     * 새 주문 접수 시 관리자에게 이메일 알림 발송
     */
    @Async
    public void sendNewOrderNotification(Order order) {
        try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject("[무비데이] 새 주문 접수 - " + order.getVideoStyle() + " 스타일");
        message.setText(
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
        mailSender.send(message);
        log.info("관리자 이메일 알림 발송 완료 - 주문번호: {}", order.getId());
        } catch (Exception e) {
            log.error("이메일 발송 실패 - 주문번호: {}, 에러: {}", order.getId(), e.getMessage());
        }
    }
}
