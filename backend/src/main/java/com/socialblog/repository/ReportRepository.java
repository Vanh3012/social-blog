package com.socialblog.repository;

import com.socialblog.model.entity.Report;
import com.socialblog.model.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // Lấy tất cả báo cáo sắp xếp theo ngày tạo
    List<Report> findAllByOrderByCreatedAtDesc();

    // Đếm báo cáo chờ xử lý
    long countByStatus(ReportStatus status);

    // Helper method
    default long countByStatusPending() {
        return countByStatus(ReportStatus.PENDING);
    }
}