package allen.town.podcast.dialog

import allen.town.focus_common.views.AccentMaterialDialog
import allen.town.podcast.R
import allen.town.podcast.databinding.AuthenticationDialogBinding
import android.content.Context
import android.content.DialogInterface
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View

/**
 * Displays a dialog with a username and password text field and an optional checkbox to save username and preferences.
 */
abstract class AuthenticationDialog(
    context: Context?, titleRes: Int, enableUsernameField: Boolean,
    usernameInitialValue: String?, passwordInitialValue: String?
) : AccentMaterialDialog(
    context!!, R.style.MaterialAlertDialogTheme
) {
    var passwordHidden = true
    protected open fun onCancelled() {}
    protected abstract fun onConfirmed(username: String?, password: String?)

    init {
        setTitle(titleRes)
        val viewBinding = AuthenticationDialogBinding.inflate(LayoutInflater.from(context))
        setView(viewBinding.root)
        viewBinding.usernameEditText.isEnabled = enableUsernameField
        if (usernameInitialValue != null) {
            viewBinding.usernameEditText.setText(usernameInitialValue)
        }
        if (passwordInitialValue != null) {
            viewBinding.passwordEditText.setText(passwordInitialValue)
        }
        viewBinding.showPasswordButton.setOnClickListener { v: View? ->
            if (passwordHidden) {
                viewBinding.passwordEditText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                viewBinding.showPasswordButton.alpha = 1.0f
            } else {
                viewBinding.passwordEditText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                viewBinding.showPasswordButton.alpha = 0.6f
            }
            passwordHidden = !passwordHidden
        }
        setOnCancelListener { dialog: DialogInterface? -> onCancelled() }
        setNegativeButton(R.string.cancel_label) { dialog: DialogInterface?, which: Int -> onCancelled() }
        setPositiveButton(R.string.confirm_label) { dialog: DialogInterface?, which: Int ->
            onConfirmed(
                viewBinding.usernameEditText.text.toString(),
                viewBinding.passwordEditText.text.toString()
            )
        }
    }
}