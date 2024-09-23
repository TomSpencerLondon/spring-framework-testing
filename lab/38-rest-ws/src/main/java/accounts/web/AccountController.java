package accounts.web;

import accounts.AccountManager;
import common.money.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * A controller handling requests for CRUD operations on Accounts and their
 * Beneficiaries.
 */
@RestController
public class AccountController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AccountManager accountManager;

	/**
	 * Creates a new AccountController with a given account manager.
	 */
	@Autowired
	public AccountController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@GetMapping(value = "/accounts")
	public List<Account> accountSummary() {
		return accountManager.getAllAccounts();
	}

	@GetMapping(value = "/accounts/{id}")
	public Account accountDetails(@PathVariable int id) {
		return retrieveAccount(id);
	}

	@PostMapping(value = "/accounts")
	public ResponseEntity<Void> createAccount(@RequestBody Account newAccount) {
		Account account = accountManager.save(newAccount);
		return entityWithLocation(account.getEntityId());
	}

	/**
	 * Return a response with the location of the new resource. 
	 *
	 * Suppose we have just received an incoming URL of, say,
	 *   http://localhost:8080/accounts and resourceId is "1111".
	 * Then the URL of the new resource will be
	 *   http://localhost:8080/accounts/1111.
	 */
	private ResponseEntity<Void> entityWithLocation(Object resourceId) {
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequestUri()
				.path("/{resourceId}")
				.buildAndExpand(resourceId)
				.toUri();

		return ResponseEntity.created(location).build();
	}

	/**
	 * Returns the Beneficiary with the given name for the Account with the
	 * given id.
	 */
	@GetMapping(value = "/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	public Beneficiary getBeneficiary(@PathVariable("accountId") int accountId,
			@PathVariable("beneficiaryName") String beneficiaryName) {
		return retrieveAccount(accountId).getBeneficiary(beneficiaryName);
	}

	@PostMapping(value = "/accounts/{accountId}/beneficiaries")
	public ResponseEntity<Void> addBeneficiary(@PathVariable long accountId, @RequestBody String beneficiaryName) {
		accountManager.addBeneficiary(accountId, beneficiaryName);
		return entityWithLocation(beneficiaryName);
	}

	@DeleteMapping(value = "/accounts/{accountId}/beneficiaries/{beneficiaryName}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeBeneficiary(@PathVariable long accountId, @PathVariable String beneficiaryName) {
		Account account = accountManager.getAccount(accountId);
		if (account == null) {
			throw new IllegalArgumentException("No such account with id " + accountId);
		}
		Beneficiary deletedBeneficiary = account.getBeneficiary(beneficiaryName);
		HashMap<String, Percentage> allocationPercentages = new HashMap<>();


		if (account.getBeneficiaries().size() != 1 && (!deletedBeneficiary.getAllocationPercentage().equals(Percentage.zero()))) {
			Percentage p = deletedBeneficiary.getAllocationPercentage();
			int remaining = account.getBeneficiaries().size() - 1;
			double extra = p.asDouble() / remaining;

			for (Beneficiary beneficiary : account.getBeneficiaries()) {
				if (beneficiary != deletedBeneficiary) {
					double newValue = beneficiary.getAllocationPercentage().asDouble() + extra;
					allocationPercentages.put(beneficiary.getName(), new Percentage(newValue));
				}
			}
		}

		accountManager.removeBeneficiary(accountId, beneficiaryName, allocationPercentages);
	}

	/**
	 * Maps IllegalArgumentExceptions to a 404 Not Found HTTP status code.
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({ IllegalArgumentException.class })
	public void handleNotFound(Exception ex) {
		logger.error("Exception is: ", ex);
		// just return empty 404
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({DataIntegrityViolationException.class })
	public void handleAlreadyExists(Exception ex) {
		logger.error("Exception is: ", ex);
	}
	
	/**
	 * Finds the Account with the given id, throwing an IllegalArgumentException
	 * if there is no such Account.
	 */
	private Account retrieveAccount(long accountId) throws IllegalArgumentException {
		Account account = accountManager.getAccount(accountId);
		if (account == null) {
			throw new IllegalArgumentException("No such account with id " + accountId);
		}
		return account;
	}

}
