package com.cornellappdev.volume.analytics

import android.content.Intent
import android.os.Parcelable
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

enum class VolumeEvent(val event: String) {
    /// General events
    START_ONBOARDING("start_onboarding"),
    COMPLETE_ONBOARDING("complete_onboarding"),

    /// Publication-specific events
    FOLLOW_PUBLICATION("follow_publication"),
    UNFOLLOW_PUBLICATION("unfollow_publication"),
    OPEN_PUBLICATION("open_publication"),
    CLOSE_PUBLICATION("open_publication"),

    /// Article-specific events
    OPEN_ARTICLE("open_article"),
    CLOSE_ARTICLE("close_article"),
    SHARE_ARTICLE("share_article"),
    SHOUTOUT_ARTICLE("shoutout_article"),
    BOOKMARK_ARTICLE("bookmark_article"),
    UNBOOKMARK_ARTICLE("unbookmark_article");

    companion object {
        fun logEvent(eventType: EventType,
                     volumeEvent: VolumeEvent,
                     navigationSource: NavigationSource = NavigationSource.UNSPECIFIED,
                     id: String = "error") {
            when (eventType) {
                EventType.ARTICLE -> {
                    Firebase.analytics.logEvent(volumeEvent.event) {
                        param("articleID", id)
                        param("navigationSource", navigationSource.source)
                    }
                }
                EventType.PUBLICATION -> {
                    Firebase.analytics.logEvent(volumeEvent.event) {
                        param("publicationID", id)
                        param("navigationSource", navigationSource.source)
                    }
                }
                EventType.GENERAL -> Firebase.analytics.logEvent(volumeEvent.event) {}
            }
        }
    }
}

@Parcelize
enum class NavigationSource(val source: String) : Parcelable {
    // Article Entry Points
    BOOKMARK_ARTICLES("bookmark_articles"),
    FOLLOWING_ARTICLES("following_articles"),
    OTHER_ARTICLES("other_articles"),
    PUBLICATION_DETAIL("publication_detail"),
    TRENDING_ARTICLES("trending_articles"),

    // Publication Entry Points
    ARTICLE_DETAIL("article_detail"),
    FOLLOWING_PUBLICATIONS("following_publications"),
    MORE_PUBLICATIONS("more_publications"),
    ONBOARDING("onboarding"),

    UNSPECIFIED("unspecified");

    companion object {
        const val INTENT_KEY = "source"

        fun Intent.putParcelableExtra(key: String, value: Parcelable) {
            putExtra(key, value)
        }
    }
}

enum class EventType(val type: String) {
    ARTICLE("article"),
    GENERAL("general"),
    PUBLICATION("publication"),
}