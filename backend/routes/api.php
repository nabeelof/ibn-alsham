<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\AddressController;
use App\Http\Controllers\Api\CartController;
use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\ClubPointController;
use App\Http\Controllers\Api\CouponController;
use App\Http\Controllers\Api\DeliveryController;
use App\Http\Controllers\Api\HomeController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\OrderController;
use App\Http\Controllers\Api\PaymentController;
use App\Http\Controllers\Api\PickupPointController;
use App\Http\Controllers\Api\ProductController;
use App\Http\Controllers\Api\ProfileController;
use App\Http\Controllers\Api\ReviewController;
use App\Http\Controllers\Api\SettingsController;
use App\Http\Controllers\Api\UploadController;
use App\Http\Controllers\Api\WalletController;
use App\Http\Controllers\Api\WishlistController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes - IBN ALSHAM Restaurant
|--------------------------------------------------------------------------
*/

// Locale middleware applies to all routes
Route::middleware(['set.locale'])->group(function () {

    // ── Auth (Public) ──────────────────────────────────────────────────
    Route::prefix('auth')->group(function () {
        Route::post('/send-otp', [AuthController::class, 'sendOtp']);
        Route::post('/verify-otp', [AuthController::class, 'verifyOtp']);
        Route::post('/register', [AuthController::class, 'register']);
        Route::post('/guest', [AuthController::class, 'guestLogin']);
        Route::post('/login', [AuthController::class, 'login']);
        Route::post('/social-login', [AuthController::class, 'socialLogin']);
        Route::post('/resend_code', [AuthController::class, 'resendCode']);
        Route::post('/confirm_code', [AuthController::class, 'confirmCode']);
        Route::post('/password/forget_request', [AuthController::class, 'passwordForgetRequest']);
        Route::post('/password/confirm_reset', [AuthController::class, 'passwordConfirmReset']);
        Route::post('/password/resend_code', [AuthController::class, 'passwordResendCode']);
        Route::get('/info', [AuthController::class, 'info']);
    });
    Route::post('/guest-customer-info-check', [AuthController::class, 'guestInfoCheck']);
    Route::post('/guest-user-account-create', [AuthController::class, 'guestAccountCreate']);

    // ── Public Endpoints ────────────────────────────────────────────────
    Route::get('/home', [HomeController::class, 'index']);
    Route::get('/categories', [ProductController::class, 'categories']);
    Route::get('/categories/featured', [CategoryController::class, 'featured']);
    Route::get('/categories/top', [CategoryController::class, 'top']);
    Route::get('/category/info/{slug}', [CategoryController::class, 'info']);
    Route::get('/filter/categories', [CategoryController::class, 'filterList']);
    Route::get('/filter/brands', fn () => response()->json([]));
    Route::get('/brands', fn () => response()->json([]));
    Route::get('/all-brands', fn () => response()->json([]));
    Route::get('/products', [ProductController::class, 'index']);
    Route::get('/products/featured', [ProductController::class, 'featured']);
    Route::get('/products/best-seller', [ProductController::class, 'bestSeller']);
    Route::get('/products/inhouse', [ProductController::class, 'inhouse']);
    Route::get('/products/todays-deal', [ProductController::class, 'todaysDeal']);
    Route::get('/products/search', [ProductController::class, 'search']);
    Route::get('/products/last-viewed', [ProductController::class, 'lastViewed']);
    Route::get('/products/category/{id}', [ProductController::class, 'categoryProducts']);
    Route::get('/products/brand/{slug}', [ProductController::class, 'brandProducts']);
    Route::get('/products/frequently-bought/{slug}', [ProductController::class, 'frequentlyBought']);
    Route::get('/products/top-from-seller/{slug}', [ProductController::class, 'topFromSeller']);
    Route::post('/products/variant/price', [ProductController::class, 'variantPrice']);
    Route::post('/varient-price', [ProductController::class, 'varientPrice']);
    Route::get('/products/{slug}/{userId}', [ProductController::class, 'detailsBySlug']);
    Route::get('/products/{product}', [ProductController::class, 'show']);
    Route::get('/get-search-suggestions', [ProductController::class, 'searchSuggestions']);
    Route::get('/wholesale/all-products', [ProductController::class, 'inhouse']);
    Route::get('/flash-deals', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/flash-deals/info/{slug}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/flash-deal-products/{id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/sliders', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/banners-one', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/banners-two', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/banners-three', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/flash-deals-banners', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/seller/products/categories', [ProductController::class, 'categories']);
    Route::get('/products/seller/{id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/shops', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/shops/details/{slug}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/shops/products/top/{id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/shops/products/new/{id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/shops/products/featured/{id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/seller/top', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/followed-seller/check/{id}', fn () => response()->json(['data' => false, 'success' => true, 'status' => 200]));
    Route::get('/followed-seller/store/{id}', fn () => response()->json(['result' => true]));
    Route::get('/followed-seller/remove/{id}', fn () => response()->json(['result' => true]));
    Route::get('/followed-seller', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/addon-list', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));

    // ── Cart (Guest or Auth) ─────────────────────────────────────────────
    Route::post('/carts', [CartController::class, 'index']);
    Route::post('/cart-count', [CartController::class, 'count']);
    Route::post('/cart-summary', [CartController::class, 'summary']);
    Route::post('/carts/add', [CartController::class, 'add']);
    Route::post('/carts/process', [CartController::class, 'process']);
    Route::delete('/carts/{cartItem}', [CartController::class, 'remove']);

    // ── Delivery Fee Calculator (Semi-public – can be called pre-login) ─
    Route::post('/delivery/calculate', [DeliveryController::class, 'calculate']);
    Route::get('/delivery-info', [DeliveryController::class, 'info']);
    Route::post('/shipping_cost', [DeliveryController::class, 'shippingCost']);
    Route::get('/pickup-list', [PickupPointController::class, 'list']);

    // ── Payments (Public checkout flow) ────────────────────────────────
    Route::get('/payment-types', [PaymentController::class, 'paymentTypes']);
    Route::post('/order/store', [PaymentController::class, 'orderStore']);
    Route::post('/payments/pay/cod', [PaymentController::class, 'payCod']);
    Route::post('/payments/pay/wallet', [PaymentController::class, 'payWallet']);
    Route::post('/payments/pay/manual', [PaymentController::class, 'payManual']);
    Route::get('/paypal/payment/url', [PaymentController::class, 'paypalUrl']);
    Route::post('/offline/payment/submit', fn () => response()->json(['result' => false, 'message' => 'Offline payment not configured']));
    Route::get('/customer-packages', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::post('/free/packages-payment', fn () => response()->json(['result' => false, 'message' => 'Not configured']));
    Route::post('/offline/packages-payment', fn () => response()->json(['result' => false, 'message' => 'Not configured']));
    Route::post('/coupon-apply', [CouponController::class, 'apply']);
    Route::post('/coupon-remove', [CouponController::class, 'remove']);
    Route::get('/coupon-list', [CouponController::class, 'list']);
    Route::get('/coupon-products/{id}', [CouponController::class, 'products']);
    Route::get('/languages', [SettingsController::class, 'languages']);
    Route::get('/currencies', [SettingsController::class, 'currencies']);
    Route::get('/business-settings', [SettingsController::class, 'businessSettings']);
    Route::post('/file/upload', [UploadController::class, 'upload']);
    Route::post('/file/image-upload', [UploadController::class, 'imageUpload']);
    Route::get('/file/all', [UploadController::class, 'list']);
    Route::get('/file/delete/{id}', [UploadController::class, 'delete']);
    Route::get('/reviews/product/{productId}', [ReviewController::class, 'productReviews']);
    Route::post('/reviews/submit', [ReviewController::class, 'submit']);

    // Chat
    Route::get('/chat/conversations', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/chat/messages/{conversation_id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::post('/chat/insert-message', fn () => response()->json(['result' => false, 'message' => 'Chat not configured']));
    Route::get('/chat/get-new-messages/{conversation_id}/{last_message_id}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::post('/chat/create-conversation', fn () => response()->json(['result' => false, 'message' => 'Chat not configured']));

    // Auction
    Route::get('/auction/products', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/auction/products/{slug}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::post('/auction/place-bid', fn () => response()->json(['result' => false, 'message' => 'Auctions not configured']));
    Route::get('/auction/bided-products', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/auction/purchase-history', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));

    // Classifieds
    Route::get('/classified/all', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/classified/own-products', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/classified/related-products/{slug}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/classified/product-details/{slug}', fn () => response()->json(['data' => [], 'success' => true, 'status' => 200]));
    Route::get('/classified/delete/{id}', fn () => response()->json(['result' => false, 'message' => 'Classifieds not configured']));
    Route::get('/classified/change-status/{id}', fn () => response()->json(['result' => false, 'message' => 'Classifieds not configured']));
    Route::post('/classified/store', fn () => response()->json(['result' => false, 'message' => 'Classifieds not configured']));
    Route::post('/classified/update/{id}', fn () => response()->json(['result' => false, 'message' => 'Classifieds not configured']));

    // ── Authenticated Routes ────────────────────────────────────────────
    Route::middleware(['auth:sanctum'])->group(function () {

        // Auth
        Route::post('/auth/logout', [AuthController::class, 'logout']);
        Route::post('/auth/account-deletion', [AuthController::class, 'deleteAccount']);

        // Profile
        Route::get('/profile', [ProfileController::class, 'show']);
        Route::put('/profile', [ProfileController::class, 'update']);
        Route::delete('/profile', [ProfileController::class, 'deleteAccount']);
        Route::post('/profile/update-device-token', [ProfileController::class, 'updateDeviceToken']);
        Route::post('/profile/update-image', [ProfileController::class, 'updateImage']);
        Route::post('/profile/check-phone-and-email', [ProfileController::class, 'checkPhoneAndEmail']);
        Route::get('/profile/counters', [ProfileController::class, 'counters']);
        Route::get('/customer/info', [ProfileController::class, 'customerInfo']);

        // Orders
        Route::get('/orders', [OrderController::class, 'index']);
        Route::post('/orders', [OrderController::class, 'store']);
        Route::get('/orders/{order}', [OrderController::class, 'show']);
        Route::get('/orders/{order}/status', [OrderController::class, 'status']);
        Route::post('/orders/{order}/cancel', [OrderController::class, 'cancel']);
        Route::post('/orders/{order}/confirm-delivery', [OrderController::class, 'confirmDelivery']);
        Route::post('/orders/{order}/review', [OrderController::class, 'review']);
        Route::post('/orders/{order}/reorder', [OrderController::class, 'reorder']);
        Route::get('/purchase-history', [OrderController::class, 'purchaseHistory']);
        Route::get('/purchase-history-details/{id}', [OrderController::class, 'purchaseHistoryDetails']);
        Route::get('/purchase-history-items/{id}', [OrderController::class, 'purchaseHistoryItems']);
        Route::get('/re-order/{id}', [OrderController::class, 'reorder']);
        Route::get('/order/cancel/{id}', [OrderController::class, 'cancel']);
        Route::get('/digital/purchased-list', [OrderController::class, 'digitalPurchasedList']);

        // Favorites
        Route::get('/favorites', [ProductController::class, 'favorites']);
        Route::post('/products/{product}/favorite', [ProductController::class, 'toggleFavorite']);
        Route::get('/wishlists', [WishlistController::class, 'index']);
        Route::delete('/wishlists/{wishlist_id}', [WishlistController::class, 'delete']);
        Route::get('/wishlists-check-product/{product_slug}', [WishlistController::class, 'check']);
        Route::get('/wishlists-add-product/{product_slug}', [WishlistController::class, 'add']);
        Route::get('/wishlists-remove-product/{product_slug}', [WishlistController::class, 'remove']);

        // Notifications
        Route::get('/notifications', [NotificationController::class, 'index']);
        Route::get('/notifications/unread-count', [NotificationController::class, 'unreadCount']);
        Route::post('/notifications/mark-all-read', [NotificationController::class, 'markAllRead']);
        Route::post('/notifications/bulk-delete', [NotificationController::class, 'bulkDelete']);
        Route::get('/all-notification', [NotificationController::class, 'index']);
        Route::get('/unread-notifications', [NotificationController::class, 'unreadCount']);

        // Wallet & Club Points
        Route::get('/wallet/balance', [WalletController::class, 'balance']);
        Route::get('/wallet/history', [WalletController::class, 'history']);
        Route::post('/wallet/offline-recharge', [WalletController::class, 'offlineRecharge']);
        Route::get('/clubpoint/get-list', [ClubPointController::class, 'list']);
        Route::post('/clubpoint/convert-into-wallet', [ClubPointController::class, 'convert']);

        // Addresses
        Route::get('/user/shipping/address', [AddressController::class, 'index']);
        Route::post('/user/shipping/create', [AddressController::class, 'create']);
        Route::post('/user/shipping/update', [AddressController::class, 'update']);
        Route::post('/user/shipping/update-location', [AddressController::class, 'updateLocation']);
        Route::post('/user/shipping/make_default', [AddressController::class, 'makeDefault']);
        Route::get('/user/shipping/delete/{id}', [AddressController::class, 'delete']);
        Route::get('/get-home-delivery-address', [AddressController::class, 'homeDelivery']);
        Route::get('/countries', [AddressController::class, 'countries']);
        Route::get('/states-by-country/{country_id}', [AddressController::class, 'statesByCountry']);
        Route::get('/cities-by-state/{state_id}', [AddressController::class, 'citiesByState']);
        Route::get('/cities-by-country/{country_id}', [AddressController::class, 'citiesByCountry']);
        Route::get('/areas-by-city/{city_id}', [AddressController::class, 'areasByCity']);
        Route::post('/update-address-in-cart', [AddressController::class, 'updateAddressInCart']);
        Route::post('/update-shipping-type-in-cart', [AddressController::class, 'updateShippingTypeInCart']);
    });
});
