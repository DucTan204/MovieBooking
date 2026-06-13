package com.moviebooking;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // Import cần thiết cho lập lịch

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling // Kích hoạt tính năng lập lịch (dùng để chạy các tác vụ tự động như xóa SeatLock)
public class MovieBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBookingApplication.class, args);
    }

    /**
     * Cấu hình đồng nhất múi giờ cho toàn bộ hệ thống.
     * Đối với dự án đặt vé xem phim, việc quản lý Showtime (giờ chiếu)
     * và SeatLock (giờ hết hạn) cực kỳ quan trọng về mặt thời gian.
     */
    @PostConstruct
    public void init() {
        // Thiết lập múi giờ mặc định là giờ Việt Nam (GMT+7)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        System.out.println("----------------------------------------------");
        System.out.println("Movie Booking System is running...");
        System.out.println("Current TimeZone: " + TimeZone.getDefault().getID());
        System.out.println("----------------------------------------------");
    }
}