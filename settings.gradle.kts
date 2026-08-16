pluginManagement {
    includeBuild("gradle-conventions")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ibn-alsham"

// Android App
include(":androidApp")

// iOS Framework Aggregator
include(":kmm:shared")

// KMM Library layer
include(":kmm:library:di")
include(":kmm:library:core:common")
include(":kmm:library:core:environment")
include(":kmm:library:core:stringsResources")
include(":kmm:library:core:date")
include(":kmm:library:core:priceFormatter")
include(":kmm:library:data:network")
include(":kmm:library:data:network-error")
include(":kmm:library:data:cache")
include(":kmm:library:data:database")
include(":kmm:library:presentation:mvi")
include(":kmm:library:presentation:ui")
include(":kmm:library:cross:analytics")
include(":kmm:library:cross:logger")
include(":kmm:library:cross:tracker")
include(":kmm:library:test")

// KMM Infra layer
include(":kmm:infra:analytics")
include(":kmm:infra:authentication")
include(":kmm:infra:deepLinking")
include(":kmm:infra:featureFlag")
include(":kmm:infra:logger")
include(":kmm:infra:notification")
include(":kmm:infra:platformFile")
include(":kmm:infra:tracker")
include(":kmm:infra:userSettings")

// KMM Service layer
include(":kmm:service:authentication")
include(":kmm:service:customer")
include(":kmm:service:deepLinking")
include(":kmm:service:featureFlag")
include(":kmm:service:i18n")
include(":kmm:service:notification")
include(":kmm:service:platformFile")

// KMM Data layer (API domains)
include(":kmm:data:auth-api")
include(":kmm:data:home-api")
include(":kmm:data:menu-api")
include(":kmm:data:order-api")
include(":kmm:data:profile-api")
include(":kmm:data:favorites-api")
include(":kmm:data:notification-api")
include(":kmm:data:settings-api")
include(":kmm:data:catalog-api")
include(":kmm:data:product-api")
include(":kmm:data:search-api")
include(":kmm:data:cart-api")
include(":kmm:data:payment-api")
include(":kmm:data:address-api")
include(":kmm:data:coupon-api")
include(":kmm:data:wallet-api")
include(":kmm:data:upload-api")
include(":kmm:data:review-api")
include(":kmm:data:checkout-api")
include(":kmm:data:shipping-api")

// KMM Feature layer
include(":kmm:feature:domain")
include(":kmm:feature:navigation")
include(":kmm:feature:splash")
include(":kmm:feature:auth")
include(":kmm:feature:home")
include(":kmm:feature:menu")
include(":kmm:feature:productDetails")
include(":kmm:feature:cart")
include(":kmm:feature:checkout")
include(":kmm:feature:orders")
include(":kmm:feature:profile")
include(":kmm:feature:favorites")
include(":kmm:feature:notifications")
include(":kmm:feature:more")
include(":kmm:feature:catalog")
include(":kmm:feature:search")
include(":kmm:feature:wishlist")
include(":kmm:feature:settings")
include(":kmm:feature:wallet")
include(":kmm:feature:coupons")
include(":kmm:feature:sellers")
include(":kmm:feature:blog")
include(":kmm:feature:chat")
include(":kmm:feature:auctions")
include(":kmm:feature:classifieds")
include(":kmm:feature:digital")
include(":kmm:feature:upload")

// Android UI layer
include(":ui:design:designSystem")
include(":ui:design:helpers")
include(":ui:feature:splash-impl")
include(":ui:feature:auth-impl")
include(":ui:feature:home-impl")
include(":ui:feature:menu-impl")
include(":ui:feature:productDetails-impl")
include(":ui:feature:cart-impl")
include(":ui:feature:checkout-impl")
include(":ui:feature:orders-impl")
include(":ui:feature:profile-impl")
include(":ui:feature:favorites-impl")
include(":ui:feature:notifications-impl")
include(":ui:feature:more-impl")
include(":ui:feature:settings-impl")
include(":ui:feature:catalog-impl")
include(":ui:feature:search-impl")
