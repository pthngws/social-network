package com.phithang.mysocialnetwork.config;

import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    ApplicationRunner init(UserRepository userRepository){
        return args -> {
            if(userRepository.findByEmail("admin@admin")==null)
            {
                UserEntity userDto = new UserEntity();
                userDto.setEmail("admin@admin");
                userDto.setPassword(passwordEncoder.encode("admin@admin"));
                userDto.setRole("ADMIN");
                userRepository.save(userDto);
                log.warn("Admin created");
            }
        };
    }
}
