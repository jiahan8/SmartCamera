package com.jiahan.smartcamera.data.repository

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class FirebaseAnalyticsRepository @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsRepository {

    companion object {
        private const val NOTE_SEARCH_EVENT = "note_search"
        private const val FAVORITE_SEARCH_EVENT = "favorite_search"
        private const val EXPLORE_SEARCH_EVENT = "explore_search"
        private const val SEARCH_TERM_PARAM = "search_term"
        private const val NOTE_CREATE_EVENT = "note_create"
        private const val NOTE_EDIT_EVENT = "note_edit"
        private const val NOTE_TEXT_PARAM = "note_text"
        private const val TEXT_EVENT = "text"
        private const val TEXT_VALUE_PARAM = "text_value"
        private const val DISPLAY_NAME_EVENT = "display_name"
        private const val DISPLAY_NAME_PARAM = "display_name"
        private const val USERNAME_EVENT = "username"
        private const val USERNAME_PARAM = "username"
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun logSearch(query: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params)
    }

    override fun logNoteSearch(query: String) {
        val params = Bundle().apply {
            putString(SEARCH_TERM_PARAM, query)
        }
        firebaseAnalytics.logEvent(NOTE_SEARCH_EVENT, params)
    }

    override fun logNoteCreate(text: String) {
        val params = Bundle().apply {
            putString(NOTE_TEXT_PARAM, text)
        }
        firebaseAnalytics.logEvent(NOTE_CREATE_EVENT, params)
    }

    override fun logNoteEdit(text: String) {
        val params = Bundle().apply {
            putString(NOTE_TEXT_PARAM, text)
        }
        firebaseAnalytics.logEvent(NOTE_EDIT_EVENT, params)
    }

    override fun logFavoriteSearch(query: String) {
        val params = Bundle().apply {
            putString(SEARCH_TERM_PARAM, query)
        }
        firebaseAnalytics.logEvent(FAVORITE_SEARCH_EVENT, params)
    }

    override fun logExploreSearch(query: String) {
        val params = Bundle().apply {
            putString(SEARCH_TERM_PARAM, query)
        }
        firebaseAnalytics.logEvent(EXPLORE_SEARCH_EVENT, params)
    }

    override fun logText(text: String) {
        val params = Bundle().apply {
            putString(TEXT_VALUE_PARAM, text)
        }
        firebaseAnalytics.logEvent(TEXT_EVENT, params)
    }

    override fun logDisplayName(displayName: String) {
        val params = Bundle().apply {
            putString(DISPLAY_NAME_PARAM, displayName)
        }
        firebaseAnalytics.logEvent(DISPLAY_NAME_EVENT, params)
    }

    override fun logUsername(username: String) {
        val params = Bundle().apply {
            putString(USERNAME_PARAM, username)
        }
        firebaseAnalytics.logEvent(USERNAME_EVENT, params)
    }
}