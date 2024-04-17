package com.example.bb1;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
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
        TextView reAlarmTextView = findViewById(R.id.re_alarm);

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

        reAlarmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 알람 재설정 팝업 표시
                showAlarmResetPopup();
            }
        });

        TextView medCalTextView = findViewById(R.id.med_cal);

        medCalTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference medInfoReference = FirebaseDatabase.getInstance().getReference("user/medicationP");

                medInfoReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // 데이터를 받아 TextView에 보여줄 문자열을 생성
                        StringBuilder dataToShow = new StringBuilder();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String time = snapshot.child("time").getValue(String.class);

                            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            if (currentDate.equals(time)) {
                                // 만약 현재 날짜가 time과 일치한다면 해당 데이터를 StringBuilder에 추가
                                String daysC = snapshot.child("daysC").getValue(String.class);
                                String daysNo = snapshot.child("daysNo").getValue(String.class);
                                String daysTotal = snapshot.child("daysTotal").getValue(String.class);
                                String medicationNo = snapshot.child("medicationNo").getValue(String.class);
                                String medicineNo = snapshot.child("medicineNo").getValue(String.class);
                                String startD = snapshot.child("startD").getValue(String.class);

                                dataToShow.append("일수: ").append(daysC).append("\n")
                                        .append("횟수: ").append(daysNo).append("\n")
                                        .append("총 횟수: ").append(daysTotal).append("\n")
                                        .append("약 번호: ").append(medicineNo).append("\n")
                                        .append("시작일: ").append(startD).append("\n");
                            }
                        }

                        // AlertDialog를 통해 팝업 창에 데이터 표시
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                        builder.setTitle("약 정보 확인하기");
                        builder.setMessage(dataToShow.toString());
                        builder.setPositiveButton("닫기", null);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // 에러 처리
                    }
                });
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
                    alarmSleepingTextView.setText("취침 : " + dataSnapshot.child("medicationS").getValue(String.class));

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


    private void showAlarmResetPopup() {
        // 팝업창을 열기 위한 코드
        BottomSheetDialog alarmResetDialog = new BottomSheetDialog(this);
        View alarmResetView = getLayoutInflater().inflate(R.layout.alarm_reset_popup, null);
        alarmResetDialog.setContentView(alarmResetView);

        // Firebase에서 기존 알람 시간을 가져와 TextView에 설정하는 로직
        TextView morningAlarmTextView = alarmResetView.findViewById(R.id.morning_alarm_reset);
        TextView lunchAlarmTextView = alarmResetView.findViewById(R.id.lunch_alarm_reset);
        TextView dinnerAlarmTextView = alarmResetView.findViewById(R.id.dinner_alarm_reset);
        TextView sleepAlarmTextView = alarmResetView.findViewById(R.id.sleep_alarm_reset);

        morningAlarmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MaterialTimePicker를 이용해 아침 알람 설정
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(0)
                        .setMinute(0)
                        .setTitleText("아침 알람 설정")
                        .build();


                timePicker.show(getSupportFragmentManager(), "Timepickerdialog");

                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 사용자가 시간을 선택하면 Firebase에 알람 시간 업데이트
                        int hourOfDay = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        String newAlarmTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference().child("user").child("timing").child("-NfvO2lRttUFdEkQbZGr");
                        alarmRef.child("medicationM").setValue(newAlarmTime);
                    }
                });
            }
        });

        lunchAlarmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(0)
                        .setMinute(0)
                        .setTitleText("점심 알람 설정")
                        .build();

                timePicker.show(getSupportFragmentManager(), "Timepickerdialog");

                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int hourOfDay = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        String newAlarmTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference().child("user").child("timing").child("-NfvO2lRttUFdEkQbZGr");
                        alarmRef.child("medicationL").setValue(newAlarmTime);
                    }
                });
            }
        });

        dinnerAlarmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(0)
                        .setMinute(0)
                        .setTitleText("저녁 알람 설정")
                        .build();

                timePicker.show(getSupportFragmentManager(), "Timepickerdialog");

                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int hourOfDay = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        String newAlarmTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference().child("user").child("timing").child("-NfvO2lRttUFdEkQbZGr");
                        alarmRef.child("medicationD").setValue(newAlarmTime);
                    }
                });
            }
        });

        sleepAlarmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(0)
                        .setMinute(0)
                        .setTitleText("취침 전 알람 설정")
                        .build();

                timePicker.show(getSupportFragmentManager(), "Timepickerdialog");

                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int hourOfDay = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        String newAlarmTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference().child("user").child("timing").child("-NfvO2lRttUFdEkQbZGr");
                        alarmRef.child("medicationS").setValue(newAlarmTime);
                    }
                });
            }
        });

        alarmResetDialog.show(); // 알람 재설정 팝업창을 보여줍니다.
    }

}
