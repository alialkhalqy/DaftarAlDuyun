package com.daftar.alduyun.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DaftarDao {
    @Query("SELECT * FROM people ORDER BY name COLLATE NOCASE")
    fun people(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people ORDER BY id")
    suspend fun allPeople(): List<PersonEntity>

    @Query("SELECT * FROM transactions ORDER BY id")
    suspend fun allTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM labels ORDER BY `key`")
    suspend fun allLabels(): List<LabelEntity>

    @Query("DELETE FROM people")
    suspend fun clearPeople()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM labels")
    suspend fun clearLabels()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(rows: List<PersonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(rows: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabels(rows: List<LabelEntity>)

    @Transaction
    suspend fun restoreAll(people: List<PersonEntity>, transactions: List<TransactionEntity>, labels: List<LabelEntity>) {
        clearTransactions()
        clearPeople()
        clearLabels()
        if (people.isNotEmpty()) insertPeople(people)
        if (transactions.isNotEmpty()) insertTransactions(transactions)
        if (labels.isNotEmpty()) insertLabels(labels)
    }

    @Query("SELECT * FROM people WHERE type = :type ORDER BY name COLLATE NOCASE")
    fun peopleByType(type: String): Flow<List<PersonEntity>>

    @Insert
    suspend fun addPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun transactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY createdAt DESC")
    fun transactionsForPerson(personId: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun addTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT * FROM labels")
    fun labels(): Flow<List<LabelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLabel(label: LabelEntity)

    @Query("SELECT * FROM labels WHERE `key` = :key LIMIT 1")
    suspend fun getLabel(key: String): LabelEntity?
}
