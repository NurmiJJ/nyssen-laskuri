package fi.tuni2022.nysselaskin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for showing selected options at Main UI
 */
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

