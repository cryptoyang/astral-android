package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIArgs

fun UI.State.isReady() = method.isReady(args)

fun Service.Method?.isReady(args: UIArgs) = args.keys == this?.params?.keys
