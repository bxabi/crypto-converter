package com.bxabi.coin.data;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoinData {
	private String name;
	private String symbol;
	private String slug;
	
	@JsonProperty("quote")
	private Map<String, Quote> quotes;

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getSlug() {
		return slug;
	}

	public Map<String, Quote> getQuotes() {
		return quotes;
	}

}
