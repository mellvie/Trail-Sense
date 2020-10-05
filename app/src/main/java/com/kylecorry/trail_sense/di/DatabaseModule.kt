package com.kylecorry.trail_sense.di

import android.content.Context
import com.kylecorry.trail_sense.navigation.infrastructure.database.BeaconRepo
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.weather.infrastructure.database.PressureRepo
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providePressureRepo(@ApplicationContext context: Context): PressureRepo {
        return PressureRepo.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideBeaconRepo(@ApplicationContext context: Context): BeaconRepo {
        return BeaconRepo.getInstance(context)
    }

    @Singleton
    @Provides
    fun providePreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Singleton
    @Provides
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(context)
    }

}