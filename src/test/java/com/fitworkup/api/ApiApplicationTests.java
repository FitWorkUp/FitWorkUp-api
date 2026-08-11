package com.fitworkup.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitworkup.models.StoreItem;
import com.fitworkup.models.User;
import com.fitworkup.models.Achievement;
import com.fitworkup.enums.StoreEffectType;
import com.fitworkup.repository.StoreItemRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.repository.AchievementRepository;
import com.fitworkup.service.GamificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StoreItemRepository storeItemRepository;

	@Autowired
	private GamificationService gamificationService;

	@Autowired
	private AchievementRepository achievementRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldRegisterLoginAndAccessAuthenticatedProfile() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "atleta_teste",
						  "email": "atleta@fitworkup.test",
						  "password": "senha123",
						  "weightKg": 72.5
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("atleta_teste"));

		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "login": "atleta@fitworkup.test",
						  "password": "senha123"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.user.email").value("atleta@fitworkup.test"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode body = objectMapper.readTree(loginResponse);
		String accessToken = body.get("accessToken").asText();

		mockMvc.perform(get("/api/v1/users/me")
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("atleta_teste"))
				.andExpect(jsonPath("$.totalDistanceKm").value(0.0));
	}

	@Test
	void shouldPurchaseEquipAndPreventDuplicateCosmetic() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "comprador_teste",
						  "email": "comprador@fitworkup.test",
						  "password": "senha123"
						}
						"""))
				.andExpect(status().isCreated());

		User user = userRepository.findByEmail("comprador@fitworkup.test").orElseThrow();
		user.setFitcoins(100);
		userRepository.save(user);

		StoreItem storeItem = new StoreItem();
		storeItem.setName("Moldura de Teste");
		storeItem.setPrice(30);
		storeItem.setCategory("AVATAR_FRAME");
		storeItem.setDescription("Item usado pelo teste de compra.");
		storeItem.setIconEmoji("T");
		storeItem.setActive(true);
		storeItem.setRepeatable(false);
		storeItem = storeItemRepository.save(storeItem);

		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "login": "comprador_teste",
						  "password": "senha123"
						}
						"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

		String purchaseResponse = mockMvc.perform(post("/api/v1/store/purchase/{id}", storeItem.getId())
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.remainingFitcoins").value(70))
				.andExpect(jsonPath("$.quantity").value(1))
				.andReturn().getResponse().getContentAsString();

		long inventoryItemId = objectMapper.readTree(purchaseResponse)
				.get("inventoryItemId").asLong();

		mockMvc.perform(post("/api/v1/store/equip/{id}", inventoryItemId)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipped").value(true));

		mockMvc.perform(get("/api/v1/users/me")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fitcoins").value(70))
				.andExpect(jsonPath("$.avatarBorder").value("Moldura de Teste"));

		mockMvc.perform(post("/api/v1/store/purchase/{id}", storeItem.getId())
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldActivateExtendAndApplyXpBoost() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "boost_teste",
						  "email": "boost@fitworkup.test",
						  "password": "senha123"
						}
						"""))
				.andExpect(status().isCreated());

		User user = userRepository.findByEmail("boost@fitworkup.test").orElseThrow();
		user.setFitcoins(100);
		userRepository.save(user);

		StoreItem boost = new StoreItem();
		boost.setName("Boost XP de Teste");
		boost.setPrice(20);
		boost.setCategory("BOOST");
		boost.setDescription("Dobra XP no teste.");
		boost.setIconEmoji("XP");
		boost.setActive(true);
		boost.setRepeatable(true);
		boost.setEffectType(StoreEffectType.XP_MULTIPLIER);
		boost.setMultiplier(2.0);
		boost.setDurationMinutes(30);
		boost = storeItemRepository.save(boost);

		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "login": "boost_teste",
						  "password": "senha123"
						}
						"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

		mockMvc.perform(post("/api/v1/store/purchase/{id}", boost.getId())
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.remainingFitcoins").value(80))
				.andExpect(jsonPath("$.repeatable").value(true))
				.andExpect(jsonPath("$.boostExpiresAt").isNotEmpty());

		gamificationService.rewardUserForActivity(user.getId(), 10, 5);
		User rewardedUser = userRepository.findById(user.getId()).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(20, rewardedUser.getXp());
		org.junit.jupiter.api.Assertions.assertEquals(85, rewardedUser.getFitcoins());

		mockMvc.perform(post("/api/v1/store/purchase/{id}", boost.getId())
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.remainingFitcoins").value(65));

		mockMvc.perform(get("/api/v1/store/boosts/active")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].effectType").value("XP_MULTIPLIER"))
				.andExpect(jsonPath("$[0].multiplier").value(2.0));
	}

	@Test
	void shouldCalculateCaloriesAndUnlockDailyStepAchievement() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "passos_teste",
						  "email": "passos@fitworkup.test",
						  "password": "senha123",
						  "weightKg": 70.0
						}
						"""))
				.andExpect(status().isCreated());

		Achievement achievement = new Achievement();
		achievement.setName("Meta 1K de Teste");
		achievement.setDescription("Complete mil passos.");
		achievement.setXpReward(50);
		achievement.setFitCoinsReward(5);
		achievement.setIconName("steps_1k");
		achievement.setCriteriaType("DAILY_STEPS");
		achievement.setTargetValue(1000);
		achievementRepository.save(achievement);

		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "login": "passos_teste",
						  "password": "senha123"
						}
						"""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

		mockMvc.perform(post("/api/v1/activities")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "type": "CAMINHADA",
						  "distanceKm": 0.8,
						  "steps": 1000,
						  "avgSpeed": 5.0,
						  "acceptedSteps": 1000,
						  "heldSteps": 0,
						  "riskScore": 0,
						  "fraudReasons": []
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.caloriesBurned").value(42));

		mockMvc.perform(get("/api/v1/activities/today-summary")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalSteps").value(1000))
				.andExpect(jsonPath("$.totalCalories").value(42));

		mockMvc.perform(get("/api/v1/users/me/achievements")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Meta 1K de Teste"))
				.andExpect(jsonPath("$[0].unlocked").value(true));
	}

	@Test
	void shouldRankApprovedWeeklyStepsAndExposeActiveDays() throws Exception {
		String leaderToken = registerAndLoginForRanking(
				"ranking_lider",
				"ranking-lider@fitworkup.test"
		);
		String challengerToken = registerAndLoginForRanking(
				"ranking_desafiante",
				"ranking-desafiante@fitworkup.test"
		);

		registerRankingActivity(leaderToken, 2000, 1.5);
		registerRankingActivity(challengerToken, 1000, 0.8);

		mockMvc.perform(get("/api/ranking/weekly")
				.header("Authorization", "Bearer " + leaderToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.weekStart").isNotEmpty())
				.andExpect(jsonPath("$.weekEnd").isNotEmpty())
				.andExpect(jsonPath("$.stepsPerPoint").value(100))
				.andExpect(jsonPath("$.entries[0].username").value("ranking_lider"))
				.andExpect(jsonPath("$.entries[0].validatedSteps").value(2000))
				.andExpect(jsonPath("$.entries[0].movementPoints").value(20))
				.andExpect(jsonPath("$.entries[0].activeDays").value(1))
				.andExpect(jsonPath("$.entries[0].currentUser").value(true));
	}

	private String registerAndLoginForRanking(String username, String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "%s",
						  "email": "%s",
						  "password": "senha123"
						}
						""".formatted(username, email)))
				.andExpect(status().isCreated());

		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "login": "%s",
						  "password": "senha123"
						}
						""".formatted(email)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readTree(loginResponse).get("accessToken").asText();
	}

	private void registerRankingActivity(String token, int steps, double distanceKm) throws Exception {
		mockMvc.perform(post("/api/v1/activities")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "type": "CAMINHADA",
						  "distanceKm": %s,
						  "steps": %d,
						  "avgSpeed": 5.0,
						  "acceptedSteps": %d,
						  "heldSteps": 0,
						  "riskScore": 0,
						  "fraudReasons": []
						}
						""".formatted(distanceKm, steps, steps)))
				.andExpect(status().isOk());
	}

}
