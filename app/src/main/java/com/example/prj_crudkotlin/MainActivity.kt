package com.example.prj_crudkotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.prj_crudkotlin.roomDB.Pessoa
import com.example.prj_crudkotlin.roomDB.PessoaDataBase
import com.example.prj_crudkotlin.ui.theme.PRJ_CRUDKotlinTheme
import com.example.prj_crudkotlin.viewModel.PessoaViewModel
import com.example.prj_crudkotlin.viewModel.Repository


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon



import androidx.compose.material3.*

import androidx.compose.material.icons.filled.Settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Phone


import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration


class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            PessoaDataBase::class.java,
            "pessoa.db"
        ).build()
    }

    private val viewModel by viewModels<PessoaViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PessoaViewModel(Repository(db)) as T
                }
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PRJ_CRUDKotlinTheme {
                App(viewModel, this)
            }
        }
    }

}

@Composable
fun App(viewModel: PessoaViewModel, mainActivity: MainActivity) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Cadastro", "Configurações")

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Black) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.Home, contentDescription = item)
                                1 -> Icon(Icons.Filled.Person, contentDescription = item)
                                2 -> Icon(Icons.Filled.Settings, contentDescription = item)
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> HomeScreen(viewModel, mainActivity)
                1 -> CadastroScreen(viewModel)
                2 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: PessoaViewModel, mainActivity: MainActivity) {
    var pessoaList by remember { mutableStateOf(listOf<Pessoa>()) }
    viewModel.getPessoa().observe(mainActivity) { pessoaList = it }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lista de Contatos",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.White
        )

        Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.padding(vertical = 10.dp))

        LazyColumn {
            items(pessoaList) { pessoa ->
                val context = LocalContext.current

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Ícone de pessoa",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = pessoa.name, color = Color.White)
                    }

                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Ícone de telefone",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ClickableText(
                            text = AnnotatedString(pessoa.telefone),
                            style = LocalTextStyle.current.copy(
                                color = Color.White,
                                textDecoration = TextDecoration.Underline
                            ),
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${pessoa.telefone}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CadastroScreen(viewModel: PessoaViewModel) {
    var nome by remember { mutableStateOf(TextFieldValue("")) }
    var telefone by remember { mutableStateOf(TextFieldValue("")) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastrar Contatos",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.padding(vertical = 10.dp))

        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = telefone,
            onValueChange = {

                if (it.text.all { char -> char.isDigit() }) {
                    telefone = it
                }
            },
            label = { Text("Telefone") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (nome.text.isNotEmpty() && telefone.text.isNotEmpty()) {
                        viewModel.upsertPessoa(Pessoa(nome.text, telefone.text))
                        nome = TextFieldValue("")
                        telefone = TextFieldValue("")
                        keyboardController?.hide()
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nome.text.isNotEmpty() && telefone.text.isNotEmpty()) {
                    viewModel.upsertPessoa(Pessoa(nome.text, telefone.text))
                    nome = TextFieldValue("")
                    telefone = TextFieldValue("")
                    keyboardController?.hide()
                }
            }
        ) {
            Text("Cadastrar")
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Configurações",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.White
        )

        Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.padding(vertical = 10.dp))

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Desenvolvido por Ana Beatriz 1ADS-AMS",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}
