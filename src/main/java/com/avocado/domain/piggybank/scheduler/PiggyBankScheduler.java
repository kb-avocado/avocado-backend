package com.avocado.domain.piggybank.scheduler;

import com.avocado.domain.piggybank.service.PiggyBankBonusService;
import com.avocado.domain.piggybank.service.PiggyBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 저금통 7일 달성 승격 및 보너스 재촉 알림 스케줄러
@Component
@RequiredArgsConstructor
public class PiggyBankScheduler {

    private final PiggyBankService piggyBankService;
    private final PiggyBankBonusService piggyBankBonusService;

    // 매일 자정 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 0 * * *")
    public void promoteAchievements() {
        piggyBankService.promoteAchievements();
    }

    // 매일 09시 실행: 보너스 미지급 저금통에 재촉 알림 발송
    @Scheduled(cron = "0 0 9 * * *")
    public void remindUnpaidBonuses() {
        piggyBankBonusService.sendBonusReminders();
    }
}