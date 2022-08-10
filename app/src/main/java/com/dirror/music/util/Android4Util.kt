package com.dirror.music.util

import android.app.Activity
import android.content.Intent
import android.view.View
import com.dirror.music.App
import com.dirror.music.R
import com.dirror.music.manager.ActivityCollector
import com.dirror.music.ui.activity.AboutActivity
import com.dirror.music.ui.activity.FeedbackActivity

/**
 * @author JuanLv created at 2022/8/9
 * olbbme@gmail.com
 */
class Android4Util {

    companion object {
        fun initMenuAndroid4(menu: View, activity: Activity) {
            menu.findViewById<View>(R.id.itemSponsor)?.setOnClickListener {
                App.activityManager.startWebActivity(activity, AboutActivity.SPONSOR)
            }
            menu.findViewById<View>(R.id.itemSwitchAccount)?.setOnClickListener {
                App.activityManager.startLoginActivity(activity)
            }
            menu.findViewById<View>(R.id.itemSettings)?.setOnClickListener {
                App.activityManager.startSettingsActivity(activity)
            }
            // 反馈
            menu.findViewById<View>(R.id.itemFeedback)?.setOnClickListener {
                activity.startActivity(Intent(activity, FeedbackActivity::class.java))
            }
            menu.findViewById<View>(R.id.itemAbout)?.setOnClickListener {
                activity.startActivity(Intent(activity, AboutActivity::class.java))
            }
            menu.findViewById<View>(R.id.itemExitApp)?.setOnClickListener {
                App.musicController.value?.stopMusicService()
                ActivityCollector.finishAll()
                object : Thread() {
                    override fun run() {
                        super.run()
                        sleep(500)
                        Secure.killMyself()
                    }
                }.start()
            }
        }
    }


}