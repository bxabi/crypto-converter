package com.bxabi.coin;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.FocusNotifier;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

	private PriceService priceService;

	private static final long serialVersionUID = -4061880784472661873L;

	private List<TextField> numbers;
	private List<ComboBox<String>> combos;

	private int activeRow;

	private VerticalLayout rows;

	private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	static {
		DATEFORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private static ComponentEventListener<FocusEvent<ComboBox<String>>> focusListener = new ComponentEventListener<FocusNotifier.FocusEvent<ComboBox<String>>>() {
		private static final long serialVersionUID = -8266280287827257045L;

		@Override
		public void onComponentEvent(FocusEvent<ComboBox<String>> event) {
			event.getSource().setValue("");
		}
	};

	public MainView(@Autowired PriceService priceService) {
		priceService.refreshPrice();

		add(new H2("Online Cryptocurrency Converter for the top 200 coins"));

		this.priceService = priceService;

		numbers = new ArrayList<>();
		combos = new ArrayList<>();

		rows = new VerticalLayout();
		add(rows);

		addRow("USDT");
		addRow("BTC");
		addRow("ETH");

		Button button = new Button("Add more");
		ComponentEventListener<ClickEvent<Button>> listener = new ComponentEventListener<ClickEvent<Button>>() {
			private static final long serialVersionUID = -8390552027110773465L;

			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				addRow("");
			}
		};
		button.addClickListener(listener);
		add(button);

		add(new H3("The page is under development, use it at your own risk."));

		Footer footer = new Footer();
		footer.add(new Paragraph("Last price update: " + DATEFORMAT.format(priceService.getLastUpdated())));
		footer.add(new Paragraph("Made using Spring Boot and Vaadin. Prices from CoinMarketCap."));
		add(footer);
	}

	private void addRow(String coin) {
		TextField n1 = new TextField();
		n1.setValueChangeMode(ValueChangeMode.EAGER);
		numbers.add(n1);

		ComboBox<String> c1 = new ComboBox<>(10);
		c1.setItems(priceService.getCoinList());
		c1.setValue(coin);
		c1.addFocusListener(focusListener);
		combos.add(c1);

		n1.addValueChangeListener(new NumberChangeListener(numbers.size() - 1));
		c1.addValueChangeListener(new NumberChangeListener(combos.size() - 1));

		rows.add(new HorizontalLayout(n1, c1));
	}

	class NumberChangeListener implements ValueChangeListener<ComponentValueChangeEvent<? extends Component, String>> {

		private static final long serialVersionUID = 9192684297366604945L;

		private int id;

		public NumberChangeListener(int id) {
			this.id = id;
		}

		@Override
		public void valueChanged(ComponentValueChangeEvent<? extends Component, String> event) {
			if (!event.isFromClient())
				return;
			if (event.getSource() instanceof TextField) {
				activeRow = id;
			}

			BigDecimal toConvert = new BigDecimal(numbers.get(activeRow).getValue());
			String from = combos.get(activeRow).getValue();
			for (int i = 0; i < numbers.size(); i++) {
				if (i == activeRow)
					continue;

				String to = combos.get(i).getValue();

				BigDecimal inBtc = priceService.convertToUsd(toConvert, from);
				BigDecimal converted = priceService.convertFromUsd(inBtc, to);

				numbers.get(i).setValue(converted.toString());
			}
		}
	};
}
