package com.ibnalsham.app.data.search

import com.ibnalsham.app.data.home.CategoryDto
import com.ibnalsham.app.data.home.toDomain
import com.ibnalsham.app.feature.domain.FilterOptionsDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilterOptionsDto(
    val categories: List<CategoryDto> = emptyList(),
    @SerialName("min_price") val minPrice: Double = 0.0,
    @SerialName("max_price") val maxPrice: Double = 0.0,
    val currency: String = "JOD",
    val sorts: List<String> = emptyList(),
)

fun FilterOptionsDto.toDomain(): FilterOptionsDomain = FilterOptionsDomain(
    categories = categories.map { it.toDomain() },
    minPrice = minPrice,
    maxPrice = maxPrice,
)
