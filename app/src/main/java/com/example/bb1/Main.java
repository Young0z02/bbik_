package com.example.bb1;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



public class Main extends AppCompatActivity {

    private static final String DATABASE_PATH = "user/sideeffectIn"; // 데이터베이스 경로

    private TextView alarmMorningTextView, alarmLunchingTextView, alarmDinneringTextView, alarmSleepingTextView;
    private DatabaseReference alarmsReference;
    private View mapping_id;
    private TextView bodyTextView;
    private BottomSheetDialog bottomSheetDialog;
    private TextView todayTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //firebase 연결
        FirebaseApp.initializeApp(this);

        Button showBottomSheetButton = findViewById(R.id.showBottomSheetButton);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bodyTextView = findViewById(R.id.__p______body_2);
        todayTextView = findViewById(R.id.today);
        bottomSheetDialog = new BottomSheetDialog(this, R.style.RoundCornerBottomSheetDialogTheme);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // TextView에 현재 날짜 설정
        todayTextView.setText("오늘 날짜: " + currentDate);

        bodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        showBottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet();
            }

        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(Main.this, Main.class));
                        return true;

                    case R.id.Calendar:
                        startActivity(new Intent(Main.this, CalendarActivity.class));
                        return true;

                    case R.id.ocr:
                        startActivity(new Intent(Main.this, Ocr.class));
                        return true;

                    case R.id.mypage:
                        startActivity(new Intent(Main.this, Mypage.class));
                        return true;

                    default:
                        return false;
                }
            }
        });

        // Firebase에서 알람 데이터를 읽어올 경로 설정
        alarmsReference = FirebaseDatabase.getInstance().getReference().child("user").child("timing").child("-NfvO2lRttUFdEkQbZGr");

        // TextView 초기화
        alarmMorningTextView = findViewById(R.id.morningalarm);
        alarmLunchingTextView = findViewById(R.id.lunchalarm);
        alarmDinneringTextView = findViewById(R.id.dinneralarm);
        alarmSleepingTextView = findViewById(R.id.sleepalarm);

        mapping_id = findViewById(R.id.mapping_id);

        // Firebase에서 알람 데이터를 읽어오고 텍스트 뷰에 표시
        readAlarmsFromFirebase();
    }

    private void showBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.modal_bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Chip[] chips = new Chip[8];

        for (int i = 0; i < 8; i++) {
            Chip chip = chips[i] = bottomSheetView.findViewById(getResources().getIdentifier("checkbox" + (i + 1), "id", getPackageName()));
        }

        // 데이터베이스 업데이트 코드
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(DATABASE_PATH); // 데이터베이스 경로 설정

        // 새로운 데이터를 추가하려면 push() 메서드를 사용하여 고유한 키 생성
        DatabaseReference newChildRef = ref.push();

        bottomSheetDialog.show(); // 다이얼로그를 엽니다.

        Button saveButton = bottomSheetView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> updateData = new HashMap<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());

                for (int i = 0; i < 8; i++) {
                    String chipKey = "checkbox" + (i + 1);
                    boolean isChecked = chips[i].isChecked();
                    updateData.put(chipKey, isChecked);

                    // 업데이트가 발생하는 시점에 로그 메시지를 출력
                    Log.d("FirebaseUpdate", "Chip " + (i + 1) + " isChecked: " + isChecked);
                }

                updateData.put("date", currentDate);

                // 데이터베이스에 업데이트
                newChildRef.setValue(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Firebase 연결 확인용 토스트 메시지 표시
                        Toast.makeText(Main.this, "부작용 입력 완료", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss(); // 저장 버튼을 눌렀을 때 다이얼로그를 닫습니다.
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Firebase 업데이트 실패 처리
                        Toast.makeText(Main.this, "부작용 입력 실패", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss(); // 저장 버튼을 눌렀을 때 다이얼로그를 닫습니다.
                    }
                });
            }
        });
    }

    // Firebase에서 알람 데이터를 읽어오는 메서드
    private void readAlarmsFromFirebase() {
        alarmsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    alarmMorningTextView.setText("아침 : " + dataSnapshot.child("medicationM").getValue(String.class));
                    alarmLunchingTextView.setText("점심 : " + dataSnapshot.child("medicationL").getValue(String.class));
                    alarmDinneringTextView.setText("저녁 : " + dataSnapshot.child("medicationD").getValue(String.class));
                    alarmSleepingTextView.setText("취침 전: " + dataSnapshot.child("medicationS").getValue(String.class));

                    setMorningAlarm(dataSnapshot.child("medicationM").getValue(String.class));
                    setLunchAlarm(dataSnapshot.child("medicationL").getValue(String.class));
                    setDinnerAlarm(dataSnapshot.child("medicationD").getValue(String.class));
                    setSleepAlarm(dataSnapshot.child("medicationS").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    // 아침 알람 설정
    void setMorningAlarm(String alarmTime) {
        // AlarmManager 초기화
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 알람 시간을 파싱하여 시간과 분을 추출
        String[] timeParts = alarmTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // 현재 시간을 가져옴
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // 알람 시간 설정
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 이미 지난 시간이면 다음 날로 설정
        if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 알람 인텐트 생성
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 알람 설정
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }

    // 점심 알람 설정
    void setLunchAlarm(String alarmTime) {
        // AlarmManager 초기화
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 알람 시간을 파싱하여 시간과 분을 추출
        String[] timeParts = alarmTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // 현재 시간을 가져옴
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // 알람 시간 설정
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 이미 지난 시간이면 다음 날로 설정
        if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 알람 인텐트 생성
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 알람 설정
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    // 저녁 알람 설정
    void setDinnerAlarm(String alarmTime) {
        // AlarmManager 초기화
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 알람 시간을 파싱하여 시간과 분을 추출
        String[] timeParts = alarmTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // 현재 시간을 가져옴
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // 알람 시간 설정
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 이미 지난 시간이면 다음 날로 설정
        if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 알람 인텐트 생성
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 3, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 알람 설정
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    // 잠자기 전 알람 설정
    void setSleepAlarm(String alarmTime) {
        // AlarmManager 초기화
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 알람 시간을 파싱하여 시간과 분을 추출
        String[] timeParts = alarmTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // 현재 시간을 가져옴
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // 알람 시간 설정
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 이미 지난 시간이면 다음 날로 설정
        if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 알람 인텐트 생성
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 4, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 알람 설정
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
