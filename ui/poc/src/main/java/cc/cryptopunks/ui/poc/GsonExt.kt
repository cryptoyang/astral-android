package cc.cryptopunks.ui.poc

import com.google.gson.Gson
import java.io.Reader

inline fun<reified T> Gson.fromJson(string: Reader) = fromJson(string,T::class.java)!!
inline fun<reified T> Gson.fromJson(string: String) = fromJson(string,T::class.java)!!
