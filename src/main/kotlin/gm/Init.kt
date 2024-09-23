package com.fengsheng.gm

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

internal val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
