-- Schema do sistema academico de matriculas.
-- SQL escrito de forma portavel (sem ENGINE=, sem charset explicito, sem
-- crases) para rodar tanto em MySQL 8 (producao/docker) quanto em H2 em
-- modo de compatibilidade MySQL (perfil de testes de integracao).

CREATE TABLE aluno (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    data_nascimento DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NULL,
    atualizado_em TIMESTAMP NULL,
    CONSTRAINT uk_aluno_email UNIQUE (email),
    CONSTRAINT uk_aluno_cpf UNIQUE (cpf)
);

CREATE TABLE curso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    carga_horaria_total INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NULL,
    atualizado_em TIMESTAMP NULL,
    CONSTRAINT uk_curso_codigo UNIQUE (codigo)
);

CREATE TABLE disciplina (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    carga_horaria INT NOT NULL,
    curso_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NULL,
    atualizado_em TIMESTAMP NULL,
    CONSTRAINT uk_disciplina_codigo UNIQUE (codigo),
    CONSTRAINT fk_disciplina_curso FOREIGN KEY (curso_id) REFERENCES curso (id)
);

CREATE TABLE turma (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    disciplina_id BIGINT NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    vagas_totais INT NOT NULL,
    vagas_ocupadas INT NOT NULL DEFAULT 0,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    criado_em TIMESTAMP NULL,
    atualizado_em TIMESTAMP NULL,
    CONSTRAINT uk_turma_codigo UNIQUE (codigo),
    CONSTRAINT fk_turma_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplina (id),
    CONSTRAINT chk_turma_vagas CHECK (vagas_ocupadas <= vagas_totais AND vagas_ocupadas >= 0)
);

CREATE TABLE matricula (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aluno_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_matricula TIMESTAMP NOT NULL,
    data_confirmacao TIMESTAMP NULL,
    data_cancelamento TIMESTAMP NULL,
    criado_em TIMESTAMP NULL,
    atualizado_em TIMESTAMP NULL,
    CONSTRAINT uk_matricula_aluno_turma UNIQUE (aluno_id, turma_id),
    CONSTRAINT fk_matricula_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id),
    CONSTRAINT fk_matricula_turma FOREIGN KEY (turma_id) REFERENCES turma (id)
);

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    username VARCHAR(60) NOT NULL,
    password VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NULL,
    atualizado_em TIMESTAMP NULL,
    CONSTRAINT uk_usuario_username UNIQUE (username)
);

CREATE INDEX idx_disciplina_curso ON disciplina (curso_id);
CREATE INDEX idx_turma_disciplina ON turma (disciplina_id);
CREATE INDEX idx_matricula_aluno ON matricula (aluno_id);
CREATE INDEX idx_matricula_turma ON matricula (turma_id);
CREATE INDEX idx_matricula_status ON matricula (status);
