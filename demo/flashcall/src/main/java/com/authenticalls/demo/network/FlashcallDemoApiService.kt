import com.authenticalls.demo.network.models.FlashcallRequestBody
import com.authenticalls.demo.network.models.FlashcallResponse
import com.authenticalls.demo.network.models.FlashcallValidateBody
import com.authenticalls.demo.network.models.FlashcallValidateResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val httpLogger = run {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.apply {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    }
}

internal interface FlashcallDemoApiService {
    @POST("phone_validation/request/")
    suspend fun requestFlashcall(
        @Body body: FlashcallRequestBody
    ): FlashcallResponse

    @POST("phone_validation/validate/")
    suspend fun validateFlashcall(
        @Body body: FlashcallValidateBody
    ): FlashcallValidateResponse
}

/**
 * Singleton that implements FlashcallDemoApiService
 * Use this object for all API calls
 */
internal object FlashcallDemoApiInternal {
    lateinit var retrofitService: FlashcallDemoApiService
    var isEmulator: Boolean = false
    val isReady: Boolean
        get() = ::retrofitService.isInitialized
}

internal var flashcallDemoInitialized = false

object FlashcallDemoApi {
    /**
     * Initializes Flashcall API
     * @param isEmulator Detect if emulator for testing purposes
     */

    private var BASE_URL = "https://demo.authenticalls.com/api/"

    fun initialize(isEmulator: Boolean) {
        if (flashcallDemoInitialized) {
            throw RuntimeException("FLashcall API already initialized")
        }

        val okHttpClient = OkHttpClient.Builder()
            .apply {
                addInterceptor(
                    Interceptor { chain ->
                        val builder = chain.request().newBuilder()
                        builder.header("User-Agent", "Flashcall-Android-API")
//                        builder.header("Authorization", "Bearer $apiKey")
                        return@Interceptor chain.proceed(builder.build())
                    }
                )
            }
            .apply {
                addNetworkInterceptor(httpLogger)
            }
            .build()

        if(isEmulator) {
            BASE_URL = "http://demoapp.klo.tech:8888/api/"
        }

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()

        FlashcallDemoApiInternal.retrofitService = retrofit.create(FlashcallDemoApiService::class.java)
        FlashcallDemoApiInternal.isEmulator = isEmulator

        flashcallDemoInitialized = true
    }
}