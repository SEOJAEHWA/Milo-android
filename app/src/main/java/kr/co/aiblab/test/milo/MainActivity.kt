package kr.co.aiblab.test.milo

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
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

        val viewModel = ViewModelProvider(
            this,
            OpcUaViewModelFactory(application)
        ).get(OpcUaViewModel::class.java)

        viewModel.data.observe(this) {
            Logger.i("DATA >> $it")
            updateDataText(it)
        }

        viewModel.status.observe(this) {
            Logger.i("STATUS >> $it")
            updateStatusText(it)
        }

        abtn_connection.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) {
                    viewModel.doConnect()
                } else {
                    viewModel.doDisconnect()
                }
            }
        }

        btn_read.setOnClickListener {
            viewModel.read()
        }

        btn_browse.setOnClickListener {
            viewModel.browse()
        }

        btn_subscribe.setOnClickListener {
            viewModel.subscribe()
        }

        btn_subscribe_coord.setOnClickListener {
            viewModel.subscribeCoordination()
        }

        btn_method.setOnClickListener {
            viewModel.method()
        }

        tv_info.movementMethod = ScrollingMovementMethod()
    }

    private fun appendTextAndScroll(text: String) {
        with(tv_info) {
//            append("$text\n".trimIndent())
            this.text = text
            tv_info.layout?.let {
                if (layout != null) {
                    val scrollDelta: Int = layout.getLineBottom(lineCount - 1) - scrollY - height
                    if (scrollDelta > 0) scrollBy(0, scrollDelta)
                }
            }
        }
    }

    private fun updateDataText(message: String) {
        if (TextUtils.isEmpty(tv_info.text)) {
            appendTextAndScroll(message)
        } else {
            val newMessage = tv_info.text.toString() + "\n\n" + message
            appendTextAndScroll(newMessage)
        }
    }

    private fun updateStatusText(status: String) {
        tv_conn_status.text = status
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
