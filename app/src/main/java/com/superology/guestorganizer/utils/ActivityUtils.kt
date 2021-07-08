package com.superology.guestorganizer.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.net.ConnectivityManager
import android.net.Network
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject

object ActivityUtils {

    private val internetStateSubject = ReplaySubject.create<Boolean>(1)
    private val accountStatusSubject = ReplaySubject.create<Boolean>(1)
    private val TAG = ActivityUtils::class.java.canonicalName

    var isInternetOn = false
    var isAccountAvailable = false

    fun init(connectivityManager: ConnectivityManager, accountManager: AccountManager) {
        isInternetOn = connectivityManager.activeNetwork != null
        getGoogleAccount(accountManager) != null
    }

    fun observeInternetState(connectivityManager: ConnectivityManager): Observable<Boolean> {
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isInternetOn = true
                internetStateSubject.onNext(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isInternetOn = false
                internetStateSubject.onNext(false)
            }
        })
        return internetStateSubject.subscribeOn(Schedulers.newThread())
    }

    fun observeAccountState(accountManager: AccountManager): Observable<Boolean> {
        isAccountAvailable = getGoogleAccount(accountManager) != null
        accountStatusSubject.onNext(isAccountAvailable)
        return accountStatusSubject.subscribeOn(Schedulers.newThread())
    }

    fun getAccountEmail(accountManager: AccountManager): String =
        if (accountStatusSubject.value == true) getGoogleAccount(accountManager)?.name ?: ""
        else ""

    private fun getGoogleAccount(accountManager: AccountManager): Account? {
        val googleAccounts = accountManager.getAccountsByType("com.google")
        if (googleAccounts.isNotEmpty()) {
            isAccountAvailable = googleAccounts.first() != null
            accountStatusSubject.onNext(isAccountAvailable)
            return googleAccounts.first()
        }

        return null
    }
}