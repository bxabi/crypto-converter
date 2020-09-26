package com.bxabi.coin;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.FocusNotifier;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.PageConfigurator;

@PWA(name = "CryptoCurrency Converter", shortName = "Crypto Conv.", iconPath = "exchange.png", enableInstallPrompt = false)
@Push

@Route
@PageTitle("Online CryptoCurrency Converter")
public class MainView extends VerticalLayout implements PageConfigurator {

	private PriceService priceService;

	private static final long serialVersionUID = -4061880784472661873L;

	private List<NumberField> numbers;
	private List<ComboBox<String>> combos;

	private int activeRow;

	private Div rows;

	private Paragraph lastPriceUpdate = new Paragraph();

	private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	static {
		DATEFORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void configurePage(InitialPageSettings settings) {
		settings.addMetaTag("description", "Online Cryptocurrency Converter");

		settings.addFavIcon("icon", "exchange.svg", "");
		settings.addFavIcon("icon", "exchange-512x512.png", "");
		settings.addFavIcon("icon", "exchange-512x512.png", "512x512");
	}

	private static ComponentEventListener<FocusEvent<ComboBox<String>>> focusListener = new ComponentEventListener<FocusNotifier.FocusEvent<ComboBox<String>>>() {
		private static final long serialVersionUID = -8266280287827257045L;

		@Override
		public void onComponentEvent(FocusEvent<ComboBox<String>> event) {
			event.getSource().setValue("");
		}
	};

	public MainView(@Autowired PriceService priceService) {
		this.priceService = priceService;

		add(new H2("Online Cryptocurrency Converter for the top 500 coins"));

		numbers = new ArrayList<>();
		combos = new ArrayList<>();

		rows = new Div();
		add(rows);
		// NumberField numField =
		addRow("BTC", 1d);
		addRow("USDT", null);
		addRow("ETH", null);

		// numField.setValue(1d);
		activeRow=0;
		// calculateValues();

		Button button = new Button("Add more");
		ComponentEventListener<ClickEvent<Button>> listener = new ComponentEventListener<ClickEvent<Button>>() {
			private static final long serialVersionUID = -8390552027110773465L;

			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				addRow("", null);
			}
		};
		button.addClickListener(listener);
		add(button);

		addFooter();
	}

	private void addFooter() {
		Footer footer = new Footer();

		lastPriceUpdate.setText(getLastPriceUpdateText());
		footer.add(lastPriceUpdate);

		addContact(footer);

		// references
		Paragraph paragraph = new Paragraph("Made with ");
		Anchor spring = new Anchor("https://spring.io/projects/spring-boot", "Spring Boot");
		spring.setTarget("_blank");
		paragraph.add(spring);
		paragraph.add(new Text(" and "));
		Anchor vaadin = new Anchor("https://vaadin.com/flow", "Vaadin Flow");
		vaadin.setTarget("_blank");
		paragraph.add(vaadin);
		paragraph.add(new Text(". Prices from "));
		Anchor cmc = new Anchor("https://pro.coinmarketcap.com/", "CoinMarketCap API");
		cmc.setTarget("_blank");
		paragraph.add(cmc);
		footer.add(paragraph);

		footer.add(new Html(
				"<p>Icon made by <a target='_blank' href=\"https://www.freepik.com/\" title=\"Freepik\">Freepik</a> from <a target='_blank' href=\"https://www.flaticon.com/\""
						+ "      title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\"       "
						+ "          title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></p>"));
		add(footer);

		add(new H4("The page is under development, use it at your own risk."));
	}

	private void addContact(Footer footer) {
		Text contactLabel = new Text("For improvements, suggesions, bugs, support, write to: ");
		Anchor link = new Anchor("mailto:csabi@bxabi.com", "csabi@bxabi.com");
		Button emailbutton = new Button("Show email");
		H4 contact = new H4(contactLabel, emailbutton);
		footer.add(contact);

		ComponentEventListener<ClickEvent<Button>> emailListener = new ComponentEventListener<ClickEvent<Button>>() {
			private static final long serialVersionUID = -3293472377300681191L;

			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				contact.remove(emailbutton);
				contact.add(link);
			}
		};
		emailbutton.addClickListener(emailListener);
	}

	private NumberField addRow(String coin, Double value) {
		NumberField n1 = new NumberField();
		n1.setValueChangeMode(ValueChangeMode.EAGER);
		n1.setStep(0.000000000000000001);
		// n1.setPreventInvalidInput(true);
		n1.setClearButtonVisible(true);
		n1.setAutoselect(true);
		numbers.add(n1);

		ComboBox<String> c1 = new ComboBox<>(10);
		c1.setItems(priceService.getCoinList());
		c1.setValue(coin);
		c1.addFocusListener(focusListener);
		combos.add(c1);

		n1.addValueChangeListener(new NumberChangeListener(numbers.size() - 1));
		c1.addValueChangeListener(new NumberChangeListener(combos.size() - 1));

		// n1.setMaxWidth("180px");
		n1.setWidth("180px");
		// n1.setMinWidth("120px");
		c1.setWidth("120px");

		n1.setValue(value);

		HorizontalLayout hl = new HorizontalLayout(n1, c1);
		// hl.setMaxWidth("300px");
		rows.add(hl);
		return n1;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		new Thread(() -> {
			priceService.refreshPrices();
			attachEvent.getUI().access(() -> {
				calculateValues();
			});
		}).start();
	}

	private String getLastPriceUpdateText() {
		return "Last price update: " + DATEFORMAT.format(priceService.getLastUpdated());
	}

	private class NumberChangeListener
			implements ValueChangeListener<ComponentValueChangeEvent<? extends Component, ?>> {

		private static final long serialVersionUID = 9192684297366604945L;

		private int id;

		public NumberChangeListener(int id) {
			this.id = id;
		}

		@Override
		public void valueChanged(ComponentValueChangeEvent<? extends Component, ?> event) {
			if (!event.isFromClient())
				return;

			if (event.getSource() instanceof NumberField) {
				activeRow = id;
			}
			calculateValues();
		}
	};

	private void calculateValues() {
		priceService.refreshPrices();
		lastPriceUpdate.setText(getLastPriceUpdateText());

		Double value = numbers.get(activeRow).getValue();
		if (value == null) {
			return;
		}

		BigDecimal toConvert = new BigDecimal(value);
		String from = combos.get(activeRow).getValue();
		for (int i = 0; i < numbers.size(); i++) {
			if (i == activeRow)
				continue;

			String to = combos.get(i).getValue();

			BigDecimal inBtc = priceService.convertToUsd(toConvert, from);
			BigDecimal converted = priceService.convertFromUsd(inBtc, to);

			numbers.get(i).setValue(converted.doubleValue());
		}
	}
}
