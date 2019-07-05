package com.bxabi.coin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
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

	public MainView(@Autowired PriceService priceService) {
		add(new H2("Any-to-any Cryptocurrency Converter"));
		
		this.priceService = priceService;
		Set<String> coinList = priceService.getCoinList();

		numbers = new ArrayList<>();
		combos = new ArrayList<>();

		TextField n1 = new TextField();
		n1.setValueChangeMode(ValueChangeMode.EAGER);
		numbers.add(n1);

		ComboBox<String> c1 = new ComboBox<>(10);
		c1.setItems(coinList);
		c1.setValue("BTC");
		combos.add(c1);

		TextField n2 = new TextField();
		n2.setValueChangeMode(ValueChangeMode.EAGER);
		numbers.add(n2);

		ComboBox<String> c2 = new ComboBox<>(10);
		c2.setItems(coinList);
		c2.setValue("ETH");
		combos.add(c2);

		n1.addValueChangeListener(new NumberChangeListener(0));
		n2.addValueChangeListener(new NumberChangeListener(1));
		c1.addValueChangeListener(new NumberChangeListener(0));
		c2.addValueChangeListener(new NumberChangeListener(1));

		add(new HorizontalLayout(n1, c1));
		add(new HorizontalLayout(n2, c2));
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

			BigDecimal toConvert = new BigDecimal(numbers.get(id).getValue());
			String from = combos.get(id).getValue();
			for (int i = 0; i < 2; i++) {
				if (i == id)
					continue;

				String to = combos.get(i).getValue();

				BigDecimal inBtc = priceService.convertToUsd(toConvert, from);
				BigDecimal converted = priceService.convertFromUsd(inBtc, to);

				numbers.get(i).setValue(converted.toString());
			}
		}
	};
}
