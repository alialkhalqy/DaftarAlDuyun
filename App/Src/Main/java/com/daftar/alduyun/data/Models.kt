package com.daftar.alduyun.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val type: String, // CUSTOMER or SUPPLIER
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long? = null,
    val personName: String = "",
    val type: String, // SALE_CASH, SALE_CREDIT, CUSTOMER_PAYMENT, SUPPLIER_DEBT, SUPPLIER_PAYMENT, EXPENSE, INCOME, DHIMAR
    val amount: Long,
    val cost: Long = 0,
    val title: String = "",
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val key: String,
    val value: String
)
