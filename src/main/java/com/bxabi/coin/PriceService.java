package com.bxabi.coin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
		Map<String, String> params = new HashMap<>();
		params.put("limit", "200");
		// params.put("convert", "EUR"); // ,YEN,etc

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-CMC_PRO_API_KEY", "***");

		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<CoinList> response = new RestTemplate().exchange(
				"https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest", HttpMethod.GET, entity,
				CoinList.class, params);
		CoinList coinList = response.getBody();

		synchronized (mapping) {
			mapping.clear();
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

	public void refreshPrice() {
		Date now = new Date();
		// last update was more than a minute ago
		if (now.getTime() - lastUpdated.getTime() > 60000) {
			loadPrices();
		}
	}
}
