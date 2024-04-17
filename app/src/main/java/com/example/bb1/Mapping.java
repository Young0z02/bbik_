package com.example.bb1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private Button morningButton, lunchButton, lunchButton1, dinnerButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapping);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.ocr);

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
        lunchButton1 = findViewById(R.id.lunchButton1);
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
                showMedicationDialog("서랍형0");
            }
        });

        lunchButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog("서랍형1");
            }
        });

        dinnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog("약통");
            }
        });

        // 액티비티가 생성될 때 데이터를 설정
        updateFirebaseData("initial", true, true, true, true);
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
            selectedItems.add("취짐 전");
        }

        // 배열을 Firebase에 업데이트
        ref.child("selectedItems").setValue(selectedItems).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Mapping.this, "연결 성공", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Mapping.this, "연결 실패", Toast.LENGTH_SHORT).show();
            }
        });

        TextView medCalTextView = findViewById(R.id.mapping_id);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user/medicine");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder allMedicineNames = new StringBuilder();

                for (int i = 1; i <= 8; i++) {
                    String medicineNameKey = "medicineName" + i;
                    DataSnapshot medicineSnapshot = dataSnapshot.child(medicineNameKey);

                    if (medicineSnapshot.exists()) {
                        String medicineName = medicineSnapshot.getValue(String.class);

                        if (medicineName != null) {
                            Log.d("FirebaseData", "Medicine Name " + i + ": " + medicineName);
                            allMedicineNames.append("Medicine Name ").append(i).append(": ").append(medicineName).append("\n");
                        } else {
                            Log.e("FirebaseData", "Medicine Name " + i + " is null");
                        }
                    } else {
                        Log.e("FirebaseData", "Snapshot for Medicine Name " + i + " does not exist");
                    }
                }

                // 모든 Medicine Names를 TextView에 설정
                medCalTextView.setText(allMedicineNames.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 에러 처리
                Log.e("FirebaseData", "Database Error: " + databaseError.getMessage());
            }
        });
    }
}