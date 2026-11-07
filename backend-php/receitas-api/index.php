<?php
// Página de teste: abra http://localhost/receitas-api/ no navegador do PC.
header('Content-Type: application/json; charset=utf-8');
require __DIR__ . '/conexao.php';

try {
    $total = (int) conectar()->query('SELECT COUNT(*) FROM receitas')->fetchColumn();
    echo json_encode(['status' => 'ok', 'mysql' => 'conectado', 'receitas' => $total], JSON_UNESCAPED_UNICODE);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['status' => 'erro', 'detalhe' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
