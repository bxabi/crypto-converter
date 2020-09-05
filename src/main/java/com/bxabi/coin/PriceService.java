package com.bxabi.coin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.bxabi.coin.data.CoinData;
import com.bxabi.coin.data.CoinList;

@Component
public class PriceService {

	private Map<String, CoinData> mapping = new TreeMap<>();

	private Date lastUpdated;

	public PriceService() {
		loadPrices();
	}

	private void loadPrices() {
		final RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-CMC_PRO_API_KEY", "***");
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<String> entity = new HttpEntity<String>(headers);

		UriComponents uri = UriComponentsBuilder
				.fromHttpUrl("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest")
				.queryParam("limit", 500).build();
		// .queryParam("convert", "EUR"); // ,YEN,etc

		ResponseEntity<CoinList> response = restTemplate.exchange(uri.toUriString(), HttpMethod.GET, entity,
				CoinList.class);

		synchronized (mapping) {
			mapping.clear();
			CoinList coinList = response.getBody();
			for (CoinData coinData : coinList.getData()) {
				mapping.put(coinData.getSymbol(), coinData);
			}
		}

		lastUpdated = new Date();
	}

	public BigDecimal convertToUsd(BigDecimal toConvert, String from) {
		if (from.equals("USD"))
			return toConvert;

		BigDecimal rate = getRate(from, "USD");
		return toConvert.multiply(rate);
	}

	public BigDecimal convertFromUsd(BigDecimal inUSD, String to) {
		if (to.equals("USD"))
			return inUSD;

		BigDecimal rate = getRate(to, "USD");
		return inUSD.divide(rate, RoundingMode.HALF_EVEN);
	}

	private BigDecimal getRate(String from, String string) {
		synchronized (mapping) {
			return mapping.get(from).getQuotes().get(string).getPrice();
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

	public void refreshPrices() {
		Date now = new Date();
		// last update was more than a minute ago
		if (lastUpdated == null || now.getTime() - lastUpdated.getTime() > 60000) {
			loadPrices();
		}
	}
}
