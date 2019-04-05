package com.huang.homan.androidtv.View.Fragment


import android.app.Activity
import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.*
import androidx.test.uiautomator.Direction.*
import com.huang.homan.androidtv.Data.MyHeaderList
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFragmentUiaTestKotlin {

    private var mTV: UiDevice? = null // this Android TV
    private var packageManager: PackageManager? = null
    private var context: Context? = null
    private var activity: Activity? = null

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    private val launcherPackageName: String
        get() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            val pm = getApplicationContext<Context>().packageManager
            val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            return resolveInfo.activityInfo.packageName
        }

    private val visibleFragment: Fragment?
        get() {
            val fragmentManager = activity!!.fragmentManager
            val fragments = fragmentManager.fragments
            if (fragments != null) {
                for (fragment in fragments) {
                    if (fragment != null && fragment.isVisible)
                        return fragment
                }
            }
            return null
        }

    @Before
    fun startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mTV = UiDevice.getInstance(getInstrumentation())

        // Start from the home screen
        mTV!!.pressHome()

        // Wait for launcher
        val launcherPackage = mTV!!.launcherPackageName
        ltag("pkg: $launcherPackage")
        assertThat(launcherPackage, notNullValue())
        mTV!!.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                LAUNCH_TIMEOUT.toLong())

        // Launch the blueprint app
        context = getApplicationContext()
        assertThat<Context>(context, notNullValue())

        packageManager = context!!.packageManager
        assertThat<PackageManager>(packageManager, notNullValue())

        val inst = InstrumentationRegistry.getInstrumentation()
        val monitor = inst.addMonitor(PackageName + MainActivityName, null, false)

        val intent = Intent()
        intent.component = ComponentName(
                PackageName,
                PackageName + MainActivityName)

        ltag("Loading Activity.")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)    // Clear out any previous instances
        context!!.startActivity(intent)

        // Wait for the app to appear
        mTV!!.wait(Until.hasObject(By.pkg(PackageName).depth(0)), LAUNCH_TIMEOUT.toLong())
        activity = monitor.waitForActivityWithTimeout(LAUNCH_TIMEOUT.toLong())

    }

    // Check Android TV has turned on.
    @Test
    fun checkPreconditions() {
        assertThat<UiDevice>(mTV, notNullValue())
    }

    @Test
    @Throws(UiObjectNotFoundException::class)
    fun moveToAllObj() {
        checkPreconditions()

        val mainFragment = visibleFragment as MainFragment?
        assertThat<MainFragment>(mainFragment, notNullValue())

        for (i in 1..MyHeaderList.HEADER_CATEGORY.size) {
            pressDpad(RIGHT)
            //trySomeItems(3);
            trySomeItems(mainFragment!!.numCols)
            pressDpad(DOWN)
        }
    }

    @Throws(UiObjectNotFoundException::class)
    fun trySomeItems(count: Int) {
        for (i in 0 until count) {
            mClick()
            mBack()
            pressDpad(RIGHT)
        }
        mBack()
    }

    fun pressDpad(direction: Direction) {
        when (direction) {
            UP -> mTV!!.pressDPadUp()
            DOWN -> mTV!!.pressDPadDown()
            LEFT -> mTV!!.pressDPadLeft()
            RIGHT -> mTV!!.pressDPadRight()
            else -> throw IllegalArgumentException(direction.toString())
        }
    }

    fun mBack() {
        mTV!!.pressBack()
    }

    @Throws(UiObjectNotFoundException::class)
    fun mClick() {
        // click
        val uiObject = mTV!!.findObject(UiSelector().focused(true))
        assertThat(uiObject, notNullValue())
        uiObject.click()
    }

    companion object {

        /* Log tag and shortcut */
        internal val TAG = "MYLOG " + MainFragmentUiaTestKotlin::class.java!!.getSimpleName()

        fun ltag(message: String) {
            Log.i(TAG, message)
        }

        private val PackageName = "com.huang.homan.androidtv"

        private val MainActivityName = ".View.Activity.MainActivity"

        private val LAUNCH_TIMEOUT = 5000 // 5s
    }

}