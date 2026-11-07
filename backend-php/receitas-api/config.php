<?php
/**
 * Dados de conexão com o MySQL.
 * Padrão do XAMPP: usuário "root" e senha vazia.
 *
 * Dica: para não versionar sua senha, crie um arquivo config.local.php
 * (já está no .gitignore) retornando só as chaves que quer sobrescrever:
 *   <?php return ['senha' => 'minha_senha'];
 */
return [
    'host'    => '127.0.0.1',
    'porta'   => 3306,
    'banco'   => 'receitas_app',
    'usuario' => 'root',
    'senha'   => '',
    // 'dsn' => '...'   // opcional: DSN PDO completo (sobrescreve host/porta/banco)
];
