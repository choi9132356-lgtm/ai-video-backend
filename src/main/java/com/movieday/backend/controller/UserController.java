package com.movieday.backend.controller;

import com.movieday.backend.domain.User;
import com.movieday.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 기존 가짜 API는 그대로 둡니다.
    @GetMapping("/api/test")
    public String testApi() {
        return "백엔드 서버와 성공적으로 연결되었습니다! 🚀";
    }

    // 💡 실제 DB의 유저 목록을 반환하는 진짜 API 주소를 엽니다.
    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 🛠️ [신규 추가] 프론트엔드 A방식(실시간 중복 확인 버튼)을 위한 API
    @GetMapping("/api/check-id")
    public ResponseEntity<?> checkIdDuplicate(@RequestParam("userId") String userId) {
        try {
            // UserService를 통해 해당 userId(이메일)가 이미 존재하는지 체크합니다.
            boolean isDuplicate = userService.isUserIdExists(userId);

            // 프론트엔드가 편하게 꺼내 쓸 수 있도록 JSON 구조 { "isDuplicate": true/false } 로 묶어 보냅니다.
            Map<String, Object> response = new HashMap<>();
            response.put("isDuplicate", isDuplicate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("중복 확인 중 서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // 성공 시 가입된 유저 객체와 함께 200 OK 상태코드를 반환합니다.
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            // 💡 UserService에서 던진 "이미 존재하는 아이디입니다." 메시지를
            // 400 Bad Request 상태코드와 함께 프론트엔드로 친절하게 던집니다.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 에러 처리
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // 서비스의 로그인 로직을 호출합니다.
            User loggedInUser = userService.login(loginRequest.getUserId(), loginRequest.getPassword());

            // 로그인 성공 시 200 OK와 함께 유저 정보 반환
            return ResponseEntity.ok(loggedInUser);
        } catch (IllegalArgumentException e) {
            // 아이디 없음 또는 비밀번호 틀림 에러 처리 (400 Bad Request)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    // 💡 프론트엔드에서 ID, PW만 담아서 보낼 때 받아줄 바구니(DTO)
    @lombok.Data
    public static class LoginRequest {
        private String userId;
        private String password;
    }

}