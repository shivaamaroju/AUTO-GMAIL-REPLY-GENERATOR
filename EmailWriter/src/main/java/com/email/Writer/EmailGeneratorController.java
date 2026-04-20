package com.email.Writer;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin("*")
public class EmailGeneratorController {
    private final EmailGeneratorService emailGeneratorService;

@PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest email) {
    String response =emailGeneratorService.generateEmailReply(email);
    return ResponseEntity.ok(response);

    }
}
