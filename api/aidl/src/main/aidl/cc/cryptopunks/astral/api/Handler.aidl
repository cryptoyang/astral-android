// AstralApi.aidl
package cc.cryptopunks.astral.api;

import cc.cryptopunks.astral.api.Connection;

// PortHandler is a handler for a locally registered port
interface Handler {
    Connection next();
    void close();
}
