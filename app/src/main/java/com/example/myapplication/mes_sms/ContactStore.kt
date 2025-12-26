package com.example.myapplication.mes_sms

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.myapplication.mes_sms.data.AppDatabase
import com.example.myapplication.mes_sms.data.ContactEntity

object ContactStore {
    private const val TAG = "ContactStore"

    // Reactive Flow of contacts (mapped to Contact data class)
    fun contactsFlow(context: Context): Flow<List<Contact>> {
        val db = AppDatabase.getInstance(context)
        return db.contactDao().getAllFlow().map { list ->
            list.map { Contact(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
        }
    }

    // Suspend loader
    suspend fun loadContactsSuspend(context: Context): List<Contact> {
        val db = AppDatabase.getInstance(context)
        val ents = db.contactDao().getAll()
        return ents.map { Contact(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
    }

    // Suspend saver
    suspend fun saveContactsSuspend(context: Context, contacts: List<Contact>) {
        val db = AppDatabase.getInstance(context)
        db.contactDao().deleteAll()
        val ents = contacts.map { ContactEntity(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
        db.contactDao().insertAll(ents)
    }

    // Suspend find
    suspend fun findByNumberSuspend(context: Context, phone: String?): Contact? {
        if (phone == null) return null
        val db = AppDatabase.getInstance(context)
        val ent = db.contactDao().findByPhone(normalize(phone))
        return ent?.let { Contact(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
    }

    // Suspend known number check
    suspend fun isKnownNumberSuspend(context: Context, phone: String?): Boolean {
        if (phone == null) return false
        val db = AppDatabase.getInstance(context)
        val ent = db.contactDao().findByPhone(normalize(phone))
        return ent != null
    }

    private fun normalize(phone: String): String {
        return phone.replace(Regex("[^0-9+]"), "")
    }
}
