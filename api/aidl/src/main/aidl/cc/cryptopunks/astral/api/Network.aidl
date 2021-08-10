// AstralApi.aidl
package cc.cryptopunks.astral.api;

import cc.cryptopunks.astral.api.Handler;
import cc.cryptopunks.astral.api.Stream;


// Network provides access to core network APIs
interface Network {
    Handler register(String port);
    Stream connect(String identity, String port);
    String identity();
}
