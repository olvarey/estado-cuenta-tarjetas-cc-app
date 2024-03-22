package com.bancoazul.estados.cuenta.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EstadosCuentaTarjetasCCSchedule {
    // Method
    // To trigger the scheduler every one minute
    // between 19:00 PM to 19:59 PM
    @Scheduled(cron = "0 * 10 * * ?")
    public void scheduleTask() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        String strDate = dateFormat.format(new Date());
        log.info("Cron job Scheduler: Job running at: {}", strDate);
    }
}
