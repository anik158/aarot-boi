package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE type = :type OR type = 'BOTH' ORDER BY name ASC")
    fun getPartiesByType(type: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Query("UPDATE parties SET balance = balance + :delta WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double)

    @Query("UPDATE parties SET emptyBagsDue = emptyBagsDue + :delta WHERE id = :id")
    suspend fun updateEmptyBags(id: Long, delta: Int)

    @Query("DELETE FROM parties WHERE id = :id")
    suspend fun deleteParty(id: Long)

    @Query("SELECT COUNT(*) FROM parties")
    suspend fun countParties(): Int
}
