-- Concede as 29 conquistas atuais somente para testar a interface.
-- Não altera XP, FitCoins, nível ou título do usuário.
BEGIN;

CREATE TABLE IF NOT EXISTS dev_test_achievement_grants (
    user_achievement_id BIGINT PRIMARY KEY
        REFERENCES tb_user_achievements(id) ON DELETE CASCADE,
    test_key VARCHAR(80) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE LOWER(username) = 'ronaldo') THEN
        RAISE EXCEPTION 'Usuário ronaldo não encontrado.';
    END IF;

    IF (SELECT COUNT(*) FROM tb_achievements
        WHERE code ~ '^C(0[1-9]|[12][0-9]|30)$' AND code <> 'C26') <> 29 THEN
        RAISE EXCEPTION 'O catálogo atual de 29 conquistas ainda não está completo. Reinicie a API primeiro.';
    END IF;
END $$;

WITH inserted AS (
    INSERT INTO tb_user_achievements (user_id, achievement_id, unlocked_at)
    SELECT u.id, a.id, CURRENT_TIMESTAMP
    FROM users u
    CROSS JOIN tb_achievements a
    WHERE LOWER(u.username) = 'ronaldo'
      AND a.code ~ '^C(0[1-9]|[12][0-9]|30)$'
      AND a.code <> 'C26'
    ON CONFLICT (user_id, achievement_id) DO NOTHING
    RETURNING id
)
INSERT INTO dev_test_achievement_grants (user_achievement_id, test_key)
SELECT id, 'RONALDO_ALL_ACHIEVEMENTS'
FROM inserted
ON CONFLICT (user_achievement_id) DO NOTHING;

COMMIT;

SELECT a.code, a.name, ua.unlocked_at
FROM tb_user_achievements ua
JOIN users u ON u.id = ua.user_id
JOIN tb_achievements a ON a.id = ua.achievement_id
WHERE LOWER(u.username) = 'ronaldo'
  AND a.code ~ '^C(0[1-9]|[12][0-9]|30)$'
  AND a.code <> 'C26'
ORDER BY a.code;
