package io.horizontalsystems.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.util.Log
import io.horizontalsystems.core.IKeyProvider
import io.horizontalsystems.core.IKeyStoreCleaner
import io.horizontalsystems.core.IKeyStoreManager
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.UnrecoverableKeyException
import java.util.logging.Logger
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyStoreManager(private val keyAlias: String, private val keyStoreCleaner: IKeyStoreCleaner)
    : IKeyStoreManager, IKeyProvider {

    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    private val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private val AUTH_DURATION_SEC = 86400 // 24 hours in seconds (24x60x60)

    private val keyStore: KeyStore

    private val logger = Logger.getLogger("KeyStoreManager")

    init {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
    }

    override val isKeyInvalidated: Boolean
        get() = try {
            validateKey()
            false
        } catch (ex: Exception) {
            logger.warning("isKeyInvalidated: \n ${Log.getStackTraceString(ex)}")
            ex is KeyPermanentlyInvalidatedException || ex is UnrecoverableKeyException
        }

    override val isUserNotAuthenticated: Boolean
        get() = try {
            validateKey()
            false
        } catch (ex: Exception) {
            logger.warning("isUserNotAuthenticated: \n ${Log.getStackTraceString(ex)}")
            ex is UserNotAuthenticatedException
        }

    override fun getKey(): SecretKey {
        try {
            keyStore.getKey(keyAlias, null)?.let {
                return it as SecretKey
            }
        } catch (ex: UnrecoverableKeyException) {
            //couldn't get key due to exception
        }
        return createKey()
    }

    override fun removeKey() {
        try {
            keyStore.deleteEntry(keyAlias)
        } catch (ex: KeyStoreException) {
            logger.warning("removeKey: \n ${Log.getStackTraceString(ex)}")
        }
    }

    override fun resetApp() {
        keyStoreCleaner.cleanApp()
    }

    @Synchronized
    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

        keyGenerator.init(KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(AUTH_DURATION_SEC)
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(PADDING)
                .build())

        return keyGenerator.generateKey()
    }

    private fun validateKey() {
        keyStoreCleaner.encryptedSampleText?.let { encryptedText ->
            val key = keyStore.getKey(keyAlias, null) ?: throw InvalidKeyException()
            CipherWrapper().decrypt(encryptedText, key)
        } ?: run {
            val text = CipherWrapper().encrypt("abc", getKey())
            keyStoreCleaner.encryptedSampleText = text
        }
    }

}
