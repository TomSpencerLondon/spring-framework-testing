package rewards.internal;

import org.springframework.transaction.annotation.Transactional;
import rewards.AccountContribution;
import rewards.Dining;
import rewards.RewardConfirmation;
import rewards.RewardNetwork;
import rewards.internal.account.Account;
import rewards.internal.account.AccountRepository;
import rewards.internal.restaurant.Restaurant;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.RewardRepository;

import common.money.MonetaryAmount;

public class RewardNetworkImpl implements RewardNetwork {

	private AccountRepository accountRepository;

	private RestaurantRepository restaurantRepository;

	private RewardRepository rewardRepository;

	/**
	 * Creates a new reward network.
	 * @param accountRepository the repository for loading accounts to reward
	 * @param restaurantRepository the repository for loading restaurants that determine how much to reward
	 * @param rewardRepository the repository for recording a record of successful reward transactions
	 */
	public RewardNetworkImpl(AccountRepository accountRepository, RestaurantRepository restaurantRepository,
			RewardRepository rewardRepository) {
		this.accountRepository = accountRepository;
		this.restaurantRepository = restaurantRepository;
		this.rewardRepository = rewardRepository;
	}

	@Transactional
	public RewardConfirmation rewardAccountFor(Dining dining) {
		Account account = accountRepository.findByCreditCard(dining.getCreditCardNumber());
		Restaurant restaurant = restaurantRepository.findByMerchantNumber(dining.getMerchantNumber());
		MonetaryAmount amount = restaurant.calculateBenefitFor(account, dining);
		AccountContribution contribution = account.makeContribution(amount);
		accountRepository.updateBeneficiaries(account);
		return rewardRepository.confirmReward(contribution, dining);
	}
}
