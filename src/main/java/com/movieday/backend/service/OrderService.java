package com.movieday.backend.service;

import com.movieday.backend.domain.Order;
import com.movieday.backend.domain.OrderFile;
import com.movieday.backend.domain.OrderResponseDto;
import com.movieday.backend.repository.OrderRepository;
import com.movieday.backend.repository.OrderFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value; // 💡 추가 필요
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderFileRepository orderFileRepository;
    private final EmailService emailService;

    // 💡 application.properties에 설정한 경로를 동적으로 주입받습니다!
    @Value("${file.upload-dir}")
    private String uploadPath;

    @Transactional
    public Order saveOrderWithFiles(String userId, String videoStyle, String plan, Long price,
                                    boolean bgmYn, boolean narrationYn, String textStory,
                                    List<MultipartFile> files) throws IOException {

        LocalDateTime now = LocalDateTime.now();

        // 1. 주문 정보 빌드 및 저장
        Order order = new Order();
        order.setUserId(userId);
        order.setVideoStyle(videoStyle);
        order.setPlan(plan);
        order.setPrice(price);
        order.setBgmYn(bgmYn);
        order.setNarrationYn(narrationYn);
        order.setTextStory(textStory);

        order.setInputId(userId);
        order.setInputDt(now);
        order.setModifyId(userId);
        order.setModifyDt(now);

        Order savedOrder = orderRepository.save(order);

        // 2. 실제 물리 파일 저장 및 파일 메타데이터 테이블 기록
        if (files != null && !files.isEmpty()) {
            // 주입받은 uploadPath로 폴더 객체 생성
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String originalFileName = file.getOriginalFilename();
                String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
                Long fileSize = file.getSize();

                String fileExtension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
                }

                // 💾 설정 파일 경로에 진짜 파일 물리 저장
                File saveFile = new File(uploadPath, storedFileName);
                file.transferTo(saveFile);

                // 🗄️ DB 파일 테이블(order_files)에 메타데이터 기록
                OrderFile orderFile = new OrderFile();
                orderFile.setOrderId(savedOrder.getId());
                orderFile.setOriginalFileName(originalFileName);
                orderFile.setStoredFileName(storedFileName);
                orderFile.setFileSize(fileSize);
                orderFile.setFileExtension(fileExtension);

                orderFile.setInputId(userId);
                orderFile.setInputDt(now);
                orderFile.setModifyId(userId);
                orderFile.setModifyDt(now);

                orderFileRepository.save(orderFile);
            }
        }

        // 3. 관리자에게 새 주문 이메일 알림
        try {
            emailService.sendNewOrderNotification(savedOrder);
        } catch (Exception e) {
            // 이메일 실패해도 주문은 정상 처리
            System.err.println("이메일 발송 실패: " + e.getMessage());
        }

        return savedOrder;
    }

    // 💡 아래 메서드를 OrderService 클래스 내부에 추가해 주세요!

    public List<OrderResponseDto> getAllOrdersWithFiles() {
        // 1. 모든 주문 정보를 가져옵니다 (최신 주문이 맨 위로 오도록 정렬 가능)
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDto> dtoList = new java.util.ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setId(order.getId());
            dto.setUserId(order.getUserId());
            dto.setVideoStyle(order.getVideoStyle());
            dto.setPlan(order.getPlan());
            dto.setPrice(order.getPrice());
            dto.setBgmYn(order.isBgmYn());
            dto.setNarrationYn(order.isNarrationYn());
            dto.setTextStory(order.getTextStory());
            dto.setOrderStatus(order.getOrderStatus());

            // 🎯 [이 한 줄을 추가!] DB에 저장된 완료 파일명을 DTO 가방에 쏙 넣어줍니다.
            // (※ 만약 OrderResponseDto에 completedFileName 필드가 없다면 변수명에 맞게 추가/수정해 주세요)
            dto.setCompletedFileName(order.getCompletedFileName());

            dto.setInputId(order.getInputId());
            dto.setInputDt(order.getInputDt());
            dto.setModifyId(order.getModifyId());
            dto.setModifyDt(order.getModifyDt());

            // 🔍 2. 현재 주문 번호(orderId)에 묶여있는 파일들을 싹 찾아서 가방에 넣어줍니다.
            List<OrderFile> files = orderFileRepository.findByOrderId(order.getId());
            dto.setFiles(files);

            dtoList.add(dto);
        }

        return dtoList;
    }

    // 🎯 [유저 화면용] 특정 유저의 주문 내역을 가져오는 서비스 로직
    public List<OrderResponseDto> getOrdersByUserId(String userId) {
        // 1. 해당 유저의 주문들을 DB에서 조회
        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderResponseDto> dtoList = new java.util.ArrayList<>();

        for (Order order : orders) {
            OrderResponseDto dto = new OrderResponseDto();

            // 기존 세팅 코드들 (id, videoStyle, price, orderStatus 등)
            dto.setId(order.getId());
            dto.setUserId(order.getUserId());
            dto.setVideoStyle(order.getVideoStyle());
            dto.setPlan(order.getPlan());
            dto.setPrice(order.getPrice());
            dto.setBgmYn(order.isBgmYn());
            dto.setNarrationYn(order.isNarrationYn());
            dto.setTextStory(order.getTextStory());
            dto.setOrderStatus(order.getOrderStatus());

            // 🎯 [핵심 추가] 아까 수정한 가방(DTO)에 관리자가 올린 완료 파일명도 채워줍니다!
            dto.setCompletedFileName(order.getCompletedFileName());

            // 유저가 첨부했던 파일들도 리스트에 매핑
            List<OrderFile> files = orderFileRepository.findByOrderId(order.getId());
            dto.setFiles(files);

            dtoList.add(dto);
        }
        return dtoList;
    }

    // 💡 [추가] 주문 상태 업데이트 서비스
    @org.springframework.transaction.annotation.Transactional // 🎯 데이터 수정을 위해 추가
    public void updateOrderStatus(Long id, String orderStatus) {
        // 주문번호로 찾고, 없으면 예외 발생
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. id=" + id));

        // 엔티티의 상태값을 변경 (Lombok 세터 기준)
        order.setOrderStatus(orderStatus);
    }

    // 🎯 [서비스 전용] 상태 변경과 함께 완료 파일을 물리 하드디스크에 저장하는 로직
    @jakarta.transaction.Transactional
    public void updateOrderStatusWithFile(Long id, String orderStatus, MultipartFile file) throws Exception {
        // 1. 해당 주문이 존재하는지 먼저 조회
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. ID: " + id));

        // 2. 상태 업데이트
        order.setOrderStatus(orderStatus);

        // 3. 만약 상태가 'COMPLETED'(제작완료)이고 파일이 넘어왔다면 파일 저장 진행
        if ("COMPLETED".equals(orderStatus) && file != null && !file.isEmpty()) {
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 파일명 중복 방지를 위해 타임스탬프 결합
            String originalFileName = file.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "_" + originalFileName;

            // 물리 파일 저장
            File saveFile = new File(uploadPath, storedFileName);
            file.transferTo(saveFile);

            // 4. 엔티티에 완료 파일명 기록 (DB에 반영됨)
            order.setCompletedFileName(storedFileName);

            // 정보 수정자/수정일시 세팅
            order.setModifyId("ADMIN");
            order.setModifyDt(LocalDateTime.now());
        }
    }



}