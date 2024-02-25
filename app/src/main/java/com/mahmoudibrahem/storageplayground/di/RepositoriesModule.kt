package com.mahmoudibrahem.storageplayground.di

import com.mahmoudibrahem.storageplayground.repository.images.ImagesRepository
import com.mahmoudibrahem.storageplayground.repository.images.ImagesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Provides
    @Singleton
    fun provideImagesRepository(): ImagesRepository {
        return ImagesRepositoryImpl()
    }

}