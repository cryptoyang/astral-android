package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIArgs

fun UI.State.isReady() = method.isReady(args)

fun Service.Method?.isReady(args: UIArgs) = args.keys == this?.params?.keys
