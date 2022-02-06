package com.cornellappdev.volume.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cornellappdev.volume.OnboardingActivity
import com.cornellappdev.volume.R
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class NotificationService : FirebaseMessagingService() {

    enum class NotificationDataKeys(val key: String) {
        ARTICLE_ID("articleID"),
        ARTICLE_URL("articleURL"),
        NOTIFICATION_TYPE("notification_type")
    }

    enum class NotificationType(val type: String) {
        WEEKLY_DEBRIEF("weekly_debrief"),
        NEW_ARTICLE("new_article")
    }

    enum class NotificationIntentKeys(val key: String) {
        ARTICLE("article")
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val id = task.result
                Log.d(TAG, "Installation ID: $id")
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(it, remoteMessage.data)
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }
    // [END receive_message]

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, token)
        sendRegistrationToServer(token)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        val prefUtils = PrefUtils(this)
        val graphQlUtil = GraphQlUtil()
        val disposables = CompositeDisposable()

        // Create user.
        val createUserObservable = token?.let {
            graphQlUtil
                .createUser(
                    prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toList(),
                    it
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        createUserObservable?.subscribe { response ->
            response.data?.createUser?.let {
                    user -> prefUtils.save(PrefUtils.UUID, user.uuid)
            }
        }?.let { disposables.add(it) }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: MutableMap<String, String>
    ) {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(
            NotificationIntentKeys.ARTICLE.key,
            data[NotificationIntentKeys.ARTICLE.key]
        )

        when (data[NotificationDataKeys.NOTIFICATION_TYPE.key]) {
            NotificationType.NEW_ARTICLE.type -> {
                intent.putExtra(
                    NotificationDataKeys.ARTICLE_ID.key,
                    data[NotificationDataKeys.ARTICLE_ID.key]
                )
                intent.putExtra(
                    NotificationDataKeys.ARTICLE_URL.key,
                    data[NotificationDataKeys.ARTICLE_URL.key]
                )
            }
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = getString((R.string.default_notification_channel_id))
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.volume_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.volume_icon
                )
            )
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        if (notification.imageUrl != null) {
            val bitmap: Bitmap? = getBitmapFromUrl(notification.imageUrl.toString())
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
            )
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                packageName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e(TAG, "Error in getting notification image: " + e.localizedMessage)
            null
        }
    }

    companion object {
        private const val TAG = "NotificationService"
    }
}