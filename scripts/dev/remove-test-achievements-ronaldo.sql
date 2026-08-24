-- Remove apenas as associações registradas como concessões de teste.
-- Não subtrai XP ou FitCoins porque o script de concessão também não os adiciona.
BEGIN;

DELETE FROM tb_user_achievements ua
USING dev_test_achievement_grants test_grant
WHERE ua.id = test_grant.user_achievement_id
  AND test_grant.test_key = 'RONALDO_ALL_ACHIEVEMENTS';

COMMIT;

SELECT COUNT(*) AS concessoes_de_teste_restantes
FROM dev_test_achievement_grants
WHERE test_key = 'RONALDO_ALL_ACHIEVEMENTS';
