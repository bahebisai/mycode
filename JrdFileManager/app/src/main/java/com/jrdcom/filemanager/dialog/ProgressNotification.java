package com.jrdcom.filemanager.dialog;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FileBrowserActivity;

/**
 * Created by user on 16-8-1.
 */


public class ProgressNotification {
    private static Context mContext;

    public static Notification.Builder newInstance(Context context, long createTime) {
        mContext = context;
        long mCreateTime = createTime;

        Notification.Builder build;
        RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.progress_notification);

        if(Build.VERSION.SDK_INT < 24) {
            Intent notificationIntent = new Intent(mContext, FileBrowserActivity.class);
            Bundle turnBundle = new Bundle();
            turnBundle.putBoolean("notification", true);
            turnBundle.putLong("turnTime", mCreateTime);
            notificationIntent.putExtras(turnBundle);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext.getApplicationContext(), (int)mCreateTime+2, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            build = new Notification.Builder(mContext);
            contentView.setImageViewResource(R.id.noti_icon,R.drawable.ic_launcher);
            build.setSmallIcon(R.drawable.ic_launcher);
            build.setContentIntent(contentIntent);
            build.setContent(contentView);
            build.build().flags |= Notification.FLAG_NO_CLEAR;
        }else {
            Intent show = new Intent(mContext, FileBrowserActivity.class);
            Bundle showBundle = new Bundle();
            showBundle.putBoolean("showDialog", true);
            showBundle.putLong("createTime",mCreateTime);
            show.putExtras(showBundle);
            PendingIntent showIntent = PendingIntent.getActivity(mContext.getApplicationContext(), (int)mCreateTime,
                    show, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent cancel = new Intent(mContext, FileBrowserActivity.class);
            Bundle cancelBundle = new Bundle();
            cancelBundle.putBoolean("cancel", true);
            cancelBundle.putLong("cancelTime",mCreateTime);
            cancel.putExtras(cancelBundle);
            PendingIntent cancelIntent = PendingIntent.getActivity(mContext.getApplicationContext(), (int)mCreateTime+1,
                    cancel, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Action showAction = new Notification.Action.Builder(0, mContext.getString(R.string.show), showIntent).build();
            Notification.Action cancelAction = new Notification.Action.Builder(0, mContext.getString(R.string.cancel), cancelIntent).build();
            build = new Notification.Builder(mContext)
                    .setCustomContentView(contentView)
                    .addAction(cancelAction)
                    .addAction(showAction)
                    .setStyle(new Notification.DecoratedCustomViewStyle());
            build.build().flags |= Notification.FLAG_NO_CLEAR;
        }
        return build;
    }
}
