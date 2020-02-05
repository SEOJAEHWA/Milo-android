package kr.co.aiblab.test.milo

import android.content.Context
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kr.co.aiblab.test.milo.viewmodel.OpcUaViewModel
import kr.co.aiblab.test.milo.viewmodel.OpcUaViewModelFactory
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        initLogger()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val model = ViewModelProvider(
            this,
            OpcUaViewModelFactory(application)
        ).get(OpcUaViewModel::class.java)

        model.data.observe(this) {
            Logger.i("Subscribing.... >> $it")
            updateText(it)
        }

        btn_read.setOnClickListener {
            model.read()
        }

        btn_browse.setOnClickListener {
            model.browse()
        }

        btn_subscribe_on.setOnClickListener {
            model.subscribe()
        }

        btn_subscribe_off.setOnClickListener {
            model.unsubscribe()
        }
    }

    private fun updateText(message: String) {
        tv_info.text = message
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

        fun getSecureDirectory(context: Context): File {
            val directory = File("${context.filesDir}${File.separator}secure${File.separator}")
            if (!directory.exists()) {
                directory.mkdir()
            }
            return directory
        }
    }
}
