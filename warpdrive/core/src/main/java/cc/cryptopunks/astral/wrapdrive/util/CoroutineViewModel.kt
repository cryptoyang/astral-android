package cc.cryptopunks.astral.wrapdrive.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

abstract class CoroutineViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext by lazy { viewModelScope.coroutineContext }
    override fun onCleared() {
        super.onCleared()
        cancel()
    }
}
