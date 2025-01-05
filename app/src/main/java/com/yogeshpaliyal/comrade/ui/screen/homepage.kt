package com.yogeshpaliyal.comrade.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun Homepage(viewModel: HomeViewModel = hiltViewModel()){
    val data = viewModel.listOfItems.collectAsState(listOf())

    if (data.value.isEmpty()){
        Text("No Data Found")
    } else {
        LazyColumn() {
            items(data.value) {
                Text(it.packageName)
            }
        }
    }
}