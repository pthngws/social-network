package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.ReportRequest;

public interface IReportService {
    boolean  saveReport(ReportRequest reportRequest);
}
