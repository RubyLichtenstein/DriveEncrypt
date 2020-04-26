package com.ruby.driveencrypt.lockscreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

interface ILockScreenViewModel {
    fun onNextClick()
    fun onNumberClick(number: String)

    fun onCodeCompleted()
    fun onCodeValidationFailed()
    fun onCodeCreated()
    fun onCodeFailed()
    fun clearCode()
}

class LockScreenViewModel(application: Application) : AndroidViewModel(application),
    ILockScreenViewModel {

    val titleCreate = "Create Code"
    val titleAuth = "Unlock with your pin code or fingerprint"
    val titleChange = "Please confirm your pin to continue."

    private val pinPreferences = PinPreferences()
    private val codeLength = 4

    fun init(changeCode: Boolean) {
        val pinExist = pinPreferences.isPinExist(getApplication())

        val mode = if (pinExist)
            Mode.AUTH
        else
            Mode.CREATE

        val title = getTitleText(
            mode,
            changeCode
        )

        stateLiveData.value = State(
            mode = mode,
            changeCode = changeCode,
            code = "",
            title = title,
            codeValidation = ""
        )
    }

    val stateLiveData = MutableLiveData<State>()

    val finishLiveData = MutableLiveData<Unit>()
    val showMainActivityLiveData = MutableLiveData<Unit>()
    val wrongCodeLiveData = MutableLiveData<Unit>()
    val nextButtonEnabledLiveData = MutableLiveData(false)
    val addOrRemoveDotLiveData = MutableLiveData<Boolean>()

    data class State(
        val mode: Mode,
        val changeCode: Boolean,
        val code: String,
        val codeValidation: String,
        val title: String
    )

    enum class Mode {
        CREATE,
        AUTH,
        VALIDATION
    }

    private fun getTitleText(
        mode: Mode,
        changeCode: Boolean
    ): String {
        return when (mode) {
            Mode.CREATE -> if (changeCode)
                titleChange
            else
                titleCreate

            Mode.AUTH -> titleAuth
            else -> ""
        }
    }

    private fun onCodeCreated(code: String) {
//        Toast.makeText(requireContext(), "Code created", Toast.LENGTH_SHORT).show()
        closeLockScreen()
    }

    fun withState(block: State.() -> Unit) {
        stateLiveData.value?.let {
            block(it)
        }
    }

    fun newState(block: State.() -> State) {
        stateLiveData.value?.let {
            val newState = block(it)
            stateLiveData.value = newState
        }
    }

    private fun closeLockScreen() {
        withState {
            if (changeCode) {
                finishLiveData.value = Unit
            } else {
                showMainActivityLiveData.value = Unit
            }
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
            newState {
                copy(
                    title = "Code validation failed.",
                    code = ""
                )
            }
        }
    }

    private fun setCode(code: String) {
        newState {
            copy(code = code)
        }

        if (code.length == codeLength) {
            onCodeCompleted()
        }
    }

    fun codeDelete() {
        withState {
            if (code.isNotEmpty()) {
                setCode(code.dropLast(1))
                addOrRemoveDotLiveData.value = false
            }
        }
    }

    override fun onNumberClick(number: String) {
        withState {
            if (code.length == codeLength) return@withState
            setCode(code + number)
            addOrRemoveDotLiveData.value = true
        }
    }

    override fun onNextClick() {
        withState {
            if (mode === Mode.VALIDATION) {
                validateCode(
                    code,
                    codeValidation
                )
            } else {
                newState {
                    copy(
                        mode = Mode.VALIDATION,
                        codeValidation = this.code,
                        code = "",
                        title = "Please input code again"
                    )
                }
            }
        }
    }

    override fun onCodeValidationFailed() {
//        TODO("Not yet implemented")
    }

    override fun onCodeCreated() {
        TODO("Not yet implemented")
    }

    override fun onCodeFailed() {
        TODO("Not yet implemented")
    }

    override fun clearCode() {
        newState {
            copy(code = "")
        }
    }

    override fun onCodeCompleted() {
        withState {
            when (mode) {
                Mode.CREATE -> {
                    nextButtonEnabledLiveData.value = true
                }
                Mode.AUTH -> {
                    if (isCodeCorrect(code)) {
                        onCodeInputSuccessful()
                    } else {
                        wrongCodeLiveData.value = Unit
                        newState {
                            copy(code = "")
                        }
                    }
                }
                Mode.VALIDATION -> {
                    validateCode(
                        code,
                        codeValidation
                    )
                }
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
}