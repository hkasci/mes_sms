package com.example.myapplication.mes_sms.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.myapplication.mes_sms.Contact

class ContactRepository(private val dao: ContactDao) {
    val contactsFlow: Flow<List<Contact>> = dao.getAllFlow().map { list ->
        list.map { Contact(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
    }

    suspend fun getAll(): List<Contact> {
        return dao.getAll().map { Contact(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
    }

    suspend fun insert(contact: Contact) {
        val ent = ContactEntity(id = contact.id, firstName = contact.firstName, lastName = contact.lastName, department = contact.department, city = contact.city, phoneNumber = contact.phoneNumber)
        dao.insert(ent)
    }

    suspend fun deleteByPhone(phone: String) {
        val normalized = phone.replace(Regex("[^0-9+]"), "")
        val ent = dao.findByPhone(normalized)
        ent?.let { dao.delete(it) }
    }

    suspend fun findByPhone(phone: String): Contact? {
        val normalized = phone.replace(Regex("[^0-9+]"), "")
        val ent = dao.findByPhone(normalized)
        return ent?.let { Contact(id = it.id, firstName = it.firstName, lastName = it.lastName, department = it.department, city = it.city, phoneNumber = it.phoneNumber) }
    }
}

