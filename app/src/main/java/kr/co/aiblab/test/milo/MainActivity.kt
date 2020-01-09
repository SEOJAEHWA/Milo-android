package kr.co.aiblab.test.milo

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.aiblab.test.milo.client.MiloClientRunner
import kr.co.aiblab.test.milo.client.ReadExample

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_read.setOnClickListener {
            read(it.context)
        }
    }

    private fun read(context: Context) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // TODO
                //  1. 키스토어 키 생성, 로딩
                //  2. EndPointDescription 획득, 전달
                //  3. client 생성
                //  4. client connection 후 run 실행
                //      >> 얻어온 데이터(or Exception) 를 ui 로 넘김
                //  5. client disconnection, 완료 처리

                // TODO CompletableFuture 는 걷어내볼 것!


                MiloClientRunner(ReadExample()).run(context)
            }
        }
    }
}
