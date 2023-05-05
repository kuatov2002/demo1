package com.example.demo.service;

import com.example.demo.Repo.BillboardRepo;
import com.example.demo.models.Billboard;
import org.aspectj.bridge.MessageUtil;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class BillboardService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BillboardRepo billboardRepo;

    public BillboardService(BillboardRepo billboardRepo) {
        this.billboardRepo = billboardRepo;
    }
    @Scheduled(cron = "*/5 * * * * *")
    public void updateExpiredStatus() {
        System.out.println("My scheduled task is running...");
        logger.info("Scheduled method started...");
    }
}
