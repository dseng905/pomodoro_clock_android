package com.example.pomodoro_clock.pomodoro

import android.os.CountDownTimer
import android.os.health.TimerStat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

const val MINUTE = 60000L
const val SECOND = 1000L

enum class TimerStatus(val status : String) {
    WORK_TIME("Work"),
    BREAK_TIME("Break"),
    STOPPED(""),
    FINISHED("Finished!")
}

class PomodoroViewModel : ViewModel() {

    private lateinit var timer : CountDownTimer
    private var timerPaused = true
    private var timerInSession = false

    // Session Tracker
    private var _sessions = MutableLiveData<Int>(3)
    val sessions : LiveData<Int>
        get() = _sessions

    private var _currentSession = MutableLiveData<Int>(0)
    val currentSession : LiveData<Int>
        get() = _currentSession
    var currentSessionString : LiveData<String> = Transformations.map(_currentSession){currentSession ->
        if(currentSession == 0) {
            " "
        }
        else {
            "Session: $currentSession / $maxSessionsSet"
        }
    }

    private var maxSessionsSet = 3

    //Timer Status
    private var _timerStatus = MutableLiveData<TimerStatus>(TimerStatus.STOPPED)
    var timerStatus = Transformations.map(_timerStatus) {
        it.status
    }

    //Work Time
    private var _timeSet = MutableLiveData<Long>(25*MINUTE)
    val timeSet : LiveData<Long>
        get() = _timeSet
    var timeSetString = Transformations.map(_timeSet) {
        getTimeString(it)
    }

    //Break Time
    private var _breakTime = MutableLiveData<Long>(3*MINUTE)
    val breakTime : LiveData<Long>
        get() = _breakTime
    val breakTimeString = Transformations.map(_breakTime) {
        getTimeString(it)
    }

    //Current Time. Can be work time or break time
    private var _currentTime = MutableLiveData<Long>(25*MINUTE)
    val currentTime : LiveData<Long>
        get() = _currentTime
    var currentTimeString = Transformations.map(_currentTime) {
        getTimeString(it)
    }



    init {
        timer = createTimer(_currentTime.value!!)
    }

    //Set Work Time Length
    fun increaseTimeByOneMinute() {
        if(_timeSet.value!! < 1000*MINUTE) {
            _timeSet.value = _timeSet.value!! + MINUTE
        }
        setTime()
    }

    fun decreaseTimeByOneMinute() {
        if(_timeSet.value!! > MINUTE) {
            _timeSet.value = _timeSet.value!! - MINUTE
        }
        setTime()
    }

    private fun setTime() {
        if(!timerInSession) {
            _currentTime.value = _timeSet.value!!
        }
    }

    //Set Break Time Length
    fun increaseBreakTimeByOneMinute() {
        if(_breakTime.value!! < 1000*MINUTE)
            _breakTime.value = _breakTime.value!! + MINUTE
    }
    fun decreaseBreakTimeByOneMinute() {
        if(_breakTime.value!! > MINUTE)
            _breakTime.value = _breakTime.value!! - MINUTE
    }

    //Set number of sessions
    fun incrementSessions() {
        if(_sessions.value!! < 1000) {
            _sessions.value = _sessions.value!! + 1
            maxSessionsSet = _sessions.value!!
        }
    }

    fun decrementSessions() {
        if(_sessions.value!! > 1) {
            _sessions.value = _sessions.value!! - 1
            maxSessionsSet = _sessions.value!!
        }
    }


    //Start the timer by creating a new instance of CountdownTimer, since it does not provide a way
    //to pause it
    fun startTimer() {
        //Resume the current timer session or begin a new instance
        if(timerPaused) {
            timerInSession = true
            timerPaused = false
            timer = createTimer(_currentTime.value!!).start()
        }

        //Restart the timer values if the timer was stopped or completed
        if(_timerStatus.value == TimerStatus.STOPPED || _timerStatus.value == TimerStatus.FINISHED) {
            _timerStatus.value = TimerStatus.WORK_TIME
            _currentSession.value = 1
            maxSessionsSet = _sessions.value!!
        }
    }

    fun pauseTimer() {
        //Since CountDownTimer can't be paused, we cancel it and save the time instead.
        if(!timerPaused) {
            timerPaused = true
            timer.cancel()
        }
    }


    private fun createTimer(time : Long) : CountDownTimer {
        _currentTime.value = time
        return object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished
            }
            override fun onFinish() {
                // Here, we configure the timer based on the timer status and current session

                // Go to break time from the end of work time if we are not in the last session
                if(_timerStatus.value == TimerStatus.WORK_TIME
                    && _currentSession.value!! < maxSessionsSet) {
                    _timerStatus.value = TimerStatus.BREAK_TIME
                    timer = createTimer(_breakTime.value!!).start()
                }
                // Go to work time from break time if we are going to the last session
                else if (_timerStatus.value == TimerStatus.BREAK_TIME
                    && _currentSession.value!! < maxSessionsSet) {
                    _timerStatus.value = TimerStatus.WORK_TIME
                    _currentSession.value = _currentSession.value!! + 1
                    timer = createTimer(_timeSet.value!!).start()
                }
                // Stop the timer if all sessions are completed
                else {
                    _timerStatus.value = TimerStatus.FINISHED
                    _currentTime.value = _timeSet.value!!
                    timerInSession = false
                    _currentSession.value = 0
                }
            }
        }
    }

    //Format the time from milliseconds to minutes-seconds format
    fun getTimeString(time : Long) : String {
        val minutes : Long = time / MINUTE
        val seconds : Long = (time - (minutes*MINUTE))/SECOND

        val secondStr : String = if(seconds < 10) {
            "0$seconds"
        } else {
            "$seconds"
        }

        return "$minutes : $secondStr"
    }
}