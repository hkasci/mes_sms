package com.example.myapplication.mes_sms.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(msg: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE state = :stateVal")
    fun countByStateFlow(stateVal: String): Flow<Int>

    @Query("SELECT * FROM messages WHERE state = :stateVal")
    fun getByStateFlow(stateVal: String): Flow<List<MessageEntity>>
}

