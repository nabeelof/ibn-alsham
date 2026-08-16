package com.ibnalsham.app.feature.search.di

import com.ibnalsham.app.feature.search.presentation.viewModel.SearchViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val searchFeatureModule = module {
    factoryOf(::SearchViewModel)
}
