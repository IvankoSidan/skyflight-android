package com.wheezy.skyflight.core.network.api

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.LocationModel
import com.wheezy.skyflight.core.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body userRegisterDto: UserRegisterDto): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body userLoginDto: UserLoginDto): Response<AuthResponse>

    @POST("/api/auth/google")
    suspend fun googleAuth(@Body googleAuthDto: GoogleAuthDto): Response<AuthResponse>

    @POST("/api/auth/refresh")
    suspend fun refreshToken(): Response<AuthResponse>

    @GET("/api/auth/me")
    suspend fun getCurrentUser(): Response<AuthResponse>

    @GET("/api/flights/locations")
    suspend fun getLocations(): Response<List<LocationModel>>

    @GET("/api/flights/search")
    suspend fun searchFlights(
        @Query("departureCity") from: String,
        @Query("arrivalCity") to: String,
        @Query("flightDate") date: String? = null,
        @Query("classType") classType: String? = null
    ): Response<List<FlightModel>>

    @GET("/api/flights/{flightId}")
    suspend fun getFlightById(@Path("flightId") flightId: Long): Response<FlightModel>

    @GET("/api/flights/class-seats")
    suspend fun getClassSeats(): Response<List<String>>

    @POST("/api/bookings")
    suspend fun createBooking(@Body bookingDto: BookingRequestDto): Response<BookingResponseDTO>

    @GET("/api/bookings/flight/{flightId}")
    suspend fun getBookedSeats(@Path("flightId") flightId: Long): Response<List<String>>

    @POST("/api/payments/sheet")
    suspend fun createPaymentSheet(@Body request: PaymentSheetRequest): Response<PaymentSheetResponseDTO>

    @GET("/api/bookings/my")
    suspend fun getMyBookings(): Response<List<BookingDetailsDTO>>

    @PUT("/api/bookings/{bookingId}/status")
    suspend fun updateBookingStatus(
        @Path("bookingId") bookingId: Long,
        @Body request: BookingStatusUpdateRequest
    ): Response<Unit>

    @POST("/api/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long): Response<Unit>

    @DELETE("/api/bookings/{id}")
    suspend fun deleteBooking(@Path("id") id: Long): Response<Unit>

    @POST("/api/fcm/register")
    suspend fun registerFCMToken(@Body request: Map<String, String>): Response<Void>

    @DELETE("/api/fcm/unregister")
    suspend fun unregisterFCMToken(@Body request: Map<String, String>): Response<Void>

    @POST("/api/promocodes/validate")
    suspend fun validatePromocode(@Body request: PromocodeRequest): Response<PromocodeResponse>

    @GET("/api/referrals/my-code")
    suspend fun getReferralCode(): Response<ReferralCodeResponse>

    @POST("/api/referrals/apply")
    suspend fun applyReferralCode(@Body request: ReferralApplyRequest): Response<ReferralApplyResponse>

    @GET("/api/referrals/my-referrals")
    suspend fun getMyReferrals(): Response<ReferralInfoResponse>
}