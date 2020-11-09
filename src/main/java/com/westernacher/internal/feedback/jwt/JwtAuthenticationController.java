package com.westernacher.internal.feedback.jwt;

import com.westernacher.internal.feedback.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/{tenantId}/user")
@CrossOrigin
@Slf4j
public class JwtAuthenticationController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PersonRepository personRepository;

    @PostMapping(value = "/jwt")
    protected ResponseEntity<String> createAuthenticationToken(@RequestBody String email) throws Exception{
        return ResponseEntity.ok(jwtTokenUtil.generateToken(email));
    }

    @PostMapping(value = "/validatetoken")
    protected ResponseEntity<?> validateToken(@RequestParam String token) throws Exception {

        String email = null;
        try {
            email = jwtTokenUtil.extractUser(token);
        } catch (Exception e) {
            return new ResponseEntity<String>("invalid token", HttpStatus.UNAUTHORIZED);
        }

        if(jwtTokenUtil.isTokenExpired(token)) {
            return new ResponseEntity<String>("Token Expired ", HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(email);
    }
}
