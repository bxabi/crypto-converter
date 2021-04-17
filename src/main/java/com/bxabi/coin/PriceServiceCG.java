package com.bxabi.coin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

	// BTC, <bitcoin, X>
	private Map<String, Pair<String, BigDecimal>> mapping = new TreeMap<>();

	private Date lastUpdated;

	public PriceServiceCG() {
		final RestTemplate restTemplate = new RestTemplate();

		UriComponents uri = UriComponentsBuilder.fromHttpUrl("https://api.coingecko.com/api/v3/coins/list").build();

		ResponseEntity<CoinDataCG[]> response = restTemplate.exchange(uri.toUriString(), HttpMethod.GET, null,
				CoinDataCG[].class);

		CoinDataCG[] coinData = response.getBody();
		for (CoinDataCG data : coinData) {
			mapping.put(data.getSymbol().toUpperCase(), new MutablePair<String, BigDecimal>(data.getId(), null));
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
		StringBuilder coinList=new StringBuilder();
		Map<String, String> coinmap = new HashMap<>();
		for (String coin : coins) {
			String id=mapping.get(coin).getKey();
			coinmap.put(id, coin);
			coinList.append(id);
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

		synchronized (mapping) {
			Map<String, Map<String, BigDecimal>> prices = response.getBody();
			for (Entry<String, Map<String, BigDecimal>> entry : prices.entrySet()) {
				Pair<String, BigDecimal> inMapping = mapping.get(coinmap.get(entry.getKey()));
				inMapping.setValue(entry.getValue().get("usd"));
			}
		}
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
		synchronized (mapping) {
			return mapping.get(coin).getValue();
		}
	}

	public Set<String> getCoinList() {
		synchronized (mapping) {
			return mapping.keySet();
		}
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}
}
