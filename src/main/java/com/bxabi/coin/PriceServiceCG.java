package com.bxabi.coin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.bxabi.coin.data.CoinDataCG;

@Component
public class PriceServiceCG {

	private Map<String, Coin> coins = new HashMap<>();

	private Map<String, BigDecimal> prices = new HashMap<>();

	private Date lastUpdated;

	public PriceServiceCG() {
		final RestTemplate restTemplate = new RestTemplate();

		UriComponents uri = UriComponentsBuilder.fromHttpUrl("https://api.coingecko.com/api/v3/coins/list").build();

		ResponseEntity<CoinDataCG[]> response = restTemplate.exchange(uri.toUriString(), HttpMethod.GET, null,
				CoinDataCG[].class);

		CoinDataCG[] coinData = response.getBody();
		for (CoinDataCG data : coinData) {
			coins.put(data.getId(), new Coin(data.getName(), data.getSymbol().toUpperCase()));
		}
		// refreshPrices();
	}

	public synchronized void refreshPrices(List<String> coins) {
		// Date now = new Date();
		// last update was more than a minute ago
		// if (lastUpdated == null || now.getTime() - lastUpdated.getTime() > 60000) {
		loadCgPrices(coins);
		lastUpdated = new Date();
		// }
	}

	public void loadCgPrices(List<String> coins) {
		StringBuilder coinList = new StringBuilder();
		for (String coinId : coins) {
			coinList.append(coinId);
			coinList.append(",");
		}
		coinList.deleteCharAt(coinList.length() - 1);

		final RestTemplate restTemplate = new RestTemplate();
		UriComponents uri = UriComponentsBuilder.fromHttpUrl("https://api.coingecko.com/api/v3/simple/price")
				.queryParam("ids", coinList).queryParam("vs_currencies", "usd").build();

		ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>> responseType = new ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>>() {
		};
		ResponseEntity<Map<String, Map<String, BigDecimal>>> response = restTemplate.exchange(uri.toUriString(),
				HttpMethod.GET, null, responseType);

		synchronized (prices) {
			Map<String, Map<String, BigDecimal>> loadedPrices = response.getBody();
			for (Entry<String, Map<String, BigDecimal>> entry : loadedPrices.entrySet()) {
				prices.put(entry.getKey(), entry.getValue().get("usd"));
			}
		}
	}

	public Coin getCoin(String coinId) {
		return coins.get(coinId);
	}

	public BigDecimal convertToUsd(BigDecimal toConvert, String from) {
		BigDecimal rate = getRate(from);
		return toConvert.multiply(rate);
	}

	public BigDecimal convertFromUsd(BigDecimal inUSD, String to) {
		BigDecimal rate = getRate(to);
		return inUSD.divide(rate, 16, RoundingMode.HALF_EVEN);
	}

	private BigDecimal getRate(String coin) {
		synchronized (prices) {
			return prices.get(coin);
		}
	}

	public List<String> getCoinList() {
		List<String> list = new ArrayList<>(coins.keySet());
		list.sort((String a, String b) -> {
			return coins.get(a).getName().compareTo(coins.get(b).getName());
		});
		return list;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}
}
