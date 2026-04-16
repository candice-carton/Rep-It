package project.repit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import project.repit.MainActivity
import project.repit.model.data.AppDatabase
import project.repit.model.domain.model.RoutineHistory
import project.repit.model.data.RoutineRepository

/**
 * Service gérant le minuteur des défis en arrière-plan.
 */
class TimerService : Service() {

    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var totalDurationInMillis: Long = 0
    private var isRunning = false
    private var routineId = ""
    private var routineName = ""

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: RoutineRepository

    companion object {
        const val CHANNEL_ID = "TimerServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_NAME = "EXTRA_NAME"
        const val EXTRA_ID = "EXTRA_ID"
    }

    override fun onCreate() {
        super.onCreate()
        // Initialisation du repository via la base de données Room
        val dao = AppDatabase.getDatabase(applicationContext).routineDao()
        repository = RoutineRepository(dao)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0)
                routineId = intent.getStringExtra(EXTRA_ID) ?: ""
                routineName = intent.getStringExtra(EXTRA_NAME) ?: "Défi en cours"
                startTimer(duration)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(duration: Long) {
        if (isRunning) return
        
        timeLeftInMillis = duration
        totalDurationInMillis = duration
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Démarrage..."))

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val progress = calculateProgress(millisUntilFinished)
                
                // Mise à jour de la progression en base de données
                updateProgressInDatabase(progress)
                
                // Mise à jour de la notification
                updateNotification(formatTime(millisUntilFinished))
            }

            override fun onFinish() {
                isRunning = false
                saveHistoryToDatabase(true) // Succès
                updateNotification("Défi terminé !")
                stopForeground(STOP_FOREGROUND_DETACH)
            }
        }.start()

        isRunning = true
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        updateNotification("En pause : ${formatTime(timeLeftInMillis)}")
    }

    private fun stopTimer() {
        timer?.cancel()
        isRunning = false
        saveHistoryToDatabase(false) // Échec/Interruption
        stopSelf()
    }

    private fun calculateProgress(millisUntilFinished: Long): Int {
        if (totalDurationInMillis == 0L) return 0
        return (((totalDurationInMillis - millisUntilFinished).toFloat() / totalDurationInMillis) * 100).toInt()
    }

    private fun updateProgressInDatabase(progress: Int) {
        if (routineId.isEmpty()) return
        serviceScope.launch {
            val routine = repository.getRoutineById(routineId)
            routine?.let {
                repository.upsert(it.copy(progress = progress))
            }
        }
    }

    private fun saveHistoryToDatabase(isCompleted: Boolean) {
        if (routineId.isEmpty()) return
        serviceScope.launch {
            val progress = if (isCompleted) 100 else calculateProgress(timeLeftInMillis)
            val history = RoutineHistory(
                routineId = routineId,
                durationMillis = totalDurationInMillis - timeLeftInMillis,
                isCompleted = isCompleted,
                progress = progress
            )
            repository.insertHistory(history)
            
            // Si le défi est fini avec succès, on s'assure que la routine affiche 100%
            if (isCompleted) {
                val routine = repository.getRoutineById(routineId)
                routine?.let {
                    repository.upsert(it.copy(progress = 100))
                }
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun buildNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(routineName)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Chronomètre de Défi",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
