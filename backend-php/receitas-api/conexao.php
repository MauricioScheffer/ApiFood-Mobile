<?php
/**
 * Cria a conexão PDO com o MySQL usando as configurações de config.php.
 */
function conectar(): PDO
{
    $config = require __DIR__ . '/config.php';
    $local = __DIR__ . '/config.local.php';
    if (file_exists($local)) {
        $config = array_merge($config, require $local);
    }

    $dsn = $config['dsn'] ?? sprintf(
        'mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4',
        $config['host'],
        $config['porta'],
        $config['banco']
    );

    return new PDO($dsn, $config['usuario'], $config['senha'], [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION, // erros viram exceções
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,       // linhas como array associativo
        PDO::ATTR_EMULATE_PREPARES   => false,                  // prepared statements reais
    ]);
}
