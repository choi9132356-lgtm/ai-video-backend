package com.movieday.backend.service;

import com.movieday.backend.domain.User;
import com.movieday.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // Repository 리모컨을 자동으로 연결해 줍니다.
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // DB에 있는 모든 유저 목록을 가져오는 메서드
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // UserService.java에 추가할 코드
    public User registerUser(User user) {

        // 💡 [핵심 추가] DB에 이미 이 userId를 쓰는 사람이 있는지 검사합니다.
        if (userRepository.findByUserId(user.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 💡 실제 서비스라면 여기서 비밀번호 암호화(BCrypt) 등을 진행하지만,
        // 지금은 연동 테스트를 위해 필수 데이터만 가공해서 그대로 저장합니다.
        user.setRole("USER"); // 기본 권한 설정
        user.setInputId(user.getUserId());
        user.setInputDt(java.time.LocalDateTime.now());
        user.setModifyId(user.getUserId());
        user.setModifyDt(java.time.LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 관리자에게 회원가입 알림 메일 발송
        try {
            emailService.sendSignupNotification(user.getUserId(), user.getName(), user.getEmail(), user.getPhone());
        } catch (Exception e) {
            log.error("회원가입 알림 메일 발송 실패: {}", e.getMessage());
        }

        return savedUser;
    }

    // 🔑 로그인 검증 로직 추가
    public User login(String userId, String password) {
        // 1. 우선 DB에서 해당 아이디를 가진 유저가 있는지 찾습니다.
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 2. 유저가 있다면, 입력한 비밀번호가 DB에 저장된 비밀번호와 일치하는지 비교합니다.
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 둘 다 통과하면 로그인 성공! 유저 정보를 리턴합니다.
        return user;
    }

    // UserService.java 내부에 추가

    public boolean isUserIdExists(String userId) {
        // userRepository에 이미 존재 여부를 체크하는 메서드가 있다면 활용합니다.
        // 리턴 타입이 Optional<User>라면 .isPresent()를, 아니라면 조건에 맞게 null 체크를 해줍니다.
        return userRepository.findByUserId(userId).isPresent();
    }
}