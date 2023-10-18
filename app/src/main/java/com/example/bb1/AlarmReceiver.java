package com.example.bb1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Alarm이 울릴 때 호출되는 메서드
        Log.d("AlarmReceiver", "알람이 울림");

        // TTS 초기화
        textToSpeech = new TextToSpeech(context, this);

        // 토스트 메시지 표시
        Toast.makeText(context, "알람이 울립니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS 초기화에 성공한 경우

            // TTS 언어 설정 (한국어)
            int result = textToSpeech.setLanguage(Locale.KOREAN);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

            } else {
                // TTS로 읽어줄 텍스트 생성
                String timeToSpeak = "약 드실 시간입니다.";

                // TTS로 텍스트 읽어주기
                textToSpeech.speak(timeToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {

        }
    }
}
