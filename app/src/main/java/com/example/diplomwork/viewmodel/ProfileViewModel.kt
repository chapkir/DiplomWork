package com.example.diplomwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.network.ApiClient
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class ProfileViewModel @Inject constructor(
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    init{
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            val profile = ApiClient.apiService.getProfile()
            _username.value = profile.username
        }
    }
}