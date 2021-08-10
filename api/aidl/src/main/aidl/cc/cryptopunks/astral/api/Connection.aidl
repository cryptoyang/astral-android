// AstralApi.aidl
package cc.cryptopunks.astral.api;

import cc.cryptopunks.astral.api.Stream;

// ConnectionRequest represents a connection request sent to a port
interface Connection {
	String caller();
	String query();
	Stream accept();
	void reject();
}
