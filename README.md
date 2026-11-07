# Cozinha Fácil — App Android de Receitas (Java)

Trabalho prático de **Desenvolvimento de Aplicações para Dispositivos Móveis** (IFSUL) — tema **Alimentação** (catálogo de receitas).

- CRUD completo de receitas (cadastrar, listar, editar, excluir)
- Banco **SQLite** no aparelho (padrão, exigido no R6) **ou** **MySQL** via backend PHP — escolha em *Configurações*
- Consumo da API pública **TheMealDB** (busca por nome, por categoria, receita do dia e detalhes)
- Importação de uma receita da API para as suas receitas (o formulário já abre preenchido)

---

## 1. Estrutura do projeto

```
ReceitasApp/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/receitas/
│   │   ├── ReceitasApp.java              → Application (aplica o tema salvo)
│   │   ├── model/
│   │   │   ├── Receita.java              → entidade do CRUD
│   │   │   └── Meal.java                 → receita vinda da TheMealDB
│   │   ├── data/
│   │   │   ├── ReceitaRepository.java    → interface do CRUD (as telas só usam ela)
│   │   │   ├── RepositoryProvider.java   → escolhe SQLite ou MySQL conforme Configurações
│   │   │   ├── local/                    → SQLite: DatabaseHelper, ReceitaDao, SqliteReceitaRepository
│   │   │   └── remote/                   → MySQL via PHP: ApiReceitaRepository, ReceitaJsonMapper
│   │   ├── network/
│   │   │   ├── HttpClient.java           → GET/POST/PUT/DELETE com HttpURLConnection
│   │   │   ├── HttpException.java
│   │   │   └── MealDbService.java        → endpoints da TheMealDB
│   │   ├── ui/
│   │   │   ├── BaseActivity.java         → menu da ActionBar, Toast, Snackbar (comum a todas)
│   │   │   ├── MainActivity.java         → HOME
│   │   │   ├── ReceitaListActivity.java  → LISTAGEM (busca + filtros)
│   │   │   ├── ReceitaFormActivity.java  → CADASTRO e EDIÇÃO (com validação)
│   │   │   ├── ReceitaDetalheActivity.java → detalhes + editar/excluir/compartilhar
│   │   │   ├── ExplorarActivity.java     → busca na TheMealDB
│   │   │   ├── MealDetalheActivity.java  → detalhes da API + "Salvar nas minhas receitas"
│   │   │   ├── ConfiguracoesActivity.java → nome, tema, SQLite/MySQL (SharedPreferences)
│   │   │   └── adapter/                  → ReceitaAdapter, MealAdapter (RecyclerView)
│   │   └── util/                         → AppExecutors, Validador, DateUtils, PrefsManager...
│   └── res/
│       ├── layout/        → 7 telas + 2 itens de lista (.xml)
│       ├── values/        → strings.xml, colors.xml, themes.xml, dimens.xml
│       ├── values-night/  → cores do tema escuro
│       ├── menu/          → menu_comum.xml (Atualizar, Configurações, Sobre)
│       ├── drawable/      → ícones vetoriais
│       └── xml/           → network_security_config.xml (libera HTTP só para o servidor local)
├── backend-php/receitas-api/   → API PHP que acessa o MySQL (copiar para o htdocs do XAMPP)
│   ├── config.php        → usuário/senha do MySQL
│   ├── conexao.php       → conexão PDO
│   ├── receitas.php      → endpoints REST do CRUD
│   └── index.php         → página de teste da conexão
└── database/schema.sql   → cria o banco receitas_app e a tabela receitas
```

**Por que o app não conecta direto no MySQL?** O Android não tem um driver suportado/seguro para MySQL, e colocar usuário e senha do banco dentro do APK expõe o banco. O caminho correto é: `App → HTTP/JSON → PHP → MySQL`. O SQLite continua sendo o banco padrão porque o enunciado (R6) pede "SQLite ou Room".

---

## 2. Passo a passo: abrir no Android Studio

**Pré‑requisitos:** Android Studio Ladybug (2024.2) ou mais novo, com o Android SDK 35 instalado.

1. Descompacte o `.zip` em uma pasta **sem acentos nem espaços** (ex.: `C:\projetos\ReceitasApp`).
2. Android Studio → **File → Open** → selecione a pasta `ReceitasApp` (a que contém `settings.gradle.kts`) → **OK**.
3. Se perguntar *Trust Project*, clique em **Trust Project**.
4. Aguarde o **Gradle Sync** terminar (barra inferior). Na primeira vez ele baixa o Gradle 8.9 e as bibliotecas — precisa de internet.
   - Se aparecer *"SDK location not found"*: **File → Project Structure → SDK Location** e aponte para o seu Android SDK.
   - Se aparecer *"Android Gradle Plugin requires Java 17"*: **File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** → escolha **jbr-17** ou **jbr-21** (vem com o Android Studio).
   - Se faltar o SDK 35: **Tools → SDK Manager → SDK Platforms** → marque **Android 15.0 (API 35)** → Apply.
   - Se o Android Studio sugerir atualizar o Android Gradle Plugin (*AGP Upgrade Assistant*), pode aceitar ou ignorar — o projeto funciona nas duas situações.
5. Crie um emulador: **Tools → Device Manager → + (Create Virtual Device)** → Pixel 7 → imagem **API 34 ou 35** → Finish.
6. Clique em **Run ▶ (Shift+F10)**.

O app abre com duas receitas de exemplo no SQLite. Só isso já cobre todos os requisitos R1–R8 — o MySQL é opcional.

### Alternativa: criar o projeto do zero e copiar os arquivos
Se preferir gerar o projeto pelo assistente do Android Studio:
1. **File → New → New Project → Empty Views Activity**.
2. Name: `ReceitasApp` · Package name: `com.example.receitas` · Language: **Java** · Minimum SDK: **API 24** · Build configuration language: Kotlin DSL.
3. Apague a `MainActivity.java` e o `activity_main.xml` gerados.
4. Copie deste projeto as pastas `app/src/main/java/com/example/receitas/` e `app/src/main/res/` (substituindo) e o `AndroidManifest.xml`.
5. No `app/build.gradle.kts`, garanta estas dependências e faça o Sync:
   ```kotlin
   implementation("androidx.appcompat:appcompat:1.7.0")
   implementation("com.google.android.material:material:1.12.0")
   implementation("androidx.constraintlayout:constraintlayout:2.2.0")
   implementation("androidx.recyclerview:recyclerview:1.3.2")
   implementation("com.github.bumptech.glide:glide:4.16.0")
   ```

---

## 3. Passo a passo: integrar com o MySQL (opcional)

Usando o **XAMPP** (Apache + MySQL/MariaDB + PHP).

1. Instale o XAMPP (https://www.apachefriends.org) e, no *XAMPP Control Panel*, clique em **Start** no **Apache** e no **MySQL**.
2. **Criar o banco:** abra `http://localhost/phpmyadmin` → aba **SQL** → cole o conteúdo de `database/schema.sql` → **Executar**. Isso cria o banco `receitas_app`, a tabela `receitas` e 2 receitas de exemplo.
3. **Publicar a API:** copie a pasta `backend-php/receitas-api` para `C:\xampp\htdocs\` (fica `C:\xampp\htdocs\receitas-api\`).
4. **Senha do MySQL:** se o seu MySQL tem senha (o padrão do XAMPP é `root` sem senha), edite `config.php` ou crie `config.local.php` com `<?php return ['senha' => 'sua_senha'];`.
5. **Testar no navegador do PC:**
   - `http://localhost/receitas-api/` → deve mostrar `{"status":"ok","mysql":"conectado","receitas":2}`
   - `http://localhost/receitas-api/receitas.php` → lista as receitas em JSON
6. **No app:** menu ⋮ → **Configurações** → *Onde salvar as receitas* = **MySQL (servidor PHP)** → URL:
   - **Emulador:** `http://10.0.2.2/receitas-api/` (10.0.2.2 é o "localhost" do PC visto de dentro do emulador)
   - **Celular físico (mesmo Wi‑Fi):** `http://IP-DO-PC/receitas-api/` — descubra o IP com `ipconfig` (Windows) e adicione esse IP em `res/xml/network_security_config.xml` (há uma linha comentada pronta). Libere a porta 80 no firewall do Windows se necessário.
7. Toque em **Testar conexão** → deve aparecer *"Conexão OK! 2 receita(s) no MySQL."* → **Salvar**.

A partir daí, Listagem, Cadastro, Edição e Exclusão passam a gravar no MySQL. Para voltar ao SQLite, basta trocar em Configurações (os dois bancos são independentes).

**Sem XAMPP?** Com PHP e MySQL instalados, rode na raiz do projeto:
```
php -S 0.0.0.0:8000 -t backend-php
```
e use a URL `http://10.0.2.2:8000/receitas-api/` no app.

### Endpoints da API PHP
| Método | URL | Ação |
|---|---|---|
| GET | `receitas.php` | lista todas |
| GET | `receitas.php?id=1` | busca uma |
| POST | `receitas.php` (JSON no corpo) | cria → `{"id": 10}` |
| PUT | `receitas.php?id=1` (JSON no corpo) | atualiza |
| DELETE | `receitas.php?id=1` | exclui |

Erros voltam como `{"erro": "mensagem"}` com status 400/404/422/500, e o app mostra essa mensagem ao usuário. O PHP também valida os dados, então nada inválido é gravado mesmo fora do app.

### Problemas comuns
| Mensagem no app | Causa provável |
|---|---|
| "Não foi possível conectar ao servidor..." | Apache desligado, URL errada ou falta `/receitas-api/` no final |
| "Não foi possível conectar ao MySQL..." | MySQL desligado ou usuário/senha errados no `config.php` |
| `CLEARTEXT communication ... not permitted` no Logcat | IP do celular não foi adicionado no `network_security_config.xml` |
| "Erro no servidor (código 404)" | Pasta copiada com outro nome no `htdocs` |

---

## 4. Mapa de telas (navegação)

```
Home (MainActivity)
 ├─ Minhas receitas → ReceitaListActivity
 │     ├─ toque no item ──(extra: id)──> ReceitaDetalheActivity
 │     │     ├─ Editar ──(extra: id)──> ReceitaFormActivity ──(retorno: mensagem)──┘
 │     │     └─ Excluir (AlertDialog) ──(retorno: "Receita excluída")──> Lista
 │     ├─ toque longo → AlertDialog de exclusão
 │     ├─ estrela → favoritar/desfavoritar
 │     └─ botão + ──> ReceitaFormActivity (cadastro) ──(retorno: mensagem)──> Lista
 ├─ Cadastrar nova receita ──> ReceitaFormActivity ──(retorno: mensagem)──> Home
 ├─ Receita do dia (API) ──(extra: idMeal)──> MealDetalheActivity
 └─ Explorar receitas ──> ExplorarActivity
       └─ toque ──(extras: idMeal, nome)──> MealDetalheActivity
             └─ Salvar nas minhas receitas ──(extra: objeto Receita)──> ReceitaFormActivity
Menu ⋮ (todas as telas): Atualizar · Configurações (ConfiguracoesActivity) · Sobre (AlertDialog)
```

---

## 5. Rastreabilidade dos requisitos (base para a documentação em PDF)

| Req. | Onde está implementado |
|---|---|
| **R1** Estrutura e navegação | 7 Activities (seção 4). Intents em todas as telas. **Envio de dados:** `ReceitaListActivity.onClick` → `ReceitaDetalheActivity.EXTRA_RECEITA_ID`; `MealDetalheActivity.importar` envia um objeto `Receita`. **Retorno de dados:** `ReceitaFormActivity.finalizarComSucesso` e `ReceitaDetalheActivity.excluir` devolvem `EXTRA_MENSAGEM` via `registerForActivityResult` |
| **R2** Componentes | TextView, EditText (`TextInputEditText`), Button, ImageView, CheckBox (`cbFavorita`, `cbSomenteFavoritas`), RadioButton (`rgDificuldade`, `rgTema`), Spinner (`spCategoria`, `spFonteDados`...). Todos os textos em `strings.xml`; layouts com `ScrollView`/`weight` para várias telas |
| **R3** Validação | `ReceitaFormActivity.validar()` + `util/Validador.java`: obrigatórios, tamanho (3–80), número e faixa (tempo 1–1440, porções 1–50), data dd/mm/aaaa válida e não futura, URL. Erro exibido no próprio campo (`TextInputLayout.setError`) e foco no primeiro inválido. Só salva se tudo for válido. Também em `ConfiguracoesActivity` |
| **R4** Alertas e menus | AlertDialog antes de excluir (`ReceitaDetalheActivity`, `ReceitaListActivity`) e no "Sobre". Toast/Snackbar após salvar, excluir, favoritar. Menu da ActionBar com 3 itens funcionais em `BaseActivity` (`res/menu/menu_comum.xml`) |
| **R5** Listas | `RecyclerView` + `ReceitaAdapter` (imagem, nome, categoria, detalhes, estrela) e `MealAdapter` (grade). Clique abre o detalhe |
| **R6** Persistência | SQLite: `DatabaseHelper` + `ReceitaDao` (INSERT/SELECT/UPDATE/DELETE). Dados permanecem ao fechar o app. SharedPreferences: `PrefsManager` (nome, tema, fonte de dados, última busca) |
| **R7** Serviço web | `MealDbService` (TheMealDB): `search.php`, `filter.php`, `list.php`, `lookup.php`, `random.php`. Executado fora da thread principal por `AppExecutors`. Sem internet/resposta inválida → mensagem + botão "Tentar novamente" (`ExplorarActivity`, `MealDetalheActivity`) |
| **R8** Qualidade | Java, nomes significativos, `Log.d/i/w/e` com TAG por classe (filtre o Logcat por `com.example.receitas`), callbacks verificam `isTelaAtiva()` para não atualizar telas fechadas |

**API utilizada (para o PDF):** TheMealDB — `https://www.themealdb.com/api/json/v1/1/`
- `search.php?s={nome}` → lista de receitas com nome, categoria, área, instruções, imagem, ingredientes
- `filter.php?c={categoria}` → id, nome e imagem das receitas da categoria
- `lookup.php?i={id}` → detalhes completos de uma receita
- `random.php` → uma receita aleatória
- `list.php?c=list` → nomes das categorias

A chave `1` é a chave de testes, liberada para uso educacional.

---

## 6. Onde mexer para manutenção

| Quero... | Arquivo |
|---|---|
| Mudar textos | `res/values/strings.xml` |
| Mudar cores / tema escuro | `res/values/colors.xml` e `res/values-night/colors.xml` |
| Adicionar uma categoria | array `categorias` no `strings.xml` (e o mapeamento em `CategoriaMapper`, se vier da API) |
| Mudar uma regra de validação | `util/Validador.java` e as chamadas em `ReceitaFormActivity.validar()` |
| Adicionar um campo na receita | `Receita.java` → `DatabaseHelper` (coluna + subir `DB_VERSAO`) → `ReceitaDao` → `ReceitaJsonMapper` → `schema.sql`/`receitas.php` → layout e `ReceitaFormActivity` |
| Adicionar um endpoint da TheMealDB | `network/MealDbService.java` |
| Atualizar bibliotecas | `gradle/libs.versions.toml` |

> Lembrete do enunciado: o uso de IA generativa deve ser declarado na documentação (o que foi usado e para quê), e o grupo precisa saber explicar qualquer trecho do código na apresentação.
