<?php
/**
 * API REST do CRUD de receitas (MySQL) usada pelo app Android.
 *
 *   GET    receitas.php          -> lista todas
 *   GET    receitas.php?id=1     -> busca uma
 *   POST   receitas.php          -> cria   (corpo JSON)      -> 201 {"id": 10}
 *   PUT    receitas.php?id=1     -> atualiza (corpo JSON)    -> {"sucesso": true}
 *   DELETE receitas.php?id=1     -> exclui                   -> {"sucesso": true}
 *
 * Erros sempre no formato {"erro": "mensagem"} com o status HTTP adequado.
 */

require __DIR__ . '/conexao.php';

header('Content-Type: application/json; charset=utf-8');

const COLUNAS = 'id, nome, categoria, origem, tempo_preparo, porcoes, dificuldade, '
    . 'ingredientes, modo_preparo, imagem_url, favorita, data_receita, id_meal_api';

const DIFICULDADES = ['FACIL', 'MEDIO', 'DIFICIL'];

function responder(int $status, $dados): void
{
    http_response_code($status);
    echo json_encode($dados, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function erro(int $status, string $mensagem): void
{
    responder($status, ['erro' => $mensagem]);
}

/** Converte os tipos vindos do banco (o PDO pode devolver números como texto). */
function formatar(array $linha): array
{
    $linha['id'] = (int) $linha['id'];
    $linha['tempo_preparo'] = (int) $linha['tempo_preparo'];
    $linha['porcoes'] = (int) $linha['porcoes'];
    $linha['favorita'] = (int) $linha['favorita'];
    return $linha;
}

/** Lê o corpo JSON da requisição. */
function lerCorpo(): array
{
    $dados = json_decode(file_get_contents('php://input'), true);
    if (!is_array($dados)) {
        erro(400, 'Corpo da requisição deve ser um JSON válido.');
    }
    return $dados;
}

function texto(array $d, string $campo): ?string
{
    if (!isset($d[$campo])) {
        return null;
    }
    $valor = trim((string) $d[$campo]);
    return $valor === '' ? null : $valor;
}

/**
 * Validação no servidor (mesmas regras do app). Mesmo que alguém chame a API
 * sem passar pelo app, dados inválidos não são gravados.
 */
function validar(array $d): array
{
    $nome = texto($d, 'nome');
    $categoria = texto($d, 'categoria');
    $ingredientes = texto($d, 'ingredientes');
    $modoPreparo = texto($d, 'modo_preparo');
    $data = texto($d, 'data_receita');
    $dificuldade = texto($d, 'dificuldade') ?? 'FACIL';
    $tempo = filter_var($d['tempo_preparo'] ?? null, FILTER_VALIDATE_INT);
    $porcoes = filter_var($d['porcoes'] ?? null, FILTER_VALIDATE_INT);

    if ($nome === null || mb_strlen($nome) < 3 || mb_strlen($nome) > 80) {
        erro(422, 'Nome é obrigatório (3 a 80 caracteres).');
    }
    if ($categoria === null || mb_strlen($categoria) > 40) {
        erro(422, 'Categoria é obrigatória.');
    }
    if ($tempo === false || $tempo < 1 || $tempo > 1440) {
        erro(422, 'Tempo de preparo deve ser entre 1 e 1440 minutos.');
    }
    if ($porcoes === false || $porcoes < 1 || $porcoes > 50) {
        erro(422, 'Porções deve ser entre 1 e 50.');
    }
    if (!in_array($dificuldade, DIFICULDADES, true)) {
        erro(422, 'Dificuldade inválida.');
    }
    if ($ingredientes === null || $modoPreparo === null) {
        erro(422, 'Ingredientes e modo de preparo são obrigatórios.');
    }
    $dt = $data ? DateTime::createFromFormat('Y-m-d', $data) : false;
    if (!$dt || $dt->format('Y-m-d') !== $data) {
        erro(422, 'Data inválida (formato esperado: AAAA-MM-DD).');
    }
    $origem = texto($d, 'origem');
    if ($origem !== null && mb_strlen($origem) > 40) {
        erro(422, 'Origem deve ter no máximo 40 caracteres.');
    }
    $imagem = texto($d, 'imagem_url');
    if ($imagem !== null && (mb_strlen($imagem) > 500 || !filter_var($imagem, FILTER_VALIDATE_URL))) {
        erro(422, 'URL da imagem inválida.');
    }

    return [
        ':nome' => $nome,
        ':categoria' => $categoria,
        ':origem' => $origem,
        ':tempo_preparo' => $tempo,
        ':porcoes' => $porcoes,
        ':dificuldade' => $dificuldade,
        ':ingredientes' => $ingredientes,
        ':modo_preparo' => $modoPreparo,
        ':imagem_url' => $imagem,
        ':favorita' => !empty($d['favorita']) ? 1 : 0,
        ':data_receita' => $data,
        ':id_meal_api' => texto($d, 'id_meal_api'),
    ];
}

function idDaUrl(): int
{
    $id = filter_var($_GET['id'] ?? null, FILTER_VALIDATE_INT);
    if ($id === false || $id === null || $id < 1) {
        erro(400, 'Informe um id válido na URL (ex.: receitas.php?id=1).');
    }
    return $id;
}

// ---------------------------------------------------------------------

try {
    $pdo = conectar();
} catch (PDOException $e) {
    error_log('Falha ao conectar no MySQL: ' . $e->getMessage());
    erro(500, 'Não foi possível conectar ao MySQL. Verifique se ele está ligado e o config.php.');
}

try {
    switch ($_SERVER['REQUEST_METHOD']) {

        case 'GET':
            if (isset($_GET['id'])) {
                $stmt = $pdo->prepare('SELECT ' . COLUNAS . ' FROM receitas WHERE id = :id');
                $stmt->execute([':id' => idDaUrl()]);
                $linha = $stmt->fetch();
                if (!$linha) {
                    erro(404, 'Receita não encontrada.');
                }
                responder(200, formatar($linha));
            }
            $stmt = $pdo->query('SELECT ' . COLUNAS . ' FROM receitas ORDER BY favorita DESC, nome ASC');
            responder(200, array_map('formatar', $stmt->fetchAll()));
            break;

        case 'POST':
            $valores = validar(lerCorpo());
            $sql = 'INSERT INTO receitas (nome, categoria, origem, tempo_preparo, porcoes, dificuldade,
                        ingredientes, modo_preparo, imagem_url, favorita, data_receita, id_meal_api)
                    VALUES (:nome, :categoria, :origem, :tempo_preparo, :porcoes, :dificuldade,
                        :ingredientes, :modo_preparo, :imagem_url, :favorita, :data_receita, :id_meal_api)';
            $pdo->prepare($sql)->execute($valores);
            responder(201, ['id' => (int) $pdo->lastInsertId()]);
            break;

        case 'PUT':
            $id = idDaUrl();
            $valores = validar(lerCorpo());
            $valores[':id'] = $id;
            $sql = 'UPDATE receitas SET nome = :nome, categoria = :categoria, origem = :origem,
                        tempo_preparo = :tempo_preparo, porcoes = :porcoes, dificuldade = :dificuldade,
                        ingredientes = :ingredientes, modo_preparo = :modo_preparo,
                        imagem_url = :imagem_url, favorita = :favorita,
                        data_receita = :data_receita, id_meal_api = :id_meal_api
                    WHERE id = :id';
            $pdo->prepare($sql)->execute($valores);
            // rowCount() é 0 quando nada mudou, então conferimos se o id existe
            $existe = $pdo->prepare('SELECT COUNT(*) FROM receitas WHERE id = :id');
            $existe->execute([':id' => $id]);
            if ((int) $existe->fetchColumn() === 0) {
                erro(404, 'Receita não encontrada.');
            }
            responder(200, ['sucesso' => true]);
            break;

        case 'DELETE':
            $stmt = $pdo->prepare('DELETE FROM receitas WHERE id = :id');
            $stmt->execute([':id' => idDaUrl()]);
            if ($stmt->rowCount() === 0) {
                erro(404, 'Receita não encontrada.');
            }
            responder(200, ['sucesso' => true]);
            break;

        default:
            erro(405, 'Método não permitido.');
    }
} catch (PDOException $e) {
    error_log('Erro de banco: ' . $e->getMessage());
    erro(500, 'Erro ao acessar o banco de dados.');
}
