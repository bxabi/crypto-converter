package com.bxabi.coin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;

@PWA(name = "CryptoCurrency Converter", shortName = "Crypto Conv.", iconPath = "exchange.png")
@Push
public class AppShell implements AppShellConfigurator {

	private static final long serialVersionUID = 1878588289772550905L;
	
	@Override
    public void configurePage(AppShellSettings settings) {
        settings.addMetaTag("description", "Online Cryptocurrency Converter");

        settings.addFavIcon("icon", "exchange.svg", "");
        settings.addFavIcon("icon", "exchange-512x512.png", "");
        settings.addFavIcon("icon", "exchange-512x512.png", "512x512");
    }
}