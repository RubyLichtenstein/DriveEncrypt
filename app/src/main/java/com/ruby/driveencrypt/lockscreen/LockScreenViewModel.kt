package com.ruby.driveencrypt.lockscreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

interface ILockScreenViewModel {
    fun onNextClick()
    fun onNumberClick()
    fun onCodeValidationFailed()
    fun onCodeCreated()
    fun onLoginFailed()
    fun onCodeCompleted()
    fun isCreateMode(): Boolean
}

interface IView {
    fun setTitleText()
    fun isNextActive()
}

class LockScreenViewModel(application: Application) : AndroidViewModel(application),
    ILockScreenViewModel {

    val titleCreate = "Create Code"
    val titleAuth = "Unlock with your pin code or fingerprint"
    val titleChange = "Please confirm your pin to continue."

    private val pinPreferences = PinPreferences()
    private val codeLength = 4
    private var mCodeValidation: String? = ""

    val codeLiveData = MutableLiveData("")
    val modeLiveData = MutableLiveData<Mode>()
    val finishLiveData = MutableLiveData<Unit>()
    val showMainActivityLiveData = MutableLiveData<Unit>()
    val titleLiveData = MutableLiveData<String>()
    val wrongCodeLiveData = MutableLiveData<Unit>()
    val nextButtonEnabledLiveData = MutableLiveData<Boolean>(false)
    val addOrRemoveDotLiveData = MutableLiveData<Boolean>()

    enum class Mode {
        CREATE,
        AUTH,
        VALIDATION
    }

    data class ViewState(
        val deleteEnabled: Boolean,
        val nextEnabled: Boolean
    )

    init {
        val pinExist = pinPreferences.isPinExist(getApplication())
        modeLiveData.value = if (pinExist)
            Mode.AUTH
        else
            Mode.CREATE

        titleLiveData.value = getTitleText()
    }

    private fun getTitleText(): String {
        return when (modeLiveData.value) {
            Mode.CREATE -> if (changeCodeMode) titleChange else titleCreate
            Mode.AUTH -> titleAuth
            else -> ""
        }
    }

    var changeCodeMode = false

    private fun onCodeCreated(code: String) {
//        Toast.makeText(requireContext(), "Code created", Toast.LENGTH_SHORT).show()
        closeLockScreen()
    }

    private fun closeLockScreen() {
        if (changeCodeMode) {
            finishLiveData.value = Unit
        } else {
            showMainActivityLiveData.value = Unit
        }
    }

    private fun validateCode(
        code: String,
        codeValidation: String
    ) {
        if (code == codeValidation) {
            pinPreferences.savePin(getApplication(), code)
            onCodeCreated(code)
        } else {
            titleLiveData.value = "Code validation failed."
            clearCode()
        }
    }

    fun clearCode() {
        codeLiveData.value = ""
    }

    private fun setCode(code: String) {
        codeLiveData.value = code
        if (code.length == codeLength) {
            onCodeCompleted(code)
        }
    }

    fun codeDelete() {
        val currentCode = codeLiveData.value
        if (currentCode.isNullOrEmpty()) return
        setCode(currentCode.dropLast(1))
        addOrRemoveDotLiveData.value = false
    }

    fun input(number: String) {
        val currentCode = codeLiveData.value
        if (currentCode == null || currentCode.length == codeLength) return
        setCode(currentCode + number)
        addOrRemoveDotLiveData.value = true
    }

    override fun onNextClick() {
        if (modeLiveData.value === Mode.VALIDATION) {
            validateCode(
                codeLiveData.value!!,
                mCodeValidation!!
            )
        } else {
            modeLiveData.value = Mode.VALIDATION
            mCodeValidation = codeLiveData.value
            codeLiveData.value = ""
            titleLiveData.value = "Please input code again"
            clearCode()
        }
    }

    override fun onNumberClick() {
        TODO("Not yet implemented")
    }

    override fun onCodeValidationFailed() {
//        TODO("Not yet implemented")
    }

    override fun onCodeCreated() {
        TODO("Not yet implemented")
    }

    override fun onLoginFailed() {
        TODO("Not yet implemented")
    }

    override fun onCodeCompleted() {

    }

    private fun onCodeCompleted(code: String) {
        when (modeLiveData.value) {
            Mode.CREATE -> {
                nextButtonEnabledLiveData.value = true
            }

            Mode.AUTH -> {
                if (isCodeCorrect(code)) {
                    onCodeInputSuccessful()
                } else {
                    wrongCodeLiveData.value = Unit
                    clearCode()
                }
            }
            Mode.VALIDATION -> {
                validateCode(
                    codeLiveData.value!!,
                    mCodeValidation!!
                )
            }
            null -> {
            }
        }
    }

    private fun onCodeInputSuccessful() {
        closeLockScreen()
    }

    private fun isCodeCorrect(code: String): Boolean {
        return pinPreferences.checkPin(
            getApplication(),
            code
        )
    }

    override fun isCreateMode(): Boolean {
        val value = modeLiveData.value
        return value == Mode.CREATE
    }
}