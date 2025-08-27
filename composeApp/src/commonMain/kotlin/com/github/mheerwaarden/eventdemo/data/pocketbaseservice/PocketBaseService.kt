package com.github.mheerwaarden.eventdemo.data.pocketbaseservice

import com.github.mheerwaarden.eventdemo.data.model.Event
import com.github.mheerwaarden.eventdemo.data.model.IEvent
import com.github.mheerwaarden.eventdemo.data.model.User
import com.github.mheerwaarden.eventdemo.data.preferences.PocketBaseClientType
import kotlinx.coroutines.flow.SharedFlow

interface PocketBaseService {
    // For emitting events from the SSE stream to multiple collectors
    val eventSubscriptionFlow: SharedFlow<SubscriptionState<IEvent>>

    /**
     * Subscribes to real-time updates for events.
     *
     * This function establishes a connection to the PocketBase real-time service and listens for
     * create or update events on the "events" collection. When an event is received,
     * the provided `onUpdate` callback is invoked with the updated [IEvent].
     *
     * @param onUpdate A callback function that will be executed when an event is created or updated.
     *                 It receives the updated [EventRecord] as a parameter.
     * @return A function that can be called to unsubscribe from the event updates.
     *         Calling this function will cancel the subscription and close the real-time connection.
     *         If the initial subscription fails, an empty function is returned.
     */
    fun subscribeToEvents(onUpdate: (IEvent) -> Unit): () -> Unit
    fun startListeningToEvents(collectionNames: List<String> = listOf("events"))
    fun stopListeningToEvents()

    /**
     * Cleans up resources used by the PocketBaseService.
     * This method should be called when the service is no longer needed, for example,
     * in a ViewModel's `onCleared()` method.
     * It ensures that any open connections or subscriptions are properly closed
     * to prevent resource leaks.
     */
    fun cleanup()

    /**
     * Authenticates a user with the given email and password.
     *
     * This function attempts to log in a user using their email and password.
     * On successful authentication, it returns an [AuthResult] containing the user's
     * authentication token and user model.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return A [PocketBaseResult] which is either a [PocketBaseResult.Success] containing an [AuthResult]
     *         on successful login, or a [PocketBaseResult.Failure] containing an error message if
     *         the login fails.
     */
    suspend fun login(email: String, password: String): PocketBaseResult<AuthResult>

    /**
     * Logs out the current user.
     *
     * This function sends a request to the PocketBase server to log out the currently authenticated
     * user. The user's session will always be invalidated, and they will no longer be able to
     * access protected resources.
     *
     * @return A [PocketBaseResult] indicating the success or failure of the logout operation.
     *         If the logout is successful, the result will contain [Unit].
     *         If the logout fails (e.g., due to a network error or an invalid session), the result
     *         will contain an error.
     */
    suspend fun logout(): PocketBaseResult<Unit>

    /**
     * Retrieves a list of events from the PocketBase backend.
     *
     * This function makes an API call to fetch all records from the "events" collection.
     *
     * @return A [PocketBaseResult] containing either a [List] of [Event] objects on success,
     *         or an error if the request fails.
     */
    suspend fun getEvents(): PocketBaseResult<List<IEvent>>

    /**
     * Creates a new event in the PocketBase backend.
     *
     * @param event The [IEvent] object to be created.
     * @return A [PocketBaseResult] indicating the success or failure of the operation.
     *         If successful, the result will contain the created [Event] object.
     */
    suspend fun createEvent(event: IEvent): PocketBaseResult<IEvent>

    /**
     * Updates an existing event in the PocketBase backend.
     *
     * @param event The [IEvent] object containing the updated information. The `id` field of this
     *              object must correspond to an existing event in the database.
     * @return A [PocketBaseResult] indicating the success or failure of the update operation.
     *         If successful, the [PocketBaseResult.Success] will contain the updated [Event] object
     *         as returned by the server.
     */
    suspend fun updateEvent(event: IEvent): PocketBaseResult<IEvent>

    /**
     * Deletes an event with the specified ID.
     *
     * @param eventId The ID of the event to delete.
     * @return A [PocketBaseResult] indicating the success or failure of the deletion.
     *         If successful, the result will contain `true`.
     */
    suspend fun deleteEvent(eventId: String): PocketBaseResult<Boolean>

    /**
     * Register a new user with the given email and password and auto-login after successful registration.
     * @param email The user's email.
     * @param password The user's password.
     * @param passwordConfirm The user's password confirmation.
     * @param name The user's name.
     * @return A [Result] indicating the success or failure of the registration and login process.
     */
    suspend fun register(
        email: String,
        password: String,
        passwordConfirm: String,
        name: String
    ): PocketBaseResult<AuthResult>

    /**
     * Checks if the user is currently authenticated.
     * @return `true` if the user is authenticated, `false` otherwise.
     */
    suspend fun isAuthenticated(): Boolean
    /**
     * Retrieves the currently authenticated user.
     *
     * This function attempts to fetch the current user's data from the PocketBase authentication store.
     * If a user is authenticated and their data is available, it's returned as a [User] object.
     * If no user is authenticated or an error occurs during the fetch, `null` is returned.
     *
     * @return A [PocketBaseResult] containing the [User] object if successful, or `null` otherwise.
     */
    suspend fun getCurrentUser(): PocketBaseResult<User?>
}

expect class PocketBaseServiceFactory {
    fun create(baseUrl: String, clientType: PocketBaseClientType): PocketBaseService
}