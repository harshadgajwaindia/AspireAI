package com.AspireAI.backend.analyzer.service;

import com.AspireAI.backend.analyzer.exception.AnalyzerException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
public class ResumeParserService {

    private static final Tika tika = new Tika();

    
    private static final int MAX_CHARS = 5000;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );

    public String extractText(MultipartFile file) {
        validateFile(file);
        try {
            String raw = tika.parseToString(file.getInputStream());
            String cleaned = cleanText(raw);

            if (cleaned.length() < 100) {
                throw new AnalyzerException("Resume appears to be empty or unreadable. "
                        + "Please upload a text-based PDF, not a scanned image.");
            }

            
            return cleaned.length() > MAX_CHARS
                    ? cleaned.substring(0, MAX_CHARS)
                    : cleaned;

        } catch (IOException | TikaException e) {
            throw new AnalyzerException("Failed to read resume file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AnalyzerException("Resume file is required.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new AnalyzerException("Only PDF and DOCX files are supported.");
        }
        if (file.getSize() > 5 * 1024 * 1024) { 
            throw new AnalyzerException("File size must be under 5MB.");
        }
    }

    private String cleanText(String raw) {
        return raw
                .replaceAll("(?i)page\\s+\\d+\\s*(of\\s*\\d+)?", "")
                .replaceAll("(\r?\n){3,}", "\n\n")
                .replaceAll("[^\\x09\\x0A\\x0D\\x20-\\x7E]", " ")
                .trim();
    }
}