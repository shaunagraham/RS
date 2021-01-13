package com.rap.sheet.di

import android.content.Context
import android.content.SharedPreferences
import com.rap.sheet.BuildConfig
import com.rap.sheet.retrofit.EveryOneAPIInterface
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.viewmodel.*
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

val applicationModule = module {
    single<SharedPreferences> {
        androidContext().getSharedPreferences(
                "rs_user_info",
                Context.MODE_PRIVATE
        )
    }
}

val viewModelModule = module {
    viewModel { SplashViewModel(get()) }
    viewModel { ProfileDetailViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { MyContactViewModel(get()) }
    viewModel { ContactDetailViewModel(get()) }
    viewModel { CommentViewModel(get()) }
    viewModel { BlackListViewModel(get()) }
    viewModel { AddContactViewModel(get()) }
    viewModel { FeedbackViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { GetProfileViewModel(get()) }
}

val appModule = module {

//    single {
//        createWebService<RestInterface>(
//                okHttpClient = get(),
//                baseUrl = BuildConfig.APIPATH
//        )
//    }

    single {
        OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
    }

    single<Retrofit>(named("everyone")) {
        Retrofit.Builder()
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(BuildConfig.EVERYONE_API)
                .build()
    }

    single<Retrofit>(named("normal")) {
        Retrofit.Builder()
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(BuildConfig.APIPATH)
                .build()
    }
}

//fun createHttpClient(): OkHttpClient {
//    val client = OkHttpClient.Builder()
//    client.readTimeout(5 * 60, TimeUnit.SECONDS)
//    return client.build()
//}

/* function to build our Retrofit service */
inline fun <reified T> createWebService(
        okHttpClient: OkHttpClient,
        baseUrl: String
): T {
    val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okHttpClient)
            .build()
    return retrofit.create(T::class.java)
}

val moreRetrosModule = module {
    single { get<Retrofit>(named("everyone")).create(EveryOneAPIInterface::class.java) }
    single { get<Retrofit>(named("normal")).create(RestInterface::class.java) }
}

/**
 * List of all modules.
 */
val mainAppModules = listOf(
        appModule,
        applicationModule,
        viewModelModule,
        moreRetrosModule
)