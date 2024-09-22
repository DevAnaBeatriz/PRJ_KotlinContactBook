package com.example.prj_crudkotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract.Colors
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.SnackbarDefaults.contentColor
import androidx.compose.runtime.livedata.observeAsState


import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


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
        val items = listOf("Home", "Cadastro", "Mensagem")

        var pessoaBeingEdited by remember { mutableStateOf<Pessoa?>(null) }
        var isEditing by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.Black) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                when (index) {
                                    0 -> Icon(Icons.Filled.Home, contentDescription = item)
                                    1 -> Icon(Icons.Filled.Person, contentDescription = item)
                                    2 -> Icon(Icons.Filled.Email, contentDescription = item)
                                }
                            },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = {
                                if (index == 1) {
                                    pessoaBeingEdited = null
                                    isEditing = false
                                }
                                selectedItem = index
                            }
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
                    0 -> HomeScreen(
                        viewModel,
                        mainActivity,
                        onNavigateToCadastro = { pessoa, editing ->
                            pessoaBeingEdited = pessoa
                            isEditing = editing
                            selectedItem = 1
                        })

                    1 -> CadastroScreen(
                        viewModel,
                        pessoaBeingEdited,
                        isEditing,
                        onNavigate = { screen ->
                            selectedItem = screen
                        })

                    2 -> SmsScreen(viewModel, mainActivity)
                }
            }
        }
    }

@Composable
fun showDeleteConfirmationDialog(mainActivity: MainActivity, pessoa: Pessoa, viewModel: PessoaViewModel) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Confirmar Deleção") },
            text = { Text("Você realmente deseja deletar ${pessoa.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePessoa(pessoa)
                        openDialog.value = false
                    }
                ) {
                    Text("Deletar")
                }
            },
            dismissButton = {
                Button(onClick = { openDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun HomeScreen(
    viewModel: PessoaViewModel,
    mainActivity: MainActivity,
    onNavigateToCadastro: (Pessoa?, Boolean) -> Unit
) {
    var pessoaList by remember { mutableStateOf(listOf<Pessoa>()) }
    var expandedItemId by remember { mutableStateOf<Long?>(null) }

    var openDialog by remember { mutableStateOf(false) }
    var pessoaToDelete by remember { mutableStateOf<Pessoa?>(null) }


    viewModel.getPessoa().observe(mainActivity) { pessoaList = it.sortedBy { it.name } }
    Column(
        Modifier.background(Color.Black)
    ){

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 80.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contatos",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Thin,
                fontSize = 55.sp,
                color = Color.White
            )
            Spacer(Modifier.padding(top = 10.dp))
            Box(
                modifier = Modifier
                    .size(200.dp, 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pessoaList.size} contatos encontrados",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.padding(bottom = 80.dp))

            LazyColumn(
                modifier = Modifier
                    .background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                items(pessoaList) { pessoa ->
                    Column (Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            expandedItemId =
                                if (expandedItemId == pessoa.id?.toLong()) null else pessoa.id?.toLong()
                        }
                        .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()

                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                ) ,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Ícone de pessoa",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                text = pessoa.name,
                                color = Color.Black
                            )
                        }

                        if (expandedItemId == pessoa.id?.toLong()) {
                            Column(Modifier.padding(start = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Ícone de telefone",
                                            tint = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pessoa.telefone ?: "Sem telefone",
                                            color = Color.Gray,
                                            modifier = Modifier.clickable {
                                                val uri = Uri.parse("tel:${pessoa.telefone}")
                                                val intent = Intent(Intent.ACTION_DIAL, uri)
                                                mainActivity.startActivity(intent)
                                            }
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = { onNavigateToCadastro(pessoa, true) }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = Color.Gray
                                            )
                                        }

                                        IconButton(onClick = {
                                            pessoaToDelete = pessoa
                                            openDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // diálogo de confirmação
        if (openDialog && pessoaToDelete != null) {
            AlertDialog(
                onDismissRequest = { openDialog = false },
                title = { Text("Confirmar Deleção") },
                text = { Text("Você realmente deseja deletar ${pessoaToDelete?.name}?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deletePessoa(pessoaToDelete!!)
                        openDialog = false
                        pessoaToDelete = null
                    }) {
                        Text("Deletar")
                    }
                },
                dismissButton = {
                    Button(onClick = { openDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun CadastroScreen(
        viewModel: PessoaViewModel,
        pessoaBeingEdited: Pessoa?,
        isEditing: Boolean,
        onNavigate: (Int) -> Unit
    ) {
        var nome by remember { mutableStateOf(TextFieldValue(pessoaBeingEdited?.name ?: "")) }
        var telefone by remember {
            mutableStateOf(
                TextFieldValue(
                    pessoaBeingEdited?.telefone ?: ""
                )
            )
        }

        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        fun formatPhoneNumber(text: String): String {
            val digits = text.filter { it.isDigit() }
            return when (digits.length) {
                in 0..2 -> digits
                in 3..6 -> "(${digits.substring(0, 2)}) ${digits.substring(2)}"
                in 7..10 -> "(${digits.substring(0, 2)}) ${
                    digits.substring(
                        2,
                        7
                    )
                }-${digits.substring(7)}"

                else -> "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${
                    digits.substring(
                        7,
                        11
                    )
                }"
            }
        }

        Column(
            Modifier
                .background(Color.Black)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = if (isEditing) "Atualizar" else "Cadastrar",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Thin,
                fontSize = 55.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Contato",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Thin,
                fontSize = 55.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))
            TextField(
                placeholder = {Text("Digite o nome...")},
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
                placeholder = { Text("Digite o número...") },
                value = telefone,
                onValueChange = {
                    val unformatted = it.text.filter { char -> char.isDigit() }
                    if (unformatted.length <= 11) {
                        val formatted = formatPhoneNumber(unformatted)
                        telefone = it.copy(text = formatted, selection = TextRange(formatted.length))
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
                            if (isEditing) {
                                viewModel.upsertPessoa(
                                    Pessoa(nome.text, telefone.text, pessoaBeingEdited!!.id)
                                )
                            } else {
                                viewModel.upsertPessoa(Pessoa(nome.text, telefone.text))
                            }
                            nome = TextFieldValue("")
                            telefone = TextFieldValue("")
                            keyboardController?.hide()

                            onNavigate(0)
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier= Modifier.fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = nome.text.isNotEmpty() && telefone.text.isNotEmpty(),
                onClick = {
                    if (nome.text.isNotEmpty() && telefone.text.isNotEmpty()) {
                        if (isEditing) {

                            viewModel.upsertPessoa(
                                Pessoa(nome.text, telefone.text, pessoaBeingEdited!!.id)
                            )
                        } else {

                            viewModel.upsertPessoa(Pessoa(nome.text, telefone.text))
                        }
                        nome = TextFieldValue("")
                        telefone = TextFieldValue("")
                        keyboardController?.hide()


                        onNavigate(0)
                    }
                }
            ) {
                Text(
                    text = if (isEditing) "Atualizar" else "Cadastrar",
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Default,
                    fontSize = 15.sp
                    )
            }
        }
    }


@Composable
fun SmsScreen(viewModel: PessoaViewModel, mainActivity: MainActivity) {
    var selectedPessoa by remember { mutableStateOf<Pessoa?>(null) }
    var message by remember { mutableStateOf("") }
    val pessoaList by viewModel.getPessoa().observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color= Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Envio de SMS",
            fontSize = 50.sp,
            fontWeight = FontWeight.Thin,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(Modifier.height(40.dp))
        Column(
            Modifier.height(300.dp)
        ) {
            LazyColumn(
                Modifier
                    .background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                items(pessoaList.sortedBy { it.name }) { pessoa ->
                    val isSelected = pessoa == selectedPessoa
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { selectedPessoa = pessoa }
                            .background(
                                if (isSelected) Color.LightGray else Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color.Gray else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isSelected) Color.Gray else Color.LightGray, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ícone de pessoa",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = pessoa.name,
                                color = if (isSelected) Color.Gray else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = pessoa.telefone ?: "Sem telefone",
                                color = if (isSelected) Color.DarkGray else Color.Gray
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Column (Modifier.padding(start = 40.dp, end = 40.dp)) {
            TextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Digite a mensagem") },
                placeholder = { Text("Digite sua mensagem aqui...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedPessoa?.let {
                        sendSms(it.telefone, message, mainActivity)
                    }
                },
                shape = RoundedCornerShape(8.dp) ,
                enabled = selectedPessoa != null && message.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar SMS")
            }
        }
    }
}


fun sendSms(phoneNumber: String, message: String, mainActivity: MainActivity) {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        mainActivity.startActivity(smsIntent)
    }

