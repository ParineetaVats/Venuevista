package com.venuevista.controller;

import com.venuevista.dto.ContactMessageRequest;
import com.venuevista.entity.ContactMessage;
import com.venuevista.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseEntity<Void> submitContactMessage(@Valid @RequestBody ContactMessageRequest request) {
        ContactMessage message = new ContactMessage(request.name().trim(), request.email().trim().toLowerCase(), request.message().trim());
        contactMessageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
