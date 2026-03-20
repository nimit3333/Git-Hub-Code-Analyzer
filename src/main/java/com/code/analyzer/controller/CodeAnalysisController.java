package com.code.analyzer.controller;

import com.code.analyzer.dto.CodeRequest;
import com.code.analyzer.service.CodeAnalysisService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
@AllArgsConstructor
@CrossOrigin(origins = {
        "chrome-extension://keafhfoeinjobhmcejimmnceebkfibgi",
        "https://github.com",
        "http://127.0.0.1:8080/api/code/analyze"
})
public class CodeAnalysisController {

    private final CodeAnalysisService service;

    // 🔥 Limit size (important for security + performance)
    private static final int MAX_CODE_LENGTH = 3000;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCode(@RequestBody CodeRequest request){

        // ✅ 1. Null check
        if (request == null || request.getCode() == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid request: code is required");
        }

        String code = request.getCode();

        // ✅ 2. Empty check
        if (code.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Code cannot be empty");
        }

        // ✅ 3. Size limit check (VERY IMPORTANT)
        if (code.length() > MAX_CODE_LENGTH) {
            return ResponseEntity
                    .badRequest()
                    .body("Code too large. Max allowed: " + MAX_CODE_LENGTH + " characters");
        }

        try {

            // ✅ 4. Process safely
            String result = service.analyzeCode(String.valueOf(request));

            // ✅ 5. Return structured response
            return ResponseEntity.ok(result);

        } catch (Exception e) {

            // ✅ 6. Error handling (no internal leak)
            return ResponseEntity
                    .internalServerError()
                    .body("Something went wrong while analyzing code");
        }
    }
}