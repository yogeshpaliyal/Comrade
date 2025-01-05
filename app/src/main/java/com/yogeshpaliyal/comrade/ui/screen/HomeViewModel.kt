package com.yogeshpaliyal.comrade.ui.screen

import androidx.lifecycle.ViewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dagger.hilt.android.lifecycle.HiltViewModel
import data.ComradeQueries
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val comradeQueries: ComradeQueries): ViewModel() {

    val listOfItems = comradeQueries.getAllFilesList().asFlow().mapToList(Dispatchers.IO)

}