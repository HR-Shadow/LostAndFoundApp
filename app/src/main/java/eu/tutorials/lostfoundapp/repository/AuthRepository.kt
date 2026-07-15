package eu.tutorials.lostfoundapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.lostfoundapp.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("User creation failed")

        val user = User(
            userId = firebaseUser.uid,
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim()
        )
        firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .set(user.toMap())
            .await()
        user
    }

    suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("Sign in failed")
        getUserProfile(firebaseUser.uid).getOrThrow()
    }

    suspend fun getUserProfile(userId: String): Result<User> = runCatching {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
        if (snapshot.exists()) {
            User.fromMap(snapshot.data ?: emptyMap())
        } else {
            val firebaseUser = auth.currentUser
            User(
                userId = userId,
                name = firebaseUser?.displayName ?: "",
                email = firebaseUser?.email ?: ""
            )
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
