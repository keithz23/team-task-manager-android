package com.example.taskmanager

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity rỗng có Hilt, chỉ có trong bản debug. UI test dùng
 * createAndroidComposeRule<HiltTestActivity>() rồi setContent { Screen(hiltViewModel()) }.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
