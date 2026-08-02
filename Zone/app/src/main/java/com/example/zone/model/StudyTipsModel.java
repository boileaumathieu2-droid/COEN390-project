package com.example.zone.model;


import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.example.zone.R;

import java.util.Random;

public class StudyTipsModel {
    public String[] physicalWellnessTips = {"Exercise at least 30 minutes per day. Regular exercise will boost your daily energy levels, allowing you to be more productive.", "Stay hydrated. Drink plenty of water", "Get 7-9 hours of sleep. Aim to be in bed by 10-11pm so that you can make the most of the next day. Sleep is vital for brain functions and your immune system.", "Eat healthy and well-balanced foods. A balanced meal should contain a mix of grains, fruits/vegetables and protein."};
    public String[] stressManagementTips = {"Take deep, slow breaths. Breathing will help calm your nervous system when you are feeling stressed.", "Avoid cramming by starting assignments or studies well ahead of the deadlines or exams.", "Break large tasks down into smaller, more manageable ones.", "Celebrate your progress. Taking time to celebrate on your accomplishments is a great way to keep your morale high.", "Take breaks during your study sessions. Stretch or go for a short walk but don't lose focus of your study goals. A commonly used technique called the Pomodoro method encourages studying for 25 minutes at a time followed by a 5 minute break."};
    public String[] examPrepTips = {"When studying for an exam, create a schedule. Allocate an appropriate number of days to study for each exam.", "Make sure you are well rested on the night before an exam. Studies show that good night's rest before an exam will increase your mental acuity and generally improves exam performance.", "Test yourself. Do any practice questions given by the lecturer and redo previous assessments for the subject. Try to do these without looking at the solutions.", "Some people study better while listening to music. Some study better when studying with a group. See what works for you specifically."};

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private final int delay = 45000;
    Random random = new Random();
    private int category;

    private String randomPhysicalWellnessTip(){
        Random random = new Random();
        int randomIndex = random.nextInt(physicalWellnessTips.length);
        return physicalWellnessTips[randomIndex];
    }
    private String randomStressManagementTip(){
        Random random = new Random();
        int randomIndex = random.nextInt(stressManagementTips.length);
        return stressManagementTips[randomIndex];
    }

    private String randomExamPrepTip(){
        Random random = new Random();
        int randomIndex = random.nextInt(examPrepTips.length);
        return examPrepTips[randomIndex];
    }

    public String randomTip(){
       category = random.nextInt(3);
       switch(category){
           case 0:
               return randomPhysicalWellnessTip();
           case 1:
               return randomStressManagementTip();
           case 2:
               return randomExamPrepTip();
       }
            return "aaa";

    }


}
