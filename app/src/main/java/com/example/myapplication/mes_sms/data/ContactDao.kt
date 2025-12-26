package com.example.myapplication.mes_sms.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()

    @Query("SELECT * FROM contacts ORDER BY firstName")
    fun getAllFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY firstName")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE city = :city")
    fun getByCityFlow(city: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE department = :dept")
    fun getByDepartmentFlow(dept: String): Flow<List<ContactEntity>>
}
