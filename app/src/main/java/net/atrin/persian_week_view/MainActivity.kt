package net.atrin.persian_week_view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.atrin.persian_week_view_library.WeekView
import net.atrin.persian_week_view_library.WeekViewEvent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val week= findViewById<WeekView>(R.id.weekView)
        week.setWeekViewLoader(object : WeekView.WeekViewLoader{
            override fun onWeekViewLoad(): List<WeekViewEvent> {
                return ArrayList()
            }
        })
    }
}