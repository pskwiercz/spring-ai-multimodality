package com.pskwiercz.smultimodality.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class AudioController {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;

    public AudioController(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel,
                           OpenAiAudioSpeechModel openAiAudioSpeechModel) {
        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
        this.openAiAudioSpeechModel = openAiAudioSpeechModel;
    }

    @GetMapping("/transcribe")
    public String transcribe(@Value("classpath:SpringAI.mp3") Resource audioFile) {
        return openAiAudioTranscriptionModel.call(audioFile);
    }

    @GetMapping("/transcribe-options")
    String transcribeWithOptions(@Value("classpath:SpringAI.mp3") Resource audioFile) {
        var audioTranscriptionResponse = openAiAudioTranscriptionModel.call(new AudioTranscriptionPrompt(
                audioFile, OpenAiAudioTranscriptionOptions.builder()
                .prompt("Talking about Spring AI")
                .language("en")
                .temperature(0.5f)
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.VTT).build()));
        return audioTranscriptionResponse.getResult().getOutput();
    }

    @GetMapping("/speech")
    public String speech(@RequestParam("msg")  String msg) throws IOException {
        byte[] bytes = openAiAudioSpeechModel.call(msg);
        Path path = Paths.get("output.mp3");
        Files.write(path, bytes);
        return "MP3 file saved";
    }

    @GetMapping("/speech-options")
    String spechWithOptions(@RequestParam("message") String message) throws IOException {
        TextToSpeechResponse speechResponse = openAiAudioSpeechModel
                .call(new TextToSpeechPrompt(message,
                        OpenAiAudioSpeechOptions.builder()
                                .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
                                .speed(2.0)
                                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                                .build()));
        Path path = Paths.get("speech.mp3");
        Files.write(path, speechResponse.getResult().getOutput());
        return "MP3 file saved";
    }
}
