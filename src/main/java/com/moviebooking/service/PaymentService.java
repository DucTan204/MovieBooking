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

    // Các biến này sẽ lấy từ Environment Variables trên Render hoặc application.properties
    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Value("${payos.webhook-url}")
    private String webhookUrl;

    /**
     * Khởi tạo bản ghi thanh toán trong Database
     */
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

    /**
     * Tạo link thanh toán PayOS
     */
    @Transactional
    public PayOSResponse createPayOSPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt vé"));

        if (booking.getStatus() == Booking.BookingStatus.PAID) {
            return PayOSResponse.builder().status("PAID").build();
        }

        // Tạo orderCode ngẫu nhiên (PayOS yêu cầu kiểu số)
        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3, 13)) + bookingId;

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Vé xem phim - " + booking.getBookingCode())
                .quantity(1)
                .price(booking.getTotalPrice().longValue())
                .build();

        // PayOS description không cho phép ký tự đặc biệt như "-"
        String descriptionForPayOS = booking.getBookingCode().replace("-", "");

        // Build request gửi sang PayOS
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(booking.getTotalPrice().longValue())
                .description(descriptionForPayOS)
                .items(List.of(item))
                .returnUrl(returnUrl + "?bookingId=" + bookingId)
                .cancelUrl(cancelUrl + "?bookingId=" + bookingId)
           
                .build();

        try {
            log.info("Đang tạo link thanh toán với Webhook: {}", webhookUrl);
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

            // Lưu thông tin payment vào DB để đối soát
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
            log.error("Lỗi PayOS: {}", e.getMessage());
            throw new RuntimeException("Lỗi tạo PayOS link: " + e.getMessage());
        }
    }

    /**
     * Xử lý dữ liệu từ Webhook gửi về
     */
    @Transactional
    public void handleWebhook(String webhookBody) {
        try {
            // Xác thực chữ ký dữ liệu từ PayOS gửi về để đảm bảo an toàn
            WebhookData data = payOS.webhooks().verify(webhookBody);

            // Description lúc này là mã Booking (không có dấu -)
            String bookingCodeFromWebhook = data.getDescription();

            // "00" nghĩa là thanh toán thành công
            if ("00".equals(data.getCode())) {
                log.info("Thanh toán thành công qua Webhook cho mã: {}", bookingCodeFromWebhook);
                confirmPayment(bookingCodeFromWebhook);
            }
        } catch (Exception e) {
            log.error("Xác thực Webhook thất bại: {}", e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái Booking và tạo vé QR
     */
    @Transactional
    public void confirmPayment(String bookingCode) {
        String tempCode = bookingCode;

        // Khôi phục lại định dạng mã BK-XXXX nếu PayOS làm mất dấu gạch ngang
        if (!tempCode.contains("-") && tempCode.startsWith("BK") && tempCode.length() > 2) {
            tempCode = "BK-" + tempCode.substring(2);
        }

        final String finalBookingCode = tempCode;

        Booking booking = bookingRepository.findByBookingCode(finalBookingCode)
                .orElseThrow(() -> new NotFoundException("Mã vé không tồn tại: " + finalBookingCode));

        // Nếu đã thanh toán rồi thì không xử lý lại
        if (booking.getStatus() == Booking.BookingStatus.PAID) return;

        // 1. Cập nhật trạng thái Booking
        booking.setStatus(Booking.BookingStatus.PAID);
        bookingRepository.save(booking);

        // 2. Tạo mã QR vé
        try {
            ticketQRService.generateQRForBooking(booking.getId());
            log.info("Đã tạo vé QR cho Booking ID: {}", booking.getId());
        } catch (Exception e) {
            log.error("Lỗi tạo QR: {}", e.getMessage());
        }

        // 3. Cập nhật trạng thái Payment sang SUCCESS
        paymentRepository.findByBookingId(booking.getId()).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(p);
        });
    }

    public String checkPaymentStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        return booking.getStatus() == Booking.BookingStatus.PAID ? "PAID" : "PENDING";
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
}