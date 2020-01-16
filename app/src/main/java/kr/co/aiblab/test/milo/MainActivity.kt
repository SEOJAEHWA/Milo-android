package kr.co.aiblab.test.milo

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.aiblab.test.milo.client.BrowseExample
import kr.co.aiblab.test.milo.client.ReadExample
import kr.co.aiblab.test.milo.milo.KeyStoreLoader
import kr.co.aiblab.test.milo.milo.MiloClientRunner
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        initLogger()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_read.setOnClickListener {
            read(it.context)
        }

        btn_browse.setOnClickListener {
            browse(it.context)
        }
    }

    private fun read(context: Context) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // FIXME KeyStoreLoader 가 어떻게 활용되느냐에 맞춰 MiloClientLoader class 수정 예정
                val loader = getKeyStoreLoader(context)
                with(
                    MiloClientRunner(
                        loader,
                        ReadExample()
                    ).run()
                ) {
                    val state = ServerState.from(this!![0]!!.value.value as Int)
                    val currentTime = this[1]!!.value.value

                    Logger.d("State=$state")
                    Logger.d("CurrentTime=$currentTime")

                    showToast(context, "State=${state}")
                    setText("State=$state\nCurrentTime=$currentTime")
                }
            }
        }
    }

    private fun browse(context: Context) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val loader = getKeyStoreLoader(context)
                with(
                    MiloClientRunner(
                        loader,
                        BrowseExample()
                    ).run()
                ) {
                    this?.let {
                        for (referenceDescription in it) {
                            Logger.i("Node=${referenceDescription.browseName.name}")
                        }
                    }
                }
            }
        }
    }

    private suspend fun showToast(context: Context, message: String) =
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    private suspend fun setText(message: String) =
        withContext(Dispatchers.Main) {
            tv_info.text = message
        }

    private suspend fun getKeyStoreLoader(
        context: Context
    ): KeyStoreLoader = withContext(Dispatchers.Default) {
        val securityTempDir = File(getSecureDirectory(context).absolutePath)
        Logger.d("security temp dir: ${securityTempDir.absolutePath}")
        KeyStoreLoader().load(securityTempDir)
    }

    private fun initLogger() {
        Logger.clearLogAdapters()
        Logger.addLogAdapter(object : AndroidLogAdapter(
            PrettyFormatStrategy.newBuilder()
                .tag("MILO_TEST")
                .build()
        ) {
            override fun isLoggable(priority: Int, @Nullable tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    companion object {

        private fun getSecureDirectory(context: Context): File {
            val directory = File("${context.filesDir}${File.separator}secure${File.separator}")
            if (!directory.exists()) {
                directory.mkdir()
            }
            return directory
        }
    }
}
