package fi.tuni2022.nysselaskin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NysseViewModel : ViewModel() {

    val customer: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val seasonDuration: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val zones: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}

