-- Dados de exemplo para facilitar a avaliacao manual da aplicacao.
-- Usuario admin: username "admin", senha "admin123".
INSERT INTO usuario (nome, username, password, ativo) VALUES
    ('Administrador do Sistema', 'admin', '$2b$10$meFpfkfGs/D0qxvPTtfFIOqmVDxpYTUKqDPXFCwCHmGMe2XkhDW.G', TRUE);

INSERT INTO curso (nome, codigo, carga_horaria_total, ativo) VALUES
    ('Engenharia de Software', 'ENG-SW', 3600, TRUE),
    ('Analise e Desenvolvimento de Sistemas', 'ADS', 2400, TRUE);

INSERT INTO disciplina (nome, codigo, carga_horaria, curso_id, ativo) VALUES
    ('Estrutura de Dados', 'ENG-SW-101', 80, 1, TRUE),
    ('Banco de Dados', 'ENG-SW-102', 60, 1, TRUE),
    ('Programacao Web', 'ADS-101', 60, 2, TRUE);

-- A turma de Programacao Web tem apenas 2 vagas de proposito, para
-- facilitar testar manualmente a regra de limite de vagas via Swagger.
INSERT INTO turma (codigo, disciplina_id, periodo, vagas_totais, vagas_ocupadas, data_inicio, data_fim, status) VALUES
    ('ED-2025-2-A', 1, '2025.2', 30, 0, '2025-08-04', '2025-12-12', 'ABERTA'),
    ('BD-2025-2-A', 2, '2025.2', 25, 0, '2025-08-04', '2025-12-12', 'ABERTA'),
    ('PW-2025-2-A', 3, '2025.2', 2, 0, '2025-08-04', '2025-12-12', 'ABERTA');

INSERT INTO aluno (nome, email, cpf, data_nascimento, ativo) VALUES
    ('Ana Beatriz Souza', 'ana.souza@email.com', '11122233344', '2001-03-15', TRUE),
    ('Carlos Eduardo Lima', 'carlos.lima@email.com', '22233344455', '2000-07-22', TRUE),
    ('Mariana Costa Ferreira', 'mariana.ferreira@email.com', '33344455566', '2002-11-05', TRUE);
