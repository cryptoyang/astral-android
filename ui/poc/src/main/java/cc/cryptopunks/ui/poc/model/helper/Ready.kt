package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIArgs

fun UI.State.isReady() = method.isReady(args)

fun Api.Method?.isReady(args: UIArgs) = args.keys == this?.params?.keys
