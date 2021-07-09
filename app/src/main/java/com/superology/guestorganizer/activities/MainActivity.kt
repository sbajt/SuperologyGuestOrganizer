package com.superology.guestorganizer.activities

import android.accounts.AccountManager
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.scorealarm.functions.FunctionUtils
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.superology.guestorganizer.R
import com.superology.guestorganizer.data.DataService
import com.superology.guestorganizer.data.models.Element
import com.superology.guestorganizer.enums.ElementModificationType
import com.superology.guestorganizer.fragments.AddElementDialog
import com.superology.guestorganizer.fragments.DialogActionListener
import com.superology.guestorganizer.fragments.UpdateElementStatusDialog
import com.superology.guestorganizer.list.ListAdapter
import com.superology.guestorganizer.utils.NotificationUtils
import com.superology.guestorganizer.utils.ActivityUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

class MainActivity : AppCompatActivity(), ListItemActionListener<Element>, DialogActionListener {

    private val TAG = MainActivity::class.java.toString()
    private val RESULT_CODE_ACCOUNT = 9999
    private val disposable = CompositeDisposable()
    private val listAdapter = ListAdapter(this)
    private val accountManager: AccountManager by lazy { getSystemService(Context.ACCOUNT_SERVICE) as AccountManager }
    private val connectivityManager: ConnectivityManager by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private var isObservingData = false
    private lateinit var statusTextView: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        NotificationUtils.init(this, notificationManager)
        FunctionUtils.observeDb(this)
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isSuccessful) {
                token = it.result.toString()
                Log.d(TAG, "Token: $token")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ActivityUtils.init(connectivityManager, accountManager)
        if (ActivityUtils.isInternetOn)
            if (ActivityUtils.isAccountAvailable) {
                observeInternet()
                observeAccount()
                observeData()
            } else {
                val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    true,
                    null,
                    null,
                    null,
                    null)
                startActivityForResult(chooseAccountIntent, RESULT_CODE_ACCOUNT)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        isObservingData = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_CODE_ACCOUNT -> {
                ActivityUtils.isAccountAvailable = true
                observeAccount()
                if (ActivityUtils.isInternetOn)
                    observeData()
                else
                    showNoInternet()
            }
        }
    }

    override fun onListElementClick(data: Element) {
        UpdateElementStatusDialog.getInstance(data, this).show(supportFragmentManager, UpdateElementStatusDialog.TAG)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.change_list_mode, menu)
        if (menu != null) {
            for (i in 0 until menu.size()) {
                if (menu.getItem(i).icon != null) {
                    DrawableCompat.setTint(menu.getItem(i).icon, getColor(android.R.color.white))
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.lockListAction -> {
                listAdapter.mode = ListAdapter.ModeType.READ_ONLY
                invalidateOptionsMenu()
                return true
            }
            R.id.unlockListAction -> {
                listAdapter.mode = ListAdapter.ModeType.EDIT_ON_CLICK
                invalidateOptionsMenu()
                return true
            }
            R.id.addElementAction -> {
                AddElementDialog.getInstance(ActivityUtils.getAccountEmail(accountManager), this).show(supportFragmentManager, AddElementDialog.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (listAdapter.itemCount == 0 && ActivityUtils.isAccountAvailable) {
            menu?.findItem(R.id.unlockListAction)?.isVisible = false
            menu?.findItem(R.id.lockListAction)?.isVisible = false
            menu?.findItem(R.id.addElementAction)?.isVisible = true
        } else if (listAdapter.itemCount == 0 && !ActivityUtils.isAccountAvailable) {
            menu?.findItem(R.id.unlockListAction)?.isVisible = false
            menu?.findItem(R.id.lockListAction)?.isVisible = false
            menu?.findItem(R.id.addElementAction)?.isVisible = false
        } else {
            when (listAdapter.mode) {
                ListAdapter.ModeType.READ_ONLY -> {
                    menu?.findItem(R.id.unlockListAction)?.isVisible = true
                    menu?.findItem(R.id.lockListAction)?.isVisible = false
                    menu?.findItem(R.id.addElementAction)?.isVisible = false
                }
                ListAdapter.ModeType.EDIT_ON_CLICK -> {
                    menu?.findItem(R.id.unlockListAction)?.isVisible = false
                    menu?.findItem(R.id.lockListAction)?.isVisible = true
                    menu?.findItem(R.id.addElementAction)?.isVisible = true
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun add(element: Element) {
        showDatePickerDialog(element, ElementModificationType.ADD)
    }

    override fun update(element: Element) {
        showDatePickerDialog(element, ElementModificationType.UPDATE)
    }

    private fun initViews() {
        statusTextView = findViewById(R.id.statusTextView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = listAdapter
            visibility = View.GONE
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun isItemViewSwipeEnabled(): Boolean {
                    return when (listAdapter.mode) {
                        ListAdapter.ModeType.READ_ONLY -> false
                        ListAdapter.ModeType.EDIT_ON_CLICK -> true
                    }
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewHolder.adapterPosition.run {
                        listAdapter.deleteItemWithUndo(this)
                        showUndoSnackbar()
                    }
                }
            }).attachToRecyclerView(this)
        }
    }

    private fun observeData() {
        if (!isObservingData) {
            isObservingData = true
            disposable.add(DataService.observeData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (ActivityUtils.isInternetOn)
                        if (ActivityUtils.isAccountAvailable)
                            if (it?.isEmpty() == true)
                                showNoData()
                            else {
                                hideNoData()
                                listAdapter.update(it)
                            }
                        else
                            showNoGmailAccount()
                    else
                        showNoInternet()

                    invalidateOptionsMenu()
                }) { Log.e(TAG, it.message.toString(), it) })
        }
    }

    private fun observeAccount() =
        disposable.add(ActivityUtils.observeAccountState(accountManager)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    hideNoGmailAccount()
                    if (ActivityUtils.isInternetOn)
                        observeData()
                    else
                        showNoInternet()
                } else
                    showNoGmailAccount()

                invalidateOptionsMenu()
            }) { Log.d(TAG, it.message.toString(), it) }
        )

    private fun observeInternet() =
        disposable.add(
            ActivityUtils.observeInternetState(connectivityManager)
                .subscribe({
                    if (it) {
                        hideNoInternet()
                        if (ActivityUtils.isAccountAvailable)
                            hideNoGmailAccount()
                        else
                            showNoGmailAccount()
                    } else
                        showNoInternet()
                }) { Log.d(TAG, it.message, it) }
        )

    private fun showNoData() {
        statusTextView.setText(R.string.label_no_data)
        statusTextView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideNoData() {
        statusTextView.setText(R.string.label_no_data)
        statusTextView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showNoInternet() {
        statusTextView.setText(R.string.label_no_internet)
        statusTextView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideNoInternet() = hideNoData()

    private fun showNoGmailAccount() {
        statusTextView.setText(R.string.label_no_gmail_account)
        statusTextView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideNoGmailAccount() = hideNoData()

    private fun showDatePickerDialog(modifiedElement: Element, elementModificationType: ElementModificationType) {
        val dateTime = when (elementModificationType) {
            ElementModificationType.UPDATE -> modifiedElement.dateTime
            else -> DateTime.now()
        }
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                modifiedElement.dateTime = DateTime(year, month + 1, dayOfMonth, 0, 0)
                showTimePickerDialog(modifiedElement, dateTime, elementModificationType)
            },
            dateTime.year,
            dateTime.monthOfYear - 1,
            dateTime.dayOfMonth
        ).show()
    }

    private fun showTimePickerDialog(modifiedElement: Element, dateTime: DateTime, elementModificationType: ElementModificationType) {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                modifiedElement.dateTime = modifiedElement.dateTime.withTime(hour, minute, 0, 0)
                when (elementModificationType) {
                    ElementModificationType.ADD -> DataService.addElement(this, modifiedElement)
                    ElementModificationType.UPDATE -> DataService.updateElement(this, modifiedElement)
                }
                invalidateOptionsMenu()
            },
            dateTime.hourOfDay,
            0,
            true
        ).show()
    }

    private fun showUndoSnackbar() {
        Snackbar.make(findViewById(R.id.contentLayout), R.string.snack_bar_undo, Snackbar.LENGTH_LONG)
            .setAction(R.string.snack_bar_undo) { listAdapter.undoDeleteItem() }
            .addCallback(object : Snackbar.Callback() {

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (listAdapter.canUndoDeletedItem)
                        DataService.removeElement(this@MainActivity, listAdapter.deletedItem?.second)
                }
            })
            .show()
    }
}