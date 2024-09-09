package rewards;

import common.money.MonetaryAmount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RewardNetworkTests {

    private RewardNetwork rewardNetwork;

    @BeforeEach
    void setUp() {
        ConfigurableApplicationContext context = SpringApplication.run(TestInfrastructureConfig.class);

        rewardNetwork = context.getBean(RewardNetwork.class);
    }

    @Test
    void testRewardForDining() {
        Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");

        RewardConfirmation confirmation = rewardNetwork.rewardAccountFor(dining);

        assertNotNull(confirmation);
        assertNotNull(confirmation.getConfirmationNumber());

        AccountContribution contribution = confirmation.getAccountContribution();
        assertNotNull(contribution);

        assertEquals("123456789", contribution.getAccountNumber());
        assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());
        assertEquals(2, contribution.getDistributions().size());

        assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount());
        assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount());
    }
}
