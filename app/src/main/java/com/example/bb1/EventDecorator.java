package com.example.bb1;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

public class EventDecorator implements DayViewDecorator {
    private final int color;
    private final HashSet<CalendarDay> dates;
    private final SharedPreferences sharedPreferences; // SharedPreferences 추가
    private final String KEY_COLOR = "circle_color"; // 파란색 원의 색을 저장하는 키

    public EventDecorator(int color, Collection<CalendarDay> dates, SharedPreferences sharedPreferences) {
        this.color = color;
        this.dates = new HashSet<>(dates);
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        Drawable circleDrawable = getCircleDrawable();

        // 저장된 상태를 확인하여 색상 설정
        int storedColor = sharedPreferences.getInt(KEY_COLOR, Color.TRANSPARENT);
        if (storedColor == color) {
            view.setSelectionDrawable(circleDrawable);
        }
    }

    private Drawable getCircleDrawable() {
        ShapeDrawable circleDrawable = new ShapeDrawable(new OvalShape());
        circleDrawable.getPaint().setColor(color);
        return circleDrawable;
    }

    // 상태 저장 메서드
    public void saveState() {
        // 파란색 원의 색상을 저장
        sharedPreferences.edit().putInt(KEY_COLOR, color).apply();
    }
}
