package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.request.ReportRequest;
import com.phithang.mysocialnetwork.entity.ReportEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.PostRepository;
import com.phithang.mysocialnetwork.repository.ReportRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportService implements IReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean saveReport(ReportRequest reportRequest) {
        var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        UserEntity author = userRepository.findByEmail(authentication.getName());
        if (author == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }


        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setPost(postRepository.findById(reportRequest.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)));
        reportEntity.setReportDate(LocalDateTime.now());
        reportEntity.setTitle(reportRequest.getTitle());
        reportEntity.setUser(author);
        try {
            return reportRepository.save(reportEntity) != null;
        } catch (Exception e) {
            throw new AppException(ErrorCode.REPORT_CREATION_FAILED);
        }
    }
}