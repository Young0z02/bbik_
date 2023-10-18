package com.example.bb1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.bb1.EventDecorator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.HashSet;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private DatabaseReference databaseReference;
    private BottomNavigationView bottomNavigationView;
    private TextView mappingIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        calendarView = findViewById(R.id.materialCalendar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        mappingIdTextView = findViewById(R.id.sideeffect);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.Calendar);


        // Firebase에서 sideeffectIn 데이터 가져오기
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child("sideeffectIn");

        // CalendarView의 날짜 선택 리스너 설정
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // 선택된 날짜 정보 가져오기
                int year = date.getYear();
                int month = date.getMonth() + 1; // 월은 0부터 시작하므로 1을 더해줍니다.
                int day = date.getDay();

                // 선택된 날짜를 date 형식으로 변환
                String formattedDate = String.format("%04d-%02d-%02d", year, month, day);

                // Firebase에서 데이터 필터링 및 표시
                databaseReference.orderByChild("date").equalTo(formattedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("FirebaseData", "DataSnapshot: " + dataSnapshot.toString()); // 로그 추가

                        StringBuilder sb = new StringBuilder("");

                        boolean hasSideEffect = false; // 부작용 여부를 나타내는 플래그

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            boolean checkbox1 = snapshot.child("checkbox1").getValue(Boolean.class);
                            boolean checkbox2 = snapshot.child("checkbox2").getValue(Boolean.class);
                            boolean checkbox3 = snapshot.child("checkbox3").getValue(Boolean.class);
                            boolean checkbox4 = snapshot.child("checkbox4").getValue(Boolean.class);
                            boolean checkbox5 = snapshot.child("checkbox5").getValue(Boolean.class);
                            boolean checkbox6 = snapshot.child("checkbox6").getValue(Boolean.class);
                            boolean checkbox7 = snapshot.child("checkbox7").getValue(Boolean.class);
                            boolean checkbox8 = snapshot.child("checkbox8").getValue(Boolean.class);

                            // checkbox 값이 true인 경우에 대한 처리
                            if (checkbox1) {
                                sb.append(" 소화장애, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox2) {
                                sb.append("발진, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox3) {
                                sb.append("두통 | 어지러움, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox4) {
                                sb.append("이명, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox5) {
                                sb.append("호흡곤란, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox6) {
                                sb.append("붓는 증상, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox7) {
                                sb.append("환각, ");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                            if (checkbox8) {
                                sb.append("발열 \n");
                                hasSideEffect = true; // 부작용이 있는 경우 플래그를 true로 설정
                            }
                        }

                        // 마지막에 ", " 부분 제거
                        if (sb.length() > 0) {
                            sb.delete(sb.length() - 2, sb.length());
                        }

                        // 부작용 정보를 TextView에 설정
                        if (hasSideEffect) {
                            mappingIdTextView.setText(sb.toString());
                        } else {
                            mappingIdTextView.setText("부작용이 없습니다.");
                        }

                        // 부작용 여부에 따라 날짜 색상 설정
                        HashSet<CalendarDay> datesWithSideEffect = new HashSet<>();
                        datesWithSideEffect.add(date);
                        if (hasSideEffect) {
                            calendarView.addDecorator(new EventDecorator(Color.BLUE, datesWithSideEffect, getSharedPreferences("MyPrefs", MODE_PRIVATE)));
                        } else {
                            calendarView.removeDecorator(new EventDecorator(Color.BLUE, datesWithSideEffect, getSharedPreferences("MyPrefs", MODE_PRIVATE)));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // 데이터 검색 중 오류 발생 시 처리
                    }
                });
            }
        });

        // BottomNavigationView의 선택 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(CalendarActivity.this, Main.class));
                        return true;

                    case R.id.Calendar:
                        // 이미 CalendarActivity를 실행 중인 경우 다시 시작하지 않도록 처리
                        startActivity(new Intent(CalendarActivity.this, CalendarActivity.class));
                        finish(); // 현재 액티비티 종료
                        return true;

                    case R.id.ocr:
                        startActivity(new Intent(CalendarActivity.this, Ocr.class));
                        return true;

                    case R.id.mypage:
                        startActivity(new Intent(CalendarActivity.this, Mypage.class));
                        return true;

                    default:
                        return false;
                }
            }
        });
    }
}
