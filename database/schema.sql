-- =====================================================================
--  Banco MySQL do app "Cozinha Fácil"
--  Como usar: abra o phpMyAdmin (http://localhost/phpmyadmin) > aba SQL >
--  cole este arquivo inteiro > Executar.  (Ou: mysql -u root -p < schema.sql)
--  As colunas são as mesmas da tabela SQLite do app (DatabaseHelper.java).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS receitas_app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE receitas_app;

CREATE TABLE IF NOT EXISTS receitas (
    id             INT          NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(80)  NOT NULL,
    categoria      VARCHAR(40)  NOT NULL,
    origem         VARCHAR(40)  NULL,
    tempo_preparo  INT          NOT NULL,              -- minutos
    porcoes        INT          NOT NULL,
    dificuldade    ENUM('FACIL', 'MEDIO', 'DIFICIL') NOT NULL DEFAULT 'FACIL',
    ingredientes   TEXT         NOT NULL,              -- um por linha
    modo_preparo   TEXT         NOT NULL,
    imagem_url     VARCHAR(500) NULL,
    favorita       TINYINT(1)   NOT NULL DEFAULT 0,
    data_receita   DATE         NOT NULL,
    id_meal_api    VARCHAR(20)  NULL,                  -- id da TheMealDB quando importada
    criado_em      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_tempo   CHECK (tempo_preparo BETWEEN 1 AND 1440),
    CONSTRAINT chk_porcoes CHECK (porcoes BETWEEN 1 AND 50)
) ENGINE = InnoDB;

-- Dados de exemplo (opcional)
INSERT INTO receitas (nome, categoria, origem, tempo_preparo, porcoes, dificuldade,
                      ingredientes, modo_preparo, favorita, data_receita)
VALUES
('Pão de queijo', 'Café da manhã', 'Brasil', 40, 25, 'MEDIO',
 '500 g de polvilho azedo\n250 ml de leite\n125 ml de óleo\n2 ovos\n200 g de queijo minas ralado\nSal a gosto',
 'Ferva o leite com o óleo e o sal e escalde o polvilho. Espere amornar, junte os ovos e o queijo e sove até ficar homogêneo. Faça bolinhas e asse a 180 °C por cerca de 25 minutos.',
 1, CURDATE()),
('Feijão tropeiro', 'Acompanhamentos', 'Brasil', 45, 6, 'MEDIO',
 '500 g de feijão cozido\n200 g de bacon\n2 linguiças calabresas\n3 ovos\n2 xícaras de farinha de mandioca\nCebolinha a gosto',
 'Frite o bacon e a linguiça. Mexa os ovos na mesma panela, junte o feijão escorrido e, aos poucos, a farinha. Finalize com cebolinha.',
 0, CURDATE());

-- Usuário próprio para o app (opcional, mais seguro que usar o root):
-- CREATE USER 'receitas_user'@'%' IDENTIFIED BY 'troque_esta_senha';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON receitas_app.* TO 'receitas_user'@'%';
-- FLUSH PRIVILEGES;
