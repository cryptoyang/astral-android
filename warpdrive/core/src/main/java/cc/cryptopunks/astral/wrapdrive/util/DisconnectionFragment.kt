package cc.cryptopunks.astral.wrapdrive.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cc.cryptopunks.astral.wrapdrive.databinding.DisconnectionBinding

class DisconnectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = DisconnectionBinding.inflate(inflater, container, false).apply {
        startAstralButton.setOnClickListener {
            val intent = requireActivity().packageManager
                .getLaunchIntentForPackage("cc.cryptopunks.astral.node")
            startActivity(intent)
        }
    }.root
}
