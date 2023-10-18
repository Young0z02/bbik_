package com.example.bb1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Mapping extends AppCompatActivity {

    private Button morningButton, lunchButton, dinnerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapping);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.ocr);

        // Firebase에서 부작용 데이터를 읽어오고 텍스트 뷰에 표시 (수정 및 추가)
        readSideEffectDataFromFirebase();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(Mapping.this, Main.class));
                        return true;

                    case R.id.Calendar:
                        // Mapping 액티비티를 종료하고 다시 엽니다.
                        finish(); // 현재 액티비티 종료
                        startActivity(new Intent(Mapping.this, CalendarActivity.class)); // Mapping 액티비티 다시 열기
                        return true;


                    case R.id.ocr:
                        startActivity(new Intent(Mapping.this, Ocr.class));
                        return true;

                    case R.id.mypage:
                        startActivity(new Intent(Mapping.this, Mypage.class));
                        return true;

                    default:
                        return false;
                }
            }
        });

        morningButton = findViewById(R.id.morningButton);
        lunchButton = findViewById(R.id.lunchButton);
        dinnerButton = findViewById(R.id.dinnerButton);

        morningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog("내복약");
            }
        });

        lunchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog("서랍형");
            }
        });

        dinnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog("휴대용");
            }
        });
    }

    private void showMedicationDialog(String time) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(time);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_medication, null);
        builder.setView(dialogView);

        CheckBox morningCheckbox = dialogView.findViewById(R.id.morningCheckbox);
        CheckBox lunchCheckbox = dialogView.findViewById(R.id.lunchCheckbox);
        CheckBox dinnerCheckbox = dialogView.findViewById(R.id.dinnerCheckbox);
        CheckBox bedtimeCheckbox = dialogView.findViewById(R.id.bedtimeCheckbox);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean morningChecked = morningCheckbox.isChecked();
                boolean lunchChecked = lunchCheckbox.isChecked();
                boolean dinnerChecked = dinnerCheckbox.isChecked();
                boolean bedtimeChecked = bedtimeCheckbox.isChecked();

                updateFirebaseData(time, morningChecked, lunchChecked, dinnerChecked, bedtimeChecked);

                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateFirebaseData(String time, boolean morningChecked, boolean lunchChecked, boolean dinnerChecked, boolean bedtimeChecked) {
        String databasePath = "user/sideeffectP"; // Firebase 경로 변경

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(databasePath).child(time);

        // 선택한 항목을 배열로 저장
        ArrayList<String> selectedItems = new ArrayList<>();
        if (morningChecked) {
            selectedItems.add("아침");
        }
        if (lunchChecked) {
            selectedItems.add("점심");
        }
        if (dinnerChecked) {
            selectedItems.add("저녁");
        }
        if (bedtimeChecked) {
            selectedItems.add("잠자기 전");
        }

        // 배열을 Firebase에 업데이트
        ref.child("selectedItems").setValue(selectedItems).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Mapping.this, "복약기 연결 성공", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Mapping.this, "복약기 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readSideEffectDataFromFirebase() {
        DatabaseReference sideeffectReference = FirebaseDatabase.getInstance().getReference().child("user").child("sideeffectP");

        sideeffectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // "내복약," "약통," "서랍형" 경로에서 데이터 가져오기
                    DataSnapshot 내복약Snapshot = dataSnapshot.child("내복약");
                    DataSnapshot 약통Snapshot = dataSnapshot.child("휴대용");
                    DataSnapshot 서랍형Snapshot = dataSnapshot.child("서랍형");

                    // TextView에 표시할 문자열 초기화
                    String textToDisplay = "현재 복약 사용 중인 복약기 : \n\n" + "";

                    if (내복약Snapshot.exists()) {
                        textToDisplay += "내복약 +";
                    }
                    if (약통Snapshot.exists()) {
                        textToDisplay += "휴대용 +";
                    }
                    if (서랍형Snapshot.exists()) {
                        textToDisplay += "서랍형";
                    }

                    // Update TextView
                    TextView mapping_id = (TextView) findViewById(R.id.mapping_id);
                    mapping_id.setText(textToDisplay);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }
}
