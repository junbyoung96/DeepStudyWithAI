package me.deepStudyWithAI.service;

import lombok.RequiredArgsConstructor;
import me.deepStudyWithAI.domain.Member;
import me.deepStudyWithAI.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다: " + email));
    }

    @Transactional
    public Long save(String email, String password) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
                
        return memberRepository.save(member).getId();
    }
}
