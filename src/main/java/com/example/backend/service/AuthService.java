package com.example.backend.service;

import com.example.backend.domain.Account;
import com.example.backend.dto.auth.AuthResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RegisterRequest;
import com.example.backend.dto.auth.RegisterResponse;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.AccountRepository;
import com.example.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (accountRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "該手機號碼已被使用，請重新註冊");
        }
        if (accountRepository.existsByUserName(request.userName())) {
            throw new ApiException(HttpStatus.CONFLICT, "該使用者名稱已被使用，請重新註冊");
        }

        Account account = new Account();
        account.setPhoneNumber(request.phoneNumber());
        account.setUserName(request.userName());
        // 使用BCryptPasswordEncoder (PasswordEncoder) 加鹽和雜湊來加密密碼
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setRegistrationTime(OffsetDateTime.now());
        accountRepository.save(account);
        return new RegisterResponse(account.getUserId(), account.getUserName());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.userName(), request.password())
            );
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "登入失敗");
        }

        Account account = accountRepository.findByUserName(request.userName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "登入失敗"));
        account.setLastLoginTime(OffsetDateTime.now());
        String token = jwtService.generateToken(account.getUserId(), account.getUserName());
        return new AuthResponse(token, account.getUserName());
    }
}
