// AstralApi.aidl
package cc.cryptopunks.astral.api;

// Stream represents a bidirectional stream
interface Stream {
    long read(out byte[] buffer);
	long write(in byte[] buffer);
	void close();
}
//interface Stream {
//    String read();
//	void write(String string);
//	void close();
//}
