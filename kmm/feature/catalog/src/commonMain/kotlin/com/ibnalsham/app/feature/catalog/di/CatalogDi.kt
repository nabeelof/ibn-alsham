package com.ibnalsham.app.feature.catalog.di

import com.ibnalsham.app.feature.catalog.presentation.viewModel.CatalogViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val catalogFeatureModule = module {
    factoryOf(::CatalogViewModel)
}
