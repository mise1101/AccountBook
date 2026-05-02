package com.example.accountbook.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.graphics.Color
import com.example.accountbook.MainActivity
import com.example.accountbook.R
import com.example.accountbook.util.WidgetPrefsManager

class AccountBookWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            val views = buildRemoteViews(context)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, widgetId, newOptions)
        val views = buildRemoteViews(context)
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    companion object {
        const val ACTION_ADD_TRANSACTION = "com.example.accountbook.ACTION_ADD_TRANSACTION"

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_account_book)

            val color = WidgetPrefsManager.getWidgetColor(context)
            val text = WidgetPrefsManager.getWidgetText(context)

            views.setInt(R.id.widget_root, "setBackgroundColor", color)
            views.setTextViewText(R.id.widget_label, text)
            views.setTextColor(R.id.widget_icon, Color.WHITE)
            views.setTextColor(R.id.widget_label, Color.WHITE)

            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_TRANSACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            return views
        }
    }
}
