package com.movieday.backend.controller;

import com.movieday.backend.domain.Order;
import com.movieday.backend.domain.OrderResponseDto;
import com.movieday.backend.service.OrderService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. 유저의 주문 및 파일 업로드 API
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestParam("userId") String userId,
            @RequestParam("videoStyle") String videoStyle,
            @RequestParam("plan") String plan,
            @RequestParam("price") Long price,
            @RequestParam("bgmYn") boolean bgmYn,
            @RequestParam("narrationYn") boolean narrationYn,
            @RequestParam("textStory") String textStory,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            Order savedOrder = orderService.saveOrderWithFiles(
                    userId, videoStyle, plan, price, bgmYn, narrationYn, textStory, files
            );
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("주문 및 파일 저장 중 서버 오류가 발생했습니다.");
        }
    }

    // 2. 관리자 대시보드 주문 리스트 조회 API
    @GetMapping("/admin/list")
    public ResponseEntity<List<OrderResponseDto>> getAdminOrderList() {
        try {
            List<OrderResponseDto> list = orderService.getAllOrdersWithFiles();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 3. 관리자 대시보드 파일 다운로드 API
    @GetMapping("/admin/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("storedName") String storedName,
                                                 @RequestParam("originName") String originName) {
        try {
            String decodedStoredName = URLDecoder.decode(storedName, StandardCharsets.UTF_8);
            String decodedOriginName = URLDecoder.decode(originName, StandardCharsets.UTF_8);

            String uploadPath = "C:/ai-video-studio/backend/upload/";
            Path filePath = Paths.get(uploadPath).resolve(decodedStoredName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                System.out.println("[🚨 다운로드 에러] 물리 파일이 없습니다: " + filePath.toString());
                return ResponseEntity.notFound().build();
            }

            String encodedFileName = URLEncoder.encode(decodedOriginName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (Exception e) {
            System.out.println("[🚨 서버 에러] 파일 다운로드 처리 중 오류 발생");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. 유저별 주문 내역 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUserId(@PathVariable("userId") String userId) {
        try {
            List<OrderResponseDto> userOrders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(userOrders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }


    // 🎯 [수정] 상태 변경 + 완료 링크 저장 API (JSON 방식)
    @PostMapping("/admin/status-update")
    public ResponseEntity<?> updateOrderStatus(@RequestBody java.util.Map<String, Object> body) {

        Long id = Long.valueOf(body.get("id").toString());
        String orderStatus = body.get("orderStatus").toString();
        String completedFileUrl = body.getOrDefault("completedFileUrl", "").toString();

        System.out.println("====== 관리자 상태 변경 ======");
        System.out.println("주문 ID: " + id);
        System.out.println("변경 상태: " + orderStatus);
        System.out.println("완료 링크: " + completedFileUrl);
        System.out.println("========================================");

        try {
            orderService.updateOrderStatusWithUrl(id, orderStatus, completedFileUrl);
            return ResponseEntity.ok().body("주문 상태 업데이트 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 내부 에러: " + e.getMessage());
        }
    }

    // 🎯 [수정 완료] 디코딩 오류 방지 및 실시간 로그 추적이 강화된 이미지 뷰어 API
    @GetMapping("/view-image")
    public ResponseEntity<Resource> viewImage(@RequestParam("storedName") String storedName) {
        try {
            // 1. 브라우저에서 보낸 인코딩된 파일명을 안전하게 디코딩
            String decodedStoredName;
            try {
                decodedStoredName = URLDecoder.decode(storedName, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                // 혹시 디코딩이 실패하면 원본 파라미터 그대로 대입해 봅니다.
                decodedStoredName = storedName;
            }

            // 2. 실제 파일 물리 경로 조합
            String uploadPath = "C:/ai-video-studio/backend/upload/";
            Path filePath = Paths.get(uploadPath).resolve(decodedStoredName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 🔍 [디버깅 로그] 파일이 안 뜰 때 서버 콘솔에 출력되는 진짜 탐색 주소
            System.out.println("=================================================");
            System.out.println("[🔍 이미지 로드 시도] 원본 파라미터: " + storedName);
            System.out.println("[🔍 이미지 로드 시도] 디코딩된 파일명: " + decodedStoredName);
            System.out.println("[🔍 이미지 로드 시도] 최종 탐색 경로: " + filePath.toAbsolutePath());

            if (!resource.exists()) {
                System.out.println("[❌ 이미지 뷰어] 실제 폴더에 파일이 존재하지 않습니다!");
                System.out.println("=================================================");
                return ResponseEntity.notFound().build();
            }

            System.out.println("[✅ 이미지 뷰어] 파일을 정상적으로 찾았습니다! 브라우저로 전송합니다.");
            System.out.println("=================================================");

            // 3. 파일 확장자에 맞춰 Content-Type(MimeType) 지정
            String contentType = "image/jpeg"; // 기본값
            String lowerName = decodedStoredName.toLowerCase();
            if (lowerName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerName.endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // 다운로드가 아닌 브라우저 내 '이미지' 렌더링 지시
                    .body(resource);

        } catch (Exception e) {
            System.out.println("[🚨 서버 에러] 이미지 뷰어 처리 중 치명적 오류 발생");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🎯 OrderController.java 내부에 추가할 쇼케이스 영상 스트리밍 API
    @GetMapping("/showcase/stream")
    public ResponseEntity<Resource> streamShowcaseVideo(@RequestParam("fileName") String fileName) {
        try {
            // 💡 지정하신 업로드 베이스 경로와 파일명을 결합합니다.
            String baseDir = "C:/ai-video-studio/backend/upload/";
            Path filePath = Paths.get(baseDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 브라우저가 영상 스트리밍(재생)을 원활하게 인지할 수 있도록 비디오 타입으로 헤더를 설정합니다.
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}