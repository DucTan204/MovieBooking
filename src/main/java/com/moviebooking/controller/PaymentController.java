package com.moviebooking.controller;

import com.moviebooking.dto.PayOSResponse;
import com.moviebooking.entity.Payment;
import com.moviebooking.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.process(payment));
    }

    @PostMapping("/payos/create")
    public ResponseEntity<PayOSResponse> createPayOSLink(@RequestBody Map<String, Long> body) {
        Long bookingId = body.get("bookingId");
        PayOSResponse response = paymentService.createPayOSPayment(bookingId);
        return ResponseEntity.ok(response);
    }

    // ✅ Đổi @RequestBody từ Webhook → String
    @PostMapping("/payos/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody String webhookBody) {
        try {
            paymentService.handleWebhook(webhookBody);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("WEBHOOK_ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/status/{bookingId}")
    public ResponseEntity<Map<String, String>> checkStatus(@PathVariable Long bookingId) {
        String status = paymentService.checkPaymentStatus(bookingId);
        return ResponseEntity.ok(Map.of("status", status));
    }

    @PostMapping("/simulate-webhook")
    public ResponseEntity<?> simulateWebhook(@RequestBody Map<String, String> request) {
        String bookingCode = request.get("bookingCode");
        paymentService.confirmPayment(bookingCode);
        return ResponseEntity.ok("Xác nhận thanh toán thành công cho mã: " + bookingCode);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }
}