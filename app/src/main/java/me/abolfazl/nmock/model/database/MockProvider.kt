package me.abolfazl.nmock.model.database

import androidx.annotation.StringDef
import me.abolfazl.nmock.utils.Constant.PROVIDER_GPS
import me.abolfazl.nmock.utils.Constant.PROVIDER_NETWORK

@StringDef(PROVIDER_GPS, PROVIDER_NETWORK)
annotation class MockProvider