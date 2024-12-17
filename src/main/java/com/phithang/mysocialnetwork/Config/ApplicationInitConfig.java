package com.phithang.mysocialnetwork.Config;

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
            if(userRepository.findByEmail("admin")==null)
            {
                UserEntity userDto = new UserEntity();
                userDto.setEmail("admin");
                userDto.setPassword(passwordEncoder.encode("1"));
                userDto.setRole("ADMIN");
                userRepository.save(userDto);
                log.warn("Admin created");
            }
        };
    }
}
