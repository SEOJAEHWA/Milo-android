package kr.co.aiblab.test.milo

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.aiblab.test.milo.client.MiloClientRunner
import kr.co.aiblab.test.milo.client.ReadExample

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AsyncTask.execute {
            MiloClientRunner(ReadExample()).run(this)
        }
    }
}
