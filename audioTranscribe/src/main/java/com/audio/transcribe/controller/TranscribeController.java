package com.audio.transcribe.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/transcribe")
public class TranscribeController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    // Spring automatically injects the model
    public TranscribeController(OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    @PostMapping
    public ResponseEntity<String> transcribeAudio(
            @RequestParam("file") MultipartFile file) throws IOException {

        File tempFile = File.createTempFile("audio", ".wav");

        try {
            file.transferTo(tempFile);

            OpenAiAudioTranscriptionOptions transcriptionOptions =
                    OpenAiAudioTranscriptionOptions.builder()
                            .language("en")
                            .prompt("Ask not this, but ask that")
                            .temperature(0f)
                            .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                            .build();


            var audioFile = new FileSystemResource(tempFile);
            AudioTranscriptionPrompt transcriptionRequest =
                    new AudioTranscriptionPrompt(audioFile, transcriptionOptions);

            AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);

            return new ResponseEntity<>(response.getResult().getOutput(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // Always delete temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}