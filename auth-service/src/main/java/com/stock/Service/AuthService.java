package com.stock.Service;


import com.stock.Dto.LoginRequest;
import com.stock.Dto.LoginResponse;
import com.stock.Entity.SysUser;
import com.stock.Respority.SysUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            SysUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() ->
                        new IllegalArgumentException("用户名或密码错误"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        boolean matched = passwordEncoder.matches(
                request.password(),
                user.getPassword()
        );

        if (!matched) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtService.createToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                1800L
        );
    }
}
