package cc.cryptopunks.wrapdrive.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cc.cryptopunks.wrapdrive.databinding.DisconnectionBinding

class DisconnectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = DisconnectionBinding.inflate(inflater, container, false).apply {
        startAstralButton.setOnClickListener {
            startActivity(astralIntent())
        }
    }.root
}
