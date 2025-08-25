package com.blueing.sports_meet_system;

import com.blueing.sports_meet_system.service.imp.RtmpStreamService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SportsMeetSystemApplication {

    public static void main(String[] args) {SpringApplication.run(SportsMeetSystemApplication.class, args);
    }

}
