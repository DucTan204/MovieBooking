package com.moviebooking.service;

import com.moviebooking.dto.PayOSResponse;
import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Payment;
import com.moviebooking.exception.NotFoundException;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    private TicketQRService ticketQRService;

    @Autowired
    private PayOS payOS;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    // ✅ FIX LỖI: Bổ sung lại hàm process cho Controller gọi
    @Transactional
    public Payment process(Payment request) {
        Long bookingId = request.getBooking().getId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() == Booking.BookingStatus.PAID) {
            return paymentRepository.findByBookingId(bookingId).orElse(request);
        }

        request.setBooking(booking);
        request.setAmount(booking.getTotalPrice());
        request.setStatus(Payment.PaymentStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(request);
    }

    @Transactional
    public PayOSResponse createPayOSPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() == Booking.BookingStatus.PAID) {
            return PayOSResponse.builder().status("PAID").build();
        }

        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3, 13)) + bookingId;

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Vé xem phim - " + booking.getBookingCode())
                .quantity(1)
                .price(booking.getTotalPrice().longValue())
                .build();

        // Xóa dấu "-" khi gửi sang PayOS
        String descriptionForPayOS = booking.getBookingCode().replace("-", "");

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(booking.getTotalPrice().longValue())
                .description(descriptionForPayOS)
                .items(List.of(item))
                .returnUrl(returnUrl + "?bookingId=" + bookingId)
                .cancelUrl(cancelUrl + "?bookingId=" + bookingId)
                .build();

        try {
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

            // Lưu thông tin payment vào DB
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalPrice());
            payment.setMethod(Payment.PaymentMethod.PAYOS);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setTransactionCode("PAYOS_" + orderCode);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            return PayOSResponse.builder()
                    .checkoutUrl(response.getCheckoutUrl())
                    .paymentLinkId(response.getPaymentLinkId())
                    .orderCode(response.getOrderCode())
                    .status("PENDING")
                    .build();
        } catch (PayOSException e) {
            throw new RuntimeException("Lỗi tạo PayOS link: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String webhookBody) {
        try {
            WebhookData data = payOS.webhooks().verify(webhookBody);
            String bookingCode = data.getDescription();
            if ("00".equals(data.getCode())) {
                confirmPayment(bookingCode);
            }
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
        }
    }

    @Transactional
    public void confirmPayment(String bookingCode) {
        // ✅ FIX LỖI: Xử lý biến final cho Lambda
        String tempCode = bookingCode;
        if (!tempCode.contains("-") && tempCode.startsWith("BK") && tempCode.length() > 2) {
            tempCode = "BK-" + tempCode.substring(2);
        }
        final String finalBookingCode = tempCode;

        Booking booking = bookingRepository.findByBookingCode(finalBookingCode)
                .orElseThrow(() -> new NotFoundException("Mã vé không tồn tại: " + finalBookingCode));

        if (booking.getStatus() == Booking.BookingStatus.PAID) return;

        booking.setStatus(Booking.BookingStatus.PAID);
        bookingRepository.save(booking);

        try {
            ticketQRService.generateQRForBooking(booking.getId());
        } catch (Exception e) {
            log.warn("QR Error: {}", e.getMessage());
        }

        paymentRepository.findByBookingId(booking.getId()).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(p);
        });
    }

    public String checkPaymentStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Not found"));
        return booking.getStatus() == Booking.BookingStatus.PAID ? "PAID" : "PENDING";
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
}