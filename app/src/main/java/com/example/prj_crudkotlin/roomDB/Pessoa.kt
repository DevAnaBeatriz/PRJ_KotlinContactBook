package com.example.prj_crudkotlin.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Pessoa(
    val name: String,
    val telefone: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
