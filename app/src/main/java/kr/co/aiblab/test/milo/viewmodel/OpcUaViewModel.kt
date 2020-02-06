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
import kr.co.aiblab.milo.KeyStoreLoader
import kr.co.aiblab.milo.MiloClient
import kr.co.aiblab.test.milo.MainActivity
import kr.co.aiblab.test.milo.client.BrowseExample
import kr.co.aiblab.test.milo.client.MethodExample
import kr.co.aiblab.test.milo.client.ReadExample
import kr.co.aiblab.test.milo.client.SubscribeExample
import kr.co.aiblab.test.milo.milo.MiloClientRunner
import kr.co.aiblab.test.milo.milo.OpcUaCallback
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import java.io.File

class OpcUaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    fun doConnect() {
        Logger.d("DO CONNECT!")
        viewModelScope.launch {
            connect()
        }
    }

    fun doDisconnect() {
        Logger.d("DO DISCONNECT!")
        viewModelScope.launch {
            disconnect()
        }
    }

    fun read() {
        viewModelScope.launch {
            executeMiloClient(ReadExample(_data))
        }
    }

    fun browse() {
        viewModelScope.launch {
            executeMiloClient(BrowseExample(_data))
        }
    }

    fun subscribe() {
        viewModelScope.launch {
            executeMiloClient(SubscribeExample(_data))
        }
    }

    fun method() {
        viewModelScope.launch {
            executeMiloClient(MethodExample(_data))
        }
    }

    private suspend fun connect() = withContext(Dispatchers.IO) {
        MiloClientRunner.getInstance()
            .connect(getKeyStoreLoader(getContext()), object : OpcUaCallback.Connection {
                override fun connected(client: OpcUaClient) {
                    _status.postValue("OpcUaClient has been connected...")
                }

                override fun connectionFailed(e: Exception) {
                    _status.postValue("Error connecting:${e.message}")
                }
            })
    }

    private suspend fun disconnect() = withContext(Dispatchers.IO) {
        MiloClientRunner.getInstance()
            .disconnect(object : OpcUaCallback.Disconnection {
                override fun disconnected(client: OpcUaClient) {
                    _status.postValue("OpcUaClient has been disconnected!!!")
                }

                override fun disconnectionFailed(e: Exception) {
                    _status.postValue("Error disconnecting:${e.message}")
                }
            })
    }

    private suspend fun executeMiloClient(miloClient: MiloClient) = withContext(Dispatchers.IO) {
        MiloClientRunner.getInstance()
            .executeMiloClient(miloClient, object : OpcUaCallback.MiloClientExecute {
                override fun executeFailed(e: Exception) {
                    _status.postValue("Error miloClient executing:${e.message}")
                }
            })
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