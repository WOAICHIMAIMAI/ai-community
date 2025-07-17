package com.zheng.aicommunitybackend.dashscope;

import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AudioTranscriberTest {

    @Resource
    private AudioTranscriber audioTranscriber;

    @Test
    void transcribeAudio() {
        try {
            String audioUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3";
            String transcribedText = audioTranscriber.transcribeAudio(audioUrl);
            System.out.println("识别结果: " + transcribedText);
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.err.println("识别失败: " + e.getMessage());
        }
    }
}