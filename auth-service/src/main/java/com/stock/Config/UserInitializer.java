package com.stock.Config;

import com.stock.Entity.SysUser;
import com.stock.Respority.SysUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserInitializer {

    @Bean
    public CommandLineRunner initUser(
            SysUserRepository userRespority, PasswordEncoder passwordEncoder){
        return args -> {
            if (userRespority.findByUsername("admin").isEmpty()){
                SysUser user = new SysUser(
                        "admin",
                        passwordEncoder.encode("123"),
                        true,
                        "USER");
                userRespority.save(user);
            }
        };
    }
}
