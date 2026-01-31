package whoreads.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String senderEmail;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    // 인증 코드 발송
    public void sendVerificationCode(String email) {
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);   // 6자리 난수

        // Redis에 5분 저장(Key:Value = email:code)
        redisTemplate.opsForValue().set("CHECK_" + email, code, Duration.ofMinutes(5));

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(email);
        message.setSubject("[WoReads] 인증번호입니다.");
        message.setText("인증번호: " + code);
        mailSender.send(message);
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get("CHECK_" + email);

        if (code.equals(savedCode)) {
            // 검증 성공시 인증된 상태를 레디스에 다시 저장 (회원가입 버튼 클릭시 확인용)
            redisTemplate.opsForValue().set("VERIFIED_" + email, "DONE", Duration.ofMinutes(10));
            return true;
        }

        return false;
    }

    // 회원가입시 최종 확인
    public boolean isVerified(String email) {
        return "DONE".equals(redisTemplate.opsForValue().get("VERIFIED_" + email));
    }
}
