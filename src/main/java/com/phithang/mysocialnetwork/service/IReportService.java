package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.ReportDto;

public interface IReportService {
    boolean  saveReport(ReportDto reportDto);
}
