package kr.co.aiblab.test.milo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    // TODO client 가 여러개 관리되어야 할 수 있고 특정 시점(아마도 종료?)엔 모두 disconnect 해야함
    //  경우에 따라서는 특정 client 만 disconnect 처리되어야 할 수 있으므로 key, value 로 관리?
    //
    private val _opcUaClient = MutableLiveData<OpcUaClient>()

    fun subscribe() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val miloClient = SubscribeExample(_data)
                val client = MiloClientRunner(
                    getKeyStoreLoader(getContext()),
                    miloClient
                ).get()

                Logger.e("SubscribeExample::OpcUaClient = $client")

                try {
                    client.connect().get()
                    _data.postValue("OpcUaClient is connected...")
                    _opcUaClient.postValue(client)
                } catch (e: InterruptedException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _data.postValue("Error connecting:${e.message}")
                } catch (e: ExecutionException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _data.postValue("Error connecting:${e.message}")
                }
                miloClient.execute(client)
            }
        }
    }

    fun unsubscribe() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _opcUaClient.value?.let {
                    try {
                        it.disconnect().get()
                        delay(500)
                        _data.postValue("OpcUaClient is disconnected!!!")
                        Stack.releaseSharedResources()
                    } catch (e: InterruptedException) {
                        Logger.e("Error disconnecting:${e.message}", e)
                    } catch (e: ExecutionException) {
                        Logger.e("Error disconnecting:${e.message}", e)
                    }
                }
            }
        }
    }

    fun read() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val miloClient = ReadExample(_data)
                val client = MiloClientRunner(
                    getKeyStoreLoader(getContext()),
                    miloClient
                ).get()

                Logger.e("ReadExample::OpcUaClient = $client")

                try {
                    client.connect().get()
                    _data.postValue("OpcUaClient is connected...")

                    delay(1000)

                    miloClient.execute(client)

                    client.disconnect().get()
                    Stack.releaseSharedResources()
                } catch (e: InterruptedException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _data.postValue("Error connecting:${e.message}")
                } catch (e: ExecutionException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _data.postValue("Error connecting:${e.message}")
                }
            }
        }
    }

    fun browse() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val miloClient = BrowseExample(_data)
                val client = MiloClientRunner(
                    getKeyStoreLoader(getContext()),
                    miloClient
                ).get()

                try {
                    client.connect().get()
                    _data.postValue("OpcUaClient is connected...")

                    delay(1000)

                    miloClient.execute(client)

                    client.disconnect().get()
                    Stack.releaseSharedResources()
                } catch (e: InterruptedException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _data.postValue("Error connecting:${e.message}")
                } catch (e: ExecutionException) {
                    Logger.e("Error connecting:${e.message}", e)
                    _data.postValue("Error connecting:${e.message}")
                }
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