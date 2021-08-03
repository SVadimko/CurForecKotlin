package com.vadimko.curforeckotlin.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * DataBase file
 */

@Entity
data class Currencies(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var usdBuy: Double = 0.0, var usdSell: Double = 0.0,
    var eurBuy: Double = 0.0, var eurSell: Double = 0.0,
    var gbpBuy: Double = 0.0, var gbpSell: Double = 0.0, var dt: String = ""
)