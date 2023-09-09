package br.com.smartagro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import br.com.smartagro.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val pass = binding.editSenha.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                if (pass.length >= 6) {
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            val errorText = when (task.exception) {
                                is FirebaseAuthInvalidUserException -> "Usuário não existe."
                                is FirebaseAuthInvalidCredentialsException -> "Credenciais inválidas."
                                else -> "Erro ao fazer login. Tente novamente mais tarde."
                            }
                            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Campos vazios não são permitidos.", Toast.LENGTH_LONG).show()
            }
        }


        binding.txtCriarConta.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }
}