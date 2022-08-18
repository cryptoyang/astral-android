package cc.cryptopunks.wrapdrive.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

abstract class CoroutineViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext by lazy { viewModelScope.coroutineContext }
}
