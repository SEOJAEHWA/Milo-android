package kr.co.aiblab.test.milo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kr.co.aiblab.test.milo.MainActivity
import kr.co.aiblab.test.milo.client.BrowseExample
import kr.co.aiblab.test.milo.client.ReadExample
import kr.co.aiblab.test.milo.client.SubscribeExample
import kr.co.aiblab.test.milo.milo.KeyStoreLoader
import kr.co.aiblab.test.milo.milo.MiloClientRunner
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Stack
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
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

    private val _readData = MutableLiveData<List<DataValue?>?>()
    val readData: LiveData<List<DataValue?>?> = _readData

    fun subscribeA() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val miloClient = SubscribeExample()
                val client = MiloClientRunner(
                    getKeyStoreLoader(getContext()),
                    miloClient
                ).get()

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

    fun unsubscribeA() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                _opcUaClient.value?.let {
                    try {
                        it.disconnect().get()
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
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                /*with(
                    MiloClientRunner(
                        getKeyStoreLoader(getContext()),
                        ReadExample()
                    ).run()
                ) {
                    val state = ServerState.from(this!![0]!!.value.value as Int)
                    val currentTime = this[1]!!.value.value

                    Logger.d("State=$state")
                    Logger.d("CurrentTime=$currentTime")

                    _data.postValue("State=$state\nCurrentTime=$currentTime")
                }*/

//                MiloClientRunner(
//                    getKeyStoreLoader(getContext()),
//                    ReadExample()
//                ).run(_readData)

                val miloClient = ReadExample()
                val client = MiloClientRunner(
                    getKeyStoreLoader(getContext()),
                    miloClient
                ).get()

                try {
                    client.connect().get()
                    _data.postValue("OpcUaClient is connected...")

                    delay(1000)

                    miloClient.execute(client, _readData)

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
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                with(
                    MiloClientRunner(
                        getKeyStoreLoader(getContext()),
                        BrowseExample()
                    ).run()
                ) {
                    this?.let {
                        val stringBuilder = StringBuilder()
                        for (referenceDescription in it) {
                            stringBuilder.append("Node=${referenceDescription.browseName.name}")
                            stringBuilder.append("\n")
                            Logger.i("Node=${referenceDescription.browseName.name}")
                        }
                        _data.postValue(stringBuilder.toString())
                    }
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