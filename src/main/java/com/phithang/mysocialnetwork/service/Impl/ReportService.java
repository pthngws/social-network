package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.request.ReportRequest;
import com.phithang.mysocialnetwork.entity.ReportEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.PostRepository;
import com.phithang.mysocialnetwork.repository.ReportRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public boolean  saveReport(ReportRequest reportRequest)
    {

        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setPost(postRepository.findById(reportRequest.getPostId()).orElse(null));
        reportEntity.setReportDate(LocalDateTime.now());
        reportEntity.setTitle(reportRequest.getTitle());
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity author = userRepository.findByEmail(authentication.getName());
        reportEntity.setUser(author);
        return reportRepository.save(reportEntity) != null;
    }
}
