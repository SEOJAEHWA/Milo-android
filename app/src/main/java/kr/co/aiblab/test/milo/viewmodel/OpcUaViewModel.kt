package kr.co.aiblab.test.milo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.aiblab.test.milo.MainActivity
import kr.co.aiblab.test.milo.client.BrowseExample
import kr.co.aiblab.test.milo.client.ReadExample
import kr.co.aiblab.test.milo.client.SubscribeExample
import kr.co.aiblab.test.milo.milo.KeyStoreLoader
import kr.co.aiblab.test.milo.milo.MiloClientRunner
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Stack
import java.io.File
import java.util.concurrent.ExecutionException

class OpcUaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    private val _opcUaClient = MutableLiveData<OpcUaClient?>()

    fun connect() {
        Logger.d("DO CONNECT!")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val client = MiloClientRunner(
                    getKeyStoreLoader(getContext())
                ).get()

                try {
                    client.connect().get()
                    _status.postValue("OpcUaClient has been connected...")
                    _opcUaClient.postValue(client)
                } catch (e: InterruptedException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _status.postValue("Error connecting:${e.message}")
                } catch (e: ExecutionException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _status.postValue("Error connecting:${e.message}")
                }
            }
        }
    }

    fun disconnect() {
        Logger.d("DO DISCONNECT!")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _opcUaClient.value?.let {
                    try {
                        it.disconnect().get()
                        _status.postValue("OpcUaClient has been disconnected!!!")
                    } catch (e: InterruptedException) {
                        Logger.e("Error disconnecting:${e.message}", e)
                        _status.postValue("Error disconnecting:${e.message}")
                    } catch (e: ExecutionException) {
                        Logger.e("Error disconnecting:${e.message}", e)
                        _status.postValue("Error disconnecting:${e.message}")
                    } finally {
                        _opcUaClient.postValue(null)
                        Stack.releaseSharedResources()
                        _data.postValue("Cleaned...")
                    }
                } ?: run {
                    Logger.e("OpcUaClient is null. Do nothing...")
                }
            }
        }
    }

    fun read() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _opcUaClient.value?.let {
                    ReadExample(_data).execute(it)
                } ?: run {
                    Logger.e("OpcUaClient is null. Cannot execute miloClient...")
                    _status.postValue("OpcUaClient is disconnected!!!")
                }
            }
        }
    }

    fun browse() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _opcUaClient.value?.let {
                    BrowseExample(_data).execute(it)
                } ?: run {
                    Logger.e("OpcUaClient is null. Cannot execute miloClient...")
                    _status.postValue("OpcUaClient is disconnected!!!")
                }
            }
        }
    }

    fun subscribe() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _opcUaClient.value?.let {
                    SubscribeExample(_data).execute(it)
                } ?: run {
                    Logger.e("OpcUaClient is null. Cannot execute miloClient...")
                    _status.postValue("OpcUaClient is disconnected!!!")
                }
            }
        }
    }

    fun method() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

            }
        }
    }

    private suspend fun getKeyStoreLoader(
        context: Context
    ): KeyStoreLoader = withContext(Dispatchers.Default) {
        val securityTempDir = File(MainActivity.getSecureDirectory(context).absolutePath)
        Logger.d("security temp dir: ${securityTempDir.absolutePath}")
        KeyStoreLoader().load(securityTempDir)
    }
}

fun AndroidViewModel.getContext(): Context = getApplication<Application>().applicationContext